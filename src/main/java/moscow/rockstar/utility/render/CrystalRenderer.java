package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CrystalRenderer {
   private static final Vector3f[] VERTICES = new Vector3f[]{
      new Vector3f(0.0F, 1.5F, 0.0F),
      new Vector3f(0.0F, -1.5F, 0.0F),
      new Vector3f(1.0F, 0.0F, 0.0F),
      new Vector3f(-1.0F, 0.0F, 0.0F),
      new Vector3f(0.0F, 0.0F, 1.0F),
      new Vector3f(0.0F, 0.0F, -1.0F)
   };
   private static final int[][] FACES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
   private static final float[] FACE_BRIGHTNESS = new float[]{1.0F, 0.8F, 0.6F, 0.9F, 0.7F, 0.5F, 0.4F, 0.6F};

   public static void render(MatrixStack matrices, BufferBuilder buffer, float x, float y, float z, float size, ColorRGBA color) {
      matrices.push();
      matrices.translate(x, y, z);
      matrices.scale(size, size, size);
      Matrix4f transformationMatrix = matrices.peek().getPositionMatrix();

      for (int i = 0; i < FACES.length; i++) {
         int[] face = FACES[i];
         float brightness = FACE_BRIGHTNESS[i];
         Vector3f v1 = VERTICES[face[0]];
         Vector3f v2 = VERTICES[face[1]];
         Vector3f v3 = VERTICES[face[2]];
         int shadedColor = applyBrightness(color.getRGB(), brightness);
         buffer.vertex(transformationMatrix, v1.x, v1.y, v1.z).color(shadedColor);
         buffer.vertex(transformationMatrix, v2.x, v2.y, v2.z).color(shadedColor);
         buffer.vertex(transformationMatrix, v3.x, v3.y, v3.z).color(shadedColor);
      }

      matrices.pop();
   }

   public static BufferBuilder createBuffer() {
      setupRenderState();
      return Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
   }

   private static void setupRenderState() {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static int applyBrightness(int color, float brightness) {
      int alpha = color >> 24 & 0xFF;
      int red = (int)((color >> 16 & 0xFF) * brightness);
      int green = (int)((color >> 8 & 0xFF) * brightness);
      int blue = (int)((color & 0xFF) * brightness);
      red = Math.min(255, Math.max(0, red));
      green = Math.min(255, Math.max(0, green));
      blue = Math.min(255, Math.max(0, blue));
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   @Generated
   private CrystalRenderer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
