package moscow.rockstar.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SplashOverlay.class)
public interface SplashOverlayAccessor {
   @Accessor("reload")
   ResourceReload getReload();

   @Accessor("client")
   MinecraftClient getClient();

   @Accessor("reloadCompleteTime")
   long getReloadCompleteTime();

   @Accessor("reloadCompleteTime")
   void setReloadCompleteTime(long var1);
}
