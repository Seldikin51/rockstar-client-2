package moscow.rockstar.utility.inventory.slots;

import lombok.Generated;
import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.item.ItemStack;

public class HotbarSlot extends ItemSlot {
   private final int slotId;

   public HotbarSlot(int slotId) {
      if (slotId >= 0 && slotId <= 8) {
         this.slotId = slotId;
      } else {
         throw new IllegalArgumentException("Hotbar Slot ID must be between 0 and 8");
      }
   }

   @Override
   public ItemStack itemStack() {
      return mc.player != null && mc.player.getInventory() != null ? mc.player.getInventory().getStack(this.slotId) : ItemStack.EMPTY;
   }

   @Override
   public int getIdForServer() {
      return 36 + this.slotId;
   }

   @Generated
   public int getSlotId() {
      return this.slotId;
   }
}
