package moscow.rockstar.utility.render.batching.impl;

import lombok.Generated;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

public class ShadowBatching extends Batching {
   private final MatrixStack matrices;
   private final float width;
   private final float height;
   private final float softness;
   private final BorderRadius borderRadius;

   public ShadowBatching(VertexFormat vertexFormat, MatrixStack matrices, float width, float height, float softness, BorderRadius borderRadius) {
      super(vertexFormat);
      this.matrices = matrices;
      this.width = width;
      this.height = height;
      this.softness = softness;
      this.borderRadius = borderRadius;
   }

   @Override
   public void draw() {
      GlProgram rectangleProgram = DrawUtility.rectangleProgram;
      rectangleProgram.use();
      rectangleProgram.findUniform("Size").set(this.width, this.height);
      rectangleProgram.findUniform("Radius")
         .set(
            this.borderRadius.topLeftRadius() * 3.0F,
            this.borderRadius.bottomLeftRadius() * 3.0F,
            this.borderRadius.topRightRadius() * 3.0F,
            this.borderRadius.bottomRightRadius() * 3.0F
         );
      rectangleProgram.findUniform("Smoothness").set(this.softness);
      DrawUtility.drawSetup();
      this.build();
      DrawUtility.drawEnd();
      if (active == this) {
         active = null;
      }
   }

   @Generated
   public MatrixStack getMatrices() {
      return this.matrices;
   }
}
