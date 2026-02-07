package moscow.rockstar.utility.render;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class Utils implements IMinecraft {
   private static Matrix4f modelViewMatrix;
   private static Matrix4f projectionMatrix;

   public static void onRender(Matrix4f modelView, Matrix4f projection) {
      modelViewMatrix = new Matrix4f(modelView);
      projectionMatrix = new Matrix4f(projection);
   }

   public static Vec2f worldToScreen(Vec3d worldCoords) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d delta = worldCoords.subtract(camera.getPos());
      Vector4f clipSpacePos = new Vector4f((float)delta.x, (float)delta.y, (float)delta.z, 1.0F);
      clipSpacePos.mul(modelViewMatrix).mul(projectionMatrix);
      if (clipSpacePos.w <= 0.0F) {
         return null;
      } else {
         Vector4f ndcSpacePos = clipSpacePos.div(clipSpacePos.w);
         float screenX = (ndcSpacePos.x + 1.0F) / 2.0F * mc.getWindow().getScaledWidth();
         float screenY = (1.0F - ndcSpacePos.y) / 2.0F * mc.getWindow().getScaledHeight();
         return new Vec2f(screenX, screenY);
      }
   }

   public static Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
         MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
         MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ())
      );
   }

   public static Vec3d getInterpolatedPos(Vec3d prev, Vec3d pos, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp(tickDelta, prev.x, pos.getX()), MathHelper.lerp(tickDelta, prev.y, pos.getY()), MathHelper.lerp(tickDelta, prev.z, pos.getZ())
      );
   }

   @Generated
   private Utils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
