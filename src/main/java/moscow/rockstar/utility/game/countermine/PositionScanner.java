package moscow.rockstar.utility.game.countermine;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class PositionScanner implements IMinecraft {
   private final List<Point> points = new ArrayList<>();
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      CounterMine cm = Rockstar.getInstance().getModuleManager().getModule(CounterMine.class);
      this.points.clear();

      for (Entity entity : mc.world.getEntities()) {
         Box boundingBox = entity.getBoundingBox();
         if (boundingBox.maxX == boundingBox.minX && !(mc.player.distanceTo(entity) < 2.0F) && entity.getName().getString().contains("предмета")) {
            boolean hologramNearby = CMUtility.isHologramNearby(entity, mc.world, 2.5);
            if (entity instanceof ItemDisplayEntity itemDisplay) {
               String modelId = CMUtility.getModelIdFromNbt(itemDisplay.getItemStack(), mc.player.getRegistryManager());
               if (modelId != null) {
                  String modelJson = CMUtility.findHashedModel(modelId);
                  if (modelJson != null) {
                     boolean sex = false;
                     if (modelJson.contains("{\"elements\":[{\"from\":[7.765625,8.0,7.765625]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.765625,8.0,7.8828125]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.9375,7.796875,7.875]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.921875,7.7421875,7.8828125]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.8828125,7.6484375,7.8828125]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.9003906,7.9003906,7.8691406]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.90625,7.7421875,7.8828125]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.9453125,7.6484375,7.8828125]")
                        || modelJson.contains("{\"elements\":[{\"from\":[7.8984375,7.8984375,7.8671875]")) {
                        boolean dead = false;
                        if (!dead) {
                           if (modelJson.contains("{\"elements\":[{\"from\":[7.765625,8.0,7.765625]")) {
                              NbtCompound nbt = new NbtCompound();
                              entity.writeNbt(nbt);
                              if (nbt.contains("transformation")) {
                                 NbtCompound transformation = nbt.getCompound("transformation");
                                 if (transformation.contains("left_rotation")) {
                                    NbtList rotation = transformation.getList("left_rotation", 5);
                                    float x = rotation.getFloat(0);
                                    float y = rotation.getFloat(1);
                                    float z = rotation.getFloat(2);
                                    float w = rotation.getFloat(3);
                                    Vec3d tiltedPos = this.calculateTiltedPoint(entity.getPos(), x, y, z, w, 0.25F);
                                    if (Rockstar.getInstance().getModuleManager().getModule(CounterMine.class).isMinDamage()) {
                                       this.points.add(new Point(entity, x > -0.04F, hologramNearby, entity.getPos().add(0.0, -0.4F, 0.0)));
                                    } else {
                                       this.points.add(new Point(entity, x > -0.04F, hologramNearby, tiltedPos));
                                    }
                                 } else {
                                    this.points.add(new Point(entity, true, hologramNearby, entity.getPos().add(0.0, 0.4F, 0.0)));
                                 }
                              } else {
                                 this.points.add(new Point(entity, true, hologramNearby, entity.getPos().add(0.0, 0.4F, 0.0)));
                              }
                           } else {
                              this.points.add(new Point(entity, false, hologramNearby, entity.getPos()));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   };

   public Vec3d calculateTiltedPoint(Vec3d entityPos, float x, float y, float z, float w, float heightOffset) {
      float norm = (float)Math.sqrt(x * x + y * y + z * z + w * w);
      if (norm > 0.0F) {
         x /= norm;
         y /= norm;
         z /= norm;
         w /= norm;
      }

      float rotationIntensity = Math.abs(x) + Math.abs(y) + Math.abs(z);
      float adjustedOffset = heightOffset * (1.0F + rotationIntensity);
      Vec3d upVector = new Vec3d(0.0, adjustedOffset, 0.0);
      Vec3d rotatedVector = this.rotateVectorByQuaternion(upVector, x, y, z, w);
      return entityPos.add(rotatedVector);
   }

   private Vec3d rotateVectorByQuaternion(Vec3d vector, float qx, float qy, float qz, float qw) {
      float norm = (float)Math.sqrt(qx * qx + qy * qy + qz * qz + qw * qw);
      if (norm > 0.0F) {
         qx /= norm;
         qy /= norm;
         qz /= norm;
         qw /= norm;
      }

      float x = (float)vector.x;
      float y = (float)vector.y;
      float z = (float)vector.z;
      float qx2 = qx * qx;
      float qy2 = qy * qy;
      float qz2 = qz * qz;
      float qw2 = qw * qw;
      float rotatedX = x * (qw2 + qx2 - qy2 - qz2) + 2.0F * y * (qx * qy - qw * qz) + 2.0F * z * (qx * qz + qw * qy);
      float rotatedY = 2.0F * x * (qx * qy + qw * qz) + y * (qw2 - qx2 + qy2 - qz2) + 2.0F * z * (qy * qz - qw * qx);
      float rotatedZ = 2.0F * x * (qx * qz - qw * qy) + 2.0F * y * (qy * qz + qw * qx) + z * (qw2 - qx2 - qy2 + qz2);
      return new Vec3d(rotatedX, rotatedY, rotatedZ);
   }

   public Vector3f quaternionToEuler(float x, float y, float z, float w) {
      float norm = (float)Math.sqrt(x * x + y * y + z * z + w * w);
      x /= norm;
      y /= norm;
      z /= norm;
      w /= norm;
      float yaw = (float)Math.atan2(2.0F * (w * y + x * z), 1.0F - 2.0F * (y * y + z * z));
      float pitch = (float)Math.asin(Math.max(-1.0F, Math.min(1.0F, 2.0F * (w * x - y * z))));
      float roll = (float)Math.atan2(2.0F * (w * z + x * y), 1.0F - 2.0F * (x * x + z * z));
      return new Vector3f((float)Math.toDegrees(pitch), (float)Math.toDegrees(yaw), (float)Math.toDegrees(roll));
   }

   public Vec3d calculateTiltedPoint(Vec3d entityPos, Vector3f eulerAngles, float heightOffset) {
      float pitch = (float)Math.toRadians(eulerAngles.x);
      float yaw = (float)Math.toRadians(eulerAngles.y);
      float roll = (float)Math.toRadians(eulerAngles.z);
      double dx = -MathUtility.sin(yaw) * MathUtility.cos(pitch) * heightOffset;
      double dy = MathUtility.sin(pitch) * heightOffset;
      double dz = MathUtility.cos(yaw) * MathUtility.cos(pitch) * heightOffset;
      return new Vec3d(entityPos.x + dx, entityPos.y + dy, entityPos.z + dz);
   }

   @Generated
   public List<Point> getPoints() {
      return this.points;
   }
}
