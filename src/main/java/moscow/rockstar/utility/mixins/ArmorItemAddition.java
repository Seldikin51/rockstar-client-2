package moscow.rockstar.utility.mixins;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;

public interface ArmorItemAddition {
   EquipmentType rockstar$getType();

   ArmorMaterial rockstar$getMaterial();
}
