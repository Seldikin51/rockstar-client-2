package moscow.rockstar.systems.target;

import java.util.Comparator;
import java.util.function.Function;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TargetComparators implements IMinecraft {
   public static final Comparator<Entity> DISTANCE = Comparator.comparingDouble(entity -> entity.distanceTo(mc.player));
   public static final Comparator<Entity> HEALTH = Comparator.comparingDouble(entity -> entity instanceof LivingEntity living ? living.getHealth() : 0.0);
   public static final Comparator<Entity> FOV = Comparator.comparingDouble(entity -> {
      if (mc.player == null) {
         return Double.MAX_VALUE;
      } else {
         Vec3d playerPos = mc.player.getPos();
         Vec3d entityPos = entity.getPos();
         Vec3d playerLook = mc.player.getRotationVec(1.0F);
         Vec3d toEntity = entityPos.subtract(playerPos).normalize();
         double dot = playerLook.dotProduct(toEntity);
         return Math.acos(MathHelper.clamp(dot, -1.0, 1.0)) * (180.0 / Math.PI);
      }
   });
   public static final Comparator<Entity> BAD_ARMOR = Comparator.comparingDouble(entity -> {
      if (entity instanceof PlayerEntity player) {
         double totalArmor = 0.0;

         for (ItemStack armorStack : player.getAllArmorItems()) {
            if (armorStack != null && !armorStack.isEmpty()) {
               totalArmor += armorStack.getItem().getDefaultStack().getCount();
            }
         }

         return totalArmor;
      } else {
         return Double.MAX_VALUE;
      }
   });
   public static final Comparator<Entity> GOOD_ARMOR = BAD_ARMOR.reversed();

   public static Comparator<Entity> byValue(Function<Entity, Double> valueExtractor) {
      return Comparator.comparingDouble(valueExtractor::apply);
   }

   public static Comparator<Entity> byValueReversed(Function<Entity, Double> valueExtractor) {
      return Comparator.comparingDouble(valueExtractor::apply).reversed();
   }
}
