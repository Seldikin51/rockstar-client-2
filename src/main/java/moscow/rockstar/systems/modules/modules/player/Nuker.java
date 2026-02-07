package moscow.rockstar.systems.modules.modules.player;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationPriority;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Nuker", category = ModuleCategory.PLAYER, desc = "Копает территорию вокруг себя на авто-шахте")
public class Nuker extends BaseModule {
   private final SliderSetting xzDistance = new SliderSetting(
         this, "Дистанция XZ", "Дистанция, на которой будет работать " + this.getName() + " по горизонтали"
      )
      .step(1.0F)
      .min(2.0F)
      .max(6.0F)
      .currentValue(4.0F);
   private final SliderSetting yDistance = new SliderSetting(this, "Дистанция Y", "Дистанция, на которой будет работать " + this.getName() + " по вертикали")
      .step(1.0F)
      .min(2.0F)
      .max(6.0F)
      .currentValue(5.0F);
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      int radius = this.range();
      BlockPos minPos = new BlockPos(-71, 77, -15);
      BlockPos maxPos = new BlockPos(-51, 86, 5);
      boolean spawn = ServerUtility.spawn();

      for (int y = 0; y < radius * 2; y++) {
         for (int x = 0; x < radius * 2; x++) {
            for (int z = 0; z < radius * 2; z++) {
               BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
               BlockPos pos = mc.player.getBlockPos().add(offset);
               if ((
                     pos.getX() >= minPos.getX()
                           && pos.getX() <= maxPos.getX()
                           && pos.getY() >= minPos.getY()
                           && pos.getY() <= maxPos.getY()
                           && pos.getZ() >= minPos.getZ()
                           && pos.getZ() <= maxPos.getZ()
                        || !spawn
                  )
                  && mc.world.getBlockState(pos).getBlock() == Blocks.DIAMOND_ORE) {
                  double posX = pos.getX();
                  double posY = pos.getY();
                  double posZ = pos.getZ();
                  double deltaX = posX - mc.player.getX();
                  double deltaY = posY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
                  double deltaZ = posZ - mc.player.getZ();
                  double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                  float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F + MathUtility.random(-2.0, 2.0);
                  float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
                  mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
                  Rockstar.getInstance()
                     .getRotationHandler()
                     .rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.NORMAL);
                  Direction direction = getDirection(pos);
                  mc.interactionManager.updateBlockBreakingProgress(pos, direction);
                  mc.player.swingHand(Hand.MAIN_HAND);
                  return;
               }
            }
         }
      }

      for (int y = 0; y < this.yDistance.getCurrentValue(); y++) {
         for (int x = 0; x < radius * 2; x++) {
            for (int zx = 0; zx < radius * 2; zx++) {
               BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (zx % 2 == 0 ? -zx : zx) / 2);
               BlockPos pos = mc.player.getBlockPos().up().add(offset);
               if ((
                     pos.getX() >= minPos.getX()
                           && pos.getX() <= maxPos.getX()
                           && pos.getY() >= minPos.getY()
                           && pos.getY() <= maxPos.getY()
                           && pos.getZ() >= minPos.getZ()
                           && pos.getZ() <= maxPos.getZ()
                        || !spawn
                  )
                  && mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                  double posX = pos.getX();
                  double posY = pos.getY();
                  double posZ = pos.getZ();
                  double deltaX = posX - mc.player.getX();
                  double deltaY = posY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
                  double deltaZ = posZ - mc.player.getZ();
                  double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                  float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F + MathUtility.random(-2.0, 2.0);
                  float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance))) + MathUtility.random(-1.0, 1.0);
                  Rockstar.getInstance()
                     .getRotationHandler()
                     .rotate(new Rotation(yaw, pitch), MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.NORMAL);
                  Direction direction = getDirection(pos);
                  mc.interactionManager.updateBlockBreakingProgress(pos, direction);
                  mc.player.swingHand(Hand.MAIN_HAND);
                  return;
               }
            }
         }
      }
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      if (mc.world != null && mc.player != null) {
         MatrixStack matrices = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.lineWidth(10.0F);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         matrices.push();
         matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         int radius = this.range();
         BlockPos minPos = new BlockPos(-71, 77, -15);
         BlockPos maxPos = new BlockPos(-51, 86, 5);
         boolean spawn = ServerUtility.spawn();
         if (spawn) {
            Draw3DUtility.renderOutlinedBox(
               event.getMatrices(),
               buffer,
               new Box(minPos.getX(), minPos.getY(), minPos.getZ(), maxPos.getX() + 1, maxPos.getY() + 1, maxPos.getZ() + 1),
               ColorRGBA.GREEN.withAlpha(110.0F)
            );
         }

         for (int y = 0; y < radius * 2; y++) {
            for (int x = 0; x < radius * 2; x++) {
               for (int z = 0; z < radius * 2; z++) {
                  BlockPos additional = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                  BlockPos pos = mc.player.getBlockPos().add(additional);
                  if ((
                        pos.getX() >= minPos.getX()
                              && pos.getX() <= maxPos.getX()
                              && pos.getY() >= minPos.getY()
                              && pos.getY() <= maxPos.getY()
                              && pos.getZ() >= minPos.getZ()
                              && pos.getZ() <= maxPos.getZ()
                           || !spawn
                     )
                     && mc.world.getBlockState(pos).getBlock() == Blocks.DIAMOND_ORE) {
                     Direction direction = getDirection(pos);
                     Draw3DUtility.renderOutlinedBox(
                        event.getMatrices(),
                        buffer,
                        mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos),
                        ColorRGBA.GREEN.withAlpha(250.0F)
                     );
                     Draw3DUtility.renderBoxInternalDiagonals(
                        event.getMatrices(),
                        buffer,
                        mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos),
                        ColorRGBA.GREEN.withAlpha(250.0F)
                     );
                     BuiltBuffer builtBuffer = buffer.endNullable();
                     if (builtBuffer != null) {
                        BufferRenderer.drawWithGlobalProgram(builtBuffer);
                     }

                     RenderSystem.enableCull();
                     RenderSystem.enableDepthTest();
                     RenderSystem.disableBlend();
                     return;
                  }
               }
            }
         }

         for (int y = 0; y < this.yDistance.getCurrentValue(); y++) {
            for (int x = 0; x < radius * 2; x++) {
               for (int zx = 0; zx < radius * 2; zx++) {
                  BlockPos additional = new BlockPos((x % 2 == 0 ? -x : x) / 2, y, (zx % 2 == 0 ? -zx : zx) / 2);
                  BlockPos pos = mc.player.getBlockPos().up().add(additional);
                  if ((
                        pos.getX() >= minPos.getX()
                              && pos.getX() <= maxPos.getX()
                              && pos.getY() >= minPos.getY()
                              && pos.getY() <= maxPos.getY()
                              && pos.getZ() >= minPos.getZ()
                              && pos.getZ() <= maxPos.getZ()
                           || !spawn
                     )
                     && mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
                     Direction direction = getDirection(pos);
                     Draw3DUtility.renderOutlinedBox(
                        event.getMatrices(),
                        buffer,
                        mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos),
                        ColorRGBA.GREEN.withAlpha(250.0F)
                     );
                     Draw3DUtility.renderBoxInternalDiagonals(
                        event.getMatrices(),
                        buffer,
                        mc.world.getBlockState(pos).getCullingShape().getBoundingBox().offset(pos),
                        ColorRGBA.GREEN.withAlpha(250.0F)
                     );
                     BuiltBuffer builtBuffer = buffer.endNullable();
                     if (builtBuffer != null) {
                        BufferRenderer.drawWithGlobalProgram(builtBuffer);
                     }

                     RenderSystem.enableCull();
                     RenderSystem.enableDepthTest();
                     RenderSystem.disableBlend();
                     return;
                  }
               }
            }
         }

         BuiltBuffer builtBuffer = buffer.endNullable();
         if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
         }

         matrices.pop();
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   };

   public int range() {
      return (int)this.xzDistance.getCurrentValue();
   }

   public static Direction getDirection(BlockPos pos) {
      Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
      if (pos.getY() > eyesPos.y) {
         return mc.world.getBlockState(pos.add(0, -1, 0)).isReplaceable() ? Direction.DOWN : mc.player.getHorizontalFacing().getOpposite();
      } else {
         return !mc.world.getBlockState(pos.add(0, 1, 0)).isReplaceable() ? mc.player.getHorizontalFacing().getOpposite() : Direction.UP;
      }
   }
}
