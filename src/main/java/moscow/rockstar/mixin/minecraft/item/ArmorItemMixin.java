package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.utility.mixins.ArmorItemAddition;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin implements ArmorItemAddition {
   @Unique
   private EquipmentType rockstar$type;
   @Unique
   private ArmorMaterial rockstar$material;

   @Inject(method = "<init>", at = @At("TAIL"))
   public void saveArgs(ArmorMaterial material, EquipmentType type, Settings settings, CallbackInfo ci) {
      this.rockstar$type = type;
      this.rockstar$material = material;
   }

   @Override
   public ArmorMaterial rockstar$getMaterial() {
      return this.rockstar$material;
   }

   @Override
   public EquipmentType rockstar$getType() {
      return this.rockstar$type;
   }
}
