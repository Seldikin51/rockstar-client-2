package moscow.rockstar.utility.render.penis;

import lombok.Generated;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.util.Identifier;

public class PenisPlayer {
   private final PenisAtlas.AnimationRegion region;
   private final Timer frameTimer;
   private int currentFrame = 0;
   private boolean isPlaying = true;
   private boolean hasFinished = false;
   private boolean playOnceMode = false;

   public PenisPlayer(Identifier animationId) {
      this.region = PenisAtlas.getAnimationRegion(animationId);
      if (this.region == null) {
         throw new RuntimeException("Анимация не найдена в глобальном атласе: " + animationId);
      } else {
         this.frameTimer = new Timer();
      }
   }

   public void playOnce() {
      this.currentFrame = 0;
      this.isPlaying = true;
      this.hasFinished = false;
      this.playOnceMode = true;
      this.frameTimer.reset();
   }

   public PenisSprite getCurrentSprite() {
      if (this.isPlaying && !this.hasFinished) {
         this.update();
         return this.region.getFrameSprite(this.currentFrame);
      } else {
         return this.region.getFrameSprite(this.currentFrame);
      }
   }

   public void update() {
      if (this.isPlaying && !this.hasFinished) {
         long frameDuration = this.region.meta.getFrameDuration();
         if (this.frameTimer.finished(frameDuration)) {
            this.nextFrame();
            this.frameTimer.reset();
         }
      }
   }

   private void nextFrame() {
      this.currentFrame++;
      if (this.currentFrame >= this.region.frameCount) {
         if (this.playOnceMode) {
            this.currentFrame = this.region.frameCount - 1;
            this.hasFinished = true;
            this.playOnceMode = false;
         } else if (this.region.meta.isLoop()) {
            this.currentFrame = 0;
         } else {
            this.currentFrame = this.region.frameCount - 1;
            this.hasFinished = true;
         }
      }
   }

   public void play() {
      this.isPlaying = true;
      this.hasFinished = false;
      this.playOnceMode = false;
   }

   public void pause() {
      this.isPlaying = false;
   }

   public void stop() {
      this.isPlaying = false;
      this.currentFrame = 0;
      this.hasFinished = false;
      this.playOnceMode = false;
      this.frameTimer.reset();
   }

   public void setFrame(int frame) {
      if (frame >= 0 && frame < this.region.frameCount) {
         this.currentFrame = frame;
         this.hasFinished = false;
      }
   }

   public boolean isFinished() {
      return this.hasFinished;
   }

   public PenisMeta getMeta() {
      return this.region.meta;
   }

   public Identifier getGlobalAtlasTexture() {
      return this.region.atlasTexture;
   }

   @Generated
   public int getCurrentFrame() {
      return this.currentFrame;
   }

   @Generated
   public boolean isPlaying() {
      return this.isPlaying;
   }
}
