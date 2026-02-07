package moscow.rockstar.utility.game.prediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class ElytraPredictionSystem {
   private static final int MAX_HISTORY_SIZE = 15;
   private static final double GRAVITY = -0.08;
   private static final double AIR_RESISTANCE_X = 0.99;
   private static final double AIR_RESISTANCE_Y = 0.98;
   private static final double AIR_RESISTANCE_Z = 0.99;
   private static final double PITCH_INFLUENCE = 0.06;
   private static final double DIRECTION_CORRECTION = 0.1;
   private static final long DATA_CLEANUP_INTERVAL = 30000L;
   private static final Map<UUID, List<ElytraPredictionSystem.MovementData>> movementHistory = new ConcurrentHashMap<>();
   private static final Map<UUID, ElytraPredictionSystem.PlayerPredictionStats> playerStats = new ConcurrentHashMap<>();
   private static final Map<UUID, Integer> customPredictionTicks = new ConcurrentHashMap<>();
   private static long lastCleanupTime = System.currentTimeMillis();

   public static Vec3d predictPlayerPosition(PlayerEntity target) {
      if (target == null) {
         return Vec3d.ZERO;
      } else {
         updateEntityTracking(target);
         if (!isLeaving(target)) {
            return target.getPos();
         } else {
            int predictionTicks = calculateOptimalPredictionTicks(target);
            return simulateElytraFlight(target, predictionTicks);
         }
      }
   }

   public static void updateEntityTracking(PlayerEntity entity) {
      if (entity != null) {
         UUID uuid = entity.getUuid();
         long currentTime = System.currentTimeMillis();
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            double distanceToClient = entity.distanceTo(client.player);
            ElytraPredictionSystem.MovementData data = new ElytraPredictionSystem.MovementData(
               entity.getPos(), entity.getVelocity(), entity.getPitch(), entity.getYaw(), entity.isGliding(), currentTime, distanceToClient
            );
            List<ElytraPredictionSystem.MovementData> history = movementHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
            history.add(data);
            if (history.size() > 15) {
               history.remove(0);
            }

            updatePlayerStats(uuid, data);
            cleanupOldData(currentTime);
         }
      }
   }

   public static boolean isLeaving(PlayerEntity target) {
      if (!target.isGliding()) {
         return false;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player == null) {
            return false;
         } else {
            UUID uuid = target.getUuid();
            List<ElytraPredictionSystem.MovementData> history = movementHistory.get(uuid);
            if (history != null && history.size() >= 3) {
               boolean isDistanceIncreasing = isDistanceIncreasing(history);
               boolean isVelocityDirectedAway = isVelocityDirectedAway(target, client.player);
               boolean hasSignificantSpeed = hasSignificantSpeed(target);
               int positiveChecks = 0;
               if (isDistanceIncreasing) {
                  positiveChecks++;
               }

               if (isVelocityDirectedAway) {
                  positiveChecks++;
               }

               if (hasSignificantSpeed) {
                  positiveChecks++;
               }

               return positiveChecks >= 2;
            } else {
               return false;
            }
         }
      }
   }

   private static boolean isDistanceIncreasing(List<ElytraPredictionSystem.MovementData> history) {
      if (history.size() < 3) {
         return false;
      } else {
         int pointsToAnalyze = Math.min(5, history.size());
         List<ElytraPredictionSystem.MovementData> recentHistory = history.subList(history.size() - pointsToAnalyze, history.size());
         int increasingCount = 0;

         for (int i = 1; i < recentHistory.size(); i++) {
            if (recentHistory.get(i).distanceToClient > recentHistory.get(i - 1).distanceToClient) {
               increasingCount++;
            }
         }

         return increasingCount >= (recentHistory.size() - 1) / 2;
      }
   }

   private static boolean isVelocityDirectedAway(PlayerEntity target, PlayerEntity client) {
      Vec3d targetPos = target.getPos();
      Vec3d clientPos = client.getPos();
      Vec3d targetVelocity = target.getVelocity();
      Vec3d directionToTarget = targetPos.subtract(clientPos).normalize();
      Vec3d velocityDirection = targetVelocity.normalize();
      double dotProduct = directionToTarget.dotProduct(velocityDirection);
      return dotProduct > 0.3;
   }

   private static boolean hasSignificantSpeed(PlayerEntity target) {
      double speed = target.getVelocity().length();
      return speed > 0.8;
   }

   private static Vec3d simulateElytraFlight(PlayerEntity player, int ticksAhead) {
      Vec3d position = player.getPos();
      Vec3d velocity = player.getVelocity();
      float pitch = player.getPitch();
      float yaw = player.getYaw();
      boolean isFlying = player.isGliding();

      for (int tick = 0; tick < ticksAhead; tick++) {
         if (isFlying) {
            position = simulateElytraTick(position, velocity, pitch, yaw);
            velocity = updateElytraVelocity(velocity, pitch, yaw);
         } else {
            velocity = velocity.add(0.0, -0.08, 0.0).multiply(0.98);
            position = position.add(velocity);
         }
      }

      return position;
   }

   private static Vec3d simulateElytraTick(Vec3d position, Vec3d velocity, float pitch, float yaw) {
      return position.add(velocity);
   }

   private static Vec3d updateElytraVelocity(Vec3d velocity, float pitch, float yaw) {
      double motionX = velocity.x;
      double motionY = velocity.y;
      double motionZ = velocity.z;
      float pitchRad = (float)Math.toRadians(pitch);
      float yawRad = (float)Math.toRadians(yaw);
      Vec3d lookDirection = new Vec3d(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
      double horizontalVelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
      double lookHorizontal = Math.sqrt(lookDirection.x * lookDirection.x + lookDirection.z * lookDirection.z);
      float cosPitch = (float)Math.cos(pitchRad);
      float cosPitchSq = cosPitch * cosPitch;
      motionY += -0.08 + cosPitchSq * 0.06;
      if (motionY < 0.0 && lookHorizontal > 0.0) {
         double yawAcceleration = motionY * -0.1 * cosPitchSq;
         motionY += yawAcceleration;
         motionX += lookDirection.x * yawAcceleration / lookHorizontal;
         motionZ += lookDirection.z * yawAcceleration / lookHorizontal;
      }

      if (pitch < 0.0F && lookHorizontal > 0.0) {
         double yawAcceleration = horizontalVelocity * -Math.sin(pitchRad) * 0.04;
         motionY += yawAcceleration * 3.2;
         motionX -= lookDirection.x * yawAcceleration / lookHorizontal;
         motionZ -= lookDirection.z * yawAcceleration / lookHorizontal;
      }

      if (lookHorizontal > 0.0) {
         motionX += (lookDirection.x / lookHorizontal * horizontalVelocity - motionX) * 0.1;
         motionZ += (lookDirection.z / lookHorizontal * horizontalVelocity - motionZ) * 0.1;
      }

      motionX *= 0.99;
      motionY *= 0.98;
      motionZ *= 0.99;
      return new Vec3d(motionX, motionY, motionZ);
   }

   private static int calculateOptimalPredictionTicks(PlayerEntity target) {
      UUID uuid = target.getUuid();
      if (customPredictionTicks.containsKey(uuid)) {
         return customPredictionTicks.get(uuid);
      } else {
         int baseTicks = calculateNetworkDelay(target);
         List<ElytraPredictionSystem.MovementData> history = movementHistory.get(uuid);
         if (history != null && history.size() >= 3) {
            double velocityVariance = calculateVelocityVariance(history);
            double directionVariance = calculateDirectionVariance(history);
            if (target.isGliding()) {
               double speed = target.getVelocity().length();
               if (speed > 2.0) {
                  baseTicks += Math.min(4, (int)(speed * 1.2));
               }

               if (directionVariance > 30.0) {
                  baseTicks += 2;
               }
            }

            return Math.max(1, Math.min(15, baseTicks));
         } else {
            return baseTicks;
         }
      }
   }

   private static int calculateNetworkDelay(PlayerEntity player) {
      MinecraftClient client = MinecraftClient.getInstance();
      int ping = 100;
      if (client.getNetworkHandler() != null) {
         try {
            PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (playerListEntry != null) {
               ping = playerListEntry.getLatency();
            }
         } catch (Exception var5) {
         }
      }

      int networkTicks = Math.max(1, ping / 50);
      int serverProcessingTicks = 2;
      return networkTicks + serverProcessingTicks;
   }

   private static double calculateVelocityVariance(List<ElytraPredictionSystem.MovementData> history) {
      if (history.size() < 2) {
         return 0.0;
      } else {
         double[] speeds = history.stream().mapToDouble(data -> data.velocity.length()).toArray();
         double mean = Arrays.stream(speeds).average().orElse(0.0);
         double variance = Arrays.stream(speeds).map(speed -> Math.pow(speed - mean, 2.0)).average().orElse(0.0);
         return Math.sqrt(variance);
      }
   }

   private static double calculateDirectionVariance(List<ElytraPredictionSystem.MovementData> history) {
      if (history.size() < 2) {
         return 0.0;
      } else {
         double totalVariance = 0.0;

         for (int i = 1; i < history.size(); i++) {
            ElytraPredictionSystem.MovementData prev = history.get(i - 1);
            ElytraPredictionSystem.MovementData curr = history.get(i);
            double yawDiff = Math.abs(curr.yaw - prev.yaw);
            double pitchDiff = Math.abs(curr.pitch - prev.pitch);
            if (yawDiff > 180.0) {
               yawDiff = 360.0 - yawDiff;
            }

            totalVariance += Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
         }

         return totalVariance / (history.size() - 1);
      }
   }

   private static void updatePlayerStats(UUID uuid, ElytraPredictionSystem.MovementData data) {
      ElytraPredictionSystem.PlayerPredictionStats stats = playerStats.computeIfAbsent(uuid, k -> new ElytraPredictionSystem.PlayerPredictionStats());
      stats.update(data);
   }

   private static void cleanupOldData(long currentTime) {
      if (currentTime - lastCleanupTime >= 30000L) {
         lastCleanupTime = currentTime;
         movementHistory.entrySet().removeIf(entry -> {
            List<ElytraPredictionSystem.MovementData> history = entry.getValue();
            history.removeIf(data -> currentTime - data.timestamp > 30000L);
            return history.isEmpty();
         });
         playerStats.entrySet().removeIf(entry -> currentTime - entry.getValue().lastUpdate > 30000L);
      }
   }

   @Generated
   private ElytraPredictionSystem() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static class MovementData {
      public final Vec3d position;
      public final Vec3d velocity;
      public final float pitch;
      public final float yaw;
      public final boolean isFallFlying;
      public final long timestamp;
      public final double distanceToClient;

      public MovementData(Vec3d position, Vec3d velocity, float pitch, float yaw, boolean isFallFlying, long timestamp, double distanceToClient) {
         this.position = position;
         this.velocity = velocity;
         this.pitch = pitch;
         this.yaw = yaw;
         this.isFallFlying = isFallFlying;
         this.timestamp = timestamp;
         this.distanceToClient = distanceToClient;
      }
   }

   private static class PlayerPredictionStats {
      private double averageSpeed = 0.0;
      private int sampleCount = 0;
      private long lastUpdate = System.currentTimeMillis();

      public void update(ElytraPredictionSystem.MovementData data) {
         double speed = data.velocity.length();
         this.averageSpeed = (this.averageSpeed * this.sampleCount + speed) / (this.sampleCount + 1);
         this.sampleCount++;
         this.lastUpdate = System.currentTimeMillis();
      }
   }
}
