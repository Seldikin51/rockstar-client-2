package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.mixin.accessors.HandledScreenAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.event.impl.window.ContainerClickEvent;
import moscow.rockstar.systems.event.impl.window.ContainerReleaseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SelectSettingComponent;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

@ModuleInfo(name = "Auction", category = ModuleCategory.OTHER)
public class Auction extends BaseModule {
   private final List<Auction.AuctionItem> pageItems = new ArrayList<>();
   private double averageEffectivePrice = 0.0;
   private double minEffectivePrice = Double.MAX_VALUE;
   private String title = "";
   private final SelectSetting armor = new SelectSetting(
      this,
      "modules.settings.auction.armor",
      () -> this.title.toLowerCase().contains("кирка") || this.title.toLowerCase().contains("силы") || this.title.toLowerCase().contains("скорости")
   );
   private final SelectSetting.Value noSpike = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_spike").select();
   private final SelectSetting.Value noProt5 = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_prot5").select();
   private final SelectSetting.Value noDurability = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_durability");
   private final SelectSetting.Value noRepair = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_repair");
   private final SelectSetting pickaxe = new SelectSetting(
      this,
      "modules.settings.auction.pickaxe",
      () -> this.title.toLowerCase().contains("шлем")
         || this.title.toLowerCase().contains("нагрудник")
         || this.title.toLowerCase().contains("поножи")
         || this.title.toLowerCase().contains("ботинки")
         || this.title.toLowerCase().contains("броня")
         || this.title.toLowerCase().contains("силы")
         || this.title.toLowerCase().contains("скорости")
   );
   private final SelectSetting.Value noSilkTouch = new SelectSetting.Value(this.pickaxe, "modules.settings.auction.pickaxe.silk_touch");
   private final SelectSetting potions = new SelectSetting(
      this,
      "modules.settings.auction.potions",
      () -> this.title.toLowerCase().contains("шлем")
         || this.title.toLowerCase().contains("нагрудник")
         || this.title.toLowerCase().contains("поножи")
         || this.title.toLowerCase().contains("ботинки")
         || this.title.toLowerCase().contains("броня")
         || this.title.toLowerCase().contains("кирка")
   );
   private final SelectSetting.Value noLevel3 = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_level3");
   private final SelectSetting.Value noCombined = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_combined");
   private final Popup popup = new Popup(0.0F, 0.0F);
   private final EventListener<HudRenderEvent> onHud = event -> {
      if (mc.currentScreen == null) {
         this.title = "";
         this.popup.setShowing(false);
         if (this.popup.getAnimation().getValue() > 0.0F) {
            this.drawPopup(event.getContext());
         }
      }
   };
   private final EventListener<ScreenRenderEvent> onScreen = event -> {
      if (!this.pageItems.isEmpty() && mc.currentScreen instanceof HandledScreen screen) {
         if (this.isAuction(screen.getTitle().getString())) {
            this.popup.setShowing(true);
            HandledScreenAccessor accessor = (HandledScreenAccessor)screen;

            try {
               for (Auction.AuctionItem item : this.pageItems) {
                  if (!(item.effectivePrice > this.averageEffectivePrice)) {
                     Slot slotToHighlight = screen.getScreenHandler().getSlot(item.slotId);
                     if (slotToHighlight != null) {
                        int x = accessor.getX() + slotToHighlight.x;
                        int y = accessor.getY() + slotToHighlight.y;
                        ColorRGBA color = this.calculateHighlightColor(item.effectivePrice);
                        event.getContext()
                           .drawRoundedRect(
                              (float)x,
                              (float)y,
                              16.0F,
                              16.0F,
                              BorderRadius.all(1.0F),
                              new VerticalGradient(color.withAlpha(0.0F), color.withAlpha(0.8F * color.getAlpha()))
                           );
                     }
                  }
               }
            } catch (Exception var10) {
               this.reset();
            }

            this.drawPopup(event.getContext());
         }
      }
   };
   private final EventListener<ContainerClickEvent> onClick = event -> this.popup
      .onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
   private final EventListener<ContainerReleaseEvent> onRelease = event -> this.popup
      .onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));

   public Auction() {
      for (Setting setting : this.getSettings()) {
         if (setting instanceof SelectSetting selectSetting) {
            this.popup.add(new SelectSettingComponent(selectSetting, this.popup));
         }
      }
   }

   @Override
   public void tick() {
      if (mc.currentScreen instanceof HandledScreen<?> screen) {
         String var3 = screen.getTitle().getString();
         this.title = var3;
         if (!this.isAuction(var3)) {
            this.reset();
         } else {
            this.scanAndAnalyzePage(screen);
            super.tick();
         }
      } else {
         this.reset();
      }
   }

   private void drawPopup(CustomDrawContext orig) {
      UIContext context = UIContext.of(
         orig,
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(),
         MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)
      );
      this.popup.setWidth(120.0F);
      this.popup.pos(10.0F, sr.getScaledHeight() / 2.0F - this.popup.getHeight() / 2.0F);
      this.popup.render(context);
   }

   private ColorRGBA calculateHighlightColor(double effectivePrice) {
      double range = this.averageEffectivePrice - this.minEffectivePrice;
      double factor = range > 0.0 ? (effectivePrice - this.minEffectivePrice) / range : 0.0;
      factor = Math.max(0.0, Math.min(1.0, factor));
      int red = (int)(60.0 + 195.0 * factor);
      return new ColorRGBA(factor < 0.001F ? red : 255.0F, 255.0F, 60.0F, (float)(250.0 * (1.0 - factor)));
   }

   private boolean isAuction(String title) {
      return title.toLowerCase().contains("аукцион") || title.toLowerCase().contains("поиск") || title.toLowerCase().contains("биржа");
   }

   @Override
   public void onDisable() {
      this.reset();
      super.onDisable();
   }

   private boolean shouldHideItem(ItemStack stack, List<Text> tooltip) {
      Item item = stack.getItem();
      if (item instanceof ArmorItem) {
         if (this.noSpike.isSelected() && EnchantmentUtility.hasEnchantments(stack, Enchantments.THORNS)) {
            return true;
         }

         if (this.noProt5.isSelected() && EnchantmentUtility.getEnchantmentLevel(stack, Enchantments.PROTECTION) < 5) {
            return true;
         }

         if (this.noDurability.isSelected() && stack.getMaxDamage() > 0 && stack.isDamaged()) {
            return true;
         }

         if (this.noRepair.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.MENDING)) {
            return true;
         }
      }

      if (item instanceof PickaxeItem && this.noSilkTouch.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.SILK_TOUCH)) {
         return true;
      } else {
         if (item instanceof PotionItem) {
            List<String> tooltipStrings = tooltip.stream().map(text -> text.getString().toLowerCase()).toList();
            if (this.noLevel3.isSelected() && !this.hasLevel3Potion(tooltipStrings)) {
               return true;
            }

            if (this.noCombined.isSelected() && !this.isCombinedPotion(tooltipStrings)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean hasLevel3Potion(List<String> tooltip) {
      for (String line : tooltip) {
         if ((line.contains("сила") || line.contains("скорость")) && (line.contains("iii") || line.contains("3") || line.contains("усиленн"))) {
            return true;
         }
      }

      return false;
   }

   private boolean isCombinedPotion(List<String> tooltip) {
      boolean hasStrength = tooltip.stream().anyMatch(line -> line.contains("сила"));
      boolean hasSpeed = tooltip.stream().anyMatch(line -> line.contains("скорость"));
      return hasStrength && hasSpeed;
   }

   private void scanAndAnalyzePage(HandledScreen<?> screen) {
      this.pageItems.clear();
      this.minEffectivePrice = Double.MAX_VALUE;
      double totalEffectivePrice = 0.0;
      int pricedItemCount = 0;
      int containerSize = screen.getScreenHandler().slots.size() - 36;

      for (int i = 0; i < containerSize; i++) {
         Slot slot = screen.getScreenHandler().getSlot(i);
         if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            List<Text> tooltip = stack.getTooltip(
               TooltipContext.create(mc.world), mc.player, mc.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC
            );
            if (!this.shouldHideItem(stack, tooltip)) {
               long totalPrice = -1L;

               for (Text lineText : tooltip) {
                  String line = lineText.getString();
                  if (line.contains("Цена") || line.contains("Цeна") || line.contains("Ценa") || line.contains("Цeнa") || line.contains("Курс")) {
                     try {
                        String priceString = line.replaceAll("[^\\d]", "");
                        if (!priceString.isEmpty()) {
                           totalPrice = Long.parseLong(priceString);
                        }
                     } catch (NumberFormatException var21) {
                     }
                     break;
                  }
               }

               if (totalPrice != -1L) {
                  int count = stack.getCount();
                  int maxDurability = stack.getMaxDamage();
                  int currentDurability = maxDurability - stack.getDamage();
                  double pricePerUnit = (double)totalPrice / count;
                  double durabilityFactor = 1.0;
                  if (maxDurability > 0) {
                     durabilityFactor = (double)currentDurability / maxDurability;
                     durabilityFactor = Math.max(0.1, durabilityFactor);
                  }

                  double effectivePrice = pricePerUnit / durabilityFactor;
                  this.pageItems.add(new Auction.AuctionItem(slot.id, totalPrice, count, maxDurability, currentDurability, effectivePrice));
                  totalEffectivePrice += effectivePrice;
                  pricedItemCount++;
                  if (effectivePrice < this.minEffectivePrice) {
                     this.minEffectivePrice = effectivePrice;
                  }
               }
            }
         }
      }

      if (pricedItemCount > 0) {
         this.averageEffectivePrice = totalEffectivePrice / pricedItemCount;
      } else {
         this.reset();
      }
   }

   private void reset() {
      this.pageItems.clear();
      this.averageEffectivePrice = 0.0;
      this.minEffectivePrice = Double.MAX_VALUE;
   }

   private record AuctionItem(int slotId, long totalPrice, int count, int maxDurability, int currentDurability, double effectivePrice) {
   }
}
