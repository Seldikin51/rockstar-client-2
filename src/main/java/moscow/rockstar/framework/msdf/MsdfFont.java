package moscow.rockstar.framework.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.other.NameProtect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public final class MsdfFont {
   private final String name;
   private final AbstractTexture texture;
   private final FontData.AtlasData atlas;
   private final FontData.MetricsData metrics;
   private final Map<Integer, MsdfGlyph> glyphs;
   private final Map<Integer, Map<Integer, Float>> kernings;
   private final ConcurrentHashMap<Long, Float> widthCache = new ConcurrentHashMap<>();

   private MsdfFont(
      String name,
      AbstractTexture texture,
      FontData.AtlasData atlas,
      FontData.MetricsData metrics,
      Map<Integer, MsdfGlyph> glyphs,
      Map<Integer, Map<Integer, Float>> kernings
   ) {
      this.name = name;
      this.texture = texture;
      this.atlas = atlas;
      this.metrics = metrics;
      this.glyphs = glyphs;
      this.kernings = kernings;
   }

   public int getTextureId() {
      return this.texture.getGlId();
   }

   public void applyGlyphs(
      Matrix4f matrix, VertexConsumer consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color
   ) {
      int prevChar = -1;
      boolean skipNext = false;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (skipNext) {
            skipNext = false;
         } else if (c == 167) {
            skipNext = true;
         } else {
            MsdfGlyph glyph = this.glyphs.get(Integer.valueOf(c));
            if (glyph != null) {
               Map<Integer, Float> kerning = this.kernings.get(prevChar);
               if (kerning != null) {
                  x += kerning.getOrDefault(Integer.valueOf(c), 0.0F) * size;
               }

               x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
               prevChar = c;
            }
         }
      }
   }

   public float getWidthOld(String text, float size) {
      text = text.replace("і", "i").replace("І", "I");
      int prevChar = -1;
      float width = 0.0F;
      boolean skipNext = false;
      NameProtect nameProtectModule = Rockstar.getInstance().getModuleManager().getModule(NameProtect.class);
      if (nameProtectModule.isEnabled()) {
         text = nameProtectModule.patchName(text);
      }

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (skipNext) {
            skipNext = false;
         } else if (c == 167) {
            skipNext = true;
         } else {
            MsdfGlyph glyph = this.glyphs.get(Integer.valueOf(c));
            if (glyph != null) {
               Map<Integer, Float> kerning = this.kernings.get(prevChar);
               if (kerning != null) {
                  width += kerning.getOrDefault(Integer.valueOf(c), 0.0F) * size;
               }

               width += glyph.getWidth(size) + 0.25F;
               prevChar = c;
            }
         }
      }

      return width;
   }

   private static long widthKey(String s, float size, boolean np) {
      int h = s.hashCode();
      return h & 4294967295L ^ (long)Float.floatToIntBits(size) << 32 ^ (np ? -7046029254386353131L : 0L);
   }

   public float getWidth(String text, float size) {
      text = text.replace("і", "i").replace("І", "I");
      NameProtect nameProtectModule = Rockstar.getInstance().getModuleManager().getModule(NameProtect.class);
      boolean np = nameProtectModule.isEnabled();
      if (np) {
         text = nameProtectModule.patchName(text);
      }

      long key = widthKey(text, size, np);
      Float cached = this.widthCache.get(key);
      if (cached != null) {
         return cached;
      } else {
         float w = this.getWidthOld(text, size);
         this.widthCache.put(key, w);
         return w;
      }
   }

   public void clearWidthCache() {
      this.widthCache.clear();
   }

   public float getTextWidth(Text text, float size) {
      return this.getWidth(text.getString(), size);
   }

   public Font getFont(float size) {
      return new Font(this, size);
   }

   public String getName() {
      return this.name;
   }

   public FontData.AtlasData getAtlas() {
      return this.atlas;
   }

   public FontData.MetricsData getMetrics() {
      return this.metrics;
   }

   public static MsdfFont.Builder builder() {
      return new MsdfFont.Builder();
   }

   public static class Builder {
      private String name = "?";
      private Identifier dataIdentifer;
      private Identifier atlasIdentifier;

      private Builder() {
      }

      public MsdfFont.Builder name(String name) {
         this.name = name;
         return this;
      }

      public MsdfFont.Builder data(String dataFileName) {
         this.dataIdentifer = Identifier.of(Rockstar.MOD_ID, "fonts/msdf/" + dataFileName + ".json");
         return this;
      }

      public MsdfFont.Builder atlas(String atlasFileName) {
         this.atlasIdentifier = Identifier.of(Rockstar.MOD_ID, "fonts/msdf/" + atlasFileName + ".png");
         return this;
      }

      public MsdfFont build() {
         FontData data = ResourceProvider.fromJsonToInstance(this.dataIdentifer, FontData.class);
         AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(this.atlasIdentifier);
         if (data == null) {
            throw new RuntimeException(
               "Failed to read font data file: "
                  + this.dataIdentifer.toString()
                  + "; Are you sure this is json file? Try to check the correctness of its syntax."
            );
         } else {
            RenderSystem.recordRenderCall(() -> texture.setFilter(true, false));
            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs()
               .stream()
               .collect(Collectors.toMap(glyphData -> glyphData.unicode(), glyphData -> new MsdfGlyph(glyphData, aWidth, aHeight)));
            Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();
            data.kernings().forEach(kerning -> {
               Map<Integer, Float> map = kernings.get(kerning.leftChar());
               if (map == null) {
                  map = new HashMap<>();
                  kernings.put(kerning.leftChar(), map);
               }

               map.put(kerning.rightChar(), kerning.advance());
            });
            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
         }
      }
   }
}
