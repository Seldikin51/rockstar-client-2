package moscow.rockstar.systems.modules.modules.player;

import java.util.Map;
import java.util.TreeMap;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name = "Auto Invisible", category = ModuleCategory.PLAYER, desc = "Автоматически пьет зелье невидимости")
public class AutoInvisible extends BaseModule {
   private final Map<String, StatusEffectInstance> effects = new TreeMap<>();
   private boolean isUsingPotion;
   private final BooleanSetting preDrink = new BooleanSetting(this, "Пить заранее");
   private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
      boolean hasInvisibility = mc.player.hasStatusEffect(StatusEffects.INVISIBILITY);
      StatusEffectInstance effect = hasInvisibility ? mc.player.getStatusEffect(StatusEffects.INVISIBILITY) : null;
      boolean shouldDrink = !hasInvisibility;
      if (this.preDrink.isEnabled() && effect != null && effect.getDuration() <= 200) {
         shouldDrink = true;
      }

      if (shouldDrink) {
         ItemStack offhandItem = mc.player.getOffHandStack();
         boolean hasPotionInOffhand = this.isInvisibilityPotion(offhandItem);
         SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
         ItemSlot potionSlot = slotsToSearch.findItem(this::isInvisibilityPotion);
         OffhandSlot offhandSlot = new OffhandSlot();
         if (potionSlot != null && !hasPotionInOffhand) {
            InventoryUtility.moveItem(potionSlot, offhandSlot);
         }

         if (hasPotionInOffhand) {
            this.isUsingPotion = true;
            mc.options.useKey.setPressed(true);
         }
      } else if (this.isUsingPotion) {
         mc.options.useKey.setPressed(false);
         this.isUsingPotion = false;
         ItemStack offhandItemx = mc.player.getOffHandStack();
         if (offhandItemx.getItem() == Items.GLASS_BOTTLE) {
            mc.interactionManager.clickSlot(0, 45, 1, SlotActionType.THROW, mc.player);
         }
      }
   };

   private boolean isInvisibilityPotion(ItemStack stack) {
      return PotionUtility.hasEffect(stack, StatusEffects.INVISIBILITY);
   }

   @Generated
   public Map<String, StatusEffectInstance> getEffects() {
      return this.effects;
   }
}
