package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.gl.Framebuffer;

public class CustomRenderTarget extends Framebuffer implements IMinecraft, IWindow {
   private boolean linear;
   private float downscale = 1.0F;

   public CustomRenderTarget(boolean useDepth) {
      super(useDepth);
   }

   public CustomRenderTarget(int width, int height, boolean useDepth) {
      super(useDepth);
      this.resize(width, height);
   }

   public CustomRenderTarget setLinear() {
      this.linear = true;
      RenderSystem.recordRenderCall(() -> this.setTexFilter(9729));
      return this;
   }

   public CustomRenderTarget setDownscale(float factor) {
      this.downscale = Math.max(0.1F, Math.min(1.0F, factor));
      return this;
   }

   public void setTexFilter(int texFilter) {
      super.setTexFilter(this.linear ? 9729 : texFilter);
   }

   private void resizeFramebuffer() {
      if (this.needsNewFramebuffer()) {
         int targetWidth = Math.max((int)Math.floor(mw.getScaledWidth() * this.downscale), 1);
         int targetHeight = Math.max((int)Math.floor(mw.getScaledHeight() * this.downscale), 1);
         this.initFbo(targetWidth, targetHeight);
      }
   }

   public void setup(boolean clear) {
      this.resizeFramebuffer();
      if (clear) {
         this.clear();
      }

      this.beginWrite(false);
   }

   public void setup() {
      this.setup(true);
   }

   public void stop() {
      this.endWrite();
      mc.getFramebuffer().beginWrite(true);
   }

   private boolean needsNewFramebuffer() {
      int targetWidth = Math.max((int)Math.floor(mw.getScaledWidth() * this.downscale), 1);
      int targetHeight = Math.max((int)Math.floor(mw.getScaledHeight() * this.downscale), 1);
      return this.textureWidth != targetWidth || this.textureHeight != targetHeight;
   }
}
