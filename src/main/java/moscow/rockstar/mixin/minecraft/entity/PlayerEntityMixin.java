package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.modules.modules.player.NoPush;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
   @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
   private void attackAHook2(Entity target, CallbackInfo ci) {
      AttackEvent event = new AttackEvent(target);
      Rockstar.getInstance().getEventManager().triggerEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "attack", at = @At("RETURN"), cancellable = true)
   private void attackAHook(Entity target, CallbackInfo ci) {
      PostAttackEvent event = new PostAttackEvent(target);
      Rockstar.getInstance().getEventManager().triggerEvent(event);
   }

   @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
   private void removePushFromFluids(CallbackInfoReturnable<Boolean> cir) {
      NoPush noPush = Rockstar.getInstance().getModuleManager().getModule(NoPush.class);
      PlayerEntity player = (PlayerEntity) (Object) this;
      if (player == MinecraftClient.getInstance().player && noPush.isEnabled() && noPush.getFluids().isSelected()) {
         cir.setReturnValue(false);
      }
   }

   @Redirect(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
   private Vec3d redirectGetRotationVectorInTravel(PlayerEntity instance) {
      RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
      return rotationHandler.isIdling() ? instance.getRotationVector()
            : rotationHandler.getCurrentRotation().getRotationVector();
   }
}
