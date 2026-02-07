package moscow.rockstar.utility.game.countermine;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.animation.types.RotationAnimation;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RageBot implements IMinecraft {
   public static float TARGET_YAW;
   private BooleanSetting aim;
   private BooleanSetting rage;
   private BooleanSetting silent;
   private BooleanSetting autoShoot;
   private BooleanSetting autoStop;
   private SliderSetting shootDelay;
   private boolean stopping;
   private int stopTicks = -1;
   private final Timer shootingTimer = new Timer();
   private final RotationAnimation anim = new RotationAnimation(100L, 100L, Easing.BAKEK);
   private CounterMine counterMine;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      this.stopping = false;
      if (Math.abs(EntityUtility.getVelocity()) <= 0.1F) {
         this.stopTicks++;
      } else {
         this.stopTicks = 0;
      }

      if (this.aim.isEnabled()) {
         TARGET_YAW = mc.player.getYaw();
         TargetSettings settings = new TargetSettings.Builder().targetPlayers(true).requiredRange(200.0F).sortBy(TargetComparators.FOV).build();
         Rockstar.getInstance().getTargetManager().update(settings);
         Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
         if (targetEntity != null) {
            Vec3d pos = targetEntity.getPos().add(0.0, 0.2F, 0.0);
            Rotation toTarget = CMUtility.calculateRotation(pos);
            float yaw = toTarget.getYaw();
            float pitch = toTarget.getPitch();
            float deltaYaw = RotationMath.getAngleDifference(yaw, mc.player.getYaw());
            if (this.rage.isEnabled() || !(deltaYaw > 145.0F)) {
               if (this.rage.isEnabled()) {
                  if (this.silent.isEnabled()) {
                     Rockstar.getInstance().getRotationHandler().rotate(toTarget, MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
                  } else {
                     mc.player.setYaw(yaw);
                     mc.player.setPitch(pitch);
                     mc.player.setHeadYaw(yaw);
                  }
               } else if (this.silent.isEnabled()) {
                  if (!this.counterMine.getAntiAim().getAntiAim().isEnabled() && this.counterMine.getJumping().finished(1000L)) {
                     mc.player
                        .networkHandler
                        .sendPacket(
                           new Full(
                              mc.player.getX(),
                              mc.player.getY(),
                              mc.player.getZ(),
                              mc.player.getYaw(),
                              mc.player.getPitch(),
                              mc.player.isOnGround(),
                              mc.player.isOnGround()
                           )
                        );
                  }

                  Rockstar.getInstance()
                     .getRotationHandler()
                     .rotate(new Rotation(yaw, pitch), MoveCorrection.NONE, 180.0F, 180.0F, 180.0F, RotationPriority.TO_TARGET);
               } else {
                  this.anim.setRotation(new Rotation(yaw, pitch));
                  mc.player.setYaw(this.anim.getRotation().getYaw());
                  mc.player.setPitch(this.anim.getRotation().getPitch());
                  mc.player.setHeadYaw(this.anim.getRotation().getYaw());
               }

               TARGET_YAW = yaw;
               if (this.autoShoot.isEnabled()
                  && (this.rage.isEnabled() ? MathUtility.canShoot(pos) : MathUtility.canSeen(pos))
                  && this.shootingTimer.finished((long)this.shootDelay.getCurrentValue())) {
                  if (this.autoStop.isEnabled() && this.stopTicks <= 0) {
                     this.stop();
                  } else {
                     if (mc.player.isOnGround() || Math.abs(mc.player.getVelocity().y) < 0.05F) {
                        this.shot();
                        this.stop();
                     }
                  }
               }
            }
         }
      }
   };
   private final EventListener<InputEvent> onMove = event -> {
      if (this.stopping) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
      }
   };

   public RageBot(CounterMine cm) {
      this.counterMine = cm;
      this.aim = new BooleanSetting(cm, "Aim");
      this.rage = new BooleanSetting(cm, "Rage", () -> !this.aim.isEnabled());
      this.silent = new BooleanSetting(cm, "Silent", () -> !this.aim.isEnabled());
      this.autoShoot = new BooleanSetting(cm, "AutoShoot", () -> !this.aim.isEnabled());
      this.autoStop = new BooleanSetting(cm, "AutoStop", () -> !this.aim.isEnabled() || !this.autoShoot.isEnabled());
      this.shootDelay = new SliderSetting(cm, "Shoot Delay").min(0.0F).max(2000.0F).step(50.0F).currentValue(1000.0F).suffix(number -> " ms");
   }

   private void shot() {
      mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
      mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, BlockPos.ORIGIN, Direction.DOWN));
      this.shootingTimer.reset();
   }

   private void stop() {
      EntityUtility.setSpeed(0.0);
      this.stopping = true;
   }
}
