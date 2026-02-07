package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.BackTrack;
import moscow.rockstar.systems.modules.modules.combat.Hitboxes;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.mixins.BacktrackableEntity;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationTask;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements IMinecraft, BacktrackableEntity {
   @Shadow
   private Box boundingBox;
   @Unique
   private final List<BackTrack.Position> backTracks = new ArrayList<>();

   @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"))
   public boolean fixFalldistanceValue(boolean original) {
      return (Object) this == mc.player ? false : original;
   }

   @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
   public final void getBoundingBox(CallbackInfoReturnable<Box> cir) {
      Hitboxes hitbox = Rockstar.getInstance().getModuleManager().getModule(Hitboxes.class);
      Entity entity = (Entity) (Object) this;
      if (entity instanceof LivingEntity livingEntity && hitbox.isEnabled() && hitbox.shouldModifyHitbox(livingEntity)
            && entity.getId() != mc.player.getId()) {
         cir.setReturnValue(
               new Box(
                     this.boundingBox.minX - hitbox.getScale().getCurrentValue(),
                     this.boundingBox.minY,
                     this.boundingBox.minZ - hitbox.getScale().getCurrentValue(),
                     this.boundingBox.maxX + hitbox.getScale().getCurrentValue(),
                     this.boundingBox.maxY,
                     this.boundingBox.maxZ + hitbox.getScale().getCurrentValue()));
      }
   }

   @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
   public float movementCorrection(Entity instance) {
      RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
      RotationTask currentTask = rotationHandler.getCurrentTask();
      return currentTask != null && currentTask.getMoveCorrection() != MoveCorrection.NONE
            && instance instanceof ClientPlayerEntity
                  ? rotationHandler.getCurrentRotation().getYaw()
                  : instance.getYaw();
   }

   @Override
   public List<BackTrack.Position> rockstar2_0$getBackTracks() {
      return this.backTracks;
   }
}
