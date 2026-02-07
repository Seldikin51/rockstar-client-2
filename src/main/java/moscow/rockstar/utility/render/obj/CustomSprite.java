package moscow.rockstar.utility.render.obj;

import lombok.Generated;

public enum CustomSprite {
   COMBAT(SpriteTexture.MENU),
   MOVEMENT(SpriteTexture.MENU),
   VISUALS(SpriteTexture.MENU),
   PLAYER(SpriteTexture.MENU),
   OTHER(SpriteTexture.MENU),
   BIG_COMBAT(SpriteTexture.BIG_MENU),
   BIG_MOVEMENT(SpriteTexture.BIG_MENU),
   BIG_VISUALS(SpriteTexture.BIG_MENU),
   BIG_PLAYER(SpriteTexture.BIG_MENU),
   BIG_OTHER(SpriteTexture.BIG_MENU),
   CHECK(SpriteTexture.MENU);

   private final SpriteTexture texture;
   public final float x;

   private CustomSprite(SpriteTexture texture) {
      this.texture = texture;
      this.x = texture.x;
      texture.x = texture.x + texture.getStep();
   }

   @Generated
   public SpriteTexture getTexture() {
      return this.texture;
   }

   @Generated
   public float getX() {
      return this.x;
   }
}
