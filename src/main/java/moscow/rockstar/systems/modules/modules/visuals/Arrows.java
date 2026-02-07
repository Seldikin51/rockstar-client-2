package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Arrows", category = ModuleCategory.VISUALS, desc = "modules.descriptions.tracers")
public class Arrows extends BaseModule {
   private final BooleanSetting lines = new BooleanSetting(this, "lines");
   private final SelectSetting targets = new SelectSetting(this, "modules.settings.tracers.targets", "modules.settings.tracers.targets.description");
   private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.players").select();
   private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.animals");
   private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.mobs");
   private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.invisibles").select();
   private final SelectSetting.Value nakedPlayers = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.naked_players").select();
   private final SelectSetting.Value friends = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.friends").select();
   private final Map<Entity, Arrows.ArrowsAnimation> animations = new HashMap<>();
   private final EventListener<HudRenderEvent> onHud = event -> {
      if (mc.player != null && mc.world != null && !this.lines.isEnabled()) {
         CustomDrawContext context = event.getContext();
         MatrixStack ms = context.getMatrices();
         TargetSettings targetSettings = new TargetSettings.Builder()
            .targetPlayers(this.players.isSelected())
            .targetAnimals(this.animals.isSelected())
            .targetInvisibles(this.invisibles.isSelected())
            .targetFriends(this.friends.isSelected())
            .targetNakedPlayers(this.nakedPlayers.isSelected())
            .targetMobs(this.mobs.isSelected())
            .build();
         Set<Entity> toRemove = new HashSet<>();

         for (Entry<Entity, Arrows.ArrowsAnimation> entry : this.animations.entrySet()) {
            Entity entity = entry.getKey();
            Arrows.ArrowsAnimation animation = entry.getValue();
            boolean shouldShow = mc.world.hasEntity(entity) && entity instanceof LivingEntity livingEntity && targetSettings.isEntityValid(livingEntity);
            animation.showing.update(shouldShow);
            animation.showing.setDuration(500L);
            if (animation.showing.getValue() == 0.0F && !shouldShow) {
               toRemove.add(entity);
            }
         }

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && targetSettings.isEntityValid(livingEntity) && !this.animations.containsKey(entity)) {
               this.animations.put(entity, new Arrows.ArrowsAnimation());
            }
         }

         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         ms.push();
         IconBatching iconBatching = new IconBatching(VertexFormats.POSITION_TEXTURE_COLOR, context.getMatrices());
         ms.translate(sr.getScaledWidth() / 2.0F, sr.getScaledHeight() / 2.0F, 0.0F);

         for (Entry<Entity, Arrows.ArrowsAnimation> arrow : this.animations.entrySet()) {
            if (arrow.getValue().showing.getValue() > 0.0F) {
               RenderUtility.rotate(ms, 0.0F, 0.0F, this.calculateAngle(arrow.getKey(), event.getTickDelta()));
               RenderUtility.scale(ms, 0.0F, 0.0F, 2.0F - arrow.getValue().showing.getValue());
               context.drawTexture(
                  Rockstar.id("textures/arrow.png"),
                  -10.0F,
                  40.0F,
                  20.0F,
                  20.0F,
                  (Rockstar.getInstance().getFriendManager().isFriend(arrow.getKey().getName().getString()) ? Colors.GREEN : Colors.ACCENT)
                     .mulAlpha(arrow.getValue().showing.getValue())
               );
               RenderUtility.end(ms);
               RenderUtility.end(ms);
            }
         }

         iconBatching.draw();

         for (Entity entityx : toRemove) {
            this.animations.remove(entityx);
         }

         ms.pop();
         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
      }
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      if (mc.player != null && mc.world != null && this.lines.isEnabled()) {
         MatrixStack matrices = event.getMatrices();
         TargetSettings targetSettings = new TargetSettings.Builder()
            .targetPlayers(this.players.isSelected())
            .targetAnimals(this.animals.isSelected())
            .targetInvisibles(this.invisibles.isSelected())
            .targetFriends(this.friends.isSelected())
            .targetNakedPlayers(this.nakedPlayers.isSelected())
            .targetMobs(this.mobs.isSelected())
            .build();
         RenderUtility.setupRender3D(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && targetSettings.isEntityValid(livingEntity)) {
               Vec3d entityPos = Utils.getInterpolatedPos(livingEntity, event.getTickDelta());
               Draw3DUtility.renderLineFromPlayer(matrices, builder, entityPos.add(0.0, livingEntity.getHeight() / 2.0F, 0.0), Colors.WHITE);
            }
         }

         RenderUtility.buildBuffer(builder);
         RenderUtility.endRender3D();
      }
   };

   private float calculateAngle(Entity entity, float partialTicks) {
      Vec3d pos = Utils.getInterpolatedPos(entity, partialTicks).subtract(mc.gameRenderer.getCamera().getPos());
      double cos = MathHelper.cos((float)(mc.gameRenderer.getCamera().getYaw() * (Math.PI / 180.0)));
      double sin = MathHelper.sin((float)(mc.gameRenderer.getCamera().getYaw() * (Math.PI / 180.0)));
      double rotY = -(pos.z * cos - pos.x * sin);
      double rotX = -(pos.x * cos + pos.z * sin);
      return (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI - 90.0);
   }

   static class ArrowsAnimation {
      Animation showing = new Animation(300L, Easing.BAKEK);
      Animation rotating = new Animation(300L, Easing.BAKEK);
   }
}
