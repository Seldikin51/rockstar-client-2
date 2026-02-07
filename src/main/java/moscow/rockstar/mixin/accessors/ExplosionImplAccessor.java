package moscow.rockstar.mixin.accessors;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExplosionImpl.class)
public interface ExplosionImplAccessor {
   @Invoker("getBlocksToDestroy")
   List<BlockPos> invokeGetBlocksToDestroy();
}
