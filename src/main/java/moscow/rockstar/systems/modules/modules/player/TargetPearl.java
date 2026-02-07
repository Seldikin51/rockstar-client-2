package moscow.rockstar.systems.modules.modules.player;

import java.util.Comparator;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@ModuleInfo(name = "Target Pearl", category = ModuleCategory.PLAYER, desc = "modules.descriptions.target_pearl")
public class TargetPearl extends BaseModule {
   private final Timer delayTimer = new Timer();
   private BlockPos targetBlock;
   private int lastPearlId;
   private int lastOurPearlId;
   private float rotationYaw;
   private float rotationPitch;
   private int tick;
   private boolean shouldThrowPearl;
   private int thrownPearls = 0;
   private final Timer pearlDelayTimer = new Timer();
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (mc.player != null && mc.world != null) {
         RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
         if (this.tick > 0) {
            rotationHandler.rotate(new Rotation(this.rotationYaw, this.rotationPitch), MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.OVERRIDE);
            this.tick++;
         }

         if (!(mc.player.getHealth() < 5.0F)) {
            if (this.delayTimer.finished(1000L)) {
               for (Entity ent : mc.world.getEntities()) {
                  if (ent instanceof EnderPearlEntity pearl) {
                     if (pearl.getOwner() == mc.player) {
                        this.lastOurPearlId = pearl.getId();
                     } else if (pearl.getId() != this.lastPearlId && pearl.getId() != this.lastOurPearlId) {
                        mc.world
                           .getPlayers()
                           .stream()
                           .filter(p -> p != mc.player)
                           .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(pearl)))
                           .ifPresent(player -> {
                              this.targetBlock = this.calcTrajectory(pearl);
                              this.lastPearlId = pearl.getId();
                           });
                     }
                  }
               }

               if (this.targetBlock != null) {
                  if (!(mc.player.squaredDistanceTo(this.targetBlock.toCenterPos()) < 9.0)) {
                     this.rotationPitch = (float)(-Math.toDegrees(this.calcTrajectory(this.targetBlock)));
                     this.rotationYaw = (float)Math.toDegrees(
                           Math.atan2(this.targetBlock.getZ() + 0.5F - mc.player.getZ(), this.targetBlock.getX() + 0.5F - mc.player.getX())
                        )
                        - 90.0F;
                     BlockPos tracedBP = this.checkTrajectory(this.rotationYaw, this.rotationPitch);
                     if (tracedBP != null && !(this.targetBlock.getSquaredDistance(tracedBP) > 36.0)) {
                        this.tick = 1;
                        this.shouldThrowPearl = true;
                        this.targetBlock = null;
                        this.delayTimer.reset();
                        this.thrownPearls = 0;
                     }
                  }
               }
            }
         }
      }
   };

   @Override
   public void tick() {
      if (this.shouldThrowPearl && this.tick >= 3) {
         int pearlsToThrow = 1;
         if (this.thrownPearls < pearlsToThrow && this.pearlDelayTimer.finished(100L)) {
            this.throwPearl();
            this.thrownPearls++;
            this.pearlDelayTimer.reset();
         }

         if (this.thrownPearls >= pearlsToThrow) {
            this.shouldThrowPearl = false;
            this.tick = 0;
         }
      }
   }

   private void throwPearl() {
      SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
      ItemSlot pearlItemSlot = slotsToSearch.findItem(Items.ENDER_PEARL);
      if (pearlItemSlot != null) {
         int originalSlot = mc.player.getInventory().selectedSlot;
         int pearlSlot = pearlItemSlot.getIdForServer();
         if (pearlSlot != originalSlot) {
            mc.interactionManager.clickSlot(0, pearlSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, mc.player);
         }

         mc.getNetworkHandler()
            .sendPacket(
               new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, (int)(this.rotationYaw * 256.0F / 360.0F), (int)(this.rotationPitch * 256.0F / 360.0F))
            );
         mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
         if (pearlSlot != originalSlot) {
            mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, pearlSlot, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
               mc.interactionManager.clickSlot(0, 36 + originalSlot, 0, SlotActionType.PICKUP, mc.player);
            }
         }
      }
   }

   private float calcTrajectory(BlockPos bp) {
      double a = Math.hypot(bp.getX() + 0.5F - mc.player.getX(), bp.getZ() + 0.5F - mc.player.getZ());
      double y = 6.125 * (bp.getY() + 1.0F - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())));
      y = 0.05F * (0.05F * (a * a) + y);
      y = Math.sqrt(9.378906F - y);
      double d = 3.0625 - y;
      y = Math.atan2(d * d + y, 0.05F * a);
      d = Math.atan2(d, 0.05F * a);
      return (float)Math.min(y, d);
   }

   private BlockPos calcTrajectory(Entity e) {
      return this.traceTrajectory(e.getX(), e.getY(), e.getZ(), e.getVelocity().x, e.getVelocity().y, e.getVelocity().z);
   }

   private BlockPos checkTrajectory(float yaw, float pitch) {
      if (Float.isNaN(pitch)) {
         return null;
      } else {
         float yawRad = yaw * (float) (Math.PI / 180.0);
         float pitchRad = pitch * (float) (Math.PI / 180.0);
         double x = mc.player.getX() - Math.cos(yawRad) * 0.16F;
         double y = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
         double z = mc.player.getZ() - Math.sin(yawRad) * 0.16F;
         double motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * 0.4F;
         double motionY = -Math.sin(pitchRad) * 0.4F;
         double motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * 0.4F;
         float distance = (float)Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
         motionX /= distance;
         motionY /= distance;
         motionZ /= distance;
         motionX *= 1.5;
         motionY *= 1.5;
         motionZ *= 1.5;
         if (!mc.player.isOnGround()) {
            motionY += mc.player.getVelocity().y;
         }

         return this.traceTrajectory(x, y, z, motionX, motionY, motionZ);
      }
   }

   private BlockPos traceTrajectory(double x, double y, double z, double mx, double my, double mz) {
      for (int i = 0; i < 300; i++) {
         Vec3d lastPos = new Vec3d(x, y, z);
         x += mx;
         y += my;
         z += mz;
         mx *= 0.99;
         my *= 0.99;
         mz *= 0.99;
         my -= 0.03F;
         Vec3d pos = new Vec3d(x, y, z);
         BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
         if (bhr != null && bhr.getType() == Type.BLOCK) {
            return bhr.getBlockPos();
         }

         for (Entity ent : mc.world.getEntities()) {
            if (!(ent instanceof ArrowEntity)
               && ent != mc.player
               && !(ent instanceof EnderPearlEntity)
               && ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.2))) {
               return null;
            }
         }

         if (y <= -65.0) {
            break;
         }
      }

      return null;
   }
}
