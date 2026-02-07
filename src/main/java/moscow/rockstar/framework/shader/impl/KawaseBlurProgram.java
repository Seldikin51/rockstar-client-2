package moscow.rockstar.framework.shader.impl;

import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.interfaces.IWindow;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class KawaseBlurProgram extends GlProgram implements IWindow {
   private GlUniform resolutionUniform;
   private GlUniform offsetUniform;
   private GlUniform saturationUniform;
   private GlUniform tintIntensityUniform;
   private GlUniform tintColorUniform;

   public KawaseBlurProgram(Identifier identifier) {
      super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
   }

   @CompileBytecode
   public void updateUniforms(float offset) {
      this.offsetUniform.set(offset);
      this.resolutionUniform.set(1.0F / mw.getScaledWidth(), 1.0F / mw.getScaledHeight());
      this.saturationUniform.set(1.0F);
      this.tintIntensityUniform.set(0.0F);
      this.tintColorUniform.set(1.0F, 1.0F, 1.0F);
   }

   public void updateUniforms(float offset, int textureWidth, int textureHeight) {
      this.offsetUniform.set(offset);
      float invW = textureWidth > 0 ? 1.0F / textureWidth : 0.0F;
      float invH = textureHeight > 0 ? 1.0F / textureHeight : 0.0F;
      this.resolutionUniform.set(invW, invH);
      this.saturationUniform.set(1.0F);
      this.tintIntensityUniform.set(0.0F);
      this.tintColorUniform.set(1.0F, 1.0F, 1.0F);
   }

   @Override
   protected void setup() {
      this.resolutionUniform = this.findUniform("Resolution");
      this.offsetUniform = this.findUniform("Offset");
      this.saturationUniform = this.findUniform("Saturation");
      this.tintIntensityUniform = this.findUniform("TintIntensity");
      this.tintColorUniform = this.findUniform("TintColor");
      super.setup();
   }
}
