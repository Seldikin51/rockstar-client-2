package moscow.rockstar.systems.modules.modules.player.autofarm;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.AutoFarm;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoFarmNukeTurkey implements IMinecraft {
   private static final int SEARCH_RADIUS = 4;

   public void nuke() {
      BlockPos targetBlockPos = null;
      Block targetBlock = null;

      for (int y = -4; y <= 4; y++) {
         for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
               BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
               BlockPos pos = mc.player.getBlockPos().add(offset);
               if (!(mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 6.0)) {
                  Block block = mc.world.getBlockState(pos).getBlock();
                  AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
                  if (autoFarm.getMelon().isSelected() && block == Blocks.MELON || autoFarm.getTikva().isSelected() && block == Blocks.PUMPKIN) {
                     targetBlockPos = pos;
                     targetBlock = block;
                     break;
                  }
               }
            }

            if (targetBlockPos != null) {
               break;
            }
         }

         if (targetBlockPos != null) {
            break;
         }
      }

      if (targetBlockPos == null) {
         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
               for (int zx = 0; zx < 8; zx++) {
                  BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (zx % 2 == 0 ? -zx : zx) / 2);
                  BlockPos pos = mc.player.getBlockPos().up().add(offset);
                  if (!(mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 6.0) && mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                     Block block = mc.world.getBlockState(pos).getBlock();
                     AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
                     if (autoFarm.getMelon().isSelected() && block == Blocks.MELON
                        || autoFarm.getTikva().isSelected() && block == Blocks.PUMPKIN
                        || autoFarm.getAllCrops().isSelected() && block instanceof CropBlock) {
                        targetBlockPos = pos;
                        targetBlock = block;
                        break;
                     }
                  }
               }

               if (targetBlockPos != null) {
                  break;
               }
            }

            if (targetBlockPos != null) {
               break;
            }
         }
      }

      if (targetBlockPos != null && targetBlock != null) {
         double posX = targetBlockPos.getX();
         double posY = targetBlockPos.getY();
         double posZ = targetBlockPos.getZ();
         double deltaX = posX - mc.player.getX();
         double deltaY = posY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
         double deltaZ = posZ - mc.player.getZ();
         double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F + MathUtility.random(-2.0, 2.0);
         float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
         Rockstar.getInstance().getRotationHandler().rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0F, 80.0F, 180.0F, RotationPriority.NORMAL);
         this.equipAxe();
         Direction direction = getDirection(targetBlockPos);
         mc.interactionManager.updateBlockBreakingProgress(targetBlockPos, direction);
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   private void equipAxe() {
      SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
      ItemSlot axe = search.findItem(Items.DIAMOND_AXE);
      if (axe != null) {
         if (axe instanceof HotbarSlot itemHotbarSlot) {
            if (InventoryUtility.getCurrentHotbarSlot().item() != axe.item()) {
               InventoryUtility.selectHotbarSlot(itemHotbarSlot);
            }
         } else if (axe instanceof InventorySlot itemInventorySlot) {
            HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
            itemInventorySlot.swapTo(currentSlot);
         }
      }
   }

   public static Direction getDirection(BlockPos pos) {
      Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
      if (pos.getY() > eyesPos.y) {
         return mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable() ? Direction.DOWN : mc.player.getHorizontalFacing().getOpposite();
      } else {
         return !mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable() ? mc.player.getHorizontalFacing().getOpposite() : Direction.UP;
      }
   }
}
