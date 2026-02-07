package moscow.rockstar.utility.game;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.entry.RegistryEntry;

public final class PotionUtility {
   public static boolean hasEffect(ItemStack stack, RegistryEntry<StatusEffect> effectType) {
      if (stack != null && !stack.isEmpty()) {
         if (!(stack.getItem() instanceof PotionItem)) {
            return false;
         } else {
            PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) {
               return false;
            } else {
               for (StatusEffectInstance effect : potionContents.getEffects()) {
                  if (effect.getEffectType() == effectType) {
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static List<StatusEffectInstance> effects(ItemStack stack) {
      List<StatusEffectInstance> effects = new ArrayList<>();
      if (stack == null || stack.isEmpty()) {
         return effects;
      } else if (!(stack.getItem() instanceof PotionItem)) {
         return effects;
      } else {
         PotionContentsComponent potionContents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
         if (potionContents == null) {
            return effects;
         } else {
            potionContents.getEffects().forEach(effects::add);
            return effects;
         }
      }
   }

   @Generated
   private PotionUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
