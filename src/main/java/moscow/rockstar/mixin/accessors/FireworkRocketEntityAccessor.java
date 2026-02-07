package moscow.rockstar.mixin.accessors;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
   @Accessor("life")
   int getLife();

   @Accessor("life")
   void setLife(int var1);

   @Accessor("lifeTime")
   int getLifeTime();

   @Accessor("lifeTime")
   void setLifeTime(int var1);
}
