package moscow.rockstar.utility.inventory.group;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SlotGroup<T extends ItemSlot> {
   protected final List<T> slots;

   public SlotGroup(List<T> slots) {
      this.slots = slots;
   }

   @Nullable
   public T findItem(Item item) {
      return this.slots.stream().filter(slot -> slot.contains(item)).findFirst().orElse(null);
   }

   @Nullable
   public T findItem(Predicate<ItemStack> predicate) {
      return this.slots.stream().filter(slot -> slot.matches(predicate)).findFirst().orElse(null);
   }

   public List<@Nullable T> findItems(Item item) {
      return this.slots.stream().filter(slot -> slot.contains(item)).toList();
   }

   public List<@Nullable T> findItems(Predicate<ItemStack> predicate) {
      return this.slots.stream().filter(slot -> slot.matches(predicate)).toList();
   }

   @Nullable
   public T findEmptySlot() {
      return this.slots.stream().filter(ItemSlot::isEmpty).findFirst().orElse(null);
   }

   public boolean hasItem(Item item) {
      return this.slots.stream().anyMatch(slot -> slot.contains(item));
   }

   public int countItems(Item item) {
      return this.slots.stream().filter(slot -> slot.contains(item)).mapToInt(slot -> slot.itemStack().getCount()).sum();
   }

   public SlotGroup<ItemSlot> and(SlotGroup<? extends ItemSlot> other) {
      List<ItemSlot> combined = new ArrayList<>(this.slots.size() + other.slots.size());
      combined.addAll(this.slots);
      combined.addAll(other.slots);
      return new SlotGroup<>(combined);
   }

   public SlotGroup<ItemSlot> and(ItemSlot slot) {
      List<ItemSlot> combined = new ArrayList<>(this.slots);
      combined.add(slot);
      return new SlotGroup<>(combined);
   }

   @Generated
   public List<T> getSlots() {
      return this.slots;
   }
}
