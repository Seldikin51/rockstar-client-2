package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Auto Brew", category = ModuleCategory.PLAYER, desc = "Автоматически варит зелья")
public class AutoBrew extends BaseModule {
   private final ModeSetting potions = new ModeSetting(this, "Варить");
   private final ModeSetting.Value strength = new ModeSetting.Value(this.potions, "Зелье силы").select();
   private final ModeSetting.Value speed = new ModeSetting.Value(this.potions, "Зелье скорости");
   private final ModeSetting.Value fire = new ModeSetting.Value(this.potions, "Зелье огнестойкости");
   private final SliderSetting delay = new SliderSetting(this, "Задержка", "Задержка на перемещение ингредиентов")
      .step(10.0F)
      .min(100.0F)
      .max(1000.0F)
      .currentValue(100.0F);
   private final Timer timer = new Timer();
   private AutoBrew.State state = AutoBrew.State.IDLE;
   private final Timer actionTimer = new Timer();
   private BrewingStandBlockEntity currentBrewer;
   private ChestBlockEntity currentChest;
   private final List<BlockPos> processedBrewers = new ArrayList<>();
   private List<BrewingStandBlockEntity> brewersQueue = new ArrayList<>();
   private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
      switch (this.state) {
         case IDLE:
            this.handleIdleState();
            break;
         case OPENING_BREWER:
            this.handleOpeningState();
            break;
         case PROCESSING:
            this.handleProcessingState();
            break;
         case DEPOSITING:
            this.handleDepositingState();
            break;
         case CLOSING:
            this.handleClosingState();
      }
   };

   private void handleOpeningState() {
      if (mc.currentScreen instanceof BrewingStandScreen) {
         this.state = AutoBrew.State.PROCESSING;
      } else {
         if (this.actionTimer.finished(500L)) {
            BlockPos pos = this.currentBrewer.getPos();
            Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            BlockHitResult hit = new BlockHitResult(vec, Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            this.actionTimer.reset();
         }
      }
   }

   private void handleProcessingState() {
      if (!(mc.player.currentScreenHandler instanceof BrewingStandScreenHandler brew)) {
         this.state = AutoBrew.State.IDLE;
      } else if (brew.getFuel() <= 0 || brew.getSlot(3).getStack().getItem() == Items.AIR) {
         if (brew.getSlot(4).getStack().getItem() == Items.AIR && brew.getFuel() == 0) {
            if (this.findIngredient(Items.BLAZE_POWDER) == -1) {
               return;
            }

            this.swapOneItem(Items.BLAZE_POWDER, 4);
         }

         for (int i = 0; i < 3; i++) {
            if (brew.getSlot(i).getStack().getItem() == Items.AIR) {
               if (this.findWaterBottle(brew) == -1) {
                  return;
               }

               InventoryUtility.quickMove(this.findWaterBottle(brew));
            }
         }

         if (brew.getSlot(3).getStack().getItem() == Items.AIR) {
            if (this.isPotionType(brew, (Potion)Potions.WATER.value())) {
               if (this.findIngredient(Items.NETHER_WART) == -1) {
                  Rockstar.getInstance()
                     .getNotificationManager()
                     .addNotificationOther(
                        NotificationType.ERROR, "Предмет не найден", "Вам необходимо иметь " + Items.NETHER_WART.getName().getString() + " в инвентаре"
                     );
               }

               this.handleIngredient(Items.NETHER_WART, 3);
            }

            if (this.strength.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
               this.handleIngredient(Items.BLAZE_POWDER, 3);
            } else if (this.speed.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
               this.handleIngredient(Items.SUGAR, 3);
            } else if (this.fire.isSelected() && this.isPotionType(brew, (Potion)Potions.AWKWARD.value())) {
               this.handleIngredient(Items.MAGMA_CREAM, 3);
            }

            if (this.isPotionType(brew, (Potion)Potions.STRENGTH.value()) || this.isPotionType(brew, (Potion)Potions.SWIFTNESS.value())) {
               this.handleIngredient(Items.GLOWSTONE_DUST, 3);
            }

            if (this.isPotionType(brew, (Potion)Potions.FIRE_RESISTANCE.value())) {
               this.handleIngredient(Items.REDSTONE, 3);
            }

            if (this.isPotionType(brew, (Potion)Potions.STRONG_STRENGTH.value())
               || this.isPotionType(brew, (Potion)Potions.STRONG_SWIFTNESS.value())
               || this.isPotionType(brew, (Potion)Potions.LONG_FIRE_RESISTANCE.value())) {
               this.lootPotions(brew);
               this.state = AutoBrew.State.DEPOSITING;
            }
         }
      }
   }

   private void handleIdleState() {
      if (this.actionTimer.finished(1000L)) {
         if (this.brewersQueue.isEmpty()) {
            this.brewersQueue = this.findBrewers();
         }

         if (!this.brewersQueue.isEmpty()) {
            this.currentBrewer = this.brewersQueue.removeFirst();
            this.state = AutoBrew.State.OPENING_BREWER;
            this.actionTimer.reset();
         }
      }
   }

   private void handleIngredient(Item item, int slot) {
      if (this.findIngredient(item) == -1) {
         Rockstar.getInstance()
            .getNotificationManager()
            .addNotificationOther(NotificationType.ERROR, "Предмет не найден", "Вам необходимо иметь " + item.getName().getString() + " в инвентаре");
         this.toggle();
      } else {
         this.swapOneItem(item, slot);
         mc.player.closeHandledScreen();
      }
   }

   private void handleDepositingState() {
      if (this.actionTimer.finished(500L)) {
         List<ChestBlockEntity> chests = this.findChests();
         if (!chests.isEmpty()) {
            this.currentChest = chests.getFirst();
            this.depositPotions();
         }

         this.state = AutoBrew.State.CLOSING;
         this.actionTimer.reset();
      }
   }

   private void handleClosingState() {
      if (this.actionTimer.finished(500L)) {
         mc.player.closeHandledScreen();
         if (this.currentBrewer != null) {
            this.processedBrewers.add(this.currentBrewer.getPos());
         }

         this.state = AutoBrew.State.IDLE;
         this.currentBrewer = null;
         this.currentChest = null;
         this.actionTimer.reset();
      }
   }

   private List<BrewingStandBlockEntity> findBrewers() {
      List<BrewingStandBlockEntity> brewers = new ArrayList<>();
      int range = 10;
      BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               BlockPos pos = playerPos.add(x, y, z);
               if (mc.world.getBlockEntity(pos) instanceof BrewingStandBlockEntity brewer) {
                  brewers.add(brewer);
               }
            }
         }
      }

      return brewers;
   }

   private List<ChestBlockEntity> findChests() {
      List<ChestBlockEntity> chests = new ArrayList<>();
      int range = 10;
      BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               BlockPos pos = playerPos.add(x, y, z);
               if (mc.world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                  chests.add(chest);
               }
            }
         }
      }

      chests.sort(Comparator.comparingDouble(c -> c.getPos().getSquaredDistance(playerPos)));
      return chests;
   }

   private void depositPotions() {
      if (this.currentChest != null) {
         for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (this.isPotion(stack)) {
               int chestSlot = this.findChestSlot(this.currentChest);
               if (chestSlot != -1) {
                  mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i < 9 ? i + 36 : i, chestSlot, SlotActionType.QUICK_MOVE, mc.player);
               }
            }
         }
      }
   }

   private boolean isPotion(ItemStack stack) {
      return stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION;
   }

   private int findChestSlot(ChestBlockEntity chest) {
      for (int i = 0; i < chest.size(); i++) {
         if (chest.getStack(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   private void lootPotions(BrewingStandScreenHandler brew) {
      for (int i = 0; i < 3; i++) {
         if (!brew.getSlot(i).getStack().isEmpty()) {
            InventoryUtility.quickMove(i);
         }
      }
   }

   private void swapOneItem(Item item, int to) {
      int slot;
      if (this.timer.finished((long)(this.delay.getCurrentValue() * 2.0F)) && (slot = this.findIngredient(item)) != -1) {
         InventoryUtility.swapOneItem(slot, to);
         this.timer.reset();
      }
   }

   private int findIngredient(Item item) {
      for (int i = 5; i < 41; i++) {
         if (((Slot)mc.player.currentScreenHandler.slots.get(i)).getStack().getItem() == item) {
            return i;
         }
      }

      return -1;
   }

   private boolean isPotionType(BrewingStandScreenHandler brew, Potion potion) {
      boolean needIng = true;

      for (int i = 0; i < 3; i++) {
         ItemStack stack = ((Slot)brew.slots.get(i)).getStack();
         if (stack.getItem() == Items.POTION
            && ((RegistryEntry)((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().get()).value() != potion) {
            needIng = false;
         }
      }

      return needIng;
   }

   private int findWaterBottle(BrewingStandScreenHandler brew) {
      for (int i = 5; i < 41; i++) {
         ItemStack stack = ((Slot)brew.slots.get(i)).getStack();
         if (stack.getItem() == Items.POTION
            && ((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().isPresent()
            && ((RegistryEntry)((PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS)).potion().get()).value() == Potions.WATER.value()) {
            return i;
         }
      }

      return -1;
   }

   private static enum State {
      IDLE,
      OPENING_BREWER,
      PROCESSING,
      DEPOSITING,
      CLOSING;
   }
}
