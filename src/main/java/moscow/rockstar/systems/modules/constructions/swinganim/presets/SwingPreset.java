package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import lombok.Generated;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.util.math.Vec2f;

public final class SwingPreset {
   private final String name;
   private final Vec2f bezierStart;
   private final Vec2f bezierEnd;
   private final boolean swingBack;
   private final float speed;
   private final SwingTransformations from;
   private final SwingTransformations to;
   private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
   private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

   @Generated
   public SwingPreset(String name, Vec2f bezierStart, Vec2f bezierEnd, boolean swingBack, float speed, SwingTransformations from, SwingTransformations to) {
      this.name = name;
      this.bezierStart = bezierStart;
      this.bezierEnd = bezierEnd;
      this.swingBack = swingBack;
      this.speed = speed;
      this.from = from;
      this.to = to;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Vec2f getBezierStart() {
      return this.bezierStart;
   }

   @Generated
   public Vec2f getBezierEnd() {
      return this.bezierEnd;
   }

   @Generated
   public boolean isSwingBack() {
      return this.swingBack;
   }

   @Generated
   public float getSpeed() {
      return this.speed;
   }

   @Generated
   public SwingTransformations getFrom() {
      return this.from;
   }

   @Generated
   public SwingTransformations getTo() {
      return this.to;
   }

   @Generated
   public Animation getHoverAnimation() {
      return this.hoverAnimation;
   }

   @Generated
   public Animation getActiveAnimation() {
      return this.activeAnimation;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof SwingPreset other)) {
         return false;
      } else if (this.isSwingBack() != other.isSwingBack()) {
         return false;
      } else if (Float.compare(this.getSpeed(), other.getSpeed()) != 0) {
         return false;
      } else {
         Object this$name = this.getName();
         Object other$name = other.getName();
         if (this$name == null ? other$name == null : this$name.equals(other$name)) {
            Object this$bezierStart = this.getBezierStart();
            Object other$bezierStart = other.getBezierStart();
            if (this$bezierStart == null ? other$bezierStart == null : this$bezierStart.equals(other$bezierStart)) {
               Object this$bezierEnd = this.getBezierEnd();
               Object other$bezierEnd = other.getBezierEnd();
               if (this$bezierEnd == null ? other$bezierEnd == null : this$bezierEnd.equals(other$bezierEnd)) {
                  Object this$from = this.getFrom();
                  Object other$from = other.getFrom();
                  if (this$from == null ? other$from == null : this$from.equals(other$from)) {
                     Object this$to = this.getTo();
                     Object other$to = other.getTo();
                     if (this$to == null ? other$to == null : this$to.equals(other$to)) {
                        Object this$hoverAnimation = this.getHoverAnimation();
                        Object other$hoverAnimation = other.getHoverAnimation();
                        if (this$hoverAnimation == null ? other$hoverAnimation == null : this$hoverAnimation.equals(other$hoverAnimation)) {
                           Object this$activeAnimation = this.getActiveAnimation();
                           Object other$activeAnimation = other.getActiveAnimation();
                           return this$activeAnimation == null ? other$activeAnimation == null : this$activeAnimation.equals(other$activeAnimation);
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + (this.isSwingBack() ? 79 : 97);
      result = result * 59 + Float.floatToIntBits(this.getSpeed());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $bezierStart = this.getBezierStart();
      result = result * 59 + ($bezierStart == null ? 43 : $bezierStart.hashCode());
      Object $bezierEnd = this.getBezierEnd();
      result = result * 59 + ($bezierEnd == null ? 43 : $bezierEnd.hashCode());
      Object $from = this.getFrom();
      result = result * 59 + ($from == null ? 43 : $from.hashCode());
      Object $to = this.getTo();
      result = result * 59 + ($to == null ? 43 : $to.hashCode());
      Object $hoverAnimation = this.getHoverAnimation();
      result = result * 59 + ($hoverAnimation == null ? 43 : $hoverAnimation.hashCode());
      Object $activeAnimation = this.getActiveAnimation();
      return result * 59 + ($activeAnimation == null ? 43 : $activeAnimation.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "SwingPreset(name="
         + this.getName()
         + ", bezierStart="
         + this.getBezierStart()
         + ", bezierEnd="
         + this.getBezierEnd()
         + ", swingBack="
         + this.isSwingBack()
         + ", speed="
         + this.getSpeed()
         + ", from="
         + this.getFrom()
         + ", to="
         + this.getTo()
         + ", hoverAnimation="
         + this.getHoverAnimation()
         + ", activeAnimation="
         + this.getActiveAnimation()
         + ")";
   }
}
