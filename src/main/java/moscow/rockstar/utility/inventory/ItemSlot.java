package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public abstract class ItemSlot implements IMinecraft {
   public abstract ItemStack itemStack();

   public abstract int getIdForServer();

   public int syncId() {
      return mc.player != null && mc.player.currentScreenHandler != null ? mc.player.currentScreenHandler.syncId : 0;
   }

   public Item item() {
      return this.itemStack().getItem();
   }

   public boolean isEmpty() {
      return this.itemStack().isEmpty();
   }

   public boolean contains(Item item) {
      return this.itemStack().getItem() == item;
   }

   public boolean matches(Predicate<ItemStack> predicate) {
      return predicate.test(this.itemStack());
   }

   public void swapTo(ItemSlot newSlot) {
      InventoryUtility.moveItem(this, newSlot);
   }

   public void moveToOffHand() {
      InventoryUtility.moveToOffHand(this);
   }

   public void click() {
      if (mc.interactionManager != null) {
         mc.interactionManager.clickSlot(this.syncId(), this.getIdForServer(), 0, SlotActionType.PICKUP, mc.player);
      }
   }
}
