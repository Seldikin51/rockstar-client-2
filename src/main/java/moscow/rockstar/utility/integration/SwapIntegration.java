package moscow.rockstar.utility.integration;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.modules.player.GuiMove;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class SwapIntegration implements IMinecraft {
   private Item itemToUse = null;
   private HotbarSlot originalSlot = null;
   private boolean isProcessingItem = false;
   private ItemSlot targetSlot = null;
   private final Timer itemUseTimer = new Timer();
   private SwapIntegration.ItemUseState currentState = SwapIntegration.ItemUseState.IDLE;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.isProcessingItem) {
         this.processItemUse();
      }
   };

   public SwapIntegration() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   private void processItemUse() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.player.getItemCooldownManager() != null) {
         if (!(this.targetSlot instanceof HotbarSlot)) {
            Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).setStay(true);
         }

         switch (this.currentState) {
            case USING_ITEM:
               if (this.targetSlot instanceof HotbarSlot) {
                  mc.interactionManager
                     .sendSequencedPacket(
                        mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch())
                     );
                  this.currentState = SwapIntegration.ItemUseState.RETURNING_SLOT;
               } else if (Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).canSend()) {
                  mc.interactionManager
                     .sendSequencedPacket(
                        mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch())
                     );
                  this.currentState = SwapIntegration.ItemUseState.RETURNING_SLOT;
               }
               break;
            case RETURNING_SLOT:
               if (this.targetSlot instanceof HotbarSlot) {
                  InventoryUtility.selectHotbarSlot(this.originalSlot);
                  this.resetUseState();
               } else if (Rockstar.getInstance().getModuleManager().getModule(GuiMove.class).canSend()) {
                  HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
                  InventoryUtility.hotbarSwap(this.targetSlot.getIdForServer(), this.originalSlot.getSlotId());
                  this.resetUseState();
               }
               break;
            default:
               this.isProcessingItem = false;
               this.currentState = SwapIntegration.ItemUseState.IDLE;
         }
      } else {
         this.isProcessingItem = false;
         this.currentState = SwapIntegration.ItemUseState.IDLE;
      }
   }

   public void useItem(Item itemType) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.currentScreen == null) {
         if (!this.isProcessingItem) {
            SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory());
            ItemSlot itemSlot = group.findItem(itemType);
            if (itemSlot == null) {
               Rockstar.getInstance()
                  .getNotificationManager()
                  .addNotificationOther(NotificationType.ERROR, "Предмет не найден", "Вам необходимо иметь " + itemType.getName().getString() + " в инвентаре");
            } else if (!mc.player.getItemCooldownManager().isCoolingDown(itemSlot.itemStack())) {
               this.itemToUse = itemType;
               this.originalSlot = InventoryUtility.getCurrentHotbarSlot();
               this.targetSlot = itemSlot;
               this.isProcessingItem = true;
               this.currentState = SwapIntegration.ItemUseState.USING_ITEM;
               this.itemUseTimer.reset();
               if (itemSlot instanceof HotbarSlot itemHotbarSlot) {
                  if (InventoryUtility.getCurrentHotbarSlot().item() != itemType) {
                     InventoryUtility.selectHotbarSlot(itemHotbarSlot);
                  }
               } else if (itemSlot instanceof InventorySlot itemInventorySlot) {
                  HotbarSlot currentSlot = InventoryUtility.getCurrentHotbarSlot();
                  InventoryUtility.hotbarSwap(itemInventorySlot.getIdForServer(), currentSlot.getSlotId());
               }
            }
         }
      }
   }

   private void resetUseState() {
      this.isProcessingItem = false;
      this.currentState = SwapIntegration.ItemUseState.IDLE;
      this.itemToUse = null;
      this.originalSlot = null;
      this.targetSlot = null;
   }

   private static enum ItemUseState {
      IDLE,
      USING_ITEM,
      RETURNING_SLOT;
   }
}
