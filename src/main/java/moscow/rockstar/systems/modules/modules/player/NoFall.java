package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "No Fall", category = ModuleCategory.PLAYER)
public class NoFall extends BaseModule {
   @Override
   public void tick() {
      if (mc.player.fallDistance > 2.5) {
         Vec3d pos = mc.player.getPos();
         mc.player.networkHandler.sendPacket(new Full(pos.x, pos.y, pos.z, mc.player.getYaw(), mc.player.getPitch(), true, true));
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
         mc.player.fallDistance = 0.0F;
      }
   }
}
