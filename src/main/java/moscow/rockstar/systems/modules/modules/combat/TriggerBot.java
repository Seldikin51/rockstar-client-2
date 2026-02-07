package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

@ModuleInfo(name = "Trigger Bot", category = ModuleCategory.COMBAT, desc = "modules.descriptions.trigger_bot")
public class TriggerBot extends BaseModule {
   private final Timer timer = new Timer();
   private final BooleanSetting onlyCrits = new BooleanSetting(this, "only_crits").enable();
   private final SelectSetting targets = new SelectSetting(this, "targets");
   private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "players").select();
   private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "animals").select();
   private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "mobs").select();
   private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "invisibles").select();
   private final SelectSetting.Value nakedPlayers = new SelectSetting.Value(this.targets, "nakedPlayers").select();
   private final SelectSetting.Value friends = new SelectSetting.Value(this.targets, "friends");

   @Override
   public void tick() {
      if (mc.player != null && mc.interactionManager != null) {
         TargetSettings settings = new TargetSettings.Builder()
            .targetPlayers(this.players.isSelected())
            .targetAnimals(this.animals.isSelected())
            .targetMobs(this.mobs.isSelected())
            .targetInvisibles(this.invisibles.isSelected())
            .targetNakedPlayers(this.nakedPlayers.isSelected())
            .targetFriends(this.friends.isSelected())
            .requiredRange(3.0F)
            .build();
         if (mc.targetedEntity instanceof LivingEntity livingEntity && settings.isEntityValid(livingEntity) && this.shouldAttack(livingEntity)) {
            mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.timer.reset();
         }

         super.tick();
      }
   }

   private boolean shouldAttack(LivingEntity entity) {
      if (mc.player == null) {
         return false;
      } else if (mc.player.getAttackCooldownProgress(0.5F) <= 0.93F) {
         return false;
      } else {
         return entity.distanceTo(mc.player) > 3.0F ? false : !this.onlyCrits.isEnabled() || CombatUtility.canPerformCriticalHit(entity, false);
      }
   }
}
