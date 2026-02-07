package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.group.impl.HotbarSlotsGroup;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtility implements IMinecraft {
   public static HotbarSlot getHotbarSlot(int slotId) {
      return new HotbarSlot(slotId);
   }

   public static InventorySlot getInventorySlot(int slotId) {
      return new InventorySlot(slotId);
   }

   public static ArmorSlot getArmorSlot(int armorIndex) {
      return new ArmorSlot(armorIndex);
   }

   public static ArmorSlot getHelmetSlot() {
      return getArmorSlot(3);
   }

   public static ArmorSlot getChestplateSlot() {
      return getArmorSlot(2);
   }

   public static ArmorSlot getLeggingsSlot() {
      return getArmorSlot(1);
   }

   public static ArmorSlot getBootsSlot() {
      return getArmorSlot(0);
   }

   public static OffhandSlot getOffHandSlot() {
      return new OffhandSlot();
   }

   public static boolean hasItemInOffHand(Item item) {
      return getOffHandSlot().contains(item);
   }

   public static boolean offHandItemMatches(Predicate<ItemStack> predicate) {
      return getOffHandSlot().matches(predicate);
   }

   public static boolean isOffHandEmpty() {
      return getOffHandSlot().isEmpty();
   }

   public static void moveItem(ItemSlot from, ItemSlot to) {
      if (mc.getNetworkHandler() != null) {
         from.click();
         to.click();
         if (!to.isEmpty()) {
            from.click();
         }

         mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
      }
   }

   public static void quickMove(int from) {
      if (mc.getNetworkHandler() != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.QUICK_MOVE, mc.player);
      }
   }

   public static void moveItem(int from, int to) {
      moveItem(from, to, false);
   }

   public static void moveItem(int from, int to, boolean back) {
      if (mc.getNetworkHandler() != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, to, 0, SlotActionType.PICKUP, mc.player);
         if (back) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, mc.player);
         }
      }
   }

   public static void moveHalf(int from, int to) {
      if (mc.getNetworkHandler() != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 1, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, to, 0, SlotActionType.PICKUP, mc.player);
      }
   }

   public static void swapOneItem(int from, int to) {
      if (mc.getNetworkHandler() != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, to, 1, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0, SlotActionType.PICKUP, mc.player);
      }
   }

   public static void hotbarSwap(int from, int to) {
      if (mc.getNetworkHandler() != null) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, to, SlotActionType.SWAP, mc.player);
      }
   }

   public static boolean moveToHotbar(ItemSlot fromSlot, int hotbarSlotId) {
      HotbarSlot hotbarSlot = getHotbarSlot(hotbarSlotId);
      moveItem(fromSlot, hotbarSlot);
      return true;
   }

   public static boolean moveToArmor(ItemSlot fromSlot, int armorIndex) {
      ArmorSlot armorSlot = getArmorSlot(armorIndex);
      moveItem(fromSlot, armorSlot);
      return true;
   }

   public static void moveToOffHand(ItemSlot fromSlot) {
      OffhandSlot offHandSlot = getOffHandSlot();
      moveItem(fromSlot, offHandSlot);
   }

   @NotNull
   public static HotbarSlot getCurrentHotbarSlot() {
      return mc.player != null && mc.player.getInventory() != null ? getHotbarSlot(mc.player.getInventory().selectedSlot) : new HotbarSlot(0);
   }

   public static void selectHotbarSlot(int slotId) {
      if (mc.player != null && mc.player.getInventory() != null && mc.getNetworkHandler() != null) {
         if (slotId >= 0 && slotId <= 8) {
            mc.player.getInventory().selectedSlot = slotId;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         } else {
            throw new IllegalArgumentException("Hotbar slot ID must be between 0 and 8");
         }
      }
   }

   public static void selectHotbarSlot(HotbarSlot slot) {
      selectHotbarSlot(slot.getSlotId());
   }

   public static boolean selectItemInHotbar(Item item) {
      HotbarSlot slot = new HotbarSlotsGroup().findItem(item);
      if (slot != null) {
         selectHotbarSlot(slot);
         return true;
      } else {
         return false;
      }
   }

   public static int findItemInContainer(Predicate<ItemStack> predicate) {
      if (mc.player != null && mc.player.currentScreenHandler != null) {
         for (int i = 0; i < mc.player.currentScreenHandler.slots.size(); i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (predicate.test(stack)) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int findItemInContainer(Item item) {
      return findItemInContainer((Predicate<ItemStack>)(stack -> stack.getItem() == item));
   }

   @Generated
   private InventoryUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
