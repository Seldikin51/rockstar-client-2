package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@ModuleInfo(name = "Target ESP", category = ModuleCategory.VISUALS, desc = "Помечает активную цель")
public class TargetESP extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.target_esp.mode");
   private final ModeSetting.Value souls = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.souls");
   private final ModeSetting.Value crystals = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.crystals").select();
   private final ColorSetting colorTarget = new ColorSetting(this, "color").color(Colors.ACCENT);
   private final Animation animation = new Animation(300L, 0.0F, Easing.BOTH_CUBIC);
   private final Animation moving = new Animation(70L, 0.0F, Easing.LINEAR);
   private LivingEntity prevTarget;
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (EntityUtility.isInGame()) {
         LivingEntity target = Rockstar.getInstance().getTargetManager().getCurrentTarget() instanceof LivingEntity target2 ? target2 : null;
         this.animation.setEasing(Easing.FIGMA_EASE_IN_OUT);
         this.animation.update(target != null);
         this.moving.update(this.moving.getValue() + 10.0F + 50.0F);
         if (target != null) {
            this.prevTarget = target;
         }

         if (this.prevTarget != null && this.animation.getValue() != 0.0F) {
            MatrixStack ms = event.getMatrices();
            ms.push();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            RenderSystem.enableDepthTest();
            if (mc.world
                  .raycast(
                     new RaycastContext(mc.gameRenderer.getCamera().getPos(), this.prevTarget.getEyePos(), ShapeType.COLLIDER, FluidHandling.NONE, mc.player)
                  )
                  .getType()
               != Type.MISS) {
               RenderSystem.disableDepthTest();
            }

            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            if (this.crystals.isSelected()) {
               this.drawCrystals(ms, this.prevTarget);
            } else {
               this.drawGhosts(ms, this.prevTarget);
            }

            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
            ms.pop();
         }
      }
   };

   private void drawCrystals(MatrixStack ms, LivingEntity target) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      ColorRGBA color = this.colorTarget.getColor();
      float width = this.prevTarget.getWidth() * 1.5F;
      RenderUtility.prepareMatrices(ms, this.getRenderPos(this.prevTarget));
      BufferBuilder builder = CrystalRenderer.createBuffer();

      for (int i = 0; i < 360; i += 20) {
         float val = 1.2F - 0.5F * this.animation.getValue();
         float sin = (float)(MathUtility.sin((float)Math.toRadians(i + this.moving.getValue() * 0.3F)) * width * val);
         float cos = (float)(MathUtility.cos((float)Math.toRadians(i + this.moving.getValue() * 0.3F)) * width * val);
         float size = 0.1F;
         ms.push();
         ms.translate(sin, 0.1F + target.getHeight() * Math.abs(MathUtility.sin(i)), cos);
         Vec3d crystalPos = this.getRenderPos(this.prevTarget).add(sin, 1.0, cos);
         Vec3d targetPos = target.getPos().add(0.0, target.getHeight() / 2.0, 0.0);
         Vector3f directionToTarget = new Vector3f(
               (float)(targetPos.x - crystalPos.x), (float)(targetPos.y - crystalPos.y), (float)(targetPos.z - crystalPos.z)
            )
            .normalize();
         Vector3f initialDirection = new Vector3f(0.0F, 1.0F, 0.0F);
         Quaternionf rotation = new Quaternionf().rotationTo(initialDirection, directionToTarget);
         ms.multiply(rotation);
         CrystalRenderer.render(ms, builder, 0.0F, 0.0F, 0.0F, size, color.withAlpha(255.0F * this.animation.getValue()));
         ms.pop();
      }

      BufferRenderer.drawWithGlobalProgram(builder.end());
      Identifier id = Rockstar.id("textures/bloom.png");
      RenderSystem.setShaderTexture(0, id);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      float bigSize = 1.0F;

      for (int i = 0; i < 360; i += 20) {
         float val = 1.2F - 0.5F * this.animation.getValue();
         float sin = (float)(MathUtility.sin((float)Math.toRadians(i + this.moving.getValue() * 0.3F)) * width * val);
         float cos = (float)(MathUtility.cos((float)Math.toRadians(i + this.moving.getValue() * 0.3F)) * width * val);
         float size = 0.1F;
         ms.push();
         ms.translate(sin, 0.1F + target.getHeight() * Math.abs(MathUtility.sin(i)), cos);
         ms.multiply(camera.getRotation());
         DrawUtility.drawImage(
            ms,
            buffer,
            (double)(-bigSize / 2.0F),
            (double)(-bigSize / 2.0F),
            0.0,
            (double)bigSize,
            (double)bigSize,
            color.withAlpha(255.0F * this.animation.getValue() * 0.2F)
         );
         ms.pop();
      }

      RenderUtility.buildBuffer(buffer);
   }

   private void drawGhosts(MatrixStack ms, LivingEntity target) {
      Camera camera = mc.gameRenderer.getCamera();
      ColorRGBA color = this.colorTarget.getColor();
      Identifier id = Rockstar.id("textures/bloom.png");
      float width = this.prevTarget.getWidth() * 1.5F;
      RenderSystem.setShaderTexture(0, id);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      RenderUtility.prepareMatrices(ms, this.getRenderPos(this.prevTarget));
      int step = 2;
      int wormTick = 0;
      int wormCD = 0;
      int wormCount = 0;

      for (int i = 0; i < 360; i += step) {
         float size = 0.13F + 0.005F * wormTick;
         float bigSize = 0.7F + 0.005F * wormTick;
         if (wormCD > 0) {
            wormCD -= step;
         } else {
            wormTick += step;
            if (wormTick > 50) {
               wormCD = 100;
               wormTick = 0;
               wormCount++;
            } else {
               float val = Math.max(0.5F, 1.2F - 0.5F * this.animation.getValue());
               float sin = (float)(MathUtility.sin((float)Math.toRadians(i + this.moving.getValue() * 1.0F)) * width * val);
               float cos = (float)(MathUtility.cos((float)Math.toRadians(i + this.moving.getValue() * 1.0F)) * width * val);
               ms.push();
               ms.translate(
                  sin,
                  this.prevTarget.getHeight() / 1.5F
                     + this.prevTarget.getHeight() / 3.0F * MathUtility.sin(Math.toRadians(i / 2.0F + this.moving.getValue() / 5.0F)),
                  cos
               );
               ms.multiply(camera.getRotation());
               DrawUtility.drawImage(
                  ms,
                  builder,
                  (double)(-bigSize / 2.0F),
                  (double)(-bigSize / 2.0F),
                  (double)(-size / 2.0F),
                  (double)bigSize,
                  (double)bigSize,
                  color.withAlpha(color.getAlpha() * this.animation.getValue() * 0.05F)
               );
               DrawUtility.drawImage(
                  ms,
                  builder,
                  (double)(-size / 2.0F),
                  (double)(-size / 2.0F),
                  (double)(-size / 2.0F),
                  (double)size,
                  (double)size,
                  color.withAlpha(color.getAlpha() * this.animation.getValue())
               );
               ms.pop();
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(builder.end());
   }

   private Vec3d getRenderPos(LivingEntity target) {
      float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
      return new Vec3d(
         MathHelper.lerp(tickDelta, target.prevX, target.getX()),
         MathHelper.lerp(tickDelta, target.prevY, target.getY()),
         MathHelper.lerp(tickDelta, target.prevZ, target.getZ())
      );
   }

   @Override
   public void tick() {
      super.tick();
   }
}
