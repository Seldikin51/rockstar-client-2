package moscow.rockstar.mixin.minecraft.render.entity;

import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements EntityRenderStateAddition {
   @Unique
   private Entity rockstar$entity;

   @Unique
   @Override
   public void rockstar$setEntity(Entity entity) {
      this.rockstar$entity = entity;
   }

   @Unique
   @Override
   public Entity rockstar$getEntity() {
      return this.rockstar$entity;
   }
}
