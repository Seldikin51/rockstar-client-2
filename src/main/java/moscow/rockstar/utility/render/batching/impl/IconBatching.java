package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

public class IconBatching extends Batching {
   private final MatrixStack matrices;

   public IconBatching(VertexFormat vertexFormat, MatrixStack matrices) {
      super(vertexFormat);
      this.matrices = matrices;
   }

   @Override
   public void draw() {
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.enableBlend();
      this.build();
      DrawUtility.drawEnd();
      RenderSystem.setShaderTexture(0, 0);
      if (active == this) {
         active = null;
      }
   }

   @Generated
   public MatrixStack getMatrices() {
      return this.matrices;
   }
}
