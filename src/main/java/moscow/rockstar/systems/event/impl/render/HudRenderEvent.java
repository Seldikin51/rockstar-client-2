package moscow.rockstar.systems.event.impl.render;

import lombok.Generated;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.Event;

public class HudRenderEvent extends Event {
   private final CustomDrawContext context;
   private final float tickDelta;

   @Generated
   public CustomDrawContext getContext() {
      return this.context;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public HudRenderEvent(CustomDrawContext context, float tickDelta) {
      this.context = context;
      this.tickDelta = tickDelta;
   }
}
