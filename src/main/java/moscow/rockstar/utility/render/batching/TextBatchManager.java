package moscow.rockstar.utility.render.batching;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public final class TextBatchManager {
   private static final Map<Integer, TextBatchManager.Batch> batches = new HashMap<>();
   private static boolean globalBegun = false;

   public static void beginFrame() {
      if (!globalBegun) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         globalBegun = true;
      }
   }

   public static void addText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, float thickness, float spacing) {
      if (!globalBegun) {
         beginFrame();
      }

      int key = font.getTextureId();
      TextBatchManager.Batch batch = batches.get(key);
      if (batch == null) {
         batch = new TextBatchManager.Batch(font);
         batches.put(key, batch);
         RenderSystem.setShaderTexture(0, font.getTextureId());
         ShaderProgram shader = RenderSystem.setShader(MsdfRenderer.MSDF_FONT_SHADER_KEY);
         shader.getUniform("Range").set(font.getAtlas().range());
         shader.getUniform("Thickness").set(thickness);
         shader.getUniform("Smoothness").set(0.5F);
         shader.getUniform("EnableFadeout").set(0);
         batch.begun = true;
      }

      font.applyGlyphs(matrix, batch.builder, text, size, thickness * 0.5F * size, spacing, x - 0.75F, y + size * 0.7F, z, color);
   }

   public static void addTextWithFade(
      MsdfFont font,
      String text,
      float size,
      int color,
      Matrix4f matrix,
      float x,
      float y,
      float z,
      float thickness,
      float spacing,
      float fadeoutStart,
      float fadeoutEnd,
      float maxWidth,
      float textPosX
   ) {
      if (!globalBegun) {
         beginFrame();
      }

      int key = font.getTextureId();
      TextBatchManager.Batch batch = batches.get(key);
      if (batch == null) {
         batch = new TextBatchManager.Batch(font);
         batches.put(key, batch);
         RenderSystem.setShaderTexture(0, font.getTextureId());
         ShaderProgram shader = RenderSystem.setShader(MsdfRenderer.MSDF_FONT_SHADER_KEY);
         shader.getUniform("Range").set(font.getAtlas().range());
         shader.getUniform("Thickness").set(thickness);
         shader.getUniform("Smoothness").set(0.5F);
         batch.begun = true;
      }

      ShaderProgram shader = RenderSystem.getShader();
      shader.getUniform("EnableFadeout").set(1);
      shader.getUniform("FadeoutStart").set(fadeoutStart);
      shader.getUniform("FadeoutEnd").set(fadeoutEnd);
      shader.getUniform("MaxWidth").set(maxWidth);
      shader.getUniform("TextPosX").set(textPosX);
      font.applyGlyphs(matrix, batch.builder, text, size, thickness * 0.5F * size, spacing, x - 0.75F, y + size * 0.7F, z, color);
   }

   public static void endFrame() {
      if (globalBegun) {
         for (TextBatchManager.Batch batch : batches.values()) {
            BuiltBuffer built = batch.builder.endNullable();
            if (built != null) {
               BufferRenderer.drawWithGlobalProgram(built);
            }
         }

         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         batches.clear();
         globalBegun = false;
      }
   }

   private static final class Batch {
      final MsdfFont font;
      final BufferBuilder builder;
      boolean begun = false;

      Batch(MsdfFont font) {
         this.font = font;
         this.builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      }
   }
}
