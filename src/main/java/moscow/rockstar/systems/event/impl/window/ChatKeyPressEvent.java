package moscow.rockstar.systems.event.impl.window;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;

public class ChatKeyPressEvent extends Event {
   private final int keyCode;
   private final int scanCode;
   private final int modifiers;

   @Generated
   public ChatKeyPressEvent(int keyCode, int scanCode, int modifiers) {
      this.keyCode = keyCode;
      this.scanCode = scanCode;
      this.modifiers = modifiers;
   }

   @Generated
   public int getKeyCode() {
      return this.keyCode;
   }

   @Generated
   public int getScanCode() {
      return this.scanCode;
   }

   @Generated
   public int getModifiers() {
      return this.modifiers;
   }
}
