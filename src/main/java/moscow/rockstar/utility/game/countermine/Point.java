package moscow.rockstar.utility.game.countermine;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Point {
   Entity entity;
   boolean target;
   boolean isFriend;
   Vec3d pos;

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public boolean isTarget() {
      return this.target;
   }

   @Generated
   public boolean isFriend() {
      return this.isFriend;
   }

   @Generated
   public Vec3d getPos() {
      return this.pos;
   }

   @Generated
   public Point(Entity entity, boolean target, boolean isFriend, Vec3d pos) {
      this.entity = entity;
      this.target = target;
      this.isFriend = isFriend;
      this.pos = pos;
   }
}
