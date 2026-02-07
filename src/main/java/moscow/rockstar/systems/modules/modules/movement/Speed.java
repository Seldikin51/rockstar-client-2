package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Speed", category = ModuleCategory.MOVEMENT, desc = "modules.descriptions.speed")
public class Speed extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.speed.mode");
   private final ModeSetting.Value vanilla = new ModeSetting.Value(this.mode, "modules.settings.speed.vanilla");
   private final ModeSetting.Value elytra = new ModeSetting.Value(this.mode, "Spooky Elytra");

   @Override
   public void tick() {
      if (this.vanilla.isSelected()) {
         BlockPos pos = mc.player.getBlockPos().add(0, -1, 0);
         mc.options.sneakKey.setPressed(false);
         Rockstar.getInstance().getRotationHandler().rotate(new Rotation(mc.player.getYaw(), 90.0F));
         if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
            mc.player.jump();
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, velocity.y - 0.4F, velocity.z);
            BlockPos target5 = mc.player.getBlockPos().add(0, 1, 0);
            BlockPos target = mc.player.getBlockPos();
            BlockPos target1 = mc.player.getBlockPos().add(0, -1, 0);
            BlockPos target2 = mc.player.getBlockPos().add(0, -2, 0);
            mc.world.setBlockState(target1, Blocks.ICE.getDefaultState());
            Vec3d vector3d = new Vec3d(target.getX(), target.getY(), target.getZ());
            new BlockHitResult(vector3d, Direction.UP, target, false);
            Vec3d vector3d1 = new Vec3d(target1.getX(), target1.getY(), target1.getZ());
            BlockHitResult result1 = new BlockHitResult(vector3d1, Direction.UP, target1, false);
            Vec3d vector3d2 = new Vec3d(target2.getX(), target2.getY(), target2.getZ());
            new BlockHitResult(vector3d2, Direction.UP, target2, false);
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, result1, 0));
         }
      } else if (this.elytra.isSelected()) {
         SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
         HotbarSlot slot = slotsToSearch.findItem(Items.ELYTRA);
         if (slot != null && mc.player.fallDistance > 1.0F) {
            HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            InventoryUtility.selectHotbarSlot(slot);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            ((Slot)mc.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack(Items.ELYTRA));
            if (mc.player.isSprinting() && mc.player.input.hasForwardMovement() && mc.player.checkGliding()) {
               mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
            }

            InventoryUtility.selectHotbarSlot(currentItem);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         }
      }

      super.tick();
   }

   @Override
   public void onDisable() {
      EntityUtility.resetTimer();
      super.onDisable();
   }
}
