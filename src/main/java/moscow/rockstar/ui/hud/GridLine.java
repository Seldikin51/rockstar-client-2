package moscow.rockstar.ui.hud;

import lombok.Generated;

public class GridLine {
   private final GridLine.Type type;
   private final float pos;
   private boolean active;

   @Generated
   public GridLine.Type getType() {
      return this.type;
   }

   @Generated
   public float getPos() {
      return this.pos;
   }

   @Generated
   public boolean isActive() {
      return this.active;
   }

   @Generated
   public GridLine(GridLine.Type type, float pos) {
      this.type = type;
      this.pos = pos;
   }

   @Generated
   public void setActive(boolean active) {
      this.active = active;
   }

   public static enum Type {
      HORIZONTAL,
      VERTICAL;
   }
}
