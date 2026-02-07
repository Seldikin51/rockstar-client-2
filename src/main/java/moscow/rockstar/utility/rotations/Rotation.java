package moscow.rockstar.utility.rotations;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.util.math.Vec3d;

public class Rotation implements IMinecraft {
   public static final Rotation ZERO = new Rotation(0.0F, 0.0F);
   private float yaw;
   private float pitch;

   public Rotation difference(Rotation other) {
      float diffYaw = MathUtility.angleDifference(this.yaw, other.yaw);
      float diffPitch = MathUtility.angleDifference(this.pitch, other.pitch);
      return new Rotation(diffYaw, diffPitch);
   }

   public float differenceValue(Rotation other) {
      float diffYaw = MathUtility.angleDifference(this.yaw, other.yaw);
      float diffPitch = MathUtility.angleDifference(this.pitch, other.pitch);
      return Math.abs(diffYaw) + Math.abs(diffPitch);
   }

   public Vec3d getRotationVector() {
      return mc.player.getRotationVector(this.pitch, this.yaw);
   }

   public void modify(float yaw, float pitch) {
      this.yaw += yaw;
      this.pitch += pitch;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Rotation other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         return Float.compare(this.getYaw(), other.getYaw()) != 0 ? false : Float.compare(this.getPitch(), other.getPitch()) == 0;
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Rotation;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      return result * 59 + Float.floatToIntBits(this.getPitch());
   }

   @Generated
   @Override
   public String toString() {
      return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   @Generated
   public Rotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
