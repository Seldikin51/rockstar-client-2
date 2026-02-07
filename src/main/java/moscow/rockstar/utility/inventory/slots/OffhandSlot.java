package moscow.rockstar.utility.inventory.slots;

import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.item.ItemStack;

public class OffhandSlot extends ItemSlot {
   @Override
   public ItemStack itemStack() {
      return mc.player != null && mc.player.getInventory() != null ? (ItemStack)mc.player.getInventory().offHand.getFirst() : ItemStack.EMPTY;
   }

   @Override
   public int getIdForServer() {
      return 45;
   }
}
