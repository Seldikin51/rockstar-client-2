package moscow.rockstar.utility.render.obj;

import lombok.Generated;

public enum SpriteTexture {
   MENU("icons/batched/menu.png", 96.0F, 16.0F, 16.0F),
   BIG_MENU("icons/batched/bigmenu.png", 120.0F, 20.0F, 20.0F);

   private final String texture;
   private final float width;
   private final float height;
   private final float step;
   public float x;

   @Generated
   public String getTexture() {
      return this.texture;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public float getStep() {
      return this.step;
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   private SpriteTexture(final String texture, final float width, final float height, final float step) {
      this.texture = texture;
      this.width = width;
      this.height = height;
      this.step = step;
   }
}
