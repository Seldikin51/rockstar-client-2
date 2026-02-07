package moscow.rockstar.mixin.accessors;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
   @Accessor("buffers")
   BufferBuilderStorage buffers();

   @Accessor("lightmapTextureManager")
   LightmapTextureManager lightmapTextureManager();
}
