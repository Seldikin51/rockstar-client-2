package moscow.rockstar.mixin.minecraft.client.input;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Input.class)
public interface InputAccessor {
   @Accessor("movementForward")
   float getMovementForward();

   @Accessor("movementForward")
   void setMovementForward(float var1);

   @Accessor("movementSideways")
   float getMovementSideways();

   @Accessor("movementSideways")
   void setMovementSideways(float var1);

   @Accessor("playerInput")
   PlayerInput getInput();

   @Accessor("playerInput")
   void setInput(PlayerInput var1);
}
