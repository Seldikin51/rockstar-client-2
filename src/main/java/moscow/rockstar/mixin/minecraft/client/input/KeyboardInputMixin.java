package moscow.rockstar.mixin.minecraft.client.input;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void onTick(CallbackInfo ci) {
      Input input = (Input) (Object) this;
      InputAccessor accessor = (InputAccessor) input;
      PlayerInput keys = accessor.getInput();
      float movementForward = accessor.getMovementForward();
      float movementSideways = accessor.getMovementSideways();
      boolean jumping = accessor.getInput().jump();
      boolean sneaking = accessor.getInput().sneak();
      boolean sprint = accessor.getInput().sprint();
      InputEvent event = new InputEvent(movementForward, movementSideways, jumping, sneaking, sprint);
      Rockstar.getInstance().getEventManager().triggerEvent(event);
      accessor.setMovementForward(event.getForward());
      accessor.setMovementSideways(event.getStrafe());
      boolean forwardKey = event.getForward() > 0.0F;
      boolean backwardKey = event.getForward() < 0.0F;
      boolean leftKey = event.getStrafe() > 0.0F;
      boolean rightKey = event.getStrafe() < 0.0F;
      accessor.setInput(new PlayerInput(forwardKey, backwardKey, leftKey, rightKey, event.isJump(), event.isSneak(),
            event.isSprint()));
   }
}
