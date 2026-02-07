package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;

public class FireworkEvent extends Event {
   private final LivingEntity entity;
   private Vec3d velocity;
   private final FireworkRocketEntity rocketEntity;

   @Generated
   public LivingEntity getEntity() {
      return this.entity;
   }

   @Generated
   public Vec3d getVelocity() {
      return this.velocity;
   }

   @Generated
   public FireworkRocketEntity getRocketEntity() {
      return this.rocketEntity;
   }

   @Generated
   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }

   @Generated
   public FireworkEvent(LivingEntity entity, Vec3d velocity, FireworkRocketEntity rocketEntity) {
      this.entity = entity;
      this.velocity = velocity;
      this.rocketEntity = rocketEntity;
   }
}
