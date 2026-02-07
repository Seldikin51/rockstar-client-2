package moscow.rockstar.systems.event.impl.player;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.util.math.MathHelper;

public class InputEvent extends Event implements IMinecraft {
   private float forward;
   private float strafe;
   private boolean jump;
   private boolean sneak;
   private boolean sprint;
   private double sneakSlowDownMultiplier;

   public InputEvent(float moveForward, float moveStrafe, boolean jump, boolean sneak, boolean sprint) {
      this.forward = moveForward;
      this.strafe = moveStrafe;
      this.jump = jump;
      this.sneak = sneak;
      this.sprint = sprint;
      this.sneakSlowDownMultiplier = 0.3;
   }

   public void setYaw(float yaw, float direction) {
      float forward = this.getForward();
      float strafe = this.getStrafe();
      double angle = MathHelper.wrapDegrees(Math.toDegrees(EntityUtility.direction(direction, forward, strafe)));
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward++) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe++) {
               if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(EntityUtility.direction(yaw, predictedForward, predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         this.setForward(closestForward);
         this.setStrafe(closestStrafe);
      }
   }

   public void setYaw(float yaw) {
      if (mc.player != null) {
         this.setYaw(yaw, mc.player.getYaw());
      }
   }

   @Generated
   public float getForward() {
      return this.forward;
   }

   @Generated
   public float getStrafe() {
      return this.strafe;
   }

   @Generated
   public boolean isJump() {
      return this.jump;
   }

   @Generated
   public boolean isSneak() {
      return this.sneak;
   }

   @Generated
   public boolean isSprint() {
      return this.sprint;
   }

   @Generated
   public double getSneakSlowDownMultiplier() {
      return this.sneakSlowDownMultiplier;
   }

   @Generated
   public void setForward(float forward) {
      this.forward = forward;
   }

   @Generated
   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   @Generated
   public void setJump(boolean jump) {
      this.jump = jump;
   }

   @Generated
   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }

   @Generated
   public void setSprint(boolean sprint) {
      this.sprint = sprint;
   }

   @Generated
   public void setSneakSlowDownMultiplier(double sneakSlowDownMultiplier) {
      this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
   }
}
