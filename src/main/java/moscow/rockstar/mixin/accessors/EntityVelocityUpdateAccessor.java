package moscow.rockstar.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateAccessor {
   @Mutable
   @Accessor("velocityX")
   void setVelocityX(int var1);

   @Mutable
   @Accessor("velocityY")
   void setVelocityY(int var1);

   @Mutable
   @Accessor("velocityZ")
   void setVelocityZ(int var1);
}
