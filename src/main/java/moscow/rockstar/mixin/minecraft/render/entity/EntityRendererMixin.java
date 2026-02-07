package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.systems.modules.modules.other.CounterMine;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
   @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
   private void onShouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
      if (entity instanceof ItemDisplayEntity itemDisplay && CounterMine.shouldHideEntity(itemDisplay)) {
         cir.setReturnValue(false);
      }
   }
}
