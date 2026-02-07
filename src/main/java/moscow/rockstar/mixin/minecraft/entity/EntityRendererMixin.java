package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Nametags;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
   @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
   private void onRenderLabel(T entity, CallbackInfoReturnable<Text> cir) {
      if (entity instanceof PlayerEntity player) {
         Nametags nameTags = Rockstar.getInstance().getModuleManager().getModule(Nametags.class);
         if (nameTags.isEnabled()) {
            if (nameTags.getOffFriends().isEnabled() && Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
               return;
            }

            cir.setReturnValue(null);
         }
      }
   }
}
