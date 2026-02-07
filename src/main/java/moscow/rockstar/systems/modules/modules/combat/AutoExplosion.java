package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Auto Explosion", desc = "modules.descriptions.auto_explosion", category = ModuleCategory.COMBAT)
public class AutoExplosion extends BaseModule {
   private final BindSetting bind = new BindSetting(this, "modules.settings.auto_explosion.bind");
   private final BooleanSetting attackOthers = new BooleanSetting(this, "modules.settings.auto_explosion.attack_others");
   private final BooleanSetting selfSave = new BooleanSetting(this, "modules.settings.auto_explosion.self_save");
   private final SliderSetting delay = new SliderSetting(this, "modules.settings.auto_explosion.delay", () -> this.bind.getKey() == -1)
      .min(100.0F)
      .max(1000.0F)
      .step(50.0F)
      .currentValue(500.0F);
   private int lastPlacedCrystalId = -1;
   private final Timer delayTimer = new Timer();
   private boolean pressed;
   private final EventListener<KeyPressEvent> keyPressEvent = event -> {
      if (this.bind.isKey(event.getKey())) {
         this.pressed = event.getAction() == 1;
      }
   };

   @Override
   public void tick() {
      if (this.bind.getKey() == -1 || this.pressed) {
         SlotGroup<HotbarSlot> search = SlotGroups.hotbar();
         HotbarSlot slot = search.findItem(Items.END_CRYSTAL);
         BlockPos targetPos = this.findNearbyObsidian();
         if (slot != null && targetPos != null) {
            int crystalSlot = slot.getIdForServer();
            Vec3d targetVec = new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            float[] rotations = this.calculateLookAngles(targetVec);
            Rockstar.getInstance().getRotationHandler().rotate(new Rotation(rotations[0], rotations[1]));
            if (this.delayTimer.finished((long)this.delay.getCurrentValue() / 2L)) {
               mc.player.getInventory().selectedSlot = crystalSlot - 36;
               this.placeCrystal(targetPos.down());
               if (!this.selfSave.isEnabled() || !this.isAboveCrystal(targetPos)) {
                  this.attackNearbyCrystal(targetPos);
               }

               this.delayTimer.reset();
            }

            super.tick();
         }
      }
   }

   private BlockPos findNearbyObsidian() {
      int radius = 4;
      BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = playerPos.add(x, y, z);
               if (mc.world.getBlockState(checkPos).getBlock() == Blocks.OBSIDIAN) {
                  BlockPos placePos = checkPos.up();
                  if (mc.world.getBlockState(placePos).isAir()) {
                     return placePos;
                  }
               }
            }
         }
      }

      return null;
   }

   private void attackNearbyCrystal(BlockPos pos) {
      double range = 6.0;
      Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof EndCrystalEntity
            && entity.getPos().squaredDistanceTo(center) <= range * range
            && (this.attackOthers.isEnabled() || entity.getId() == this.lastPlacedCrystalId)
            && (!this.selfSave.isEnabled() || !(mc.player.getY() > entity.getY()))) {
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
         }
      }
   }

   private void placeCrystal(BlockPos obsidianBlockPos) {
      Vec3d hitVec = new Vec3d(obsidianBlockPos.getX() + 0.5, obsidianBlockPos.getY() + 1.0, obsidianBlockPos.getZ() + 0.5);
      BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, obsidianBlockPos, false);
      mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
      mc.player.swingHand(Hand.MAIN_HAND);
      mc.execute(() -> mc.world.getEntities().forEach(entity -> {
         if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(hitVec) < 1.0) {
            this.lastPlacedCrystalId = entity.getId();
         }
      }));
   }

   private float[] calculateLookAngles(Vec3d target) {
      Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
      double diffX = target.x - eyesPos.x;
      double diffY = target.y - eyesPos.y;
      double diffZ = target.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{
         mc.player.getYaw() + MathHelper.wrapDegrees(yaw - mc.player.getYaw()), mc.player.getPitch() + MathHelper.wrapDegrees(pitch - mc.player.getPitch())
      };
   }

   private boolean isAboveCrystal(BlockPos crystalPos) {
      return mc.player.getY() > crystalPos.getY() + 1.0;
   }
}
