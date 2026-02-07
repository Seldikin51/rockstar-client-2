package moscow.rockstar.mixin.minecraft.client.gui.screen;

import java.util.Optional;
import java.util.function.Consumer;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.components.gif.Gif;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin implements IScaledResolution, IMinecraft {
   @Unique
   private Gif daunGif;
   @Unique
   private Animation fadeOutAnimation;
   @Shadow
   private long reloadCompleteTime = -1L;
   @Final
   @Shadow
   private Consumer<Optional<Throwable>> exceptionHandler;
   @Shadow
   @Final
   private ResourceReload reload;
   @Shadow
   @Final
   private boolean reloading;
   @Shadow
   private long reloadStartTime;

   @Inject(method = "<init>", at = @At("RETURN"))
   public void init(MinecraftClient client, ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
      this.daunGif = new Gif(Rockstar.id("gifs/loading.gif"), 100.0F, 100.0F, 100.0F, 100.0F);
      this.fadeOutAnimation = new Animation(3000L, 1.0F, Easing.CUBIC_IN_OUT);
   }

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void replaceRendering(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (!Rockstar.getInstance().isPanic()) {
         ci.cancel();
         int width = context.getScaledWindowWidth();
         int height = context.getScaledWindowHeight();
         UIContext uiContext = UIContext.of(context, 0, 0, delta);
         long currentTime = Util.getMeasuringTimeMs();
         if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = currentTime;
         }

         float f = this.reloadCompleteTime > -1L ? (float)(currentTime - this.reloadCompleteTime) / 1000.0F : -1.0F;
         float g = this.reloadStartTime > -1L ? (float)(currentTime - this.reloadStartTime) / 500.0F : -1.0F;
         if (f >= 1.0F) {
            if (mc.currentScreen != null) {
               mc.currentScreen.render(context, 0, 0, delta);
            }

            int k = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
         } else if (this.reloading && mc.currentScreen != null && g < 1.0F) {
            mc.currentScreen.render(context, mouseX, mouseY, delta);
            int k = MathHelper.ceil(MathHelper.clamp(g, 0.15, 1.0) * 255.0);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
         }

         if (f < 1.0F) {
            this.daunGif
               .set(
                  0.0F,
                  sr.getScaledHeight() / 2.0F - sr.getScaledWidth() / 1920.0F * 1080.0F / 2.0F,
                  sr.getScaledWidth(),
                  sr.getScaledWidth() / 1920.0F * 1080.0F
               );
            this.daunGif.setAlpha(1.0F);
            this.daunGif.render(uiContext);
         }

         if (f >= 2.0F) {
            mc.setOverlay(null);
            this.daunGif.dispose();
         }

         if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
            try {
               this.reload.throwException();
               this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var15) {
               this.exceptionHandler.accept(Optional.of(var15));
            }

            this.reloadCompleteTime = currentTime;
            if (mc.currentScreen != null) {
               mc.currentScreen.init(mc, context.getScaledWindowWidth(), context.getScaledWindowHeight());
            }
         }
      }
   }
}
