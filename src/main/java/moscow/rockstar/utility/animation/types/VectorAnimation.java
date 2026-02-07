package moscow.rockstar.utility.animation.types;

import lombok.NonNull;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.util.math.Vec3d;

public class VectorAnimation {
   private static final Easing DEFAULT_EASING = Easing.CUBIC_IN_OUT;
   private final long duration;
   private final Animation x;
   private final Animation y;
   private final Animation z;

   public VectorAnimation(long duration, Easing easing) {
      this.duration = duration;
      this.x = new Animation(duration, easing);
      this.y = new Animation(duration, easing);
      this.z = new Animation(duration, easing);
   }

   public VectorAnimation(long duration) {
      this(duration, DEFAULT_EASING);
   }

   public VectorAnimation(long duration, Vec3d initalVec, Easing easing) {
      this.duration = duration;
      this.x = new Animation(duration, (float)initalVec.getX(), easing);
      this.y = new Animation(duration, (float)initalVec.getY(), easing);
      this.z = new Animation(duration, (float)initalVec.getZ(), easing);
   }

   public VectorAnimation(long duration, Vec3d initalVec) {
      this(duration, initalVec, DEFAULT_EASING);
   }

   public void update(@NonNull Vec3d vec) {
      if (vec == null) {
         throw new NullPointerException("vec is marked non-null but is null");
      } else {
         this.x.setValue((float)vec.getX());
         this.y.setValue((float)vec.getY());
         this.z.setValue((float)vec.getZ());
      }
   }

   public Vec3d getVec() {
      return new Vec3d((int)this.x.getValue(), (int)this.y.getValue(), (int)this.z.getValue());
   }

   public void setEasing(Easing easing) {
      this.x.setEasing(easing);
      this.y.setEasing(easing);
      this.z.setEasing(easing);
   }

   public void setDuration(long duration) {
      this.x.setDuration(duration);
      this.y.setDuration(duration);
      this.z.setDuration(duration);
   }

   public void setVec(@NonNull Vec3d vec) {
      if (vec == null) {
         throw new NullPointerException("vec is marked non-null but is null");
      } else {
         this.x.setValue((float)vec.getX());
         this.y.setValue((float)vec.getY());
         this.z.setValue((float)vec.getZ());
      }
   }
}
