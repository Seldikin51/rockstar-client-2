package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import net.minecraft.entity.effect.StatusEffects;

@ModuleInfo(name = "Effect Remover", category = ModuleCategory.OTHER)
public class EffectRemover extends BaseModule {
   private final SelectSetting effectsToRemove = new SelectSetting(this, "modules.settings.effect_remover.remove");
   private final SelectSetting.Value levitation = new SelectSetting.Value(this.effectsToRemove, "modules.settings.effect_remover.remove.levitation").select();
   private final SelectSetting.Value jumpBoost = new SelectSetting.Value(this.effectsToRemove, "modules.settings.effect_remover.remove.jump_boost").select();
   private final SelectSetting.Value slowFall = new SelectSetting.Value(this.effectsToRemove, "modules.settings.effect_remover.remove.slow_fall").select();
   private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
      if (mc.player != null) {
         if (this.levitation.isSelected()) {
            mc.player.removeStatusEffect(StatusEffects.LEVITATION);
         }

         if (this.jumpBoost.isSelected()) {
            mc.player.removeStatusEffect(StatusEffects.JUMP_BOOST);
         }

         if (this.slowFall.isSelected()) {
            mc.player.removeStatusEffect(StatusEffects.SLOW_FALLING);
         }
      }
   };
}
