package moscow.rockstar.systems.modules.modules.combat;

import java.util.List;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@ModuleInfo(name = "Auto Soup", category = ModuleCategory.COMBAT)
public class AutoSoup extends BaseModule {
   int prevSlot = -1;
   int slot = -1;
   int soupTick = -1;
   private final SliderSetting health = new SliderSetting(this, "health").step(1.0F).min(1.0F).max(20.0F).currentValue(10.0F);
   private final Timer timer = new Timer();

   @Override
   public void tick() {
      if (this.soupTick >= 0) {
         if (this.soupTick == 2) {
            mc.player.getInventory().selectedSlot = this.slot;
         } else if (this.soupTick == 1) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         } else if (this.soupTick == 0) {
            mc.player.dropSelectedItem(true);
            mc.player.getInventory().selectedSlot = this.prevSlot;
         }

         this.soupTick--;
      } else if (!(mc.player.getHealth() >= this.health.getCurrentValue()) && this.timer.finished(300L)) {
         HotbarSlot hotbarSlot = SlotGroups.hotbar().findItem(Items.MUSHROOM_STEW);
         if (hotbarSlot != null) {
            this.prevSlot = mc.player.getInventory().selectedSlot;
            this.slot = hotbarSlot.getSlotId();
            mc.player.getInventory().selectedSlot = this.slot;
            this.soupTick = 1;
         } else {
            List<InventorySlot> inventorySlots = SlotGroups.inventory().findItems(Items.MUSHROOM_STEW);
            List<HotbarSlot> emptySlots = SlotGroups.hotbar().findItems(ItemStack::isEmpty);
            if (!inventorySlots.isEmpty() && !emptySlots.isEmpty()) {
               int maxSoups = Math.min(inventorySlots.size(), emptySlots.size());
               maxSoups = Math.min(maxSoups, 8);

               for (int i = 0; i < maxSoups; i++) {
                  InventorySlot soupSlot = inventorySlots.get(i);
                  HotbarSlot emptySlot = emptySlots.get(i);
                  InventoryUtility.hotbarSwap(soupSlot.getIdForServer(), emptySlot.getSlotId());
               }

               this.prevSlot = mc.player.getInventory().selectedSlot;
               this.slot = emptySlots.get(0).getSlotId();
               this.soupTick = 2;
            }
         }

         this.timer.reset();
      }
   }
}
