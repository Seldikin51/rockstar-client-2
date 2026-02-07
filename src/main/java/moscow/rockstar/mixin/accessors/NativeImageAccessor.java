package moscow.rockstar.mixin.accessors;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {
   @Accessor("pointer")
   long getPointer();

   @Invoker("setColor")
   void invokeSetColor(int var1, int var2, int var3);
}
