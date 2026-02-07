package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.Rotation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Creeper Farm", category = ModuleCategory.PLAYER, desc = "Автоматически убивает криперов на фармилке")
public class CreeperFarm extends BaseModule {
   private boolean retreating;
   private Vec3d retreatTarget;

   @Override
   public void tick() {
      if (mc.player != null && FabricLoader.getInstance().isModLoaded("baritone")) {
         LivingEntity creeper = this.findClosestCreeper();
         if (creeper != null) {
            double distance = mc.player.getPos().distanceTo(creeper.getPos());
            if (distance <= 3.3) {
               Rotation rot = this.calculateCreeperRotation(creeper);
               Rockstar.getInstance().getRotationHandler().rotate(rot);
               if (mc.player.getAttackCooldownProgress(1.0F) >= 0.9) {
                  mc.interactionManager.attackEntity(mc.player, creeper);
                  mc.player.swingHand(Hand.MAIN_HAND);
                  Vec3d playerPos = mc.player.getPos();
                  Vec3d creeperPos = creeper.getPos();
                  Vec3d away = playerPos.subtract(creeperPos).normalize();
                  this.retreatTarget = playerPos.add(away.multiply(4.0));
                  this.retreating = true;
               }
            }
         }
      }
   }

   private Rotation calculateCreeperRotation(LivingEntity target) {
      Vec3d toCreeper = new Vec3d(
         target.getX(), target.getY() + (mc.player.distanceTo(target) < 2.0F ? 0.5F : target.getEyeHeight(target.getPose())), target.getZ()
      );
      double dx = toCreeper.x;
      double dy = toCreeper.y;
      double dz = toCreeper.z;
      double horiz = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F + MathUtility.random(-2.0, 2.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz))) + MathUtility.random(-1.0, 1.0);
      return new Rotation(yaw, pitch);
   }

   private Rotation calculateRotationToward(Vec3d targetPos) {
      Vec3d playerEyes = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
      Vec3d toPoint = targetPos.add(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0).subtract(playerEyes);
      double dx = toPoint.x;
      double dy = toPoint.y;
      double dz = toPoint.z;
      double horiz = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F + MathUtility.random(-2.0, 2.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horiz))) + MathUtility.random(-1.0, 1.0);
      return new Rotation(yaw, pitch);
   }

   private LivingEntity findClosestCreeper() {
      LivingEntity closest = null;
      double bestDist = Double.MAX_VALUE;

      for (Entity e : mc.world.getEntities()) {
         if (e instanceof CreeperEntity) {
            double d = mc.player.getPos().distanceTo(e.getPos());
            if (d <= 50.0 && Math.abs(mc.player.getY() - e.getY()) < 4.0 && d < bestDist) {
               bestDist = d;
               closest = (LivingEntity)e;
            }
         }
      }

      return closest;
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         if (FabricLoader.getInstance().isModLoaded("baritone")) {
            mc.player.networkHandler.sendChatMessage("#follow entity creeper");
            mc.player.networkHandler.sendChatMessage("#allowBreak false");
         } else {
            MessageUtility.info(Text.of("Для работы " + this.getName() + " нужен мод baritone"));
            this.toggle();
         }

         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      if (FabricLoader.getInstance().isModLoaded("baritone")) {
         mc.player.networkHandler.sendChatMessage("#stop");
      }

      super.onDisable();
   }
}
