package moscow.rockstar.utility.math;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.ElytraTarget;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.calculator.ExpressionBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class MathUtility implements IMinecraft {
   private static final int TABLE_SIZE = 65536;
   private static final double TWO_PI = Math.PI * 2;
   private static final double[] TRIG_TABLE = new double[65536];

   public static double sin(double radians) {
      int index = (int)(radians * 10430.378350470453) & 65535;
      return TRIG_TABLE[index];
   }

   public static double cos(double radians) {
      int index = (int)(radians * 10430.378350470453 + 16384.0) & 65535;
      return TRIG_TABLE[index];
   }

   public static float random(double min, double max) {
      return (float)(min + (max - min) * Math.random());
   }

   public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
      return Math.pow(1.0 - t, 3.0) * p0 + 3.0 * t * Math.pow(1.0 - t, 2.0) * p1 + 3.0 * Math.pow(t, 2.0) * (1.0 - t) * p2 + Math.pow(t, 3.0) * p3;
   }

   public static boolean canSeen(Vec3d targetVec) {
      return mc.world.raycast(new RaycastContext(mc.player.getEyePos(), targetVec, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.MISS;
   }

   public static boolean canShoot(Vec3d targetVec) {
      Vec3d start = mc.player.getEyePos();
      Vec3d direction = targetVec.subtract(start);
      double distance = direction.length();
      direction = direction.normalize();
      Set<BlockPos> checkedBlocks = new HashSet<>();
      int solidBlocks = 0;
      double step = 0.25;

      for (double d = 0.0; d <= distance; d += step) {
         Vec3d currentPos = start.add(direction.multiply(d));
         BlockPos blockPos = BlockPos.ofFloored(currentPos);
         if (!checkedBlocks.contains(blockPos)) {
            checkedBlocks.add(blockPos);
            BlockState blockState = mc.world.getBlockState(blockPos);
            if (!blockState.isAir()) {
               Block block = blockState.getBlock();
               if (!blockState.isOf(Blocks.GLASS) && !blockState.isOf(Blocks.GLASS_PANE) && !(blockState.getBlock() instanceof TrapdoorBlock)) {
                  VoxelShape collisionShape = blockState.getCollisionShape(mc.world, blockPos);
                  if (!collisionShape.isEmpty()) {
                     solidBlocks++;
                  }
               }
            }
         }
      }

      AtomicBoolean snipe = new AtomicBoolean(false);
      BossBarHud boss = mc.inGameHud.getBossBarHud();
      if (boss != null) {
         Class bossbarklass = BossBarHud.class;

         try {
            Field field = bossbarklass.getField("bossBars");
            Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)field.get(boss);

            for (UUID uuid : bossBars.keySet()) {
               ClientBossBar clientBossBar = bossBars.get(uuid);
               List<Text> siblings = clientBossBar.getName().getSiblings();
               siblings.stream().allMatch(text -> {
                  if (text.getString().contains("룳ꈣꈃ룲ꈣꈅ")) {
                     snipe.set(true);
                  }

                  return true;
               });
            }
         } catch (Exception var18) {
         }
      }

      return solidBlocks <= (snipe.get() ? 3 : (mc.player.getInventory().selectedSlot == 0 ? 2 : 1));
   }

   public static int levenshtein(String a, String b) {
      int n = a.length();
      int m = b.length();
      int[] dp = new int[m + 1];
      int j = 0;

      while (j <= m) {
         dp[j] = j++;
      }

      for (int i = 1; i <= n; i++) {
         int prev = dp[0];
         dp[0] = i;

         for (int jx = 1; jx <= m; jx++) {
            int tmp = dp[jx];
            int cost = a.charAt(i - 1) == b.charAt(jx - 1) ? 0 : 1;
            dp[jx] = Math.min(Math.min(dp[jx] + 1, dp[jx - 1] + 1), prev + cost);
            prev = tmp;
         }
      }

      return dp[m];
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      return (float)(oldValue + (newValue - oldValue) * interpolationValue);
   }

   public static HitResult rayTrace(double rayTraceDistance, float yaw, float pitch, Entity entity) {
      Vec3d startVec = mc.player.getCameraPosVec(1.0F);
      Vec3d directionVec = getVectorForRotation(pitch, yaw);
      Vec3d endVec = startVec.add(directionVec.x * rayTraceDistance, directionVec.y * rayTraceDistance, directionVec.z * rayTraceDistance);
      return mc.world.raycast(new RaycastContext(startVec, endVec, ShapeType.OUTLINE, FluidHandling.NONE, entity));
   }

   public static boolean tracedTo(Entity shooter, Vec3d startVec, Vec3d endVec, Box boundingBox, Predicate<Entity> filter, double distance, Entity target) {
      World world = shooter.getWorld();
      double d0 = distance;

      for (Entity entity1 : world.getOtherEntities(shooter, boundingBox, filter)) {
         Box box = entity1.getBoundingBox().expand(entity1.getTargetingMargin());
         Optional<Vec3d> optional = box.raycast(startVec, endVec);
         if (box.contains(startVec)) {
            if (d0 >= 0.0) {
               if (entity1 == target) {
                  return true;
               }

               d0 = 0.0;
            }
         } else if (optional.isPresent()) {
            Vec3d vec3d1 = optional.get();
            double d1 = startVec.squaredDistanceTo(vec3d1);
            if (entity1.getRootVehicle() == shooter.getRootVehicle()) {
               if (d0 == 0.0 && entity1 == target) {
                  return true;
               }
            } else {
               if (entity1 == target) {
                  return true;
               }

               d0 = d1;
            }
         }
      }

      return false;
   }

   public static boolean canTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target, boolean checkBlocks) {
      if (target != null && entity != null && mc.world != null) {
         float partialTicks = mc.getRenderTickCounter().getTickDelta(false);
         Vec3d startPos = entity.getCameraPosVec(partialTicks);
         Vec3d endPos = Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isEnabled() && target instanceof PlayerEntity player
            ? player.getBoundingBox()
               .offset(ElytraPredictionSystem.predictPlayerPosition(player))
               .offset(-player.getX(), -player.getY(), -player.getZ())
               .getCenter()
            : target.getBoundingBox().getCenter();
         if (checkBlocks) {
            HitResult blockHit = mc.world.raycast(new RaycastContext(startPos, endPos, ShapeType.COLLIDER, FluidHandling.NONE, entity));
            if (blockHit != null && blockHit.getType() == Type.BLOCK) {
               double blockDistSq = blockHit.getPos().squaredDistanceTo(startPos);
               double targetDistSq = endPos.squaredDistanceTo(startPos);
               if (blockDistSq < targetDistSq) {
                  return false;
               }
            }
         }

         Vec3d direction = getVectorForRotation(pitch, yaw);
         Vec3d rayEnd = startPos.add(direction.multiply(rayTraceDistance));
         Box searchBox = entity.getBoundingBox().stretch(direction.multiply(rayTraceDistance)).expand(1.0);
         return tracedTo(entity, startPos, rayEnd, searchBox, e -> !e.isSpectator() && e.canHit(), rayTraceDistance * rayTraceDistance, target);
      } else {
         return false;
      }
   }

   public static Vec3d getVectorForRotation(float pitch, float yaw) {
      float yawRadians = -yaw * (float) (Math.PI / 180.0) - (float) Math.PI;
      float pitchRadians = -pitch * (float) (Math.PI / 180.0);
      float cosYaw = MathHelper.cos(yawRadians);
      float sinYaw = MathHelper.sin(yawRadians);
      float cosPitch = -MathHelper.cos(pitchRadians);
      float sinPitch = MathHelper.sin(pitchRadians);
      return new Vec3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
   }

   public static float angleDifference(float angle1, float angle2) {
      float diff = (angle1 - angle2) % 360.0F;
      if (diff < -180.0F) {
         diff += 360.0F;
      } else if (diff > 180.0F) {
         diff -= 360.0F;
      }

      return diff;
   }

   public static String calculate(String expression) {
      expression = expression.replaceAll("\\s+", "");
      if (expression.isEmpty()) {
         return "";
      } else {
         try {
            double result = new ExpressionBuilder(expression).build().evaluate();
            return String.valueOf(result);
         } catch (IllegalArgumentException var3) {
            var3.printStackTrace();
            return expression;
         }
      }
   }

   @Generated
   private MathUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      for (int i = 0; i < 65536; i++) {
         TRIG_TABLE[i] = Math.sin(i * (Math.PI * 2) / 65536.0);
      }
   }
}
