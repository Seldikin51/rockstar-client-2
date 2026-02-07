package moscow.rockstar.utility.game.countermine;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AntiAim implements IMinecraft {
   public static boolean FORCE;
   public float yaw;
   public float pitch;
   private BooleanSetting antiAim;
   private BooleanSetting freestand;
   private ModeSetting mode;
   private ModeSetting.Value statich;
   private ModeSetting.Value fake;
   private CounterMine counterMine;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.antiAim.isEnabled() && this.counterMine.getJumping().finished(1000L)) {
         float fromYaw = RageBot.TARGET_YAW;
         int age = mc.player.age;
         this.yaw = this.statich.isSelected() ? fromYaw - 180.0F : fromYaw - 90.0F - (age % 5 == 0 ? 0 : (age % 5 == 1 ? 180 : 90));
         if (this.freestand.isEnabled()) {
            Vec3d eye = mc.player.getEyePos();
            float lfx = (float)MathUtility.cos(Math.toRadians(fromYaw));
            float lfz = (float)MathUtility.sin(Math.toRadians(fromYaw));
            float ltx = (float)(MathUtility.cos(Math.toRadians(fromYaw)) + MathUtility.cos(Math.toRadians(fromYaw + 90.0F)));
            float ltz = (float)(MathUtility.sin(Math.toRadians(fromYaw)) + MathUtility.sin(Math.toRadians(fromYaw + 90.0F)));
            float rfx = (float)MathUtility.cos(Math.toRadians(fromYaw - 180.0F));
            float rfz = (float)MathUtility.sin(Math.toRadians(fromYaw - 180.0F));
            float rtx = (float)(MathUtility.cos(Math.toRadians(fromYaw - 180.0F)) + MathUtility.cos(Math.toRadians(fromYaw + 90.0F)));
            float rtz = (float)(MathUtility.sin(Math.toRadians(fromYaw - 180.0F)) + MathUtility.sin(Math.toRadians(fromYaw + 90.0F)));
            boolean left = mc.world
                  .raycast(new RaycastContext(eye.add(lfx, 0.0, lfz), eye.add(ltx, 0.0, ltz), ShapeType.VISUAL, FluidHandling.NONE, mc.player))
                  .getType()
               == Type.MISS;
            boolean right = mc.world
                  .raycast(new RaycastContext(eye.add(rfx, 0.0, rfz), eye.add(rtx, 0.0, rtz), ShapeType.VISUAL, FluidHandling.NONE, mc.player))
                  .getType()
               == Type.MISS;
            if (left != right) {
               if (left) {
                  this.yaw = this.statich.isSelected() ? fromYaw - 270.0F : fromYaw - 270.0F - (age % 5 == 0 ? 180 : 0);
               } else {
                  this.yaw = this.statich.isSelected() ? fromYaw - 90.0F : fromYaw - 90.0F - (age % 5 == 0 ? 180 : 0);
               }
            }
         }

         this.pitch = 90.0F;
         mc.player
            .networkHandler
            .sendPacket(
               new Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), this.yaw, this.pitch, mc.player.isOnGround(), mc.player.horizontalCollision)
            );
      }
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      if (mc.world != null && mc.player != null && this.antiAim.isEnabled()) {
         MatrixStack ms = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         ms.push();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         this.renderTransparentPlayer(ms, camera, event.getTickDelta());
         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
         ms.pop();
      }
   };

   public AntiAim(CounterMine cm) {
      this.counterMine = cm;
      this.antiAim = new BooleanSetting(cm, "AntAim");
      this.mode = new ModeSetting(cm, "AA Mode", () -> !this.antiAim.isEnabled());
      this.statich = new ModeSetting.Value(this.mode, "Static");
      this.fake = new ModeSetting.Value(this.mode, "Fake");
      this.freestand = new BooleanSetting(cm, "AA FreeStand", () -> !this.antiAim.isEnabled());
   }

   public void renderTransparentPlayer(MatrixStack matrices, Camera camera, float tickDelta) {
      if (mc.player != null && mc.options.getPerspective() != Perspective.FIRST_PERSON) {
         Vec3d playerPos = mc.player.getPos();
         Vec3d renderPos = Utils.getInterpolatedPos(mc.player, tickDelta);
         Vec3d cameraPos = camera.getPos();
         matrices.push();
         double x = renderPos.x - cameraPos.x;
         double y = renderPos.y - cameraPos.y;
         double z = renderPos.z - cameraPos.z;
         ColorRGBA c = Colors.ACCENT;
         RenderSystem.enableBlend();
         RenderSystem.setShaderColor(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 0.5F);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
         PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer)dispatcher.getRenderer(mc.player);
         if (playerRenderer != null) {
            float originalYaw = mc.player.getYaw();
            float originalPitch = mc.player.getPitch();
            float originalHeadYaw = mc.player.headYaw;
            float originalBodyYaw = mc.player.bodyYaw;
            float originalPrevYaw = mc.player.prevYaw;
            float originalPrevPitch = mc.player.prevPitch;
            float originalPrevHeadYaw = mc.player.prevHeadYaw;
            float originalPrevBodyYaw = mc.player.prevBodyYaw;
            mc.player.setYaw(this.yaw);
            mc.player.setPitch(this.pitch);
            mc.player.headYaw = this.yaw;
            mc.player.bodyYaw = this.yaw;
            mc.player.prevYaw = this.yaw;
            mc.player.prevPitch = this.pitch;
            mc.player.prevHeadYaw = this.yaw;
            mc.player.prevBodyYaw = this.yaw;
            FORCE = true;

            try {
               dispatcher.render(mc.player, x, y, z, tickDelta, matrices, mc.getBufferBuilders().getEntityVertexConsumers(), 15728880, playerRenderer);
               mc.getBufferBuilders().getEntityVertexConsumers().draw();
            } catch (Exception var25) {
            }

            FORCE = false;
            mc.player.setYaw(originalYaw);
            mc.player.setPitch(originalPitch);
            mc.player.headYaw = originalHeadYaw;
            mc.player.bodyYaw = originalBodyYaw;
            mc.player.prevYaw = originalPrevYaw;
            mc.player.prevPitch = originalPrevPitch;
            mc.player.prevHeadYaw = originalPrevHeadYaw;
            mc.player.prevBodyYaw = originalPrevBodyYaw;
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }

   @Generated
   public BooleanSetting getAntiAim() {
      return this.antiAim;
   }

   @Generated
   public BooleanSetting getFreestand() {
      return this.freestand;
   }
}
