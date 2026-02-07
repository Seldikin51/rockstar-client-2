package moscow.rockstar.framework.shader.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.render.CustomRenderTarget;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class BlurProgram implements IMinecraft, IWindow {
   private static final Framebuffer MAIN_FBO = mc.getFramebuffer();
   public static final Supplier<CustomRenderTarget> CACHE = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
   public static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
   private final Timer timer = new Timer();
   private static KawaseBlurProgram kawaseDownProgram;
   private static KawaseBlurProgram kawaseUpProgram;
   private float blurOffset = 1.0F;
   private float blurDownscale = 0.5F;

   @Compile
   public void initShaders() {
      kawaseDownProgram = new KawaseBlurProgram(Rockstar.id("kawase_down/data"));
      kawaseUpProgram = new KawaseBlurProgram(Rockstar.id("kawase_up/data"));
   }

   public void draw() {
      if (this.timer.finished(25L)) {
         this.blurOffset = 1.0F;
         CustomRenderTarget cache = (CustomRenderTarget)CACHE.get();
         CustomRenderTarget buffer = (CustomRenderTarget)BUFFER.get();
         cache.setDownscale(this.blurDownscale).setLinear();
         buffer.setDownscale(this.blurDownscale).setLinear();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         kawaseDownProgram.use();
         kawaseDownProgram.updateUniforms(this.blurOffset, MAIN_FBO.textureWidth, MAIN_FBO.textureHeight);
         cache.setup();
         MAIN_FBO.beginRead();
         RenderSystem.setShaderTexture(0, MAIN_FBO.getColorAttachment());
         this.drawQuad(0.0F, 0.0F, mw.getScaledWidth(), mw.getScaledHeight());
         cache.stop();
         CustomRenderTarget[] buffers = new CustomRenderTarget[]{cache, buffer};
         int steps = 3;

         for (int i = 1; i < 3; i++) {
            int step = i % 2;
            buffers[step].setup();
            buffers[(step + 1) % 2].beginRead();
            RenderSystem.setShaderTexture(0, buffers[(step + 1) % 2].getColorAttachment());
            kawaseDownProgram.updateUniforms(this.blurOffset, buffers[(step + 1) % 2].textureWidth, buffers[(step + 1) % 2].textureHeight);
            this.drawQuad(0.0F, 0.0F, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[(step + 1) % 2].endRead();
            buffers[step].stop();
         }

         kawaseUpProgram.use();

         for (int i = 0; i < 3; i++) {
            int step = i % 2;
            buffers[(step + 1) % 2].setup();
            buffers[step].beginRead();
            RenderSystem.setShaderTexture(0, buffers[step].getColorAttachment());
            kawaseUpProgram.updateUniforms(this.blurOffset, buffers[step].textureWidth, buffers[step].textureHeight);
            this.drawQuad(0.0F, 0.0F, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[step].endRead();
            buffers[step].stop();
         }

         MAIN_FBO.endRead();
         MAIN_FBO.beginWrite(false);
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
      }
   }

   private void drawQuad(float x, float y, float width, float height) {
      int color = -1;
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      builder.vertex(x, y, 0.0F).texture(0.0F, 1.0F).color(color);
      builder.vertex(x, y + height, 0.0F).texture(0.0F, 0.0F).color(color);
      builder.vertex(x + width, y + height, 0.0F).texture(1.0F, 0.0F).color(color);
      builder.vertex(x + width, y, 0.0F).texture(1.0F, 1.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(builder.end());
   }

   public static int getTexture() {
      return ((CustomRenderTarget)BUFFER.get()).getColorAttachment();
   }

   @Generated
   public void setBlurOffset(float blurOffset) {
      this.blurOffset = blurOffset;
   }

   @Generated
   public void setBlurDownscale(float blurDownscale) {
      this.blurDownscale = blurDownscale;
   }
}
