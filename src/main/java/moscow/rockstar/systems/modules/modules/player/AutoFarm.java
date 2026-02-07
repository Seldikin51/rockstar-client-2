package moscow.rockstar.systems.modules.modules.player;

import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.autofarm.AutoFarmCounter;
import moscow.rockstar.systems.modules.modules.player.autofarm.AutoFarmNukeTurkey;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Auto Farm", category = ModuleCategory.PLAYER)
public class AutoFarm extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.auto_farm.mode");
   private final ModeSetting.Value carrot = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.carrot").select();
   private final ModeSetting.Value potato = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.potato");
   private final ModeSetting.Value beetroot = new ModeSetting.Value(this.mode, "modules.settings.auto_farm.mode.beetroot");
   private final BooleanSetting mineBlocks = new BooleanSetting(this, "modules.settings.auto_farm.mine_blocks");
   private final SelectSetting blocks = new SelectSetting(this, "modules.settings.auto_farm.blocks", () -> !this.mineBlocks.isEnabled());
   private final SelectSetting.Value melon = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.melon");
   private final SelectSetting.Value tikva = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.pumpkin");
   private final SelectSetting.Value allCrops = new SelectSetting.Value(this.blocks, "modules.settings.auto_farm.blocks.other_crops");
   private final BooleanSetting autoExp = new BooleanSetting(this, "modules.settings.auto_farm.auto_exp").enabled(true);
   private final BooleanSetting autoSell = new BooleanSetting(this, "modules.settings.auto_farm.auto_sell").enabled(true);
   private final AutoFarmNukeTurkey nukeTurkey = new AutoFarmNukeTurkey();
   private final AutoFarmCounter count = new AutoFarmCounter();
   private final Timer timer = new Timer();
   private boolean repairing;
   private boolean cursorCheck;
   private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
      SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
      ItemSlot exp = search.findItem(Items.EXPERIENCE_BOTTLE);
      List<Item> hoes = List.of(Items.NETHERITE_HOE, Items.DIAMOND_HOE);
      List<Item> items = List.of(Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS);
      ItemStack mainHand = mc.player.getMainHandStack();
      ItemStack offHand = mc.player.getOffHandStack();
      BlockState farmState = mc.world.getBlockState(mc.player.getBlockPos());
      BlockState cropState = mc.world.getBlockState(mc.player.getBlockPos().up());
      BlockPos cropPos = mc.player.getBlockPos().up();
      BlockHitResult blockHitResult = new BlockHitResult(
         new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()), Direction.UP, mc.player.getBlockPos(), false
      );
      BlockHitResult cropHitResult = new BlockHitResult(new Vec3d(cropPos.getX(), cropPos.getY(), cropPos.getZ()), Direction.UP, cropPos, false);
      if (!this.mineBlocks.isEnabled() && !this.melon.isSelected() && !this.tikva.isSelected() && !this.allCrops.isSelected()) {
         if (this.autoExp.isEnabled()) {
            int max = mainHand.getMaxDamage();
            int cur = max - mainHand.getDamage();
            double percent = (double)cur / max;
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
               this.cursorCheck = true;
               if (this.timer.finished(150L)) {
                  int emptySlot = mc.player.getInventory().getEmptySlot();
                  if (emptySlot != -1) {
                     mc.interactionManager.clickSlot(0, emptySlot < 9 ? emptySlot + 36 : emptySlot, 0, SlotActionType.PICKUP, mc.player);
                  }
               }

               return;
            }

            if (this.cursorCheck) {
               this.cursorCheck = false;
               this.timer.reset();
            }

            if (this.repairing) {
               if (exp != null && offHand.getItem() != exp.item() && this.timer.finished(150L)) {
                  InventoryUtility.moveItem(exp.getIdForServer(), 45, false);
                  this.timer.reset();
               } else if (exp == null && !items.contains(offHand.getItem())) {
                  InventoryUtility.moveItem(this.findItem(), 45, false);
               } else if (exp != null && offHand.getItem() == exp.item() && this.timer.finished(150L)) {
                  mc.interactionManager
                     .sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, mc.player.getYaw(), 90.0F));
                  this.timer.reset();
                  if (percent > 0.6) {
                     this.repairing = false;
                     if (this.findItem() != -1) {
                        InventoryUtility.moveItem(this.findItem(), 45, false);
                     }
                  }
               }
            }

            if (hoes.contains(mainHand.getItem()) && percent < 0.5 && !this.repairing && exp != null) {
               this.repairing = true;
            }
         }

         if (this.autoSell.isEnabled() && mc.player.getInventory().getEmptySlot() == -1) {
            if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
               if (ServerUtility.isFT()
                  ? mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.select_section_ft"))
                  : mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.select_section"))) {
                  mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 21, 0, SlotActionType.PICKUP, mc.player);
               }

               if (mc.currentScreen.getTitle().getString().equals(Localizator.translate("modules.auto_farm.screen.food_seller"))) {
                  mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, this.ssItem(), 0, SlotActionType.PICKUP, mc.player);
                  if (this.timer.finished(500L)) {
                     mc.player.closeHandledScreen();
                     this.timer.reset();
                  }
               }
            } else if (this.timer.finished(300L)) {
               mc.player.networkHandler.sendChatCommand("buyer");
               this.timer.reset();
            }
         } else if (farmState.getBlock().equals(Blocks.FARMLAND) && !this.repairing) {
            Rockstar.getInstance()
               .getRotationHandler()
               .rotate(new Rotation(mc.player.getYaw(), 90.0F), MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.USE_ITEM);
            if (cropState.getBlock() instanceof CropBlock crop) {
               if (crop.getAge(cropState) == 7) {
                  mc.player.swingHand(Hand.MAIN_HAND);
                  mc.interactionManager.attackBlock(mc.player.getBlockPos().up(), Direction.UP);
                  this.timer.reset();
               } else if (hoes.contains(mainHand.getItem())) {
                  mc.player.swingHand(Hand.MAIN_HAND);
                  mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, cropHitResult);
               }
            } else if (cropState.isAir() && items.contains(offHand.getItem())) {
               mc.player.swingHand(Hand.OFF_HAND);
               mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, blockHitResult);
            }
         }
      } else {
         this.nukeTurkey.nuke();
      }
   };

   public int findItem() {
      SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
      ItemSlot foundSlot = search.findItem(
         (Predicate<ItemStack>)(stack -> this.carrot.isSelected() && stack.getItem() == Items.CARROT
            || this.potato.isSelected() && stack.getItem() == Items.POTATO
            || this.beetroot.isSelected() && stack.getItem() == Items.BEETROOT_SEEDS)
      );
      return foundSlot != null ? foundSlot.getIdForServer() : -1;
   }

   public int ssItem() {
      if (this.carrot.isSelected()) {
         return 10;
      } else if (this.potato.isSelected()) {
         return 11;
      } else {
         return this.beetroot.isSelected() ? 12 : -1;
      }
   }

   @Override
   public void onDisable() {
      this.repairing = false;
      this.count.price = 0;
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public ModeSetting.Value getCarrot() {
      return this.carrot;
   }

   @Generated
   public ModeSetting.Value getPotato() {
      return this.potato;
   }

   @Generated
   public ModeSetting.Value getBeetroot() {
      return this.beetroot;
   }

   @Generated
   public BooleanSetting getMineBlocks() {
      return this.mineBlocks;
   }

   @Generated
   public SelectSetting getBlocks() {
      return this.blocks;
   }

   @Generated
   public SelectSetting.Value getMelon() {
      return this.melon;
   }

   @Generated
   public SelectSetting.Value getTikva() {
      return this.tikva;
   }

   @Generated
   public SelectSetting.Value getAllCrops() {
      return this.allCrops;
   }

   @Generated
   public BooleanSetting getAutoExp() {
      return this.autoExp;
   }

   @Generated
   public BooleanSetting getAutoSell() {
      return this.autoSell;
   }

   @Generated
   public AutoFarmNukeTurkey getNukeTurkey() {
      return this.nukeTurkey;
   }

   @Generated
   public AutoFarmCounter getCount() {
      return this.count;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public boolean isRepairing() {
      return this.repairing;
   }

   @Generated
   public boolean isCursorCheck() {
      return this.cursorCheck;
   }

   @Generated
   public EventListener<ClientPlayerTickEvent> getOnClientPlayerTickEvent() {
      return this.onClientPlayerTickEvent;
   }
}
