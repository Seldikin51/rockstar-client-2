package moscow.rockstar.mixin.minecraft.world;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
   @Inject(method = "handleBlockUpdate", at = @At("HEAD"))
   private void onHandleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
      XRay xrayModule = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
      if (xrayModule != null && xrayModule.isEnabled()) {
         Block block = state.getBlock();
         BlockPos immutablePos = pos.toImmutable();
         if (xrayModule.isBlockEnabled(block)) {
            xrayModule.getCachedBlocks().add(immutablePos);
         } else {
            xrayModule.getCachedBlocks().remove(immutablePos);
         }
      }
   }
}
