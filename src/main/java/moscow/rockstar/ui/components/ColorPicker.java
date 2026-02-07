package moscow.rockstar.ui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.framework.objects.gradient.impl.HorizontalGradient;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.obj.Rect;
import net.minecraft.client.gui.screen.Screen;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ColorPicker extends CustomComponent implements IScaledResolution, IWindow {
   private final Animation animation = new Animation(300L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation blurAnim = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   protected final Animation dragAnim = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation pickAnim = new Animation(300L, 0.0F, Easing.BAKEK_SIZE);
   private final ColorAnimation huePreviewColorAnimation = new ColorAnimation(300L);
   private final ColorAnimation activePreviewColorAnimation = new ColorAnimation(200L);
   private final String title;
   private boolean showing;
   private float offsetFactor;
   private boolean drag;
   private boolean pick;
   private float dragX;
   private float dragY;
   private boolean dragHue;
   private boolean dragBS;
   private boolean dragAlpha;
   private final boolean enableAlpha;
   private final Animation hueAnim = new Animation(500L, Easing.BAKEK_PAGES);
   private final Animation brightnessAnim = new Animation(500L, Easing.BAKEK_PAGES);
   private final Animation saturationAnim = new Animation(500L, Easing.BAKEK_PAGES);
   private final Animation alphaAnim = new Animation(500L, Easing.BAKEK_PAGES);
   private float hue;
   private float brightness;
   private float saturation;
   private float alpha;
   public static final List<ColorPicker.Preset> COLOR_PRESETS = new ArrayList<>(
      List.of(
         new ColorPicker.Preset(new ColorRGBA(0.0F, 122.0F, 255.0F)),
         new ColorPicker.Preset(new ColorRGBA(52.0F, 199.0F, 89.0F)),
         new ColorPicker.Preset(new ColorRGBA(255.0F, 204.0F, 0.0F)),
         new ColorPicker.Preset(new ColorRGBA(255.0F, 59.0F, 48.0F)),
         new ColorPicker.Preset(new ColorRGBA(151.0F, 71.0F, 255.0F))
      )
   );

   public ColorPicker(float x, float y, float offsetFactor, boolean enableAlpha, ColorRGBA color, String title) {
      super(x, y, 143.0F, enableAlpha ? 160.0F : 136.0F);
      this.offsetFactor = offsetFactor;
      this.enableAlpha = enableAlpha;
      this.showing = true;
      this.activePreviewColorAnimation.setColor(color);
      this.title = title;
      this.update(color);
   }

   public static void setColorPresets(List<ColorPicker.Preset> newPresets) {
      COLOR_PRESETS.clear();
      COLOR_PRESETS.addAll(newPresets);
   }

   @Override
   protected void renderComponent(UIContext context) {
      if (this.dragHue) {
         this.hue = GuiUtility.getSliderValue(0.0F, 1.0F, this.y + 22.0F, 66.0F, context.getMouseY());
      }

      if (this.dragBS) {
         this.brightness = 1.0F - GuiUtility.getSliderValue(0.0F, 1.0F, this.x + 6.0F, 114.0F, context.getMouseX());
         this.saturation = 1.0F - GuiUtility.getSliderValue(0.0F, 1.0F, this.y + 20.0F, 70.0F, context.getMouseY());
      }

      if (this.dragAlpha) {
         this.alpha = GuiUtility.getSliderValue(0.0F, 1.0F, this.x + 7.0F, 88.0F, context.getMouseX());
      }

      if (this.drag) {
         this.x = context.getMouseX() - this.dragX;
         this.y = context.getMouseY() - this.dragY;
      }

      COLOR_PRESETS.removeIf(preset -> preset.animation.getValue() == 0.0F && !preset.showing);
      this.pickAnim.setEasing(this.pick ? Easing.BAKEK : Easing.BAKEK_BACK);
      this.pickAnim.update(this.pick);
      this.animation.setEasing(this.showing ? Easing.BAKEK : Easing.BAKEK_BACK);
      this.animation.update(this.showing);
      this.blurAnim.update(this.animation.getValue() >= 0.6F);
      this.dragAnim.update(this.drag);
      this.hueAnim.update(this.hue);
      this.brightnessAnim.update(1.0F - this.brightness);
      this.saturationAnim.update(1.0F - this.saturation);
      this.alphaAnim.update(this.alpha);
      this.huePreviewColorAnimation.update(ColorRGBA.fromHSB(this.hue, 1.0F, 1.0F));
      boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      ColorRGBA bgColor = Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.9F - 0.6F * Interface.glass() : 0.7F));
      ColorRGBA withoutAlpha = ColorRGBA.fromHSB(this.hue, this.brightness, this.saturation);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.min(1.0F, this.animation.getValue()));
      RenderUtility.scale(
         context.getMatrices(), this.x + this.width / this.offsetFactor, this.y + this.height / this.offsetFactor, 0.5F + this.animation.getValue() * 0.5F
      );
      ScissorUtility.push(context.getMatrices(), this.x + 1.0F, this.y + 1.0F, this.width - 2.0F, this.height - 2.0F);
      context.drawShadow(
         this.x - 5.0F,
         this.y - 5.0F,
         this.width + 10.0F,
         this.height + 10.0F,
         15.0F,
         BorderRadius.all(6.0F),
         ColorRGBA.BLACK.withAlpha(255.0F * (0.1F + 0.15F * this.dragAnim.getValue()))
      );
      ScissorUtility.pop();
      if (Interface.showMinimalizm()) {
         context.drawBlurredRect(
            this.x,
            this.y,
            this.width,
            this.height,
            45.0F,
            7.0F,
            BorderRadius.all(6.0F),
            ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue() * Interface.minimalizm())
         );
      }

      if (Interface.showGlass()) {
         context.drawLiquidGlass(
            this.x,
            this.y,
            this.width,
            this.height,
            7.0F,
            0.05F - 0.03F * this.dragAnim.getValue(),
            BorderRadius.all(6.0F),
            ColorRGBA.WHITE.withAlpha(255.0F * this.animation.getValue() * Interface.glass())
         );
      }

      context.drawSquircle(this.x, this.y, this.width, this.height, 7.0F, BorderRadius.all(6.0F), bgColor);
      ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
      context.drawCenteredText(Fonts.MEDIUM.getFont(7.0F), this.title, this.x + this.width / 2.0F, this.y + 7.0F, Colors.getTextColor());
      context.drawTexture(Rockstar.id("icons/colorpicker/pipette.png"), this.x + 7.0F, this.y + 6.0F, 8.0F, 8.0F);
      if (GuiUtility.isHovered((double)(this.x + 7.0F), (double)(this.y + 6.0F), 8.0, 8.0, context.getMouseX(), context.getMouseY())) {
         CursorUtility.set(CursorType.HAND);
      }

      context.drawRoundedRect(this.x + this.width - 15.0F, this.y + 5.0F, 10.0F, 10.0F, BorderRadius.all(5.0F), Colors.getAdditionalColor());
      context.drawTexture(Rockstar.id("icons/colorpicker/xmark.png"), this.x + this.width - 15.0F, this.y + 5.0F, 10.0F, 10.0F);
      if (GuiUtility.isHovered((double)(this.x + this.width - 15.0F), (double)(this.y + 5.0F), 10.0, 10.0, context.getMouseX(), context.getMouseY())) {
         CursorUtility.set(CursorType.HAND);
      }

      context.drawRoundedTexture(Rockstar.id("textures/hue.png"), this.x + this.width - 18.0F, this.y + 20.0F, 12.0F, 70.0F, BorderRadius.all(4.0F));
      context.drawRoundedRect(this.x + this.width - 16.0F, this.y + 22.0F + 64.0F * this.hueAnim.getValue(), 8.0F, 2.0F, BorderRadius.all(0.2F), Colors.WHITE);
      if (GuiUtility.isHovered(this.x + this.width - 18.0F, this.y + 20.0F, 12.0, 70.0, context) || this.dragHue) {
         CursorUtility.set(CursorType.ARROW_VERTICAL);
      }

      context.drawRoundedRect(
         this.x + 6.0F,
         this.y + 20.0F,
         114.0F,
         70.0F,
         BorderRadius.all(4.0F),
         Gradient.of(this.huePreviewColorAnimation.getColor(), Colors.BLACK, Colors.WHITE, Colors.BLACK)
      );
      context.drawRoundedRect(
         this.x + 6.0F + 114.0F * this.brightnessAnim.getValue() - 3.5F,
         this.y + 20.0F + 70.0F * this.saturationAnim.getValue() - 3.5F,
         7.0F,
         7.0F,
         BorderRadius.all(2.5F),
         Colors.WHITE
      );
      context.drawRoundedRect(
         this.x + 7.0F + 114.0F * this.brightnessAnim.getValue() - 3.5F,
         this.y + 21.0F + 70.0F * this.saturationAnim.getValue() - 3.5F,
         5.0F,
         5.0F,
         BorderRadius.all(1.5F),
         withoutAlpha
      );
      if (GuiUtility.isHovered(this.x + 6.0F, this.y + 20.0F, 114.0, 70.0, context) || this.dragBS) {
         CursorUtility.set(CursorType.CROSSHAIR);
      }

      if (this.enableAlpha) {
         context.drawText(
            Fonts.MEDIUM.getFont(5.0F),
            Localizator.translate("colorpicker.opacity").toUpperCase(),
            this.x + 6.0F,
            this.y + 95.0F,
            Colors.getTextColor().withAlpha(191.25F)
         );
         context.drawRoundedTexture(Rockstar.id("textures/empty.png"), this.x + 6.0F, this.y + 102.0F, 100.0F, 12.0F, BorderRadius.all(5.0F));
         context.drawRoundedRect(
            this.x + 6.0F - 0.5F,
            this.y + 102.0F - 0.5F,
            101.0F,
            13.0F,
            BorderRadius.all(5.0F),
            new HorizontalGradient(withoutAlpha.withAlpha(0.0F), withoutAlpha)
         );
         context.drawRoundedRect(
            this.x + this.width - 32.0F, this.y + 102.0F, 26.0F, 12.0F, BorderRadius.all(2.0F), Colors.getAdditionalColor().withAlpha(255.0F)
         );
         context.drawCenteredText(
            Fonts.MEDIUM.getFont(6.0F), (int)(this.alpha * 100.0F) + "%", this.x + this.width - 32.0F + 13.0F, this.y + 106.0F, Colors.getTextColor()
         );
         context.drawRoundedBorder(this.x + 7.0F + 88.0F * this.alphaAnim.getValue(), this.y + 103.0F, 10.0F, 10.0F, 0.5F, BorderRadius.all(4.0F), Colors.WHITE);
         context.drawRoundedRect(this.x + 8.0F + 88.0F * this.alphaAnim.getValue(), this.y + 104.0F, 8.0F, 8.0F, BorderRadius.all(3.0F), this.built());
         if (GuiUtility.isHovered(this.x + 6.0F, this.y + 102.0F, 100.0, 12.0, context) || this.dragAlpha) {
            CursorUtility.set(CursorType.ARROW_HORIZONTAL);
         }
      }

      context.drawRoundedRect(this.x + 6.0F, this.y + this.height - 36.0F, 29.0F, 29.0F, BorderRadius.all(5.0F), this.built());
      float xOffset = 0.0F;
      float yOffset = 0.0F;

      for (ColorPicker.Preset preset : COLOR_PRESETS) {
         preset.animation.update(preset.showing);
         preset.selected
            .update(preset.color.getHue() == this.hue && preset.color.getSaturation() == this.saturation && preset.color.getBrightness() == this.brightness);
         if (preset.selected.getValue() > 0.0F) {
            float anim = preset.selected.getValue();
            context.drawRoundedRect(
               this.x + 45.0F + xOffset,
               this.y + this.height - 36.0F + yOffset,
               11.0F,
               11.0F,
               BorderRadius.all(4.5F),
               preset.color.withAlpha(255.0F * preset.animation.getValue())
            );
            context.drawRoundedBorder(
               this.x + 45.0F + xOffset - 1.0F + 2.0F * anim,
               this.y + this.height - 36.0F + yOffset - 1.0F + 2.0F * anim,
               13.0F - 4.0F * anim,
               13.0F - 4.0F * anim,
               0.5F,
               BorderRadius.all(6.5F - 2.0F * anim),
               Colors.WHITE.withAlpha(255.0F * preset.animation.getValue() * preset.selected.getValue())
            );
         } else {
            context.drawRoundedRect(
               this.x + 45.0F + xOffset,
               this.y + this.height - 36.0F + yOffset,
               11.0F,
               11.0F,
               BorderRadius.all(4.5F),
               preset.color.withAlpha(255.0F * preset.animation.getValue())
            );
         }

         if (GuiUtility.isHovered(this.x + 45.0F + xOffset, this.y + this.height - 36.0F + yOffset, 11.0, 11.0, context)) {
            CursorUtility.set(CursorType.HAND);
         }

         xOffset += 20.0F * preset.animation.getValue();
         if (45.0F + xOffset > this.width) {
            xOffset = 0.0F;
            yOffset += 18.0F * preset.animation.getValue();
         }
      }

      if (COLOR_PRESETS.size() < 10) {
         context.drawRoundedRect(
            this.x + 45.0F + xOffset, this.y + this.height - 36.0F + yOffset, 11.0F, 11.0F, BorderRadius.all(4.5F), Colors.getAdditionalColor()
         );
         context.drawTexture(Rockstar.id("icons/colorpicker/plus.png"), this.x + 45.0F + xOffset, this.y + this.height - 36.0F + yOffset, 11.0F, 11.0F);
         if (GuiUtility.isHovered(this.x + 45.0F + xOffset, this.y + this.height - 36.0F + yOffset, 11.0, 11.0, context)) {
            CursorUtility.set(CursorType.HAND);
         }
      }

      ScissorUtility.pop();
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.pickAnim.getValue() > 0.0F) {
         Rect pickRect = new Rect(
            context.getMouseX(),
            context.getMouseY() + 10,
            45.0F + Fonts.REGULAR.getFont(6.0F).width(Localizator.translate("colorpicker.click_to_sample")),
            30.0F
         );
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.min(1.0F, this.pickAnim.getValue()));
         RenderUtility.scale(
            context.getMatrices(),
            pickRect.getX() + pickRect.getWidth() / 2.0F,
            pickRect.getY() + pickRect.getHeight() / 2.0F,
            0.5F + this.pickAnim.getValue() * 0.5F
         );
         context.drawBlurredRect(
            pickRect.getX(),
            pickRect.getY(),
            pickRect.getWidth(),
            pickRect.getHeight(),
            45.0F,
            7.0F,
            BorderRadius.all(6.0F),
            ColorRGBA.WHITE.withAlpha(255.0F * this.pickAnim.getValue())
         );
         context.drawSquircle(
            pickRect.getX(),
            pickRect.getY(),
            pickRect.getWidth(),
            pickRect.getHeight(),
            7.0F,
            BorderRadius.all(6.0F),
            Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.8F : 0.7F))
         );
         ColorRGBA mouseColor = ColorRGBA.fromPixel(
            (float)(context.getMouseX() * sr.getScaleFactor()), (float)(mw.getHeight() - context.getMouseY() * sr.getScaleFactor())
         );
         context.drawRoundedRect(
            pickRect.getX() + 5.0F, pickRect.getY() + 5.0F, pickRect.getHeight() - 10.0F, pickRect.getHeight() - 10.0F, BorderRadius.all(5.0F), mouseColor
         );
         context.drawTexture(Rockstar.id("icons/colorpicker/click.png"), pickRect.getX() + pickRect.getHeight(), pickRect.getY() + 16.0F, 6.0F, 6.0F);
         context.drawText(
            Fonts.REGULAR.getFont(6.0F),
            String.format("RGB %s %s %s", (int)mouseColor.getRed(), (int)mouseColor.getGreen(), (int)mouseColor.getBlue()),
            pickRect.getX() + pickRect.getHeight(),
            pickRect.getY() + 8.0F,
            Colors.getTextColor()
         );
         context.drawText(
            Fonts.REGULAR.getFont(6.0F),
            Localizator.translate("colorpicker.click_to_sample"),
            pickRect.getX() + pickRect.getHeight() + 8.0F,
            pickRect.getY() + 17.0F,
            Colors.getTextColor().withAlpha(200.0F)
         );
         RenderUtility.end(context.getMatrices());
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   public ColorRGBA built() {
      this.activePreviewColorAnimation
         .update(ColorRGBA.fromHSB(this.hue, this.brightness, this.saturation).withAlpha(this.enableAlpha ? 255.0F * this.alpha : 255.0F));
      return this.activePreviewColorAnimation.getColor();
   }

   @Compile
   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (Screen.isCopy(keyCode)) {
         mc.keyboard.setClipboard(this.built().toHex());
      } else if (Screen.isPaste(keyCode)) {
         String clipboard = mc.keyboard.getClipboard();

         try {
            this.update(ColorRGBA.fromHex(clipboard));
         } catch (Exception var6) {
         }
      }

      super.onKeyPressed(keyCode, scanCode, modifiers);
   }

   @Compile
   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      boolean canAppend = COLOR_PRESETS.size() < 10;
      float xOffset = 0.0F;
      float yOffset = 0.0F;

      for (ColorPicker.Preset preset : COLOR_PRESETS) {
         if (GuiUtility.isHovered((double)(this.x + 45.0F + xOffset), (double)(this.y + this.height - 36.0F + yOffset), 11.0, 11.0, mouseX, mouseY)) {
            if (button.getButtonIndex() != 0) {
               preset.showing = false;
               Rockstar.getInstance().getFileManager().writeFile("client");
            } else {
               this.update(preset.color);
            }

            return;
         }

         if (preset.color.getHue() == this.hue && preset.color.getSaturation() == this.saturation && preset.color.getBrightness() == this.brightness) {
            canAppend = false;
         }

         xOffset += 20.0F;
         if (45.0F + xOffset > this.width) {
            xOffset = 0.0F;
            yOffset += 18.0F;
         }
      }

      if (GuiUtility.isHovered((double)(this.x + 45.0F + xOffset), (double)(this.y + this.height - 36.0F + yOffset), 11.0, 11.0, mouseX, mouseY) && canAppend) {
         COLOR_PRESETS.add(new ColorPicker.Preset(this.built()));
         Rockstar.getInstance().getFileManager().writeFile("client");
      } else if (button.getButtonIndex() != 0) {
         this.pick = false;
      } else {
         if (this.pick) {
            ColorRGBA color = ColorRGBA.fromPixel((float)(mouseX * sr.getScaleFactor()), (float)(mw.getHeight() - mouseY * sr.getScaleFactor()));
            this.update(color);
            this.pick = false;
         }

         if (GuiUtility.isHovered((double)(this.x + 7.0F), (double)(this.y + 6.0F), 8.0, 8.0, mouseX, mouseY)) {
            this.pick = true;
         } else if (GuiUtility.isHovered((double)(this.x + this.width - 15.0F), (double)(this.y + 5.0F), 10.0, 10.0, mouseX, mouseY)) {
            this.showing = false;
            this.offsetFactor = 2.0F;
         } else if (GuiUtility.isHovered((double)(this.x + this.width - 18.0F), (double)(this.y + 20.0F), 12.0, 70.0, mouseX, mouseY)) {
            this.dragHue = true;
         } else if (GuiUtility.isHovered((double)(this.x + 6.0F), (double)(this.y + 20.0F), 114.0, 70.0, mouseX, mouseY)) {
            this.dragBS = true;
         } else if (GuiUtility.isHovered((double)(this.x + 6.0F), (double)(this.y + 102.0F), 100.0, 12.0, mouseX, mouseY)) {
            this.dragAlpha = true;
         } else {
            if (this.isHovered(mouseX, mouseY)) {
               this.drag = true;
               this.dragX = (float)(mouseX - this.x);
               this.dragY = (float)(mouseY - this.y);
            }
         }
      }
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      this.drag = false;
      this.dragBS = false;
      this.dragHue = false;
      this.dragAlpha = false;
   }

   public void update(ColorRGBA color) {
      this.hue = color.getHue();
      this.brightness = color.getBrightness();
      this.saturation = color.getSaturation();
      this.alpha = color.getAlpha() / 255.0F;
      this.activePreviewColorAnimation.update(color);
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public boolean isShowing() {
      return this.showing;
   }

   @Generated
   public void setShowing(boolean showing) {
      this.showing = showing;
   }

   @Generated
   public boolean isDrag() {
      return this.drag;
   }

   @Generated
   public boolean isPick() {
      return this.pick;
   }

   public static class Preset {
      private final ColorRGBA color;
      private final Animation selected = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
      private final Animation animation = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
      private boolean showing = true;

      @Generated
      public Preset(ColorRGBA color) {
         this.color = color;
      }

      @Generated
      public ColorRGBA getColor() {
         return this.color;
      }

      @Generated
      public Animation getSelected() {
         return this.selected;
      }

      @Generated
      public Animation getAnimation() {
         return this.animation;
      }

      @Generated
      public boolean isShowing() {
         return this.showing;
      }
   }
}
