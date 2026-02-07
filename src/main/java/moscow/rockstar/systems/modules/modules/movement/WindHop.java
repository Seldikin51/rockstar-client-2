package moscow.rockstar.systems.modules.modules.movement;

import java.util.function.Predicate;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import moscow.rockstar.utility.mixins.ArmorItemAddition;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.block.Blocks;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@ModuleInfo(name = "Wind Hop", category = ModuleCategory.MOVEMENT, desc = "modules.descriptions.wind_hop")
public class WindHop extends BaseModule {
   private final BooleanSetting autoJump = new BooleanSetting(this, "modules.settings.wind_hop.autoJump", "modules.settings.wind_hop.autoJump.description")
      .enable();
   private final BooleanSetting swingArm = new BooleanSetting(this, "modules.settings.wind_hop.swingArm", "modules.settings.wind_hop.swingArm.description")
      .enable();
   private final BooleanSetting predictJump = new BooleanSetting(
         this, "modules.settings.wind_hop.predictJump", "modules.settings.wind_hop.predictJump.description"
      )
      .enable();
   private boolean elytra;
   private final EventListener<EntityJumpEvent> onJump = event -> {
      if (event.getEntity() == mc.player) {
         this.useWindCharge();
      }
   };

   private void swapElytraChestplate() {
      ItemSlot chestplateSlot = InventoryUtility.getChestplateSlot();
      SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
      ItemSlot elytraItemSlot = slotsToSearch.findItem(
         (Predicate<ItemStack>)(itemStack -> itemStack.getItem() == Items.ELYTRA && !itemStack.willBreakNextUse())
      );
      ItemSlot chestplateItemSlot = slotsToSearch.findItem(
         (Predicate<ItemStack>)(itemStack -> itemStack.getItem() instanceof ArmorItem armorItem
            && ((ArmorItemAddition)armorItem).rockstar$getType() == EquipmentType.CHESTPLATE)
      );
      boolean isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
      if (!isElytraEquipped && elytraItemSlot != null) {
         elytraItemSlot.swapTo(chestplateSlot);
      } else if (chestplateItemSlot != null) {
         chestplateItemSlot.swapTo(chestplateSlot);
      }
   }

   @Override
   public void tick() {
      if (mc.player != null && mc.world != null) {
         SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
         ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
         boolean canUse = false;
         if (!mc.player.isOnGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && mc.player.getVelocity().y > 0.4F) {
            float pitch = 75.0F;

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
                  canUse = true;
               }
            }
         } else {
            canUse = this.predictJump.isEnabled() && EntityUtility.getBlock(0.0, -1.0, 0.0) != Blocks.AIR && mc.player.fallDistance > 2.0F;
         }

         if (canUse && (mc.options.jumpKey.isPressed() || this.autoJump.isEnabled()) && slot != null) {
            this.useWindCharge();
         }

         if (mc.player.isOnGround() && this.autoJump.isEnabled() && slot != null) {
            mc.player.jump();
         }

         super.tick();
      }
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
            if (this.swingArm.isEnabled()) {
               mc.player.swingHand(hand);
            }

            if (slot instanceof HotbarSlot) {
               InventoryUtility.selectHotbarSlot(oldHotbarSlotId);
            }
         }
      }
   }
}
