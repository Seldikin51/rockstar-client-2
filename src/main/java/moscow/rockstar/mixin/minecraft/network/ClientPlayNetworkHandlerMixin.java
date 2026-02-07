package moscow.rockstar.mixin.minecraft.network;

import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.EntityS2CPacketAccessor;
import moscow.rockstar.systems.modules.modules.combat.BackTrack;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.mixins.BacktrackableEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin implements IMinecraft {
   @Inject(method = "onEntity", at = @At("TAIL"))
   public void onEntity(EntityS2CPacket packet, CallbackInfo ci) {
      ClientPlayNetworkHandler self = (ClientPlayNetworkHandler) (Object) this;
      ClientWorld world = self.getWorld();
      if (world != null && Rockstar.getInstance().getModuleManager().getModule(BackTrack.class).isEnabled()) {
         int id = ((EntityS2CPacketAccessor) packet).getId();
         Entity entity = world.getEntityById(id);
         if (entity != null) {
            double dx = packet.getDeltaX() / 4096.0;
            double dy = packet.getDeltaY() / 4096.0;
            double dz = packet.getDeltaZ() / 4096.0;
            Vec3d newPos = entity.getPos().add(dx, dy, dz);
            if (entity instanceof BacktrackableEntity backtrackable) {
               backtrackable.rockstar2_0$getBackTracks()
                     .add(new BackTrack.Position(newPos, System.currentTimeMillis()));
            }
         }
      }
   }
}
