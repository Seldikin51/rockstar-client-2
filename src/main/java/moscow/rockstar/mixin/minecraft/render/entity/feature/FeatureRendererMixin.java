package moscow.rockstar.mixin.minecraft.render.entity.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.AntiInvisible;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureRenderer.class)
public abstract class FeatureRendererMixin {
   @Unique
   private static final AntiInvisible ANTI_INVISIBLE_MODULE = Rockstar.getInstance().getModuleManager().getModule(AntiInvisible.class);

   @WrapOperation(
      method = "renderModel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"
      )
   )
   private static void changeModelColor(
      EntityModel<?> instance,
      MatrixStack matrixStack,
      VertexConsumer vertexConsumer,
      int light,
      int overlay,
      int color,
      Operation<Void> original,
      @Local(argsOnly = true) LivingEntityRenderState state
   ) {
      if (ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.shouldModifyOpacity(state)) {
         Entity entity = ((EntityRenderStateAddition)state).rockstar$getEntity();
         color = entity instanceof ArmorStandEntity
            ? Colors.WHITE.withAlpha(0.0F).getRGB()
            : Colors.WHITE.withAlpha(ANTI_INVISIBLE_MODULE.getOpacity().getCurrentValue() / 100.0F * 255.0F).getRGB();
      }

      original.call(new Object[]{instance, matrixStack, vertexConsumer, light, overlay, color});
   }

   @WrapOperation(
      method = "renderModel",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayer;getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      )
   )
   private static RenderLayer changeModelRenderLayer(Identifier texture, Operation<RenderLayer> original, @Local(argsOnly = true) LivingEntityRenderState state) {
      return ANTI_INVISIBLE_MODULE.isEnabled() && ANTI_INVISIBLE_MODULE.shouldModifyOpacity(state)
         ? RenderLayer.getItemEntityTranslucentCull(texture)
         : (RenderLayer)original.call(new Object[]{texture});
   }
}
