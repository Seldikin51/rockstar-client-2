package moscow.rockstar.ui.components.popup.list;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.ui.components.popup.CheckBoxAction;
import moscow.rockstar.ui.components.popup.PopupComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;

public class CheckBox extends PopupComponent {
   private boolean enabled;
   private final String text;
   private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
   private final Animation enableAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
   private CheckBoxAction action;

   public CheckBox(String text) {
      this.text = text;
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font nameFont = Fonts.REGULAR.getFont(8.0F);
      float nameLeftPadding = 8.0F;
      float nameHeight = nameFont.height();
      this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
      this.enableAnimation.update(this.enabled ? 1.0F : 0.0F);
      if (this.isHovered(context.getMouseX(), context.getMouseY())) {
         CursorUtility.set(CursorType.HAND);
      }

      context.drawFadeoutText(
         nameFont,
         this.text,
         this.x + nameLeftPadding,
         this.y + GuiUtility.getMiddleOfBox(nameHeight, this.height),
         Colors.getTextColor()
            .withAlpha(RenderSystem.getShaderColor()[3] * 255.0F * (0.75F + 0.25F * this.enableAnimation.getValue() + 0.25F * this.hoverAnimation.getValue())),
         0.8F,
         1.0F,
         this.width - 12.0F - 12.0F * this.enableAnimation.getValue()
      );
      float alpha = this.enableAnimation.getValue() * (RenderSystem.getShaderColor()[3] * 255.0F);
      if (this.enableAnimation.getValue() >= 0.0F) {
         context.drawTexture(
            Rockstar.id("icons/check.png"),
            this.x + this.width - 13.0F - this.enableAnimation.getValue() * 2.0F,
            this.y + 7.0F,
            6.0F,
            6.0F,
            Colors.getTextColor().withAlpha(alpha)
         );
      }
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
         this.enabled = !this.enabled;
         if (this.action != null) {
            this.action.handleAction(this.enabled);
         }
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   @Override
   public float getHeight() {
      return this.height = 19.0F;
   }

   public CheckBox enabled(boolean value) {
      this.enabled = value;
      return this;
   }

   public CheckBox action(CheckBoxAction action) {
      this.action = action;
      return this;
   }
}
