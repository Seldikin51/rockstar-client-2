package moscow.rockstar.ui.components.textfield;

import lombok.Generated;

public class FieldAction {
   private final Runnable enter;
   private final Runnable tab;

   @Generated
   public Runnable getEnter() {
      return this.enter;
   }

   @Generated
   public Runnable getTab() {
      return this.tab;
   }

   @Generated
   public FieldAction(Runnable enter, Runnable tab) {
      this.enter = enter;
      this.tab = tab;
   }
}
