package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.mixin.accessors.EntityVelocityUpdateAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT, desc = "modules.descriptions.velocity")
public class Velocity extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "mode");
   private final ModeSetting.Value cancel = new ModeSetting.Value(this.mode, "modules.settings.velocity.default");
   private final ModeSetting.Value compensation = new ModeSetting.Value(this.mode, "modules.settings.velocity.compensation");
   private final ModeSetting.Value modify = new ModeSetting.Value(this.mode, "modules.settings.velocity.modify");
   private final SliderSetting modifierX = new SliderSetting(this, "modules.settings.velocity.velocity_x", () -> !this.mode.is(this.modify))
      .suffix("%")
      .currentValue(50.0F)
      .min(0.0F)
      .max(100.0F)
      .step(1.0F);
   private final SliderSetting modifierY = new SliderSetting(this, "modules.settings.velocity.velocity_y", () -> !this.mode.is(this.modify))
      .suffix("%")
      .currentValue(50.0F)
      .min(0.0F)
      .max(100.0F)
      .step(1.0F);
   private final SliderSetting modifierZ = new SliderSetting(this, "modules.settings.velocity.velocity_z", () -> !this.mode.is(this.modify))
      .suffix("%")
      .currentValue(50.0F)
      .min(0.0F)
      .max(100.0F)
      .step(1.0F);
   private Vec3d lastMotion = Vec3d.ZERO;
   private boolean gotVelocity;
   private boolean wasHurt;
   private boolean jumped;
   private final EventListener<ReceivePacketEvent> onVelocityPacket = event -> {
      if (mc.player != null && !mc.player.isDead()) {
         Packet<?> packet = event.getPacket();
         boolean isVelocityPacket = packet instanceof EntityVelocityUpdateS2CPacket velocityPacket && velocityPacket.getEntityId() == mc.player.getId();
         boolean isExplosionPacket = packet instanceof ExplosionS2CPacket;
         if (isVelocityPacket || isExplosionPacket) {
            if (this.mode.is(this.cancel)
               && packet instanceof EntityVelocityUpdateS2CPacket velocityPacketx
               && velocityPacketx.getEntityId() == mc.player.getId()) {
               event.cancel();
            } else if (this.mode.is(this.modify)
               && packet instanceof EntityVelocityUpdateS2CPacket velocityPacketx
               && velocityPacketx.getEntityId() == mc.player.getId()) {
               int velocityX = (int)(velocityPacketx.getVelocityX() * 8000.0 * this.modifierX.getCurrentValue() / 100.0);
               int velocityY = (int)(velocityPacketx.getVelocityY() * 8000.0 * this.modifierY.getCurrentValue() / 100.0);
               int velocityZ = (int)(velocityPacketx.getVelocityZ() * 8000.0 * this.modifierZ.getCurrentValue() / 100.0);
               EntityVelocityUpdateAccessor accessor = (EntityVelocityUpdateAccessor)velocityPacketx;
               accessor.setVelocityX(velocityX);
               accessor.setVelocityY(velocityY);
               accessor.setVelocityZ(velocityZ);
            } else if (this.mode.is(this.compensation)
               && packet instanceof EntityVelocityUpdateS2CPacket velocityPacketx
               && velocityPacketx.getEntityId() == mc.player.getId()) {
               this.lastMotion = new Vec3d(
                  velocityPacketx.getVelocityX() * 8000.0, velocityPacketx.getVelocityY() * 8000.0, velocityPacketx.getVelocityZ() * 8000.0
               );
               this.gotVelocity = true;
            }
         }
      }
   };

   @Override
   public void tick() {
      if (this.mode.is(this.compensation) && this.gotVelocity && mc.player != null) {
         if (mc.player.hurtTime > 0) {
            this.wasHurt = true;
         }

         if (this.wasHurt && mc.player.hurtTime == 0) {
            if (mc.player.isOnGround()) {
               mc.player.jump();
               this.jumped = true;
            }

            Vec3d moveDir = this.lastMotion.normalize();
            Vec3d updatedMotion = moveDir.multiply(-0.2F);
            mc.player.setVelocity(updatedMotion.x, updatedMotion.y, updatedMotion.z);
            this.wasHurt = false;
            this.gotVelocity = false;
         }

         if (this.jumped && mc.player.isOnGround()) {
            this.jumped = false;
         }

         super.tick();
      }
   }
}
