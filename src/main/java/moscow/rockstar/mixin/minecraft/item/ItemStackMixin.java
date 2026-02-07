package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.FinishEatEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
   @Inject(method = "finishUsing", at = @At("TAIL"))
   private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
      if (user instanceof PlayerEntity player) {
         Rockstar.getInstance().getEventManager().triggerEvent(new FinishEatEvent(player, (ItemStack) (Object) this));
      }
   }
}
