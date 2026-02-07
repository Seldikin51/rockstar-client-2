package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@ModuleInfo(name = "High Jump", category = ModuleCategory.MOVEMENT)
public class HighJump extends BaseModule {
   @Override
   public void tick() {
      mc.options.sneakKey.setPressed(true);
      mc.player.setPitch(90.0F);
      mc.options.useKey.setPressed(true);
      if (mc.player.isOnGround() && !mc.player.getItemCooldownManager().isCoolingDown(Items.WIND_CHARGE.getDefaultStack())) {
         this.useWindCharge();
      }
   }

   @Override
   public void onDisable() {
      int keyCode = InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
      mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode));
      keyCode = InputUtil.fromTranslationKey(mc.options.useKey.getBoundKeyTranslationKey()).getCode();
      mc.options.useKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode));
   }

   private void useWindCharge() {
      if (mc.world != null && mc.player != null && mc.interactionManager != null) {
         SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
         ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
         boolean isOffhand = slot instanceof OffhandSlot;
         if (slot != null) {
            int oldHotbarSlotId = mc.player.getInventory().selectedSlot;
            if (slot instanceof HotbarSlot hotbarSlot && mc.player.getInventory().selectedSlot != hotbarSlot.getSlotId()) {
               InventoryUtility.selectHotbarSlot(hotbarSlot);
            }

            Hand hand = isOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            float yaw = mc.player.getYaw();
            float pitch;
            if (!mc.player.isOnGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && mc.player.getVelocity().y > 0.4F) {
               pitch = 75.0F;

               for (int i = 0; i < 360; i += 45) {
                  BlockHitResult result = mc.world
                     .raycast(
                        new RaycastContext(
                           mc.player.getEyePos(),
                           mc.player.getEyePos().add(mc.player.getRotationVector(pitch, i).multiply(1.5)),
                           ShapeType.COLLIDER,
                           FluidHandling.NONE,
                           mc.player
                        )
                     );
                  if (result.getType() == Type.BLOCK) {
                     yaw = RotationMath.adjustAngle(mc.player.getYaw(), i);
                  }
               }
            } else {
               pitch = 90.0F;
            }

            float finalYaw = yaw;
            mc.interactionManager.sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(hand, sequence, finalYaw, pitch));
            mc.player.swingHand(hand);
            if (slot instanceof HotbarSlot) {
               InventoryUtility.selectHotbarSlot(oldHotbarSlotId);
            }
         }
      }
   }
}
