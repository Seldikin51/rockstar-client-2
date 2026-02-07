package moscow.rockstar.utility.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class RenderUtility implements IMinecraft, IWindow {
   public static void rotate(MatrixStack ms, float x, float y, float value) {
      ms.push();
      ms.translate(x, y, 0.0F);
      ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(value));
      ms.translate(-x, -y, 0.0F);
   }

   public static void scale(MatrixStack ms, float x, float y, float scale) {
      ms.push();
      ms.translate(x, y, 0.0F);
      ms.scale(scale, scale, 1.0F);
      ms.translate(-x, -y, 0.0F);
   }

   public static void end(MatrixStack ms) {
      ms.pop();
   }

   public static void prepareMatrices(MatrixStack matrices) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      Vec3d renderPos = Vec3d.ZERO.subtract(cameraPos);
      matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
   }

   public static void prepareMatrices(MatrixStack matrices, Vec3d pos) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      Vec3d renderPos = pos.subtract(cameraPos);
      matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
   }

   public static void setupRender3D(boolean bloomColor) {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.depthMask(false);
      if (bloomColor) {
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      } else {
         RenderSystem.defaultBlendFunc();
      }
   }

   public static void endRender3D() {
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
   }

   public static void buildBuffer(BufferBuilder builder) {
      BuiltBuffer builtBuffer = builder.endNullable();
      if (builtBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtBuffer);
      }
   }

   @Generated
   private RenderUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
