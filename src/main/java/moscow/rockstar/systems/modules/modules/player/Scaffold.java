package moscow.rockstar.systems.modules.modules.player;

import java.util.Arrays;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Scaffold", desc = "modules.descriptions.scaffold", category = ModuleCategory.PLAYER)
public class Scaffold extends BaseModule {
   private static final List<Block> BLACKLIST = Arrays.asList(
      Blocks.CHEST,
      Blocks.ENDER_CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.SAND,
      Blocks.CRAFTING_TABLE,
      Blocks.FURNACE,
      Blocks.STONE_PRESSURE_PLATE,
      Blocks.OAK_PRESSURE_PLATE,
      Blocks.BIRCH_PRESSURE_PLATE,
      Blocks.SPRUCE_PRESSURE_PLATE,
      Blocks.JUNGLE_PRESSURE_PLATE,
      Blocks.ACACIA_PRESSURE_PLATE,
      Blocks.DARK_OAK_PRESSURE_PLATE,
      Blocks.CRIMSON_PRESSURE_PLATE,
      Blocks.WARPED_PRESSURE_PLATE
   );
   private final Timer placeTimer = new Timer();
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         BlockPos below = this.getPredictedPos();
         if (mc.world.getBlockState(below).isAir()) {
            int slot = this.findBlockSlot();
            if (slot == -1) {
               slot = this.findInventoryBlock();
               if (slot != -1) {
                  InventorySlot inv = InventoryUtility.getInventorySlot(slot);
                  HotbarSlot target = InventoryUtility.getCurrentHotbarSlot();
                  InventoryUtility.moveItem(inv, target);
                  slot = target.getSlotId();
               }
            }

            if (mc.player.getInventory().selectedSlot != slot) {
               mc.player.getInventory().selectedSlot = slot;
            }

            if (this.placeTimer.finished(50L)) {
               BlockHitResult hit = this.findHit(below);
               if (hit == null) {
                  return;
               }

               Vec3d hitVec = hit.getPos();
               Rotation rotation = RotationMath.getRotationTo(hitVec);
               float yawDiff = Math.abs(rotation.getYaw() - mc.player.getYaw());
               float pitchDiff = Math.abs(rotation.getPitch() - mc.player.getPitch());
               if (yawDiff > 10.0F || pitchDiff > 10.0F) {
                  Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.DIRECT, 100.0F, 100.0F, 100.0F, RotationPriority.USE_ITEM);
               }

               ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
               if (result.isAccepted() && Rockstar.getInstance().getRotationHandler().isIdling()) {
                  mc.player.swingHand(Hand.MAIN_HAND);
                  this.placeTimer.reset();
               }
            }
         }
      }
   };

   private int findInventoryBlock() {
      for (int i = 0; i < 27; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i + 9);
         if (stack.getCount() > 0 && stack.getItem() instanceof BlockItem blockItem && !BLACKLIST.contains(blockItem.getBlock())) {
            return i;
         }
      }

      return -1;
   }

   private BlockPos getPredictedPos() {
      Vec3d vel = mc.player.getVelocity();
      int dx = (int)Math.round(vel.x);
      int dz = (int)Math.round(vel.z);
      BlockPos pos = mc.player.getBlockPos().add(dx, 0, dz);
      return pos.down();
   }

   private int findBlockSlot() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.getCount() > 0 && stack.getItem() instanceof BlockItem blockItem && !BLACKLIST.contains(blockItem.getBlock())) {
            return i;
         }
      }

      return -1;
   }

   private BlockHitResult findHit(BlockPos target) {
      Direction[] faces = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

      for (Direction face : faces) {
         BlockPos neighbour = target.offset(face);
         if (!mc.world.getBlockState(neighbour).isAir()) {
            Vec3d hitVec = Vec3d.ofCenter(neighbour).add(Vec3d.of(face.getVector()).multiply(0.5));
            return new BlockHitResult(hitVec, face.getOpposite(), neighbour, false);
         }
      }

      return null;
   }
}
