package moscow.rockstar.systems.modules.modules.movement;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;

@ModuleInfo(name = "Timer", category = ModuleCategory.MOVEMENT, desc = "modules.descriptions.timer")
public class Timer extends BaseModule {
   private final SliderSetting speed = new SliderSetting(this, "modules.settings.timer.speed").step(0.1F).min(0.1F).max(15.0F).currentValue(1.0F);

   @Override
   public void tick() {
      EntityUtility.setTimer(this.speed.getCurrentValue());
      super.tick();
   }

   @Override
   public void onDisable() {
      EntityUtility.resetTimer();
      super.onDisable();
   }

   @Generated
   public SliderSetting getSpeed() {
      return this.speed;
   }
}
