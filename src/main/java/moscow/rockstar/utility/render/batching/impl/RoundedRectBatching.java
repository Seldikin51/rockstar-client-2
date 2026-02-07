package moscow.rockstar.utility.render.batching.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class RoundedRectBatching extends Batching {
   private final GlProgram rectangleProgram;
   private float smoothness = 0.5F;

   public RoundedRectBatching() {
      super(VertexFormats.POSITION_COLOR);
      this.rectangleProgram = DrawUtility.rectangleProgram;
   }

   public RoundedRectBatching smoothness(float s) {
      this.smoothness = s;
      return this;
   }

   @Override
   public void draw() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      this.rectangleProgram.use();
      this.rectangleProgram.findUniform("Smoothness").set(this.smoothness);
      BuiltBuffer built = this.getBuilder().endNullable();
      if (built != null) {
         BufferRenderer.drawWithGlobalProgram(built);
      }

      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      if (active == this) {
         active = null;
      }
   }

   public void add(Matrix4f matrix, float x, float y, float width, float height, float radiusTL, float radiusBL, float radiusTR, float radiusBR, int rgba) {
      this.rectangleProgram.findUniform("Size").set(width, height);
      this.rectangleProgram.findUniform("Radius").set(radiusTL, radiusBL, radiusTR, radiusBR);
      float horizontalPadding = -this.smoothness / 2.0F + this.smoothness * 2.0F;
      float verticalPadding = this.smoothness / 2.0F + this.smoothness;
      float ax = x - horizontalPadding / 2.0F;
      float ay = y - verticalPadding / 2.0F;
      float aw = width + horizontalPadding;
      float ah = height + verticalPadding;
      this.getBuilder().vertex(matrix, ax, ay, 0.0F).color(rgba);
      this.getBuilder().vertex(matrix, ax, ay + ah, 0.0F).color(rgba);
      this.getBuilder().vertex(matrix, ax + aw, ay + ah, 0.0F).color(rgba);
      this.getBuilder().vertex(matrix, ax + aw, ay, 0.0F).color(rgba);
   }
}
