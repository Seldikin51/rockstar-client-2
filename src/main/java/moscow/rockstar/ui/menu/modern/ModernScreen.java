package moscow.rockstar.ui.menu.modern;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.ui.menu.modern.components.ModernModule;
import moscow.rockstar.ui.menu.modern.components.ModernSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.impl.FadeOutBatching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RoundedRectBatching;
import moscow.rockstar.utility.render.batching.impl.SquircleBatching;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ModernScreen extends MenuScreen implements IMinecraft, IScaledResolution {
   private final Rect menuWindow;
   private float dragX;
   private float dragY;
   private boolean drag;
   private final ScrollHandler scrollHandler = new ScrollHandler();
   private MenuCategory current = MenuCategory.COMBAT;
   private final List<ColorPicker> colorPickers = new LinkedList<>();
   private final List<ModernCategory> categories = new ArrayList<>();
   private final List<ModernSettings> windows = new LinkedList<>();
   private final Animation currentCategory = new Animation(300L, Easing.BAKEK_SMALLER);
   private final TextField searchField;
   private final PenisPlayer searchPenis;
   private boolean prevFocused;
   Timer timer = new Timer();

   public ModernScreen() {
      float width = 500.0F;
      float height = 343.0F;
      this.menuWindow = new Rect(sr.getScaledWidth() / 2.0F - width / 2.0F, sr.getScaledHeight() / 2.0F - height / 2.0F, width, height);
      this.categories.clear();

      for (MenuCategory category : MenuCategory.values()) {
         List<ModernModule> filteredModules = new LinkedList<>();
         ModernCategory modern = new ModernCategory(category, filteredModules);

         try {
            modern.setPenis(new PenisPlayer(Rockstar.id("penises/" + category.getName().toLowerCase() + ".penis")));
         } catch (RuntimeException var10) {
         }

         this.categories.add(modern);
         filteredModules.addAll(
            Rockstar.getInstance()
               .getModuleManager()
               .getModules()
               .stream()
               .sorted(Comparator.comparing(Module::getName))
               .filter(module -> module.getCategory().equals(category.getCategory()))
               .map(module -> new ModernModule(module, modern))
               .toList()
         );
      }

      this.searchField = new TextField(Fonts.MEDIUM.getFont(6.0F));
      Map<String, FieldAction> append = new HashMap<>();

      for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
         FieldAction action = new FieldAction(
            module::toggle,
            () -> this.categories
               .forEach(
                  panel -> panel.getModules()
                     .stream()
                     .filter(component -> component.getModule() == module)
                     .forEach(modernModule -> System.out.println("poka pichego"))
               )
         );
         append.put(module.getName().replace(" ", ""), action);
         append.put(module.getName(), action);
      }

      this.searchField.setAppend(append);
      this.searchField.setPreview("Поиск");
      this.searchPenis = new PenisPlayer(Rockstar.id("penises/search.penis"));
      this.searchPenis.stop();
   }

   @Compile
   protected void init() {
      this.closing = false;

      for (ModernCategory category : this.categories) {
         if (category.getPenis() != null) {
            category.getPenis().stop();
         }
      }

      super.init();
   }

   public void tick() {
      this.handleMovementKeys();
      super.tick();
   }

   @Override
   public void render(UIContext context) {
      this.menuAnimation.update(this.closing ? 0.0F : 1.0F);
      this.menuAnimation.setEasing(!this.closing ? Easing.BAKEK : Easing.BAKEK_BACK);
      this.menuAnimation.setDuration(400L);
      this.scrollHandler.update();
      if (this.drag) {
         this.menuWindow.setX(context.getMouseX() - this.dragX);
         this.menuWindow.setY(context.getMouseY() - this.dragY);
      }

      if (this.searchField.isFocused() && !this.prevFocused) {
         this.searchPenis.playOnce();
      }

      this.prevFocused = this.searchField.isFocused();
      float scroll = (float)(-this.scrollHandler.getValue());
      float alpha = Math.min(1.0F, this.menuAnimation.getValue());

      for (ModernCategory category : this.categories) {
         if (category.getY() - scroll <= -this.scrollHandler.getTargetValue() && this.current != category.getCategory()) {
            this.current = category.getCategory();
         }
      }

      boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderUtility.scale(
         context.getMatrices(),
         this.menuWindow.getX() + this.menuWindow.getWidth() / 2.0F,
         this.menuWindow.getY() + this.menuWindow.getHeight() / 2.0F,
         0.5F + 0.5F * this.menuAnimation.getValue()
      );
      context.drawBlurredRect(
         this.menuWindow.getX(),
         this.menuWindow.getY(),
         this.menuWindow.getWidth(),
         this.menuWindow.getHeight(),
         45.0F,
         5.0F,
         BorderRadius.all(16.0F),
         Colors.WHITE
      );
      context.drawSquircle(
         this.menuWindow.getX(),
         this.menuWindow.getY(),
         this.menuWindow.getWidth(),
         this.menuWindow.getHeight(),
         5.0F,
         BorderRadius.all(16.0F),
         dark ? Colors.getAdditionalColor().mulAlpha(0.98F) : Colors.getBackgroundColor().mulAlpha(0.95F)
      );
      context.drawShadow(
         this.menuWindow.getX() + 5.0F, this.menuWindow.getY() + 5.0F, 109.0F, 333.0F, 20.0F, BorderRadius.all(14.0F), Colors.BLACK.mulAlpha(0.2F)
      );
      context.drawBlurredRect(this.menuWindow.getX() + 5.0F, this.menuWindow.getY() + 5.0F, 109.0F, 333.0F, 45.0F, BorderRadius.all(12.0F), Colors.WHITE);
      context.drawRoundedRect(
         this.menuWindow.getX() + 5.0F,
         this.menuWindow.getY() + 5.0F,
         109.0F,
         333.0F,
         BorderRadius.all(12.0F),
         Colors.getBackgroundColor().mulAlpha(dark ? 0.85F : 0.65F)
      );
      float x = this.menuWindow.getX();
      float y = this.menuWindow.getY();
      float yOff = 0.0F;
      float xOff = 0.0F;
      float moduleWidth = 177.0F;
      context.drawRoundedRect(
         x + 13.0F,
         y + 13.0F,
         93.0F,
         14.0F,
         BorderRadius.all(3.0F),
         dark ? Colors.getAdditionalColor().mulAlpha(0.6F) : Colors.getBackgroundColor().mulAlpha(0.6F)
      );
      DrawUtility.drawAnimationSprite(
         context.getMatrices(), this.searchPenis.getCurrentSprite(), x + 16.0F, y + 16.0F, 8.0F, 8.0F, Colors.getTextColor().mulAlpha(0.5F)
      );
      this.searchField.set(x + 21.0F, y + 13.0F, 80.0F, 14.0F);
      this.searchField.setTextColor(Colors.getTextColor().mulAlpha(0.5F));
      this.searchField.render(context);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      FontBatching regularBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
      context.drawText(Fonts.REGULAR.getFont(6.0F), "Функции", x + 14.0F, y + 35.0F, Colors.getTextColor().mulAlpha(0.3F));
      regularBatching.draw();

      for (ModernCategory categoryx : this.categories) {
         this.currentCategory.setDuration(150L);
         this.currentCategory.setEasing(Easing.QUAD_OUT);
         if (categoryx.getCategory() == this.current) {
            this.currentCategory.update(yOff);
         }

         if (GuiUtility.isHovered(x + 12.0F, y + 43.0F + yOff, 95.0, 16.0, context)) {
            CursorUtility.set(CursorType.HAND);
         }

         yOff += 18.0F;
      }

      context.drawSquircle(x + 12.0F, y + 43.0F + this.currentCategory.getValue(), 95.0F, 16.0F, 10.0F, BorderRadius.all(4.0F), Colors.ACCENT);
      yOff = 0.0F;
      IconBatching iconBatchingCat = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (ModernCategory categoryx : this.categories) {
         categoryx.getSelected().update(categoryx.getCategory() == this.current);
         if (categoryx.getPenis() == null) {
            context.drawSprite(
               categoryx.getCategory().getMenuSprite(),
               x + 18.0F,
               y + 47.0F + yOff,
               8.0F,
               8.0F,
               Colors.getTextColor().mix(Colors.WHITE, categoryx.getSelected().getValue())
            );
         }

         yOff += 18.0F;
      }

      iconBatchingCat.draw();
      yOff = 0.0F;
      IconBatching iconBatchingCat1 = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (ModernCategory categoryx : this.categories) {
         categoryx.getSelected().update(categoryx.getCategory() == this.current);
         if (categoryx.getPenis() != null) {
            DrawUtility.drawAnimationSprite(
               context.getMatrices(),
               categoryx.getPenis().getCurrentSprite(),
               x + 18.0F,
               y + 47.0F + yOff,
               8.0F,
               8.0F,
               Colors.getTextColor().mix(Colors.WHITE, categoryx.getSelected().getValue())
            );
         }

         yOff += 18.0F;
      }

      iconBatchingCat1.draw();
      yOff = 0.0F;
      FontBatching fontBatchingCat = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (ModernCategory categoryx : this.categories) {
         context.drawText(
            Fonts.MEDIUM.getFont(7.0F),
            categoryx.getCategory().getName(),
            x + 32.0F,
            y + 48.5F + yOff,
            Colors.getTextColor().mix(Colors.WHITE, categoryx.getSelected().getValue())
         );
         yOff += 18.0F;
      }

      fontBatchingCat.draw();
      yOff = scroll;
      ScissorUtility.push(
         context.getMatrices(), this.menuWindow.getX(), this.menuWindow.getY() + 1.0F, this.menuWindow.getWidth(), this.menuWindow.getHeight() - 2.0F
      );
      SquircleBatching squircleBatching = new SquircleBatching(5.0F);

      for (ModernCategory categoryx : this.categories) {
         float prev = yOff;
         categoryx.setY(yOff);

         for (ModernModule module : categoryx.getModules()) {
            boolean cond = !this.opened(module);
            module.getVisible().update(cond);
            module.getOffset().update(cond);
            if (!this.visibleCheck(module)) {
               module.set(x + 127.0F + xOff, y + 33.0F + yOff, moduleWidth, 28.0F);
               if (GuiUtility.isHovered(
                  (double)x,
                  (double)(y - module.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + module.getHeight()),
                  (double)module.getX(),
                  (double)module.getY()
               )) {
                  module.render(context);
                  if (GuiUtility.isHovered(module.getX(), module.getY(), module.getWidth(), module.getHeight(), context)) {
                     CursorUtility.set(CursorType.HAND);
                  }
               }

               xOff += (module.getWidth() + 6.5F) * module.getOffset().getValue();
               if (xOff > this.menuWindow.getWidth() - 139.0F) {
                  yOff += 34.0F * module.getOffset().getValue();
                  xOff = 0.0F;
               }
            }
         }

         if (xOff != 0.0F) {
            yOff += 34.0F;
         }

         xOff = 0.0F;
         yOff += 25.0F;
         if (categoryx.getCategory() == MenuCategory.OTHER && yOff - prev < this.menuWindow.getHeight()) {
            yOff = prev + this.menuWindow.getHeight();
         }
      }

      squircleBatching.draw();
      RoundedRectBatching roundBatching = new RoundedRectBatching();

      for (ModernCategory categoryx : this.categories) {
         for (ModernModule modulex : categoryx.getModules()) {
            if (!this.visibleCheck(modulex)
               && GuiUtility.isHovered(
                  (double)x,
                  (double)(y - modulex.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + modulex.getHeight()),
                  (double)modulex.getX(),
                  (double)modulex.getY()
               )) {
               modulex.renderRounds(context);
            }
         }
      }

      roundBatching.draw();
      RoundedRectBatching roundBatching2 = new RoundedRectBatching();

      for (ModernCategory categoryx : this.categories) {
         for (ModernModule modulexx : categoryx.getModules()) {
            if (!this.visibleCheck(modulexx)
               && GuiUtility.isHovered(
                  (double)x,
                  (double)(y - modulexx.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + modulexx.getHeight()),
                  (double)modulexx.getX(),
                  (double)modulexx.getY()
               )) {
               modulexx.renderInto(context);
            }
         }
      }

      roundBatching2.draw();
      FontBatching mediumBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (ModernCategory categoryx : this.categories) {
         for (ModernModule modulexxx : categoryx.getModules()) {
            if (!this.visibleCheck(modulexxx)
               && GuiUtility.isHovered(
                  (double)x,
                  (double)(y - modulexxx.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + modulexxx.getHeight()),
                  (double)modulexxx.getX(),
                  (double)modulexxx.getY()
               )) {
               modulexxx.renderMedium(context);
            }
         }
      }

      mediumBatching.draw();
      FadeOutBatching regularBatchingLeft = new FadeOutBatching(
         VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR, 0.9F, 1.0F, moduleWidth - 30.0F, x + 127.0F
      );

      for (ModernCategory categoryx : this.categories) {
         for (ModernModule modulexxxx : categoryx.getModules()) {
            if (!this.visibleCheck(modulexxxx)
               && GuiUtility.isHovered(
                  (double)x,
                  (double)(y - modulexxxx.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + modulexxxx.getHeight()),
                  (double)modulexxxx.getX(),
                  (double)modulexxxx.getY()
               )
               && modulexxxx.getX() == x + 127.0F) {
               modulexxxx.renderRegular(context);
            }
         }
      }

      regularBatchingLeft.draw();
      FadeOutBatching regularBatchingRight = new FadeOutBatching(
         VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR, 0.9F, 1.0F, moduleWidth - 30.0F, x + 127.0F + moduleWidth + 6.5F
      );

      for (ModernCategory categoryx : this.categories) {
         for (ModernModule modulexxxxx : categoryx.getModules()) {
            if (!this.visibleCheck(modulexxxxx)
               && GuiUtility.isHovered(
                  (double)x,
                  (double)(y - modulexxxxx.getHeight()),
                  (double)this.menuWindow.getWidth(),
                  (double)(this.menuWindow.getHeight() + modulexxxxx.getHeight()),
                  (double)modulexxxxx.getX(),
                  (double)modulexxxxx.getY()
               )
               && modulexxxxx.getX() != x + 127.0F) {
               modulexxxxx.renderRegular(context);
            }
         }
      }

      regularBatchingRight.draw();
      FontBatching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.SEMIBOLD);

      for (ModernCategory categoryx : this.categories) {
         if (GuiUtility.isHovered(
            (double)x,
            (double)(y - 20.0F),
            (double)this.menuWindow.getWidth(),
            (double)(this.menuWindow.getHeight() + 20.0F),
            (double)(x + 142.0F),
            (double)(y + 16.0F + categoryx.getY())
         )) {
            context.drawText(
               Fonts.SEMIBOLD.getFont(12.0F),
               categoryx.getCategory().getName(),
               x + 143.0F,
               y + 16.0F + categoryx.getY(),
               Rockstar.getInstance().getThemeManager().getCurrentTheme().getTextColor()
            );
         }
      }

      fontBatching.draw();
      IconBatching iconBatching = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (ModernCategory categoryxx : this.categories) {
         if (GuiUtility.isHovered(
               (double)x,
               (double)(y - 20.0F),
               (double)this.menuWindow.getWidth(),
               (double)(this.menuWindow.getHeight() + 20.0F),
               (double)(x + 142.0F),
               (double)(y + 16.0F + categoryxx.getY())
            )
            && categoryxx.getPenis() == null) {
            context.drawSprite(categoryxx.getCategory().getBigMenuSprite(), x + 129.0F, y + 15.0F + categoryxx.getY(), 10.0F, 10.0F, Colors.getTextColor());
         }
      }

      iconBatching.draw();
      IconBatching iconBatching1 = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (ModernCategory categoryxxx : this.categories) {
         if (GuiUtility.isHovered(
               (double)x,
               (double)(y - 20.0F),
               (double)this.menuWindow.getWidth(),
               (double)(this.menuWindow.getHeight() + 20.0F),
               (double)(x + 142.0F),
               (double)(y + 16.0F + categoryxxx.getY())
            )
            && categoryxxx.getPenis() != null) {
            DrawUtility.drawAnimationSprite(
               context.getMatrices(),
               categoryxxx.getPenis().getCurrentSprite(),
               x + 129.0F,
               y + 15.0F + categoryxxx.getY(),
               10.0F,
               10.0F,
               Colors.getTextColor()
            );
         }
      }

      iconBatching1.draw();
      float totalContentHeight = yOff - scroll;
      float visibleHeight = this.menuWindow.getHeight() - 10.0F;
      float maxScroll = -Math.max(0.0F, totalContentHeight - visibleHeight);
      this.scrollHandler.setMax(maxScroll - 10.0F);
      ScissorUtility.pop();
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

      for (ModernSettings window : this.windows) {
         window.render(context);
      }

      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.render(context);
      }

      this.windows.removeIf(window -> window.getAnimation().getValue() == 0.0F && !window.isShowing());
      this.colorPickers.removeIf(colorPickerx -> colorPickerx.getAnimation().getValue() == 0.0F && !colorPickerx.isShowing());
   }

   @Compile
   private void handleMovementKeys() {
      if (mc.player != null && !this.isTyping()) {
         long windowHandle = mc.getWindow().getHandle();
         KeyBinding[] movementKeys = new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey};

         for (KeyBinding key : movementKeys) {
            int keyCode = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }

         if (mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }
      }
   }

   private boolean isTyping() {
      return mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
   }

   @Compile
   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (!Rockstar.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
         for (ColorPicker colorPicker : this.colorPickers) {
            boolean isPick = colorPicker.isPick();
            colorPicker.onMouseClicked(mouseX, mouseY, button);
            if (colorPicker.isHovered(mouseX, mouseY) || isPick) {
               return;
            }

            colorPicker.setShowing(false);
         }

         for (ModernSettings window : this.windows) {
            window.onMouseClicked(mouseX, mouseY, button);
            if (window.isHovered(mouseX, mouseY)) {
               return;
            }

            if (!GuiUtility.isHovered(this.menuWindow, mouseX, mouseY)) {
               boolean can = true;

               for (ModernSettings window1 : this.windows) {
                  if (GuiUtility.isHovered(window1, mouseX, mouseY)) {
                     can = false;
                  }
               }

               if (can) {
                  window.setShowing(false);
               }
            }
         }

         float x = this.menuWindow.getX();
         float y = this.menuWindow.getY();
         float yOff = 0.0F;
         float xOff = 0.0F;

         for (ModernCategory category : this.categories) {
            if (GuiUtility.isHovered((double)(x + 12.0F), (double)(y + 43.0F + yOff), 95.0, 16.0, mouseX, mouseY) && category.getCategory() != this.current) {
               this.scrollHandler.scroll((-this.scrollHandler.getValue() - (category.getY() - this.scrollHandler.getValue())) / 20.0);
               if (category.getPenis() != null) {
                  category.getPenis().playOnce();
               }

               return;
            }

            yOff += 18.0F;
         }

         for (ModernCategory category : this.categories) {
            for (ModernModule module : category.getModules()) {
               if (!this.visibleCheck(module)
                  && (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY) || button != MouseButton.LEFT && button != MouseButton.RIGHT)
                  && GuiUtility.isHovered((double)module.getX(), (double)module.getY(), (double)module.getWidth(), (double)module.getHeight(), mouseX, mouseY)) {
                  module.onMouseClicked(mouseX, mouseY, button);
                  return;
               }
            }
         }

         if (button != MouseButton.MIDDLE) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
         }

         if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY)) {
            this.drag = true;
            this.dragX = (float)(mouseX - this.menuWindow.getX());
            this.dragY = (float)(mouseY - this.menuWindow.getY());
         }

         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Compile
   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      this.drag = false;

      for (ModernSettings window : this.windows) {
         window.onMouseReleased(mouseX, mouseY, button);
      }

      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onMouseReleased(mouseX, mouseY, button);
      }

      if (this.searchField.isFocused()) {
         this.searchField.onMouseReleased(mouseX, mouseY, button);
      }

      super.onMouseReleased(mouseX, mouseY, button);
   }

   @Compile
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      for (ModernSettings window : this.windows) {
         window.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
      }

      if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY)) {
         this.scrollHandler.scroll(verticalAmount);
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Compile
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.searchField.isFocused() && Screen.hasControlDown() && keyCode == 70) {
         this.searchField.setFocused(true);
      }

      this.scrollHandler.onKeyPressed(keyCode);

      for (ModernSettings window : this.windows) {
         window.onKeyPressed(keyCode, scanCode, modifiers);
      }

      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
      }

      for (ModernCategory category : this.categories) {
         for (ModernModule module : category.getModules()) {
            if (!this.visibleCheck(module)) {
               module.onKeyPressed(keyCode, scanCode, modifiers);
            }
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Compile
   public boolean charTyped(char chr, int modifiers) {
      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.charTyped(chr, modifiers);
      }

      for (ModernSettings window : this.windows) {
         window.charTyped(chr, modifiers);
      }

      for (ModernCategory category : this.categories) {
         for (ModernModule module : category.getModules()) {
            if (!this.visibleCheck(module)) {
               module.charTyped(chr, modifiers);
            }
         }
      }

      return super.charTyped(chr, modifiers);
   }

   @Compile
   public void close() {
      this.closing = true;
      Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).disable();
      Sounds soundsModule = Rockstar.getInstance().getModuleManager().getModule(Sounds.class);
      if (soundsModule.isEnabled()) {
         ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 1.0F);
      }

      Rockstar.getInstance().getFileManager().writeFile("client");
      if (Rockstar.getInstance().getConfigManager().getCurrent() != null) {
         Rockstar.getInstance().getConfigManager().getCurrent().save();
      }

      if (TextField.LAST_FIELD != null) {
         TextField.LAST_FIELD.setFocused(false);
      }

      super.close();
   }

   private boolean searchCheck(ModernModule component) {
      TextField search = this.searchField;
      return search != null
         && !search.getBuiltText().isBlank()
         && !component.getModule().getName().toLowerCase().contains(search.getBuiltText().toLowerCase())
         && !component.getModule().getName().replace(" ", "").toLowerCase().contains(search.getBuiltText().toLowerCase());
   }

   private boolean visibleCheck(ModernModule component) {
      return component.getOffset().getValue() == 0.0F || this.searchCheck(component);
   }

   private boolean opened(ModernModule component) {
      return this.windows.stream().anyMatch(window -> window.getModule() == component);
   }

   public boolean isBindingModule() {
      return this.categories.stream().flatMap(panel -> panel.getModules().stream()).anyMatch(ModernModule::isBinding);
   }

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   @Generated
   public Rect getMenuWindow() {
      return this.menuWindow;
   }

   @Generated
   public float getDragX() {
      return this.dragX;
   }

   @Generated
   public float getDragY() {
      return this.dragY;
   }

   @Generated
   public boolean isDrag() {
      return this.drag;
   }

   @Generated
   public ScrollHandler getScrollHandler() {
      return this.scrollHandler;
   }

   @Generated
   public MenuCategory getCurrent() {
      return this.current;
   }

   @Generated
   public List<ColorPicker> getColorPickers() {
      return this.colorPickers;
   }

   @Generated
   public List<ModernCategory> getCategories() {
      return this.categories;
   }

   @Generated
   public List<ModernSettings> getWindows() {
      return this.windows;
   }

   @Generated
   public Animation getCurrentCategory() {
      return this.currentCategory;
   }

   @Generated
   public TextField getSearchField() {
      return this.searchField;
   }

   @Generated
   public PenisPlayer getSearchPenis() {
      return this.searchPenis;
   }

   @Generated
   public boolean isPrevFocused() {
      return this.prevFocused;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   static {
      new MenuPanel(null);
      new BezierSettingComponent(null, null);
      new BindSettingComponent(null, null);
      new BooleanSettingComponent(null, null);
      new ModeSettingComponent(null, null);
      new ButtonSettingComponent(null, null);
      new ColorSettingComponent(null, null);
      new StringSettingComponent(null, null);
      new RangeSettingComponent(null, null);
      new SliderSettingComponent(null, null);
   }
}
