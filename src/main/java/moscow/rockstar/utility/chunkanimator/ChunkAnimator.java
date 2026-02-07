package moscow.rockstar.utility.chunkanimator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.ChunkPos;

public class ChunkAnimator {
   private static final Map<ChunkPos, ChunkAnimator.ChunkAnimation> animatingChunks = new ConcurrentHashMap<>();
   private static final long ANIMATION_DURATION = 1000L;

   public static void startAnimation(ChunkPos pos, float worldY) {
      float startY = worldY - 64.0F;
      animatingChunks.put(pos, new ChunkAnimator.ChunkAnimation(startY, worldY));
   }

   public static Float getAnimationOffset(ChunkPos pos) {
      ChunkAnimator.ChunkAnimation anim = animatingChunks.get(pos);
      if (anim == null) {
         return null;
      } else if (anim.isFinished()) {
         animatingChunks.remove(pos);
         return null;
      } else {
         return anim.getCurrentY() - anim.targetY;
      }
   }

   public static class ChunkAnimation {
      public final long startTime = System.currentTimeMillis();
      public final float startY;
      public final float targetY;

      public ChunkAnimation(float startY, float targetY) {
         this.startY = startY;
         this.targetY = targetY;
      }

      public float getCurrentY() {
         long elapsed = System.currentTimeMillis() - this.startTime;
         if (elapsed >= 1000L) {
            return this.targetY;
         } else {
            float progress = (float)elapsed / 1000.0F;
            return this.startY + (this.targetY - this.startY) * progress;
         }
      }

      public boolean isFinished() {
         return System.currentTimeMillis() - this.startTime >= 1000L;
      }
   }
}
