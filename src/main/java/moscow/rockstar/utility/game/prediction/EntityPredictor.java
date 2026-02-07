package moscow.rockstar.utility.game.prediction;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class EntityPredictor implements IMinecraft {
   public static float predictDamage(Entity crystal, PlayerEntity target) {
      Vec3d crystalPos = new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ());
      Vec3d targetPos = target.getBoundingBox().getCenter();
      double dist = targetPos.distanceTo(crystalPos);
      if (dist < 0.5) {
         dist = 0.0;
      }

      double scaledImpact = 1.0 - MathHelper.clamp(dist / 6.0, 0.0, 1.0);
      boolean blocked = target.getWorld().raycast(new RaycastContext(crystalPos, targetPos, ShapeType.COLLIDER, FluidHandling.NONE, target)).getType()
         != Type.MISS;
      float exposure = blocked ? 0.7F : 1.0F;
      return (float)(exposure * (scaledImpact * 24.0 + 1.0));
   }

   @Generated
   private EntityPredictor() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
