package moscow.rockstar.utility.rotations;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class RotationMath implements IMinecraft {
   public static Vec3d getNearestPoint(LivingEntity entity) {
      Vec3d pos = mc.player.getEyePos();
      return new Vec3d(
         MathHelper.clamp(pos.x, entity.getBoundingBox().minX, entity.getBoundingBox().maxX),
         MathHelper.clamp(pos.y, entity.getBoundingBox().minY, entity.getBoundingBox().maxY),
         MathHelper.clamp(pos.z, entity.getBoundingBox().minZ, entity.getBoundingBox().maxZ)
      );
   }

   public static Vec3d getNearestPoint(LivingEntity entity, Vec3d pos) {
      return entity.getPos().subtract(entity.getPos()).add(getNearestPoint(entity));
   }

   public static Rotation getRotationTo(Vec3d targetedEntity) {
      double posX = targetedEntity.getX();
      double posY = targetedEntity.getY();
      double posZ = targetedEntity.getZ();
      double deltaX = posX - mc.player.getX();
      double deltaY = posY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
      double deltaZ = posZ - mc.player.getZ();
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
      return new Rotation(yaw, pitch);
   }

   public static double getGcd() {
      double sensitivity = (Double)mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
      double sensitivityPow3 = sensitivity * sensitivity * sensitivity;
      return sensitivityPow3 * 8.0 * 0.15F;
   }

   @NotNull
   public static Rotation correctRotation(@NotNull Rotation rotation) {
      double gcd = getGcd();
      float yaw = (float)(rotation.getYaw() - rotation.getYaw() % gcd);
      float pitch = (float)(rotation.getPitch() - rotation.getPitch() % gcd);
      return new Rotation(yaw, pitch);
   }

   public static float getAngleDifference(float current, float target) {
      float diff = target - current;

      while (diff > 180.0F) {
         diff -= 360.0F;
      }

      while (diff < -180.0F) {
         diff += 360.0F;
      }

      return diff;
   }

   public static float adjustAngle(float currentAngle, float targetAngle) {
      float normalizedCurrent = currentAngle % 360.0F;
      if (normalizedCurrent < 0.0F) {
         normalizedCurrent += 360.0F;
      }

      float normalizedTarget = targetAngle % 360.0F;
      if (normalizedTarget < 0.0F) {
         normalizedTarget += 360.0F;
      }

      int revolutions = (int)(currentAngle / 360.0F);
      if (currentAngle < 0.0F && currentAngle % 360.0F != 0.0F) {
         revolutions--;
      }

      float adjustedTarget = normalizedTarget + revolutions * 360;
      float difference = adjustedTarget - currentAngle;
      if (difference > 180.0F) {
         adjustedTarget -= 360.0F;
      } else if (difference < -180.0F) {
         adjustedTarget += 360.0F;
      }

      return adjustedTarget;
   }

   @Generated
   private RotationMath() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
