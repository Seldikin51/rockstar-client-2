package moscow.rockstar.ui.menu.dropdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.RussianRoulette;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class DropDownScreen extends MenuScreen implements IMinecraft {
   private final Animation searchAnimation = new Animation(300L, Easing.BAKEK);
   private final Animation appendingAnim = new Animation(300L, Easing.BAKEK);
   private boolean closing;
   private List<MenuPanel> panels = new ArrayList<>();
   private float panelWidth;
   private float panelHeight;
   private String desc = "";
   private AnimatedText descText;
   private final List<ColorPicker> colorPickers = new ArrayList<>();
   private TextField searchField;

   @Compile
   protected void init() {
      this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0F), 10.0F, 300L, Easing.BAKEK).centered();
      this.closing = false;
      this.panelWidth = 115.0F;
      this.panelHeight = 240.0F;
      this.panels = Arrays.stream(MenuCategory.values()).map(MenuPanel::new).toList();

      for (MenuPanel panel : this.panels) {
         panel.setWidth(this.panelWidth);
         panel.setHeight(this.panelHeight);
         panel.onInit();
      }

      this.searchField = new TextField(Fonts.REGULAR.getFont(12.0F));
      Map<String, FieldAction> append = new HashMap<>();

      for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
         FieldAction action = new FieldAction(
            module::toggle,
            () -> this.panels
               .forEach(panelx -> panelx.getModuleComponents().stream().filter(component -> component.getModule() == module).forEach(ModuleComponent::open))
         );
         append.put(module.getName().replace(" ", ""), action);
         append.put(module.getName(), action);
      }

      this.searchField.setAppend(append);
      super.init();
   }

   public void tick() {
      this.handleMovementKeys();
      super.tick();
   }

   @Compile
   @Override
   public void render(UIContext context) {
      this.menuAnimation.setEasing(Easing.LINEAR);
      this.menuAnimation.update(this.isClosing() ? 0.0F : 1.0F);
      this.menuAnimation.setDuration(this.isClosing() ? 300L : 500L);
      this.desc = "";
      float spacing = 10.0F;
      float x = (this.width - (this.panelWidth + spacing) * this.panels.size() + spacing) / 2.0F;
      float y = (this.height - this.panelHeight) / 2.0F;
      context.pushMatrix();
      float offset = 0.0F;

      for (MenuPanel panel : this.panels) {
         panel.setX(MathUtility.interpolate(x + offset, this.width / 2.0F - this.panelWidth / 2.0F, this.closing ? 1.0F - this.menuAnimation.getValue() : 0.0));
         panel.setY(y);
         panel.setWidth(this.panelWidth);
         panel.setHeight(this.panelHeight);
         offset += this.panelWidth + spacing;
      }

      for (MenuPanel panel : this.panels) {
         panel.renderBlur(context);
         offset += this.panelWidth + spacing;
      }

      for (MenuPanel panel : this.panels) {
         panel.render(context);
      }

      Batching icon = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());

      for (MenuPanel panel : this.panels) {
         panel.drawType(context);
      }

      icon.draw();

      for (MenuPanel panel : this.panels) {
         this.scissor(context, panel, () -> {
            Batching font = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.REGULAR);
            panel.drawRegular8(context);
            font.draw();
            Batching icon1 = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
            panel.drawIcons(context);
            icon1.draw();
            Batching split = new RectBatching(VertexFormats.POSITION_COLOR, context.getMatrices());
            panel.drawSplit(context);
            split.draw();
         });
      }

      context.popMatrix();
      if (this.menuAnimation.getValue() < 0.5F) {
         this.desc = "";
      }

      this.searchAnimation.update(this.searchField.isFocused());
      float searchAlpha = this.menuAnimation.getValue() * this.searchAnimation.getValue();
      if (searchAlpha > 0.0F) {
         if (Interface.showMinimalizm()) {
            context.drawBlurredRect(
               this.searchField.getX(),
               this.searchField.getY(),
               this.searchField.getWidth(),
               this.searchField.getHeight(),
               45.0F,
               BorderRadius.all(6.0F),
               ColorRGBA.WHITE.withAlpha(255.0F * searchAlpha)
            );
         }

         if (Interface.showGlass()) {
            context.drawLiquidGlass(
               this.searchField.getX(),
               this.searchField.getY(),
               this.searchField.getWidth(),
               this.searchField.getHeight(),
               2.0F,
               0.08F,
               BorderRadius.all(6.0F),
               ColorRGBA.WHITE.withAlpha(255.0F * searchAlpha)
            );
         }

         boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
         context.drawRoundedRect(
            this.searchField.getX(),
            this.searchField.getY(),
            this.searchField.getWidth(),
            this.searchField.getHeight(),
            BorderRadius.all(6.0F),
            Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.9F - 0.7F * Interface.glass() : 0.7F) * searchAlpha)
         );
         this.searchField.set(this.width / 2.0F - this.searchField.getWidth() / 2.0F, this.height - 20 - 20.0F * searchAlpha, 100.0F, 20.0F);
         this.searchField.setAlpha(searchAlpha);
         this.searchField.setTextColor(Colors.getTextColor());
         this.searchField.render(context);
         this.appendingAnim.update(!this.searchField.getAppending().isBlank());
         context.drawCenteredText(
            Fonts.MEDIUM.getFont(11.0F),
            Localizator.translate("search.tooltip.tab"),
            this.width / 2.0F,
            this.height - 65 - 10.0F * searchAlpha * this.appendingAnim.getValue(),
            ColorRGBA.WHITE.withAlpha(150.0F * searchAlpha * this.appendingAnim.getValue())
         );
         context.drawCenteredText(
            Fonts.MEDIUM.getFont(11.0F),
            Localizator.translate("search.tooltip.enter"),
            this.width / 2.0F,
            this.height - 50 - 10.0F * searchAlpha * this.appendingAnim.getValue(),
            ColorRGBA.WHITE.withAlpha(150.0F * searchAlpha * this.appendingAnim.getValue())
         );
      } else {
         this.searchField.clear();
      }

      context.drawCenteredText(
         Fonts.MEDIUM.getFont(11.0F),
         Localizator.translate("search.tooltip"),
         this.width / 2.0F,
         this.height - 20 - 10.0F * this.menuAnimation.getValue() * (1.0F - this.searchAnimation.getValue()),
         ColorRGBA.WHITE.withAlpha(150.0F * this.menuAnimation.getValue() * (1.0F - this.searchAnimation.getValue()))
      );
      this.descText.pos(this.width / 2.0F, this.height / 2.0F - 150.0F);
      if (!this.desc.contains(".description")) {
         this.descText.update(this.desc);
         this.descText.render(context);
      }

      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.render(context);
         if (!(mc.currentScreen instanceof DropDownScreen)) {
            colorPicker.setShowing(false);
         }
      }

      this.colorPickers.removeIf(popup -> popup.getAnimation().getValue() == 0.0F && !popup.isShowing());
      RussianRoulette russianRoulette = Rockstar.getInstance().getModuleManager().getModule(RussianRoulette.class);
      if (russianRoulette.isEnabled()) {
         if (russianRoulette.getQrTexture() == null) {
            return;
         }

         if (russianRoulette.getQrAnimation().getValue() == 0.0F && russianRoulette.isQrRemoving()) {
            return;
         }

         float scale = 180.0F;
         float xQR = (mc.getWindow().getScaledWidth() - scale) / 2.0F;
         float yQR = (mc.getWindow().getScaledHeight() - scale) / 2.0F;
         context.drawTexture(
            russianRoulette.getQrTexture(), xQR, yQR, scale, scale, Colors.WHITE.withAlpha((int)(255.0F * russianRoulette.getQrAnimation().getValue()))
         );
      }
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

   public boolean isBindingModule() {
      return this.panels.stream().flatMap(panel -> panel.getModuleComponents().stream()).anyMatch(ModuleComponent::isBindingMode);
   }

   private void scissor(UIContext context, MenuPanel panel, Runnable runnable) {
      panel.scale(context);
      panel.push(context);
      runnable.run();
      ScissorUtility.pop();
      RenderUtility.end(context.getMatrices());
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

         for (MenuPanel panel : this.panels) {
            if (panel.isHovered(mouseX, mouseY)) {
               panel.onMouseClicked(mouseX, mouseY, button);
            }
         }

         if (this.searchField.isFocused() && button != MouseButton.MIDDLE) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
         }

         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Compile
   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onMouseReleased(mouseX, mouseY, button);
      }

      for (MenuPanel panel : this.panels) {
         panel.onMouseReleased(mouseX, mouseY, button);
      }

      if (this.searchField.isFocused()) {
         this.searchField.onMouseReleased(mouseX, mouseY, button);
      }

      super.onMouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      for (MenuPanel panel : this.panels) {
         panel.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Compile
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (ColorPicker colorPicker : this.colorPickers) {
         colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.searchField != null && !this.searchField.isFocused() && Screen.hasControlDown() && keyCode == 70) {
         this.searchField.setFocused(true);
      }

      for (MenuPanel panel : this.panels) {
         panel.onKeyPressed(keyCode, scanCode, modifiers);
      }

      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Compile
   public boolean charTyped(char chr, int modifiers) {
      if (this.searchField.isFocused() && !this.isBindingModule()) {
         this.searchField.charTyped(chr, modifiers);
      }

      for (MenuPanel panel : this.panels) {
         panel.charTyped(chr, modifiers);
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

   public boolean shouldPause() {
      return false;
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   @Generated
   public Animation getSearchAnimation() {
      return this.searchAnimation;
   }

   @Generated
   public Animation getAppendingAnim() {
      return this.appendingAnim;
   }

   @Generated
   @Override
   public boolean isClosing() {
      return this.closing;
   }

   @Generated
   public List<MenuPanel> getPanels() {
      return this.panels;
   }

   @Generated
   public float getPanelWidth() {
      return this.panelWidth;
   }

   @Generated
   public float getPanelHeight() {
      return this.panelHeight;
   }

   @Generated
   public String getDesc() {
      return this.desc;
   }

   @Generated
   public AnimatedText getDescText() {
      return this.descText;
   }

   @Generated
   public List<ColorPicker> getColorPickers() {
      return this.colorPickers;
   }

   @Generated
   @Override
   public void setClosing(boolean closing) {
      this.closing = closing;
   }

   @Generated
   public void setDesc(String desc) {
      this.desc = desc;
   }

   @Generated
   public TextField getSearchField() {
      return this.searchField;
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
