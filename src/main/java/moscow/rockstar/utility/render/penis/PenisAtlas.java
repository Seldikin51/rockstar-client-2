package moscow.rockstar.utility.render.penis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PenisAtlas {
   private static final Logger LOGGER = LoggerFactory.getLogger(PenisAtlas.class);
   private static final List<PenisAtlas> INSTANCES = new ArrayList<>();
   private final Map<Identifier, PenisAtlas.AnimationRegion> animationRegions = new HashMap<>();
   private final List<PenisAtlas.AnimationFrameData> allFrames = new ArrayList<>();
   private Identifier atlasTexture;
   private boolean isBuilt = false;
   private final int frameWidth;
   private final int frameHeight;

   public static PenisAtlas getOrCreateAtlasFor(int width, int height) {
      for (PenisAtlas atlas : INSTANCES) {
         if (atlas.frameWidth == width && atlas.frameHeight == height && !atlas.isBuilt()) {
            return atlas;
         }
      }

      PenisAtlas newAtlas = new PenisAtlas(width, height);
      INSTANCES.add(newAtlas);
      return newAtlas;
   }

   private PenisAtlas(int frameWidth, int frameHeight) {
      this.frameWidth = frameWidth;
      this.frameHeight = frameHeight;
   }

   public void registerAnimation(Identifier animationId, PenisMeta meta, List<NativeImage> frames) {
      if (this.isBuilt) {
         throw new RuntimeException("Атлас уже собран! Регистрируйте анимации до вызова buildAtlas()");
      } else if (frames.isEmpty()) {
         LOGGER.warn("Пустая анимация: {}", animationId);
      } else {
         for (NativeImage frame : frames) {
            if (frame.getWidth() != this.frameWidth || frame.getHeight() != this.frameHeight) {
               throw new RuntimeException(
                  String.format(
                     "Размер кадров анимации %s (%dx%d) не совпадает с размером этого атласа (%dx%d)",
                     animationId,
                     frame.getWidth(),
                     frame.getHeight(),
                     this.frameWidth,
                     this.frameHeight
                  )
               );
            }
         }

         int startIndex = this.allFrames.size();

         for (int i = 0; i < frames.size(); i++) {
            this.allFrames.add(new PenisAtlas.AnimationFrameData(animationId, i, frames.get(i)));
         }

         PenisAtlas.AnimationRegion region = new PenisAtlas.AnimationRegion(animationId, meta, startIndex, frames.size(), null);
         this.animationRegions.put(animationId, region);
         LOGGER.info("Зарегистрирована анимация {} с {} кадрами в атласе {}x{}", new Object[]{animationId, frames.size(), this.frameWidth, this.frameHeight});
      }
   }

   public void registerAnimationFromPenisFile(Identifier penisFile) {
      try {
         ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
         Optional<Resource> resourceOpt = resourceManager.getResource(penisFile);
         if (resourceOpt.isEmpty()) {
            throw new RuntimeException("Файл не найден: " + penisFile);
         } else {
            Resource resource = resourceOpt.get();
            PenisMeta meta = null;
            List<NativeImage> frames = new ArrayList<>();

            try (
               InputStream inputStream = resource.getInputStream();
               ZipInputStream zipStream = new ZipInputStream(inputStream);
            ) {
               Map<String, byte[]> frameData;
               ZipEntry entry;
               for (frameData = new TreeMap<>(); (entry = zipStream.getNextEntry()) != null; zipStream.closeEntry()) {
                  String entryName = entry.getName();
                  if ("meta.json".equals(entryName)) {
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     byte[] buffer = new byte[1024];

                     int len;
                     while ((len = zipStream.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                     }

                     String jsonContent = baos.toString(StandardCharsets.UTF_8);
                     meta = PenisMeta.fromJson(jsonContent);
                  } else if (entryName.startsWith("frames/") && entryName.endsWith(".png")) {
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     byte[] buffer = new byte[1024];

                     int len;
                     while ((len = zipStream.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                     }

                     frameData.put(entryName, baos.toByteArray());
                  }
               }

               for (byte[] pngData : frameData.values()) {
                  NativeImage frameImage = NativeImage.read(new ByteArrayInputStream(pngData));
                  frames.add(frameImage);
               }
            }

            if (meta == null) {
               throw new RuntimeException("Не найден meta.json в " + penisFile);
            } else if (frames.isEmpty()) {
               throw new RuntimeException("Нет кадров для анимации " + penisFile);
            } else if (frames.get(0).getWidth() == this.frameWidth && frames.get(0).getHeight() == this.frameHeight) {
               this.registerAnimation(penisFile, meta, frames);
            } else {
               throw new RuntimeException(
                  String.format(
                     "Размер кадров анимации %s (%dx%d) не совпадает с размером этого атласа (%dx%d)",
                     penisFile,
                     frames.get(0).getWidth(),
                     frames.get(0).getHeight(),
                     this.frameWidth,
                     this.frameHeight
                  )
               );
            }
         }
      } catch (Exception var20) {
         throw new RuntimeException("Ошибка загрузки анимации из " + penisFile, var20);
      }
   }

   public void buildAtlas() {
      if (this.isBuilt) {
         LOGGER.warn("Атлас уже собран!");
      } else if (this.allFrames.isEmpty()) {
         LOGGER.warn("Нет кадров для создания атласа!");
      } else {
         int totalFrames = this.allFrames.size();
         int columns = (int)Math.ceil(Math.sqrt(totalFrames));
         int rows = (int)Math.ceil((double)totalFrames / columns);
         int atlasWidth = columns * this.frameWidth;
         int atlasHeight = rows * this.frameHeight;
         NativeImage atlasImage = new NativeImage(atlasWidth, atlasHeight, false);

         for (int x = 0; x < atlasWidth; x++) {
            for (int y = 0; y < atlasHeight; y++) {
               atlasImage.setColor(x, y, 0);
            }
         }

         for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
            int col = frameIndex % columns;
            int row = frameIndex / columns;
            int atlasX = col * this.frameWidth;
            int atlasY = row * this.frameHeight;
            NativeImage sourceFrame = this.allFrames.get(frameIndex).image;

            for (int x = 0; x < this.frameWidth; x++) {
               for (int y = 0; y < this.frameHeight; y++) {
                  atlasImage.setColor(atlasX + x, atlasY + y, sourceFrame.getColor(x, y));
               }
            }
         }

         this.atlasTexture = Identifier.of("rockstar", "global_animation_atlas_" + this.frameWidth + "x" + this.frameHeight);
         NativeImageBackedTexture atlasTextureObj = new NativeImageBackedTexture(atlasImage);
         MinecraftClient.getInstance().getTextureManager().registerTexture(this.atlasTexture, atlasTextureObj);

         for (PenisAtlas.AnimationRegion region : this.animationRegions.values()) {
            region.atlasTexture = this.atlasTexture;
            List<PenisSprite> sprites = new ArrayList<>();

            for (int frameIndex = 0; frameIndex < region.frameCount; frameIndex++) {
               int globalIndex = region.startIndex + frameIndex;
               int col = globalIndex % columns;
               int row = globalIndex / columns;
               float u1 = (float)col / columns;
               float v1 = (float)row / rows;
               float u2 = (float)(col + 1) / columns;
               float v2 = (float)(row + 1) / rows;
               PenisSprite sprite = new PenisSprite(this.atlasTexture, u1, v1, u2, v2, this.frameWidth, this.frameHeight);
               sprites.add(sprite);
            }

            region.sprites = sprites;
         }

         this.isBuilt = true;
         LOGGER.info(
            "Атлас {}x{} собран с {} анимациями и {} кадрами", new Object[]{this.frameWidth, this.frameHeight, this.animationRegions.size(), totalFrames}
         );
      }
   }

   public static PenisAtlas.AnimationRegion getAnimationRegion(Identifier animationId) {
      for (PenisAtlas atlas : INSTANCES) {
         PenisAtlas.AnimationRegion region = atlas.animationRegions.get(animationId);
         if (region != null) {
            return region;
         }
      }

      return null;
   }

   public Identifier getAtlasTexture() {
      return this.atlasTexture;
   }

   public boolean isBuilt() {
      return this.isBuilt;
   }

   public void clear() {
      if (this.atlasTexture != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(this.atlasTexture);
      }

      for (PenisAtlas.AnimationFrameData frameData : this.allFrames) {
         try {
            frameData.image.close();
         } catch (Exception var4) {
         }
      }

      this.animationRegions.clear();
      this.allFrames.clear();
      this.isBuilt = false;
   }

   public static void clearAllAtlases() {
      for (PenisAtlas atlas : INSTANCES) {
         atlas.clear();
      }

      INSTANCES.clear();
   }

   private static class AnimationFrameData {
      public final Identifier animationId;
      public final int frameIndex;
      public final NativeImage image;

      public AnimationFrameData(Identifier animationId, int frameIndex, NativeImage image) {
         this.animationId = animationId;
         this.frameIndex = frameIndex;
         this.image = image;
      }
   }

   public static class AnimationRegion {
      public final Identifier animationId;
      public final PenisMeta meta;
      public final int startIndex;
      public final int frameCount;
      public Identifier atlasTexture;
      public List<PenisSprite> sprites;

      public AnimationRegion(Identifier animationId, PenisMeta meta, int startIndex, int frameCount, Identifier atlasTexture) {
         this.animationId = animationId;
         this.meta = meta;
         this.startIndex = startIndex;
         this.frameCount = frameCount;
         this.atlasTexture = atlasTexture;
      }

      public PenisSprite getFrameSprite(int frameIndex) {
         return this.sprites != null && frameIndex >= 0 && frameIndex < this.sprites.size() ? this.sprites.get(frameIndex) : null;
      }
   }
}
