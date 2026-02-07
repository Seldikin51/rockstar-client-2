package moscow.rockstar.utility.gui;

import lombok.Generated;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.util.math.MatrixStack;

public class ScrollHandler implements IMinecraft {
   private double max;
   private double value;
   private double targetValue;
   private double speed;
   private static final double SCROLL_SMOOTHNESS = 0.4;
   public static final double SCROLLBAR_THICKNESS = 1.0;
   private final Animation scrollAnimation = new Animation(100L, Easing.BAKEK);

   public ScrollHandler() {
      this.value = 0.0;
      this.targetValue = 0.0;
      this.speed = 20.0;
   }

   public void update() {
      this.scrollAnimation.setDuration(300L);
      this.targetValue = Math.min(Math.max(this.targetValue, this.max), 0.0);
      double delta = this.targetValue - this.value;
      this.value += delta;
      if (delta > 0.0) {
         this.scrollAnimation.setEasing(Math.abs(delta) > 21.0 ? Easing.QUARTIC_OUT : Easing.BAKEK);
      }

      this.scrollAnimation.update((float)this.value);
   }

   public double getValue() {
      return -this.scrollAnimation.getValue();
   }

   public void reset() {
      this.value = 0.0;
      this.targetValue = 0.0;
      this.scrollAnimation.reset();
   }

   public void scroll(double amount) {
      this.targetValue = this.targetValue + amount * this.speed;
   }

   public void onKeyPressed(int keyCode) {
      if (keyCode == 265) {
         this.scroll(1.0);
      } else if (keyCode == 264) {
         this.scroll(-1.0);
      }
   }

   public void renderScrollbar(MatrixStack matrixStack, double x, double y, double width, double height, double contentHeight) {
      if (!(contentHeight <= height)) {
         double scrollbarHeight = 50.0;
         double scrollbarY = y + this.value / this.max * (height - scrollbarHeight);
      }
   }

   @Generated
   public double getMax() {
      return this.max;
   }

   @Generated
   public double getTargetValue() {
      return this.targetValue;
   }

   @Generated
   public double getSpeed() {
      return this.speed;
   }

   @Generated
   public Animation getScrollAnimation() {
      return this.scrollAnimation;
   }

   @Generated
   public void setMax(double max) {
      this.max = max;
   }

   @Generated
   public void setValue(double value) {
      this.value = value;
   }

   @Generated
   public void setTargetValue(double targetValue) {
      this.targetValue = targetValue;
   }

   @Generated
   public void setSpeed(double speed) {
      this.speed = speed;
   }
}
