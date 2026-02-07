package moscow.rockstar.systems.ai;

import ai.catboost.CatBoostError;
import ai.catboost.CatBoostModel;
import ai.catboost.CatBoostPredictions;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class AIPredict implements IMinecraft {
   private long lastSwingTimeMs;
   private CatBoostModel yawModel;
   private CatBoostModel pitchModel;
   private float prevYaw;
   private float prevPitch;
   private float prevTargetYaw;
   private float prevTargetPitch;
   private float prevDistance;
   private Rotation last = Rotation.ZERO;
   private final EventListener<AttackEvent> onAttackEvent = event -> {
      Entity tgt = event.getEntity();
      if (tgt != null) {
         this.lastSwingTimeMs = System.currentTimeMillis();
      }
   };

   public AIPredict() {
      try {
         this.yawModel = CatBoostModel.loadModel("C:/Rockstar/delta_yaw_model.cbm");
         this.pitchModel = CatBoostModel.loadModel("C:/Rockstar/delta_pitch_model.cbm");
      } catch (CatBoostError var2) {
         var2.printStackTrace();
      }

      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   public Rotation predictRotation(Rotation prev, LivingEntity target) {
      long now = System.currentTimeMillis();
      double diffY = mc.player.getY() - target.getY();
      float tdy = this.calcTargetDeltaYaw(prev, target);
      float tdp = this.calcTargetDeltaPitch(prev, target);
      float dist = mc.player.distanceTo(target);
      float since = (float)Math.min(500L, now - this.lastSwingTimeMs);
      float[][] features = new float[][]{
         {
               tdy,
               tdp,
               dist,
               since,
               mc.player.fallDistance,
               (float)diffY,
               this.prevTargetYaw,
               this.prevTargetPitch,
               this.prevYaw,
               this.prevPitch,
               this.prevDistance
         }
      };
      String[][] catFeatures = new String[features.length][0];

      try {
         CatBoostPredictions predsYaw = this.yawModel.predict(features, catFeatures);
         CatBoostPredictions predsPitch = this.pitchModel.predict(features, catFeatures);
         double predictedYaw = predsYaw.get(0, 0);
         double predictedPitch = predsPitch.get(0, 0);
         this.prevTargetYaw = tdy;
         this.prevTargetPitch = tdp;
         this.prevYaw = this.normalizeAngle(this.last.getYaw() - prev.getYaw());
         this.prevPitch = this.normalizeAngle(this.last.getPitch() - prev.getPitch());
         this.prevDistance = dist;
         this.last = new Rotation(prev.getYaw(), prev.getPitch());
         return new Rotation((float)(prev.getYaw() + predictedYaw), (float)(prev.getPitch() - predictedPitch));
      } catch (CatBoostError var19) {
         var19.printStackTrace();
         return prev;
      }
   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   private float calcTargetDeltaYaw(Rotation rotation, Entity t) {
      double dx = t.getX() - mc.player.getX();
      double dz = t.getZ() - mc.player.getZ();
      float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      return this.normalizeAngle(targetYaw - rotation.getYaw());
   }

   private float calcTargetDeltaPitch(Rotation rotation, Entity t) {
      double dx = t.getX() - mc.player.getX();
      double dz = t.getZ() - mc.player.getZ();
      double dy = t.getEyeY() - mc.player.getEyeY();
      double dist = Math.sqrt(dx * dx + dz * dz);
      float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return this.normalizeAngle(targetPitch - rotation.getPitch());
   }
}
