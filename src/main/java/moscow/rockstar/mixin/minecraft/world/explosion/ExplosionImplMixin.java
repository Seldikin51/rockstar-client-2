package moscow.rockstar.mixin.minecraft.world.explosion;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.ExplosionImplAccessor;
import moscow.rockstar.systems.event.impl.game.AncientDebrisEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionImplMixin implements IMinecraft {
   @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionImpl;damageEntities()V", shift = Shift.AFTER))
   private void onAfterDamageEntities(CallbackInfo ci) {
      ExplosionImpl self = (ExplosionImpl) (Object) this;
      List<BlockPos> affectedBlocks = ((ExplosionImplAccessor) self).invokeGetBlocksToDestroy();
      List<BlockPos> debris = affectedBlocks.stream()
            .filter(pos -> self.getWorld().getBlockState(pos).isOf(Blocks.ANCIENT_DEBRIS)).toList();
      if (!debris.isEmpty() && self.getWorld().getRegistryKey() == World.NETHER) {
         Rockstar.getInstance().getEventManager().triggerEvent(new AncientDebrisEvent(debris, self.getPosition()));
      }
   }
}
