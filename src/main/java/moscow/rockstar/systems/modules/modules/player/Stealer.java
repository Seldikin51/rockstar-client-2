package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Stealer", category = ModuleCategory.PLAYER)
public class Stealer extends BaseModule {
   private final SliderSetting delay = new SliderSetting(this, "modules.settings.stealer.delay").min(0.0F).max(5.0F).step(0.1F).currentValue(0.4F);
   private final BooleanSetting close = new BooleanSetting(this, "modules.settings.stealer.close", "modules.settings.stealer.close.description");
   private final BooleanSetting off = new BooleanSetting(this, "modules.settings.stealer.off", "modules.settings.stealer.off.description");
   private final BooleanSetting openMystic = new BooleanSetting(
      this, "modules.settings.stealer.open_mystic", "modules.settings.stealer.open_mystic.description"
   );
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.stealer.mode");
   private final ModeSetting.Value up = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.up").select();
   private final ModeSetting.Value down = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.down");
   private final ModeSetting.Value center = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.center");
   private final ModeSetting.Value random = new ModeSetting.Value(this.mode, "modules.settings.stealer.mode.random");
   private final Timer clickTimer = new Timer();
   private final Timer openTimer = new Timer();
   private final List<EnderChestBlockEntity> blackList = new ArrayList<>();
   private EnderChestBlockEntity target;

   @Override
   public void tick() {
      if (mc.currentScreen instanceof GenericContainerScreen && mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler) {
         int size = handler.getInventory().size();
         if (this.mode.is(this.up)) {
            for (int i = 0; i < size && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0F + MathUtility.random(-100.0, 100.0))); i++) {
               if (!handler.getSlot(i).getStack().isEmpty()) {
                  mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                  this.clickTimer.reset();
               }
            }
         } else if (this.mode.is(this.down)) {
            for (int ix = size - 1;
               ix >= 0 && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0F + MathUtility.random(-100.0, 100.0)));
               ix--
            ) {
               if (!handler.getSlot(ix).getStack().isEmpty()) {
                  mc.interactionManager.clickSlot(handler.syncId, ix, 0, SlotActionType.QUICK_MOVE, mc.player);
                  this.clickTimer.reset();
               }
            }
         } else if (this.mode.is(this.center)) {
            int centerIndex = size / 2;

            for (int ixx = 0;
               ixx <= centerIndex && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0F + MathUtility.random(-100.0, 100.0)));
               ixx++
            ) {
               int left = centerIndex - ixx;
               int right = centerIndex + ixx;
               if (left >= 0 && !handler.getSlot(left).getStack().isEmpty()) {
                  mc.interactionManager.clickSlot(handler.syncId, left, 0, SlotActionType.QUICK_MOVE, mc.player);
                  this.clickTimer.reset();
               } else if (right < size && !handler.getSlot(right).getStack().isEmpty()) {
                  mc.interactionManager.clickSlot(handler.syncId, right, 0, SlotActionType.QUICK_MOVE, mc.player);
                  this.clickTimer.reset();
               }
            }
         } else if (this.mode.is(this.random) && this.clickTimer.finished((long)(this.delay.getCurrentValue() * 1000.0F + MathUtility.random(-100.0, 100.0)))) {
            int slot = (int)MathUtility.random(0.0, size);
            if (!handler.getSlot(slot).getStack().isEmpty()) {
               mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
               this.clickTimer.reset();
            }
         }

         if (this.isEmpty(handler)) {
            if (this.target != null && this.openMystic.isEnabled()) {
               this.blackList.add(this.target);
            }

            if (this.off.isEnabled()) {
               this.toggle();
            }

            if (this.close.isEnabled()) {
               mc.player.closeHandledScreen();
            }
         }
      } else if (this.openMystic.isEnabled()) {
         if (this.target == null || !this.isValidTarget(this.target)) {
            this.target = this.findTarget();
            this.openTimer.reset();
         }

         if (this.target != null && this.openTimer.finished(200L)) {
            BlockPos pos = this.target.getPos();
            Vec3d hitVec = Vec3d.ofCenter(pos);
            Rotation rotation = RotationMath.getRotationTo(hitVec);
            Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.NONE, 22.0F, 22.0F, 22.0F, RotationPriority.USE_ITEM);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.openTimer.reset();
         }
      }
   }

   private boolean isValidTarget(EnderChestBlockEntity entity) {
      return mc.player.squaredDistanceTo(entity.getPos().toCenterPos()) < 16.0 && !this.blackList.contains(entity);
   }

   private EnderChestBlockEntity findTarget() {
      for (BlockEntity entity : WorldUtility.blockEntities) {
         if (entity instanceof EnderChestBlockEntity e && this.isValidTarget(e)) {
            return e;
         }
      }

      return null;
   }

   private boolean isEmpty(GenericContainerScreenHandler handler) {
      Inventory inv = handler.getInventory();

      for (int i = 0; i < inv.size(); i++) {
         if (!inv.getStack(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
