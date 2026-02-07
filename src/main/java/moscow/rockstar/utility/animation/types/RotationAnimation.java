package moscow.rockstar.utility.animation.types;

import lombok.NonNull;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.rotations.Rotation;

public class RotationAnimation {
   private static final Easing DEFAULT_EASING = Easing.LINEAR;
   private final Animation yaw;
   private final Animation pitch;

   public RotationAnimation(long durationX, long durationY, Easing easing) {
      this.yaw = new Animation(durationX, easing);
      this.pitch = new Animation(durationY, easing);
   }

   public RotationAnimation(long duration) {
      this(duration, duration, DEFAULT_EASING);
   }

   public RotationAnimation(long durationX, long durationY, Rotation initialRotation, Easing easing) {
      this.yaw = new Animation(durationX, initialRotation.getYaw(), easing);
      this.pitch = new Animation(durationY, initialRotation.getPitch(), easing);
   }

   public RotationAnimation(long duration, Rotation rotation) {
      this(duration, duration, rotation, DEFAULT_EASING);
   }

   public void update(@NonNull Rotation rotation) {
      if (rotation == null) {
         throw new NullPointerException("rotation is marked non-null but is null");
      } else {
         this.yaw.update(rotation.getYaw());
         this.pitch.update(rotation.getPitch());
      }
   }

   public Rotation getRotation() {
      return new Rotation(this.yaw.getValue(), this.pitch.getValue());
   }

   public void setDurationX(long value) {
      this.yaw.setDuration(value);
   }

   public void setDurationY(long value) {
      this.pitch.setDuration(value);
   }

   public void setEasing(Easing easing) {
      this.yaw.setEasing(easing);
      this.pitch.setEasing(easing);
   }

   public void setRotation(@NonNull Rotation rotation) {
      if (rotation == null) {
         throw new NullPointerException("rotation is marked non-null but is null");
      } else {
         this.yaw.setValue(rotation.getYaw());
         this.pitch.setValue(rotation.getPitch());
      }
   }
}
