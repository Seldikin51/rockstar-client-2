package moscow.rockstar.systems.modules.modules.player;

import lombok.Generated;
import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;

@ModuleInfo(name = "No Delay", category = ModuleCategory.PLAYER, desc = "modules.descriptions.no_delay")
public class NoDelay extends BaseModule {
   private final BooleanSetting jump = new BooleanSetting(this, "modules.settings.no_delay.jump", "modules.settings.no_delay.jump.description").enable();
   private final BooleanSetting rightClick = new BooleanSetting(
      this, "modules.settings.no_delay.right_click", "modules.settings.no_delay.right_click.description"
   );

   @Override
   public void tick() {
      if (this.rightClick.isEnabled()) {
         ((IMinecraftClient)mc).setUseCooldown(0);
      }

      super.tick();
   }

   @Generated
   public BooleanSetting getJump() {
      return this.jump;
   }

   @Generated
   public BooleanSetting getRightClick() {
      return this.rightClick;
   }
}
