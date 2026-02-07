package moscow.rockstar.utility.render.batching.impl;

import lombok.Generated;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;

public class BlurBatching extends Batching {
   private final MatrixStack matrices;
   private final float width;
   private final float height;

   public BlurBatching(VertexFormat vertexFormat, MatrixStack matrices, float width, float height) {
      super(vertexFormat);
      this.matrices = matrices;
      this.width = width;
      this.height = height;
   }

   @Override
   public void draw() {
      if (active == this) {
         active = null;
      }
   }

   @Generated
   public MatrixStack getMatrices() {
      return this.matrices;
   }
}
