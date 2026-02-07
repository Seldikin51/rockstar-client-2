package moscow.rockstar.systems.event.impl.window;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class ChatReleaseEvent extends Event {
   private final float x;
   private final float y;
   private final int button;

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }

   @Generated
   public int getButton() {
      return this.button;
   }

   @Generated
   public ChatReleaseEvent(float x, float y, int button) {
      this.x = x;
      this.y = y;
      this.button = button;
   }
}
