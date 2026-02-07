package moscow.rockstar.protection.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.SoundEvent;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SoundSystemMixinProtection {
   public static void playSound(SoundInstance sound, CallbackInfo ci) {
      Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
      if (removals.isEnabled()
         && removals.getBeacon().isSelected()
         && (
            sound.getId().equals(SoundEvents.BLOCK_BEACON_ACTIVATE.id())
               || sound.getId().equals(SoundEvents.BLOCK_BEACON_AMBIENT.id())
               || sound.getId().equals(SoundEvents.BLOCK_BEACON_POWER_SELECT.id())
               || sound.getId().equals(SoundEvents.BLOCK_BEACON_DEACTIVATE.id())
         )) {
         ci.cancel();
      }

      if (removals.isEnabled()
         && removals.getWeatherSound().isSelected()
         && (
            sound.getId().equals(SoundEvents.WEATHER_RAIN.id())
               || sound.getId().equals(SoundEvents.WEATHER_RAIN_ABOVE.id())
               || sound.getId().equals(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER.id())
         )) {
         ci.cancel();
      }

      if (removals.isEnabled()
         && removals.getPhantoms().isSelected()
         && (
            sound.getId().equals(SoundEvents.ENTITY_PARROT_IMITATE_PHANTOM.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_AMBIENT.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_BITE.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_FLAP.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_DEATH.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_HURT.id())
               || sound.getId().equals(SoundEvents.ENTITY_PHANTOM_SWOOP.id())
         )) {
         ci.cancel();
      }

      Rockstar.getInstance().getEventManager().triggerEvent(new SoundEvent(sound));
   }
}
