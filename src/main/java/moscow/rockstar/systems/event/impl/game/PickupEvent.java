package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.item.ItemStack;

public class PickupEvent extends Event {
   public ItemStack itemStack;
   public int count;

   public PickupEvent(ItemStack itemStack, int count) {
      this.itemStack = itemStack;
      this.count = count;
   }

   @Generated
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Generated
   public int getCount() {
      return this.count;
   }
}
