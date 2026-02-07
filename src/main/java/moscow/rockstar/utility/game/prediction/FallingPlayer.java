package moscow.rockstar.utility.game.prediction;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FallingPlayer {
   private final ClientPlayerEntity player;
   private double x;
   private double y;
   private double z;
   private double motionX;
   private double motionY;
   private double motionZ;
   private final float yaw;
   private int simulatedTicks;

   public FallingPlayer(ClientPlayerEntity player, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw) {
      this.player = player;
      this.x = x;
      this.y = y;
      this.z = z;
      this.motionX = motionX;
      this.motionY = motionY;
      this.motionZ = motionZ;
      this.yaw = yaw;
      this.simulatedTicks = 0;
   }

   public static FallingPlayer fromPlayer(ClientPlayerEntity player) {
      return new FallingPlayer(
         player,
         player.getPos().getX(),
         player.getPos().getY(),
         player.getPos().getZ(),
         player.getVelocity().x,
         player.getVelocity().y,
         player.getVelocity().z,
         player.getYaw()
      );
   }

   public boolean findFall(float fallDist) {
      Vec3d rotationVec = this.player.getRotationVec(0.0F);
      double tempMotionX = this.motionX;
      double tempMotionY = this.motionY;
      double tempMotionZ = this.motionZ;
      double d = 0.08;
      float n = MathHelper.cos(this.player.getPitch() * (float) (Math.PI / 180.0));
      n = (float)(n * n * Math.min(rotationVec.length() / 0.4, 1.0));
      Vec3d vec3d = new Vec3d(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
      tempMotionY = vec3d.y * 0.98F;
      return tempMotionY < fallDist;
   }

   public boolean findFall(float fallDist, int ticks) {
      Vec3d rotationVec = this.player.getRotationVec(0.0F);
      double tempMotionX = this.motionX;
      double tempMotionY = this.motionY;
      double tempMotionZ = this.motionZ;
      double d = 0.08;
      float n = MathHelper.cos(this.player.getPitch() * (float) (Math.PI / 180.0));
      n = (float)(n * n * Math.min(rotationVec.length() / 0.4, 1.0));

      for (int i = 0; i < ticks; i++) {
         Vec3d vec3d = new Vec3d(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + n * 0.75), 0.0);
         tempMotionY = vec3d.y * 0.98F;
         if (tempMotionY >= fallDist) {
            return false;
         }
      }

      return true;
   }
}
