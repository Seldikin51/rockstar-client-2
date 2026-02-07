package moscow.rockstar.mixin.minecraft.client.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.CloseScreenEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEndEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.SlowDownEvent;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.modules.modules.player.InvUtils;
import moscow.rockstar.systems.modules.modules.player.NoPush;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.mixins.ClientPlayerEntityAddition;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements ClientPlayerEntityAddition, IMinecraft {
   @Unique
   private int groundTicks = 0;
   @Unique
   private final Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);

   @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
   private boolean onIsUsingItemRedirect(ClientPlayerEntity player) {
      SlowDownEvent slowDownEvent = new SlowDownEvent();
      Rockstar.getInstance().getEventManager().triggerEvent(slowDownEvent);
      return player.isUsingItem() && player.getVehicle() == null && !slowDownEvent.isCancelled();
   }

   @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
   public boolean unpressSprintKey(boolean original) {
      return this.aura.isEnabled() && this.aura.shouldPreventSprinting() ? false : original;
   }

   @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;canSprint()Z"))
   private boolean disallowSprinting(boolean original) {
      return this.aura.isEnabled() && this.aura.shouldPreventSprinting() ? false : original;
   }

   @WrapWithCondition(
      method = "closeScreen",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V")
   )
   private boolean preventCloseScreen(MinecraftClient instance, Screen screen) {
      Rockstar.getInstance().getEventManager().triggerEvent(new CloseScreenEvent(screen));
      return true;
   }

   @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
   public void removePushOutFromBlocks(double x, double z, CallbackInfo ci) {
      NoPush noPush = Rockstar.getInstance().getModuleManager().getModule(NoPush.class);
      if (noPush.isEnabled() && noPush.getBlocks().isSelected()) {
         ci.cancel();
      }
   }

   @Inject(method = "tick", at = @At("HEAD"))
   public void triggerTickEvent(CallbackInfo ci) {
      Rockstar.getInstance().getEventManager().triggerEvent(new ClientPlayerTickEvent());
   }

   @Inject(method = "tick", at = @At("RETURN"))
   public void triggerTickEndEvent(CallbackInfo ci) {
      Rockstar.getInstance().getEventManager().triggerEvent(new ClientPlayerTickEndEvent());
   }

   @Inject(method = "tickMovement", at = @At("HEAD"))
   public void updateOnGroundTicks(CallbackInfo ci) {
      if (mc.player != null && mc.player.isOnGround()) {
         this.groundTicks++;
      } else {
         this.groundTicks = 0;
      }
   }

   @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
   public float replaceMovePacketYaw(ClientPlayerEntity instance) {
      RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
      float yaw = rotationHandler.isIdling() ? instance.getYaw() : rotationHandler.getCurrentRotation().getYaw();
      rotationHandler.getServerRotation().setYaw(yaw);
      return yaw;
   }

   @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
   public float replaceMovePacketPitch(ClientPlayerEntity instance) {
      RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
      float pitch = rotationHandler.isIdling() ? instance.getPitch() : rotationHandler.getCurrentRotation().getPitch();
      rotationHandler.getServerRotation().setYaw(pitch);
      return pitch;
   }

   @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      InvUtils slotLock = Rockstar.getInstance().getModuleManager().getModule(InvUtils.class);
      if (slotLock.isEnabled() && slotLock.getSlotLock().isSelected() && slotLock.isLocked(mc.player.getInventory().selectedSlot)) {
         cir.setReturnValue(false);
         cir.cancel();
      }
   }

   @Override
   public int rockstar$getOnGroundTicks() {
      return this.groundTicks;
   }
}
