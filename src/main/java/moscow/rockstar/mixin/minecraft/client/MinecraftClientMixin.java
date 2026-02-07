package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.protection.client.MinecraftClientMixinProtection;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.systems.modules.modules.player.NoDelay;
import moscow.rockstar.utility.render.penis.PenisAtlas;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
   @Shadow
   private int itemUseCooldown;

   @Inject(method = "tick", at = @At("HEAD"))
   public void tick(CallbackInfo ci) {
      Rockstar.getInstance().getEventManager().triggerEvent(new GameTickEvent());
   }

   @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V"))
   public void initializeClient(RunArgs args, CallbackInfo ci) {
      MinecraftClientMixinProtection.init();
   }

   @Inject(method = "<init>", at = @At("RETURN"))
   public void endInitialize(RunArgs args, CallbackInfo ci) {
      PenisAtlas atlas = PenisAtlas.getOrCreateAtlasFor(16, 16);
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/combat.penis"));
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/movement.penis"));
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/visuals.penis"));
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/player.penis"));
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/other.penis"));
      atlas.registerAnimationFromPenisFile(Rockstar.id("penises/search.penis"));
      atlas.buildAtlas();
      PenisAtlas atlas12 = PenisAtlas.getOrCreateAtlasFor(12, 12);
      atlas12.registerAnimationFromPenisFile(Rockstar.id("penises/check_enable.penis"));
      atlas12.registerAnimationFromPenisFile(Rockstar.id("penises/check_disable.penis"));
      atlas12.buildAtlas();
   }

   @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;close()V", shift = Shift.AFTER))
   public void shutdownClient(CallbackInfo ci) {
      MinecraftClientMixinProtection.shutdown();
   }

   @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
   public void changeWindowTitle(CallbackInfoReturnable<String> cir) {
      MinecraftClientMixinProtection.updateTitle(cir);
   }

   @Inject(method = "doItemUse", at = @At("TAIL"))
   private void resetItemUseCooldown(CallbackInfo ci) {
      NoDelay noDelayModule = Rockstar.getInstance().getModuleManager().getModule(NoDelay.class);
      if (noDelayModule.isEnabled() && noDelayModule.getRightClick().isEnabled()) {
         this.itemUseCooldown = 0;
      }
   }
}
