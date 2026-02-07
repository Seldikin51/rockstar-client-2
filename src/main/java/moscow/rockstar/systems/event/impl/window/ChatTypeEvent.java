package moscow.rockstar.systems.event.impl.window;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class ChatTypeEvent extends Event {
   private final char text;
   private final int modifiers;

   @Generated
   public char getText() {
      return this.text;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }

   @Generated
   public ChatTypeEvent(char text, int modifiers) {
      this.text = text;
      this.modifiers = modifiers;
   }
}
