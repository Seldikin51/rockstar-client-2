package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Dynamic.class)
public class DynamicMixin {
   @Shadow
   private float lastFrameDuration;
   @Shadow
   private float tickDelta;
   @Shadow
   private long prevTimeMillis;
   @Final
   @Shadow
   private float tickTime;

   @Inject(
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J", opcode = 181, ordinal = 0),
      method = "beginRenderTick(J)I",
      cancellable = true
   )
   public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
      if (EntityUtility.getTimer() != 1.0F) {
         this.lastFrameDuration = (float)(timeMillis - this.prevTimeMillis) / this.tickTime * EntityUtility.getTimer();
         this.prevTimeMillis = timeMillis;
         this.tickDelta = this.tickDelta + this.lastFrameDuration;
         int i = (int)this.tickDelta;
         this.tickDelta -= i;
         cir.setReturnValue(i);
      }
   }
}
