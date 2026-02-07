package moscow.rockstar.ui.components.animated;

import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;

public class AnimatedNumber extends CustomComponent {
   private boolean drawZero;
   private ColorRGBA color = ColorRGBA.WHITE;
   private final float offset;
   private final Font font;
   private final String[] numbers = new String[]{"", ""};
   private final String[] prevs = new String[]{"", ""};
   private final Animation[] animations;
   private float lastOffset;

   public AnimatedNumber(Font font, float offset, long speed, Easing easing) {
      this.font = font;
      this.offset = offset;
      this.animations = new Animation[2];

      for (int i = 0; i < this.animations.length; i++) {
         this.animations[i] = new Animation(speed, easing);
      }
   }

   @Override
   public void renderComponent(UIContext context) {
      for (Animation animation : this.animations) {
         animation.update(1.0F);
      }

      context.drawText(
         this.font,
         this.prevs[0],
         this.x,
         this.y + this.offset * this.animations[0].getValue(),
         this.color.withAlpha(this.color.getAlpha() * (1.0F - this.animations[0].getValue()))
      );
      context.drawText(
         this.font,
         this.numbers[0],
         this.x,
         this.y - this.offset + this.offset * this.animations[0].getValue(),
         this.color.withAlpha(this.color.getAlpha() * this.animations[0].getValue())
      );
      context.drawText(
         this.font,
         this.prevs[1],
         this.x + this.lastOffset,
         this.y + this.offset * this.animations[1].getValue(),
         this.color.withAlpha(this.color.getAlpha() * (1.0F - this.animations[1].getValue()))
      );
      context.drawText(
         this.font,
         this.numbers[1],
         this.x + this.font.width(this.numbers[0]),
         this.y - this.offset + this.offset * this.animations[1].getValue(),
         this.color.withAlpha(this.color.getAlpha() * this.animations[1].getValue())
      );
   }

   @Override
   public float getWidth() {
      return this.font.width(this.numbers[0] + this.numbers[1]);
   }

   public void update(int updated) {
      String first = String.valueOf(updated / 10);
      String second = String.valueOf(updated % 10);
      if (!second.equals(this.numbers[1])) {
         this.lastOffset = this.font.width(this.numbers[0]);
         this.prevs[1] = this.numbers[1];
         this.numbers[1] = second;
         this.animations[1].setValue(0.0F);
      }

      if (!first.equals(this.numbers[0])) {
         this.prevs[0] = this.numbers[0];
         this.numbers[0] = this.drawZero ? first : (first.equals("0") ? "" : first);
         this.animations[0].setValue(0.0F);
      }
   }

   public void settings(boolean drawZero, ColorRGBA color) {
      this.drawZero = drawZero;
      this.color = color;
   }
}
