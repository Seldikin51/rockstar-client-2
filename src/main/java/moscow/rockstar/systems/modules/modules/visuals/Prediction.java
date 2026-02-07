package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.shared.PredicateValue;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@ModuleInfo(name = "Prediction", category = ModuleCategory.VISUALS)
public class Prediction extends BaseModule {
   private final List<Prediction.Predicted> predicted = new ArrayList<>();
   private final List<Prediction.Landed> landed = new ArrayList<>();
   private final SelectSetting entities = new SelectSetting(this, "modules.settings.prediction.entities");
   private final ModeSetting renderMode = new ModeSetting(this, "modules.settings.prediction.render_mode");
   private final ModeSetting.Value defaultMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.default");
   private final ModeSetting.Value glowMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.glow").select();
   private final BooleanSetting inHand = new BooleanSetting(this, "modules.settings.prediction.hand").enable();
   private final BooleanSetting walls = new BooleanSetting(this, "modules.settings.prediction.walls").enable();
   private final BooleanSetting hud = new BooleanSetting(this, "modules.settings.prediction.hud");
   private final EventListener<HudRenderEvent> onRender2D = event -> {
      CustomDrawContext context = event.getContext();
      MatrixStack ms = context.getMatrices();

      for (Prediction.Predicted predict : this.predicted) {
         Vec2f screenPos = Utils.worldToScreen(predict.vectors.getLast());
         if (screenPos != null) {
            float x = screenPos.x;
            float y = screenPos.y;
            Font font = Fonts.MEDIUM.getFont(13.0F);
            float height = font.height() + 6.0F;
            float yOff = -height;
            String name = predict.entity.getName().getString().replace("Брошенный эндер-жемчуг", "Эндер-жемчуг");
            if (predict.entity instanceof PotionEntity potion) {
               name = potion.getStack().getFormattedName().getString();
            }

            name = name.replace("] ", "").replace("[", "") + String.format(" (%s сек)", TextUtility.formatNumber(predict.ticks / 20.0F));

            ItemStack stack = switch (predict.entity) {
               case ThrownItemEntity item -> item.getStack();
               case PersistentProjectileEntity itemx -> itemx.getItemStack();
               case ItemEntity itemxx -> itemxx.getStack();
               default -> Items.ARROW.getDefaultStack();
            };
            float distance = (float)predict.vectors.getLast().distanceTo(mc.player.getEyePos());
            float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
            ms.push();
            ms.translate(x, y, 0.0F);
            ms.scale(scale, scale, 1.0F);
            float firstWidth = font.width(name) + 20.0F;
            context.drawRect(-firstWidth / 2.0F, yOff, firstWidth, height, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
            context.drawItem(stack, -firstWidth / 2.0F, yOff, 1.0F);
            context.drawText(font, name, -firstWidth / 2.0F + 17.0F, yOff + 3.0F, Colors.WHITE);
            yOff += height;
            if (predict.entity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof AbstractClientPlayerEntity player) {
               String owner = "От " + (projectile.getOwner() == mc.player ? "Вас" : projectile.getOwner().getName().getString());
               float secondWidth = font.width(owner) + 22.0F;
               context.drawRect(-secondWidth / 2.0F, yOff, secondWidth, height, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
               context.drawHead(player, -secondWidth / 2.0F, yOff, height, BorderRadius.ZERO, Colors.WHITE);
               context.drawText(font, owner, -secondWidth / 2.0F + 19.0F, yOff + 3.0F, Colors.WHITE);
               yOff += height;
            }

            if (predict.entity instanceof PotionEntity potion) {
               for (StatusEffectInstance effect : PotionUtility.effects(potion.getStack())) {
                  String potionName = ((StatusEffect)effect.getEffectType().value()).getName().getString();
                  int amplifier = effect.getAmplifier();
                  int duration = effect.getDuration();
                  String potionLevel = amplifier > 0 ? " " + (amplifier + 1) : "";
                  String potionTime = this.formatDuration(duration);
                  String fullPotionText = potionName + potionLevel + " (" + potionTime + ")";
                  float potionWidth = font.width(fullPotionText) + 6.0F;
                  context.drawRect(-potionWidth / 2.0F, yOff + 5.0F, potionWidth, height, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
                  context.drawText(
                     font,
                     fullPotionText,
                     -potionWidth / 2.0F + 3.0F,
                     yOff + 8.0F,
                     ColorRGBA.fromInt(((StatusEffect)effect.getEffectType().value()).getColor()).withAlpha(255.0F)
                  );
                  yOff += height;
               }
            }

            ms.pop();
         }
      }

      if (this.hud.isEnabled()) {
         Font fontx = Fonts.MEDIUM.getFont(10.0F);
         float yOffx = 0.0F;

         for (Prediction.Predicted predictx : this.predicted) {
            if (predictx.collidedEntity == mc.player && !(predictx.entity instanceof EnderPearlEntity)) {
               String namex = predictx.entity.getName().getString().replace("Брошенный эндер-жемчуг", "Эндер-жемчуг")
                  + String.format(" (%s сек)", TextUtility.formatNumber(predictx.ticks / 20.0F));
               context.drawCenteredText(fontx, "В вас летит " + namex, sr.getScaledWidth() / 2.0F, sr.getScaledHeight() / 2.0F + 20.0F + yOffx, Colors.WHITE);
               yOffx += fontx.height() + 3.0F;
            }
         }
      }
   };
   private final EventListener<Render3DEvent> onRender3D = event -> {
      MatrixStack ms = event.getMatrices();
      ms.push();
      RenderUtility.setupRender3D(true);
      RenderUtility.prepareMatrices(ms);
      RenderSystem.enableDepthTest();
      if (this.walls.isEnabled()) {
         RenderSystem.disableDepthTest();
      }

      if (this.defaultMode.isSelected()) {
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

         for (Prediction.Predicted predict : this.predicted) {
            Vec3d prevPos = predict.vectors.getFirst();
            Draw3DUtility.drawLine(ms, builder, Utils.getInterpolatedPos(predict.entity, event.getTickDelta()), prevPos, Colors.ACCENT);

            for (Vec3d pos : predict.vectors) {
               Draw3DUtility.drawLine(ms, builder, prevPos, pos, Colors.ACCENT);
               prevPos = pos;
            }
         }

         RenderUtility.buildBuffer(builder);
      } else {
         Identifier id = Rockstar.id("textures/bloom.png");
         RenderSystem.setShaderTexture(0, id);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

         for (Prediction.Predicted predict : this.predicted) {
            Vec3d prevPos = predict.vectors.getFirst();
            Vec3d entityPos = Utils.getInterpolatedPos(predict.entity, event.getTickDelta());
            if (entityPos.distanceTo(mc.player.getEyePos()) > 2.0) {
               for (int i = 0; i < 10; i++) {
                  float t = i / 10.0F;
                  Vec3d interpolatedPos = entityPos.add(prevPos.subtract(entityPos).multiply(t));
                  this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) / 3.0F, 1.0F);
                  this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) * 2.0F, 0.05F);
               }
            }

            for (Vec3d pos : predict.vectors) {
               if (pos.distanceTo(mc.player.getEyePos()) > 2.0) {
                  for (int i = 0; i < 10; i++) {
                     float t = i / 10.0F;
                     Vec3d interpolatedPos = prevPos.add(pos.subtract(prevPos).multiply(t));
                     this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) / 3.0F, 1.0F);
                     this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) * 2.0F, 0.05F);
                  }
               }

               prevPos = pos;
            }

            float size = 9.0F;
            if (predict.entity instanceof PotionEntity) {
               ms.push();
               ms.translate(predict.vectors.getLast());
               ms.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0F));
               DrawUtility.drawImage(
                  ms, buffer, (double)(-size / 2.0F), (double)(-size / 2.0F), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0F)
               );
               ms.pop();
            }
         }

         RenderUtility.buildBuffer(buffer);
      }

      float size = 1.0F;
      Identifier id = Rockstar.id("textures/hit.png");
      RenderSystem.setShaderTexture(0, id);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (Prediction.Landed landed : this.landed) {
         if (landed.collidedEntity == null) {
            ms.push();
            ms.translate(landed.hitResult.getPos());
            ms.multiply(landed.hitResult.getSide().getRotationQuaternion());
            ms.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-90.0F));
            DrawUtility.drawImage(ms, buffer, (double)(-size / 2.0F), (double)(-size / 2.0F), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0F));
            ms.pop();
         }
      }

      RenderUtility.buildBuffer(buffer);
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      for (Prediction.Landed landedx : this.landed) {
         if (landedx.collidedEntity != null) {
            Draw3DUtility.renderFilledBox(ms, quadsBuffer, landedx.collidedEntity.getBoundingBox(), Colors.ACCENT.mulAlpha(0.5F));
         }
      }

      RenderUtility.buildBuffer(quadsBuffer);
      BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for (Prediction.Landed landedxx : this.landed) {
         if (landedxx.collidedEntity != null) {
            Draw3DUtility.renderOutlinedBox(ms, linesBuffer, landedxx.collidedEntity.getBoundingBox(), Colors.ACCENT);
         }
      }

      RenderUtility.buildBuffer(linesBuffer);
      RenderUtility.endRender3D();
      ms.pop();
   };

   public Prediction() {
      new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.pearls", entity -> entity instanceof EnderPearlEntity).select();
      new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.tridents", entity -> entity instanceof TridentEntity).select();
      new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.snowballs", entity -> entity instanceof SnowballEntity).select();
      new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.arrows", entity -> entity instanceof ArrowEntity).select();
      new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.potions", entity -> entity instanceof PotionEntity).select();
      new PredicateValue<>(this.entities, "modules.settings.prediction.entities.items", entity -> entity instanceof ItemEntity);
   }

   @Override
   public void tick() {
      this.predicted.clear();
      this.landed.clear();
      List<ProjectileEntity> projectiles = new ArrayList<>();
      if (this.inHand.isEnabled()) {
         ItemStack handStack = mc.player.getMainHandStack();
         Entity inHand = null;
         if (handStack.getItem() instanceof EnderPearlItem) {
            inHand = new EnderPearlEntity(mc.world, mc.player, handStack);
         } else if (handStack.getItem() instanceof TridentItem && mc.player.isUsingItem()) {
            inHand = new TridentEntity(mc.world, mc.player, handStack);
         } else if (handStack.getItem() instanceof BowItem && mc.player.isUsingItem()) {
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            inHand = new ArrowEntity(mc.world, mc.player, arrowStack, handStack);
         } else if (handStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(handStack)) {
            boolean hasMultishot = EnchantmentUtility.getEnchantmentLevel(handStack, Enchantments.MULTISHOT) > 0;
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            if (hasMultishot) {
               for (int i = 0; i < 3; i++) {
                  ArrowEntity arrow = new ArrowEntity(mc.world, mc.player, arrowStack, handStack);
                  projectiles.add(arrow);
               }
            } else {
               inHand = new ArrowEntity(mc.world, mc.player, arrowStack, handStack);
            }
         }

         if (inHand instanceof ProjectileEntity projectile) {
            float speed = 1.5F;
            if (inHand instanceof TridentEntity) {
               speed = 2.5F;
            } else if (inHand instanceof ArrowEntity) {
               speed = 3.0F;
            }

            this.setVelocity(projectile, mc.player, mc.player.getPitch(), mc.player.getYaw(), 0.0F, speed, 1.0F);
            this.predict(projectile, true);
         }
      }

      if (!projectiles.isEmpty()) {
         float speed = 3.15F;
         float spreadAngle = 10.0F;

         for (int i = 0; i < projectiles.size(); i++) {
            ProjectileEntity projectile = projectiles.get(i);
            float yawOffset = 0.0F;
            if (i == 0) {
               yawOffset = -spreadAngle;
            } else if (i == 2) {
               yawOffset = spreadAngle;
            }

            this.setVelocity(projectile, mc.player, mc.player.getPitch(), mc.player.getYaw() + yawOffset, 0.0F, speed, 1.0F);
            this.predict(projectile, true);
         }
      }

      for (Entity entity : mc.world.getEntities()) {
         this.predict(entity, false);
      }
   }

   private void predict(Entity entity, boolean inHand) {
      if (this.isValid(entity)) {
         if (entity instanceof ProjectileEntity pearl && pearl.getOwner() == null) {
            List<AbstractClientPlayerEntity> sortedPlayers = mc.world.getPlayers();
            if (!sortedPlayers.isEmpty()) {
               Collections.sort(sortedPlayers, Comparator.comparingDouble(player -> player.distanceTo(pearl)));
               pearl.setOwner((Entity)sortedPlayers.getFirst());
            }
         }

         List<Vec3d> positions = new ArrayList<>();
         Vec3d lastPos = entity.getPos();
         Vec3d lastMotion = entity.getVelocity();
         Entity collidedEntity = null;
         int ticks = 0;
         BlockHitResult blockHitResult = null;

         for (int i = 0; i < 150; i++) {
            Vec3d motion = this.predictMotion(entity, lastMotion);
            Vec3d pos = lastPos.add(motion);
            ticks = i;
            blockHitResult = mc.world.raycast(new RaycastContext(lastPos, pos, ShapeType.COLLIDER, FluidHandling.NONE, entity));
            Entity collided = this.checkEntityCollision(entity, pos);
            if (collided != null) {
               positions.add(pos);
               collidedEntity = collided;
               break;
            }

            if (blockHitResult.getType() != Type.MISS) {
               positions.add(blockHitResult.getPos());
               break;
            }

            positions.add(pos);
            lastPos = pos;
            lastMotion = motion;
         }

         if (!positions.isEmpty()) {
            if (inHand) {
               this.landed.add(new Prediction.Landed(entity, positions.getLast(), ticks, collidedEntity, blockHitResult));
            } else {
               this.predicted.add(new Prediction.Predicted(entity, positions, ticks, collidedEntity));
            }
         }
      }
   }

   private void drawGlow(MatrixStack ms, Vec3d pos, BufferBuilder buffer, float size, float alpha) {
      ms.push();
      ms.translate(pos);
      ms.multiply(mc.gameRenderer.getCamera().getRotation());
      DrawUtility.drawImage(
         ms, buffer, (double)(-size / 2.0F), (double)(-size / 2.0F), 0.0, (double)size, (double)size, Colors.ACCENT.withAlpha(255.0F * alpha)
      );
      ms.pop();
   }

   private boolean isValid(Entity entity) {
      boolean valid = false;

      for (SelectSetting.Value selectedValue : this.entities.getSelectedValues()) {
         PredicateValue<Entity> predicateValue = (PredicateValue<Entity>)selectedValue;
         if (predicateValue.predicated(entity)) {
            valid = true;
         }
      }

      return entity instanceof TridentEntity trident && trident.returnTimer > 0
         ? false
         : valid && (Math.abs(entity.getVelocity().x + entity.getVelocity().z) > 0.01F || Math.abs(entity.getVelocity().y) > 0.2F);
   }

   private Entity checkEntityCollision(Entity movingEntity, Vec3d predictedPos) {
      Vec3d currentPos = movingEntity.getPos();
      Vec3d direction = predictedPos.subtract(currentPos);
      if (direction.lengthSquared() == 0.0) {
         return null;
      } else {
         EntityHitResult hitResult = ProjectileUtil.raycast(
            movingEntity,
            currentPos,
            predictedPos,
            movingEntity.getBoundingBox().stretch(direction).expand(0.5),
            entity -> mc.player != entity
               && entity.isAlive()
               && !(entity instanceof ItemEntity)
               && !(entity instanceof ExperienceOrbEntity)
               && entity != movingEntity,
            direction.lengthSquared()
         );
         return hitResult != null ? hitResult.getEntity() : null;
      }
   }

   private void setVelocity(ProjectileEntity entity, double x, double y, double z, float power) {
      Vec3d vec3d = this.calculateVelocity(entity, x, y, z, power);
      entity.setVelocity(vec3d);
      entity.velocityDirty = true;
      double d = vec3d.horizontalLength();
      entity.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 180.0F / (float)Math.PI));
      entity.setPitch((float)(MathHelper.atan2(vec3d.y, d) * 180.0F / (float)Math.PI));
      entity.prevYaw = entity.getYaw();
      entity.prevPitch = entity.getPitch();
   }

   private void setVelocity(ProjectileEntity entity, Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
      float f = -MathHelper.sin(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0));
      float g = -MathHelper.sin((pitch + roll) * (float) (Math.PI / 180.0));
      float h = MathHelper.cos(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0));
      this.setVelocity(entity, f, g, h, speed);
      Vec3d vec3d = shooter.getMovement();
      entity.setVelocity(entity.getVelocity().add(vec3d.x, shooter.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
   }

   private Vec3d calculateVelocity(ProjectileEntity entity, double x, double y, double z, float power) {
      return new Vec3d(x, y, z).normalize().multiply(power);
   }

   private Vec3d predictMotion(Entity entity, Vec3d motion) {
      return motion.multiply(0.99).add(0.0, -entity.getFinalGravity(), 0.0);
   }

   private String formatDuration(int ticks) {
      int seconds = ticks / 20;
      int minutes = seconds / 60;
      int remainingSeconds = seconds % 60;
      return minutes > 0 ? String.format("%d:%02d", minutes, remainingSeconds) : String.format("0:%02d", remainingSeconds);
   }

   record Landed(Entity entity, Vec3d pos, int ticks, Entity collidedEntity, BlockHitResult hitResult) {
   }

   record Predicted(Entity entity, List<Vec3d> vectors, int ticks, Entity collidedEntity) {
   }
}
