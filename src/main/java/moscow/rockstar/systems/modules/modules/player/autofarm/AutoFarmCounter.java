package moscow.rockstar.systems.modules.modules.player.autofarm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.AutoFarm;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class AutoFarmCounter implements IMinecraft {
   private final Timer timer = new Timer();
   public int price = 0;

   public int getTotalSelectedCrops() {
      AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
      int totalCount = 0;

      for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
         ItemStack stack = (ItemStack)mc.player.getInventory().main.get(i);
         if (!stack.isEmpty()) {
            if (autoFarm.getCarrot().isSelected() && stack.getItem() == Items.CARROT) {
               totalCount += stack.getCount();
            } else if (autoFarm.getPotato().isSelected() && stack.getItem() == Items.POTATO) {
               totalCount += stack.getCount();
            } else if (autoFarm.getBeetroot().isSelected() && stack.getItem() == Items.BEETROOT_SEEDS) {
               totalCount += stack.getCount();
            }
         }
      }

      ItemStack offHandStack = mc.player.getOffHandStack();
      if (!offHandStack.isEmpty()) {
         if (autoFarm.getCarrot().isSelected() && offHandStack.getItem() == Items.CARROT) {
            totalCount += offHandStack.getCount();
         } else if (autoFarm.getPotato().isSelected() && offHandStack.getItem() == Items.POTATO) {
            totalCount += offHandStack.getCount();
         } else if (autoFarm.getBeetroot().isSelected() && offHandStack.getItem() == Items.BEETROOT_SEEDS) {
            totalCount += offHandStack.getCount();
         }
      }

      return totalCount;
   }

   public int getNonSelectedCropSlots() {
      AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
      int nonSelectedSlots = 0;

      for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
         ItemStack stack = (ItemStack)mc.player.getInventory().main.get(i);
         if (!stack.isEmpty()) {
            boolean isSelectedCrop = autoFarm.getCarrot().isSelected() && stack.getItem() == Items.CARROT
               || autoFarm.getPotato().isSelected() && stack.getItem() == Items.POTATO
               || autoFarm.getBeetroot().isSelected() && stack.getItem() == Items.BEETROOT_SEEDS;
            if (!isSelectedCrop) {
               nonSelectedSlots++;
            }
         }
      }

      ItemStack offHandStack = mc.player.getOffHandStack();
      if (!offHandStack.isEmpty()) {
         boolean isSelectedCrop = autoFarm.getCarrot().isSelected() && offHandStack.getItem() == Items.CARROT
            || autoFarm.getPotato().isSelected() && offHandStack.getItem() == Items.POTATO
            || autoFarm.getBeetroot().isSelected() && offHandStack.getItem() == Items.BEETROOT_SEEDS;
         if (!isSelectedCrop) {
            nonSelectedSlots++;
         }
      }

      return nonSelectedSlots;
   }

   public void checkPrice() {
      AutoFarm autoFarm = Rockstar.getInstance().getModuleManager().getModule(AutoFarm.class);
      List<Item> items = List.of(Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS);
      if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && this.price == 0) {
         if (ServerUtility.isFT()
            ? mc.currentScreen.getTitle().getString().equals("● Выберите секцию")
            : mc.currentScreen.getTitle().getString().equals("● Выбери секцию")) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 21, 0, SlotActionType.PICKUP, mc.player);
         }

         if (mc.currentScreen.getTitle().getString().equals("Скупщик еды") && this.timer.finished(50L)) {
            ItemStack itemStack = mc.player.currentScreenHandler.getSlot(autoFarm.ssItem()).getStack();

            for (Text line : itemStack.getTooltip(TooltipContext.DEFAULT, mc.player, TooltipType.BASIC)) {
               String text = line.getString();
               if (text.contains("Цена за") && text.contains("$")) {
                  Pattern pattern = Pattern.compile("(\\d+)\\$");
                  Matcher matcher = pattern.matcher(text);
                  if (matcher.find()) {
                     this.price = Integer.parseInt(matcher.group(1));
                  }
               }
            }

            mc.player.closeHandledScreen();
            this.timer.reset();
         }
      } else if (this.timer.finished(300L) && this.price == 0) {
         mc.player.networkHandler.sendChatCommand("buyer");
         this.timer.reset();
      }
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public int getPrice() {
      return this.price;
   }
}
