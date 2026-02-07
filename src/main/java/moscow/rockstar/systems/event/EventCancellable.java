package moscow.rockstar.systems.event;

import lombok.Generated;

public class EventCancellable extends Event {
   private boolean cancelled;

   public void cancel() {
      this.cancelled = true;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }
}
