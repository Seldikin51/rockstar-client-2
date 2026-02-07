package moscow.rockstar.utility.inventory.slots;

import lombok.Generated;
import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.item.ItemStack;

public class ArmorSlot extends ItemSlot {
   private final int armorSlotIndex;

   public ArmorSlot(int armorSlotIndex) {
      if (armorSlotIndex >= 0 && armorSlotIndex <= 3) {
         this.armorSlotIndex = armorSlotIndex;
      } else {
         throw new IllegalArgumentException("Armor Slot Index must be between 0 and 3");
      }
   }

   @Override
   public ItemStack itemStack() {
      return mc.player != null && mc.player.getInventory() != null ? mc.player.getInventory().getArmorStack(this.armorSlotIndex) : ItemStack.EMPTY;
   }

   @Override
   public int getIdForServer() {
      return 8 - this.armorSlotIndex;
   }

   @Generated
   public int getArmorSlotIndex() {
      return this.armorSlotIndex;
   }
}
