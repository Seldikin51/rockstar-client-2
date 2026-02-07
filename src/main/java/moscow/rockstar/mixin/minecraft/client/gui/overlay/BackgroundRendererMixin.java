package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.CustomFog;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
   @Inject(
      method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
      Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
      if (removals.isEnabled() && removals.getBlindness().isSelected()) {
         info.setReturnValue(null);
      }
   }

   @ModifyReturnValue(method = "applyFog", at = @At("RETURN"))
   private static Fog modifyFogProperties(
      Fog original, @Local(argsOnly = true) Camera camera, @Local(argsOnly = true) FogType fogType, @Local(argsOnly = true, ordinal = 0) float viewDistance
   ) {
      CustomFog customFogModule = Rockstar.getInstance().getModuleManager().getModule(CustomFog.class);
      if (customFogModule.shouldModifyFog(camera) && fogType == FogType.FOG_TERRAIN) {
         float start = MathHelper.clamp(customFogModule.getDistance().getFirstValue(), -8.0F, viewDistance);
         float end = MathHelper.clamp(customFogModule.getDistance().getSecondValue(), 0.0F, viewDistance);
         ColorRGBA color = customFogModule.getFogColor().getColor();
         FogShape shape = FogShape.SPHERE;
         float r = color.getRed() / 255.0F;
         float g = color.getGreen() / 255.0F;
         float b = color.getBlue() / 255.0F;
         float a = color.getAlpha() / 255.0F;
         return new Fog(start, end, shape, r, g, b, a);
      } else {
         return original;
      }
   }
}
