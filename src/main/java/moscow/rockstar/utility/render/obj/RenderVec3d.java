package moscow.rockstar.utility.render.obj;

import lombok.Generated;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

public class RenderVec3d extends Vec3d {
   private final Vec3d prev;

   public RenderVec3d(double x, double y, double z, Vec3d prev) {
      super(x, y, z);
      this.prev = prev;
   }

   public RenderVec3d(Vector3f vec, Vec3d prev) {
      super(vec);
      this.prev = prev;
   }

   public RenderVec3d(Vec3i vec, Vec3d prev) {
      super(vec);
      this.prev = prev;
   }

   @Generated
   public Vec3d getPrev() {
      return this.prev;
   }
}
