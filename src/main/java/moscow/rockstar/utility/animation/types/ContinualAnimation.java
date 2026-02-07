package moscow.rockstar.utility.animation.types;

import lombok.Generated;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;

public class ContinualAnimation {
   private float output;
   private float endpoint;
   private Animation animation = new Animation(0L, Easing.SMOOTH_STEP);

   public void animate(float destination, int ms) {
      this.output = this.endpoint - this.animation.getValue();
      this.endpoint = destination;
      if (this.output != this.endpoint - destination) {
         this.animation = new Animation(ms, this.endpoint - this.output, Easing.SMOOTH_STEP);
         this.animation.update(0.0F);
      }
   }

   public boolean isDone() {
      return this.output == this.endpoint || this.animation.isDone();
   }

   public float getValue() {
      this.output = this.endpoint - this.animation.getValue();
      return this.output;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }
}
