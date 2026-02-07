package moscow.rockstar.ui.hud.impl.island;

import lombok.Generated;

public class IslandSize {
   public float width;
   public float height;

   public void update(float width, float height) {
      this.width = width;
      this.height = height;
   }

   @Generated
   public IslandSize(float width, float height) {
      this.width = width;
      this.height = height;
   }
}
