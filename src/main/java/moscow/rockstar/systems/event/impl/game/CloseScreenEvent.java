package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.gui.screen.Screen;

public class CloseScreenEvent extends Event {
   private final Screen screen;

   @Generated
   public Screen getScreen() {
      return this.screen;
   }

   @Generated
   public CloseScreenEvent(Screen screen) {
      this.screen = screen;
   }
}
