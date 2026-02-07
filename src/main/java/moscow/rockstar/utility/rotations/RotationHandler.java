package moscow.rockstar.utility.rotations;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class RotationHandler implements IMinecraft {
   private final RotationUpdateListener rotationUpdateListener;
   private Rotation currentRotation = Rotation.ZERO;
   private final Rotation serverRotation = Rotation.ZERO;
   private Rotation prevRotation = Rotation.ZERO;
   private Rotation renderRotation = Rotation.ZERO;
   private RotationState state = RotationState.IDLE;
   @Nullable
   private RotationTask currentTask;
   private final Timer rotationIdle = new Timer();

   public RotationHandler(RotationUpdateListener rotationUpdateListener) {
      this.rotationUpdateListener = rotationUpdateListener;
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   public boolean isIdling() {
      return this.state == RotationState.IDLE;
   }

   @Internal
   public void update() {
      this.prevRotation = this.currentRotation;
      if (this.currentTask == null) {
         this.currentRotation = this.getPlayerRotation();
      } else if (this.rotationIdle.finished(70L)) {
         if (this.getPlayerRotation().differenceValue(this.currentRotation) < 1.0F) {
            this.state = RotationState.IDLE;
            this.currentTask = null;
         } else {
            this.state = RotationState.ROTATING_BACK;
            mc.player.setYaw(RotationMath.adjustAngle(this.currentRotation.getYaw(), mc.player.getYaw()));
            this.currentRotation = RotationMath.correctRotation(
               new Rotation(
                  this.moveTowardsAngle(this.currentRotation.getYaw(), this.getPlayerRotation().getYaw(), this.currentTask.getReturnSpeed()),
                  this.moveTowardsAngle(this.currentRotation.getPitch(), this.getPlayerRotation().getPitch(), this.currentTask.getReturnSpeed())
               )
            );
         }
      } else {
         this.state = RotationState.ROTATING;
         this.currentRotation = RotationMath.correctRotation(
            new Rotation(
               this.moveTowardsAngle(this.currentRotation.getYaw(), this.currentTask.getRotation().getYaw(), this.currentTask.getSpeedX()),
               this.moveTowardsAngle(this.currentRotation.getPitch(), this.currentTask.getRotation().getPitch(), this.currentTask.getSpeedY())
            )
         );
      }
   }

   public void updateRender(float partialTicks) {
      if (mc.player != null) {
         float yaw = MathUtility.interpolate(this.prevRotation.getYaw(), this.currentRotation.getYaw(), partialTicks);
         float pitch = this.prevRotation.getPitch() + (this.currentRotation.getPitch() - this.prevRotation.getPitch()) * partialTicks;
         if (pitch <= -85.0F) {
            pitch = 0.0F;
         }

         this.renderRotation = new Rotation(yaw, pitch);
         if (Rockstar.getInstance().getTargetManager().getCurrentTarget() != null) {
         }
      }
   }

   public void rotate(Rotation rotation, MoveCorrection moveCorrection, float yawSpeed, float pitchSpeed, float returnSpeed, RotationPriority priority) {
      int priorityValue = priority.getPriority();
      if (this.currentTask == null || this.currentTask.getPriority() <= priorityValue || this.state != RotationState.ROTATING) {
         rotation.setYaw(
            RotationMath.adjustAngle(this.currentTask == null ? this.getPlayerRotation().getYaw() : this.currentTask.getRotation().getYaw(), rotation.getYaw())
         );
         this.currentTask = new RotationTask(rotation, moveCorrection, yawSpeed, pitchSpeed, returnSpeed, priorityValue);
         this.rotationIdle.reset();
      }
   }

   public void rotate(Rotation rotation, MoveCorrection moveCorrection, float yawSpeed, float pitchSpeed, float returnSpeed) {
      this.rotate(rotation, moveCorrection, yawSpeed, pitchSpeed, returnSpeed, RotationPriority.NORMAL);
   }

   public void rotate(Rotation rotation, RotationPriority priority) {
      this.rotate(rotation, MoveCorrection.DIRECT, 180.0F, 180.0F, 180.0F, priority);
   }

   public void rotate(Rotation rotation) {
      this.rotate(rotation, MoveCorrection.DIRECT, 180.0F, 180.0F, 180.0F, RotationPriority.NORMAL);
   }

   private float moveTowardsAngle(float current, float target, float speed) {
      float difference = RotationMath.getAngleDifference(current, target);
      return Math.abs(difference) <= speed ? target : current + Math.signum(difference) * speed;
   }

   public void rotateTowards(Entity entity, long yawSpeed, long pitchSpeed, long returnSpeed, RotationPriority priority, MoveCorrection moveCorrection) {
      if (entity != null && mc.player != null) {
         double posX = entity.getX();
         double posY = entity.getY() + entity.getEyeHeight(entity.getPose());
         double posZ = entity.getZ();
         double deltaX = posX - mc.player.getX();
         double deltaY = posY - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
         double deltaZ = posZ - mc.player.getZ();
         double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
         float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
         Rotation targetRotation = new Rotation(yaw, pitch);
         this.rotate(targetRotation, moveCorrection, (float)yawSpeed, (float)pitchSpeed, (float)returnSpeed, priority);
      }
   }

   public Rotation getRotation(LivingEntity entity) {
      return new Rotation(entity.getYaw(), entity.getPitch());
   }

   public Rotation getPlayerRotation() {
      return mc.player == null ? Rotation.ZERO : this.getRotation(mc.player);
   }

   @Generated
   public RotationUpdateListener getRotationUpdateListener() {
      return this.rotationUpdateListener;
   }

   @Generated
   public Rotation getCurrentRotation() {
      return this.currentRotation;
   }

   @Generated
   public Rotation getServerRotation() {
      return this.serverRotation;
   }

   @Generated
   public Rotation getPrevRotation() {
      return this.prevRotation;
   }

   @Generated
   public Rotation getRenderRotation() {
      return this.renderRotation;
   }

   @Generated
   public RotationState getState() {
      return this.state;
   }

   @Generated
   public Timer getRotationIdle() {
      return this.rotationIdle;
   }

   @Generated
   public void setCurrentRotation(Rotation currentRotation) {
      this.currentRotation = currentRotation;
   }

   @Generated
   public void setPrevRotation(Rotation prevRotation) {
      this.prevRotation = prevRotation;
   }

   @Generated
   public void setRenderRotation(Rotation renderRotation) {
      this.renderRotation = renderRotation;
   }

   @Generated
   public void setState(RotationState state) {
      this.state = state;
   }

   @Generated
   public void setCurrentTask(@Nullable RotationTask currentTask) {
      this.currentTask = currentTask;
   }

   @Nullable
   @Generated
   public RotationTask getCurrentTask() {
      return this.currentTask;
   }
}
