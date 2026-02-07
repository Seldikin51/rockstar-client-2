package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.CollisionShapeEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import net.minecraft.block.Blocks;

@ModuleInfo(name = "No Web", category = ModuleCategory.MOVEMENT)
public class NoWeb extends BaseModule {
   private final EventListener<CollisionShapeEvent> onCollision = event -> {
      if ((event.getState().getBlock() == Blocks.COBWEB || event.getState().getBlock() == Blocks.SWEET_BERRY_BUSH)
         && this.lengthSquared() > 1.0E-7
         && mc.options.forwardKey.isPressed()) {
         EntityUtility.setSpeed(0.66F);
         if (ServerUtility.isFT() || ServerUtility.isST()) {
            EntityUtility.setSpeed(0.12F);
         }
      }
   };

   public double lengthSquared() {
      return mc.player.getX() * mc.player.getX() + mc.player.getY() * mc.player.getY() + mc.player.getZ() * mc.player.getZ();
   }
}
