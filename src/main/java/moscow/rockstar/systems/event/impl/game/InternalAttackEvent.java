package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.EventCancellable;
import net.minecraft.entity.Entity;

public class InternalAttackEvent extends EventCancellable {
   private final Entity entity;

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public InternalAttackEvent(Entity entity) {
      this.entity = entity;
   }
}
