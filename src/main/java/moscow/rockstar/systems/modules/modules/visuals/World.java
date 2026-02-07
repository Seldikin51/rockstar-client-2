package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

@ModuleInfo(name = "World", category = ModuleCategory.VISUALS, desc = "Визуальные дополнения мира")
public class World extends BaseModule {
   private final List<World.Particle> particles = new ArrayList<>();
   private final ColorSetting color = new ColorSetting(this, "color").color(Colors.ACCENT);
   private final EventListener<Render3DEvent> on3DRender = event -> {
      MatrixStack ms = event.getMatrices();
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      ms.push();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.enableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      Identifier id = Rockstar.id("textures/bloom.png");
      RenderSystem.setShaderTexture(0, id);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (World.Particle particle : this.particles) {
         Vec3d pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getTickDelta());
         float bigSize = 4.0F * particle.size;
         ms.push();
         RenderUtility.prepareMatrices(ms, pos);
         ms.multiply(camera.getRotation());
         DrawUtility.drawImage(
            ms,
            builder,
            (double)(-bigSize / 2.0F),
            (double)(-bigSize / 2.0F),
            0.0,
            (double)bigSize,
            (double)bigSize,
            this.color.getColor().withAlpha(255.0F * particle.alpha.getValue() * 0.4F)
         );
         ms.pop();
      }

      BuiltBuffer builtLinesBuffer1 = builder.endNullable();
      if (builtLinesBuffer1 != null) {
         BufferRenderer.drawWithGlobalProgram(builtLinesBuffer1);
      }

      RenderSystem.depthMask(true);
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.disableDepthTest();
      ms.pop();
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.enableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (World.Particle particle : this.particles) {
         particle.alpha.update(!particle.timer.finished(particle.liveTicks));
         Vec3d pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getTickDelta());
         Vec3d rot = Utils.getInterpolatedPos(particle.prevRot, particle.rotate, event.getTickDelta());
         ms.push();
         ms.translate(pos.add(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()));
         ms.multiply(new Quaternionf().rotationXYZ((float)rot.x, (float)rot.y, (float)rot.z));
         ms.scale(particle.size, particle.size, particle.size);
         Draw3DUtility.renderBoxInternalDiagonals(
            ms, linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), this.color.getColor().withAlpha(255.0F * particle.alpha.getValue() * 0.4F)
         );
         Draw3DUtility.renderOutlinedBox(
            ms, linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), this.color.getColor().withAlpha(205.0F * particle.alpha.getValue())
         );
         ms.pop();
      }

      BuiltBuffer builtLinesBuffer = linesBuffer.endNullable();
      if (builtLinesBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtLinesBuffer);
      }

      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   };

   @Override
   public void tick() {
      this.particles.removeIf(particlex -> particlex.alpha.getValue() == 0.0F && particlex.timer.finished(particlex.liveTicks));

      for (World.Particle particle : this.particles) {
         particle.tick();
      }

      if (this.particles.size() < 100) {
         this.particles
            .add(
               new World.Particle(
                  mc.player.getPos().add(MathUtility.random(-20.0, 20.0), MathUtility.random(0.0, 5.0), MathUtility.random(-20.0, 20.0)),
                  Vec3d.ZERO,
                  new Vec3d(MathUtility.random(-1.0, 1.0), MathUtility.random(0.0, 2.0), MathUtility.random(-1.0, 1.0)),
                  new Vec3d(MathUtility.random(-1.0, 1.0), MathUtility.random(-1.0, 1.0), MathUtility.random(-1.0, 1.0)),
                  (long)MathUtility.random(1500.0, 4500.0),
                  MathUtility.random(0.1F, 0.3F)
               )
            );
      }
   }

   static class Particle {
      Vec3d prev;
      Vec3d prevRot;
      Vec3d pos;
      Vec3d rotate;
      Vec3d motion;
      Vec3d rotateMotion;
      final long liveTicks;
      float size;
      final Timer timer = new Timer();
      final Animation alpha = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

      public Particle(Vec3d pos, Vec3d rotate, Vec3d motion, Vec3d rotateMotion, long liveTicks, float size) {
         this.pos = pos;
         this.rotate = rotate;
         this.motion = motion.multiply(0.04F);
         this.rotateMotion = rotateMotion.multiply(0.04F);
         this.liveTicks = liveTicks;
         this.size = size;
         this.prevRot = rotate;
         this.prev = pos;
         this.alpha.setDuration(1000L);
      }

      void tick() {
         this.prev = this.pos;
         this.prevRot = this.rotate;
         this.pos = this.pos.add(this.motion);
         this.rotate = this.rotate.add(this.rotateMotion);
         this.motion = this.motion.multiply(0.98);
         this.rotateMotion = this.rotateMotion.multiply(0.98);
      }
   }
}
