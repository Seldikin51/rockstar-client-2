package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.mixins.StatusEffectInstanceAddition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements StatusEffectInstanceAddition {
   @Unique
   private final Animation potionStatusAnimation = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   @Unique
   private AnimatedNumber timeAnimation;

   @Inject(method = "<init>(Lnet/minecraft/registry/entry/RegistryEntry;IIZZZLnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("TAIL"))
   public void onInit(
      RegistryEntry<?> effect,
      int duration,
      int amplifier,
      boolean ambient,
      boolean showParticles,
      boolean showIcon,
      StatusEffectInstance hiddenEffect,
      CallbackInfo ci
   ) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
         this.timeAnimation = new AnimatedNumber(Fonts.REGULAR.getFont(7.0F), 3.0F, 300L, Easing.FIGMA_EASE_IN_OUT);
      }
   }

   @Override
   public Animation rockstar$getAnimPotion() {
      return this.potionStatusAnimation;
   }

   @Override
   public AnimatedNumber rockstar$getTimeAnimation() {
      return this.timeAnimation;
   }
}
