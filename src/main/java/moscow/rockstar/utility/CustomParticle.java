package moscow.rockstar.utility;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class CustomParticle implements IMinecraft {
   private Vec3d position;
   private Vec3d prevPosition;
   private Vec3d velocity;
   private float size = 3.0F;
   private final Identifier texture;
   private int age;
   private int maxAge = 100;
   private double gravityStrength = 0.04;
   private boolean alive;
   private boolean collidesWithWorld;
   private final Animation alphaAnimation;
   private final Animation sizeAnimation;
   private final ColorRGBA color;

   public CustomParticle(Vec3d position, Vec3d velocity, Identifier texture, ColorRGBA color) {
      this.position = position;
      this.prevPosition = position;
      this.velocity = velocity;
      this.texture = texture;
      this.color = color;
      this.age = 0;
      long animationDuration = this.maxAge * 5;
      this.alphaAnimation = new Animation(animationDuration, Easing.CUBIC_IN_OUT);
      this.sizeAnimation = new Animation(animationDuration, Easing.LINEAR);
      this.alive = true;
   }

   public void tick() {
      this.age++;
      if (this.age >= this.maxAge) {
         this.markDead();
      }

      this.prevPosition = this.position;
      this.position = this.position.add(this.velocity);
   }

   public void render(BufferBuilder builder, Camera camera) {
      this.alphaAnimation.update(this.isDead() ? 0.0F : 1.0F);
      this.sizeAnimation.update(this.isDead() ? 0.0F : 1.0F);
      float currentSize = 10.0F;
      ColorRGBA renderColor = ColorRGBA.fromInt(this.color.getRGB()).withAlpha(255.0F * this.alphaAnimation.getValue());
      RenderSystem.setShaderTexture(0, this.texture);
      MatrixStack matrices = new MatrixStack();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      Vec3d renderPos = this.getRenderPos(this.prevPosition, this.position);
      matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      builder.vertex(matrix, 0.0F, -currentSize, 0.0F).texture(0.0F, 1.0F).color(renderColor.getRGB());
      builder.vertex(matrix, -currentSize, -currentSize, 0.0F).texture(1.0F, 1.0F).color(renderColor.getRGB());
      builder.vertex(matrix, -currentSize, 0.0F, 0.0F).texture(1.0F, 0.0F).color(renderColor.getRGB());
      builder.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(renderColor.getRGB());
   }

   public void setPosition(double x, double y, double z) {
      this.position = new Vec3d(x, y, z);
   }

   public void setVelocity(double x, double y, double z) {
      this.velocity = new Vec3d(x, y, z);
   }

   private void markDead() {
      this.alive = false;
   }

   public boolean isDead() {
      return !this.alive;
   }

   public boolean shouldRemove() {
      return this.isDead() && this.alphaAnimation.getValue() == 0.0F;
   }

   private Vec3d getRenderPos(Vec3d prevPosition, Vec3d position) {
      double x = prevPosition.getX() + (position.getX() - prevPosition.getX()) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = prevPosition.getY() + (position.getY() - prevPosition.getY()) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = prevPosition.getZ() + (position.getZ() - prevPosition.getZ()) * getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      return new Vec3d(x, y, z);
   }

   private static float getTickDelta() {
      return mc.getRenderTickCounter().getTickDelta(false);
   }

   @Generated
   public Vec3d getPosition() {
      return this.position;
   }

   @Generated
   public Vec3d getPrevPosition() {
      return this.prevPosition;
   }

   @Generated
   public Vec3d getVelocity() {
      return this.velocity;
   }

   @Generated
   public float getSize() {
      return this.size;
   }

   @Generated
   public Identifier getTexture() {
      return this.texture;
   }

   @Generated
   public int getAge() {
      return this.age;
   }

   @Generated
   public int getMaxAge() {
      return this.maxAge;
   }

   @Generated
   public double getGravityStrength() {
      return this.gravityStrength;
   }

   @Generated
   public boolean isAlive() {
      return this.alive;
   }

   @Generated
   public boolean isCollidesWithWorld() {
      return this.collidesWithWorld;
   }

   @Generated
   public Animation getAlphaAnimation() {
      return this.alphaAnimation;
   }

   @Generated
   public Animation getSizeAnimation() {
      return this.sizeAnimation;
   }

   @Generated
   public ColorRGBA getColor() {
      return this.color;
   }

   @Generated
   public void setPosition(Vec3d position) {
      this.position = position;
   }

   @Generated
   public void setPrevPosition(Vec3d prevPosition) {
      this.prevPosition = prevPosition;
   }

   @Generated
   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }

   @Generated
   public void setSize(float size) {
      this.size = size;
   }

   @Generated
   public void setAge(int age) {
      this.age = age;
   }

   @Generated
   public void setMaxAge(int maxAge) {
      this.maxAge = maxAge;
   }

   @Generated
   public void setGravityStrength(double gravityStrength) {
      this.gravityStrength = gravityStrength;
   }

   @Generated
   public void setAlive(boolean alive) {
      this.alive = alive;
   }

   @Generated
   public void setCollidesWithWorld(boolean collidesWithWorld) {
      this.collidesWithWorld = collidesWithWorld;
   }
}
