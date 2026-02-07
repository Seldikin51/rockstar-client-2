package moscow.rockstar.systems.modules.modules.combat;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.movement.ElytraStrafe;
import moscow.rockstar.systems.modules.modules.player.Blink;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.ElytraUtility;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Elytra Target", category = ModuleCategory.COMBAT, desc = "Позволяет преследовать игроков на элитре")
public class ElytraTarget extends BaseModule {
   private final SliderSetting fireworkSlot = new SliderSetting(this, "modules.settings.elytra_target.fireworkSlot")
      .min(1.0F)
      .max(9.0F)
      .step(1.0F)
      .currentValue(7.0F)
      .suffix(" slot");
   private final BooleanSetting swapVector = new BooleanSetting(this, "modules.settings.elytra_target.swapVector").enable();
   private final BooleanSetting defensive = new BooleanSetting(this, "modules.settings.elytra_target.defensive").enable();
   private boolean defensiveActive;
   private boolean prevDefensive;
   private Vec3d defensivePos;
   private final EventListener<SendPacketEvent> onPacket = event -> {
      if (this.defensiveActive) {
         this.blink().savePacket(event);
      }
   };
   private final EventListener<AttackEvent> onAttack = event -> {};
   private final EventListener<PostAttackEvent> onPostAttack = event -> {
      if (CombatUtility.getMace() != null) {
         ElytraUtility.swapInHotbar(false);
         if (mc.player.isSprinting() && mc.player.input.hasForwardMovement() && mc.player.checkGliding()) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         }
      }

      LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
      ElytraUtility.setLastVec(ElytraUtility.leaveVec(target));
      ElytraUtility.useFirework(this.fireworkSlot.getCurrentValue());
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      MatrixStack matrices = event.getMatrices();
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
         if (mc.player != player) {
            ElytraUtility.drawBoxes(
               matrices,
               linesBuffer,
               player.getBoundingBox()
                  .offset(ElytraPredictionSystem.predictPlayerPosition(player))
                  .offset(-player.getX(), -player.getY(), -player.getZ())
                  .offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()),
               Colors.ACCENT.withAlpha(100.0F)
            );
         }
      }

      if (this.defensivePos != null) {
         ElytraUtility.drawBoxes(
            matrices,
            linesBuffer,
            mc.player
               .getBoundingBox()
               .offset(this.defensivePos)
               .offset(-mc.player.getX(), -mc.player.getY(), -mc.player.getZ())
               .offset(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()),
            Colors.ACCENT.withAlpha(200.0F)
         );
      }

      BuiltBuffer builtLinesBuffer = linesBuffer.endNullable();
      if (builtLinesBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtLinesBuffer);
      }

      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   };
   private final EventListener<HudRenderEvent> on2DRender = event -> {};

   @Override
   public void tick() {
      Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
      LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
      if (!Rockstar.getInstance().getModuleManager().getModule(ElytraStrafe.class).isEnabled()
         && ElytraUtility.getFireworkTimer().finished(target != null && mc.player.distanceTo(target) < 6.0F ? 500L : (target != null ? 1000L : 1500L))
         && mc.player.isGliding()
         && !mc.player.isUsingItem()) {
         ElytraUtility.useFirework(this.fireworkSlot.getCurrentValue());
      }

      if (mc.player.isGliding() && target != null) {
         RotationHandler handler = Rockstar.getInstance().getRotationHandler();
         Rotation rot = RotationMath.getRotationTo(
            ElytraUtility.leaving() ? target.getEyePos().add(ElytraUtility.leaveVec(target)) : ElytraUtility.targetPoint(target)
         );
         handler.rotate(rot, MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.OVERRIDE);
      }

      if (InventoryUtility.getChestplateSlot().item() == Items.ELYTRA
         && mc.player.isSprinting()
         && mc.player.input.hasForwardMovement()
         && mc.player.checkGliding()) {
         mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
      }

      this.defensiveActive = this.defensive.isEnabled()
         && target instanceof PlayerEntity player
         && !ElytraPredictionSystem.isLeaving(player)
         && player.isGliding()
         && !ElytraUtility.leaving()
         && mc.player.distanceTo(target) > 10.0F;
      if (this.defensiveActive != this.prevDefensive) {
         if (this.defensiveActive) {
            this.blink().onEnable();
            this.defensivePos = mc.player.getPos();
         } else {
            this.blink().onDisable();
            this.defensivePos = null;
         }
      }

      this.prevDefensive = this.defensiveActive;
      if (CombatUtility.getMace() != null) {
         ElytraUtility.swapInHotbar(
            target != null && mc.player.distanceTo(target) < 10.0F && mc.player.getAttackCooldownProgress(0.0F) >= 1.0F && mc.player.fallDistance > 5.0F
         );
      }
   }

   private Blink blink() {
      return Rockstar.getInstance().getModuleManager().getModule(Blink.class);
   }

   @Override
   public void onDisable() {
      this.defensiveActive = false;
   }

   @Generated
   public BooleanSetting getSwapVector() {
      return this.swapVector;
   }

   @Generated
   public boolean isDefensiveActive() {
      return this.defensiveActive;
   }

   @Generated
   public boolean isPrevDefensive() {
      return this.prevDefensive;
   }
}
