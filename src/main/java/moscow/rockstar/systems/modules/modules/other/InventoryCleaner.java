package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name = "Inventory Cleaner", category = ModuleCategory.OTHER, desc = "Очищает инвентарь от определенных блоков")
public class InventoryCleaner extends BaseModule {
   private final Timer timer = new Timer();
   private final List<Item> items = List.of(Items.STONE, Items.COBBLESTONE, Items.GRANITE, Items.IRON_ORE, Items.GOLD_ORE, Items.LAPIS_ORE);
   private final List<ItemSlot> slots = new ArrayList<>();
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (this.isEnabled() && mc.player != null && mc.player.currentScreenHandler != null) {
         if (this.timer.finished(150L)) {
            this.slots.clear();
            SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());

            for (Item item : this.items) {
               ItemSlot itemSlot = slotsToSearch.findItem(item);
               if (itemSlot != null) {
                  this.slots.add(itemSlot);
               }
            }

            if (this.slots.isEmpty()) {
               return;
            }

            ItemSlot slot = this.slots.removeFirst();
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.getIdForServer(), 1, SlotActionType.THROW, mc.player);
            this.timer.reset();
         }
      }
   };
}
