package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.InternalAttackEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.Hand;

@ModuleInfo(name = "Player Utils", category = ModuleCategory.PLAYER, desc = "Утилиты для игрока")
public class PlayerUtils extends BaseModule {
   private final BooleanSetting antiAfk = new BooleanSetting(this, "Anti AFK", "Не позволяет серверу кикнуть вас, пока вы AFK");
   private final BooleanSetting autoRespawn = new BooleanSetting(this, "Auto Respawn", "Автоматически возрождает при смерти");
   private final BooleanSetting autoFish = new BooleanSetting(this, "Auto Fish", "Автоматически ловит рыбу");
   private final BooleanSetting fastLadder = new BooleanSetting(this, "Fast Ladder", "Ускоряет вас на лестнице");
   private final BooleanSetting noFriendDamage = new BooleanSetting(this, "Не бить друзей", "Не позволяет бить друзей");
   private final ModeSetting antiAFKMode = new ModeSetting(this, "Режимы", () -> !this.antiAfk.isEnabled());
   private final ModeSetting.Value chat = new ModeSetting.Value(this.antiAFKMode, "Писать в чат");
   private final ModeSetting.Value jump = new ModeSetting.Value(this.antiAFKMode, "Прыгать");
   private final ModeSetting.Value swing = new ModeSetting.Value(this.antiAFKMode, "Взмах рукой");
   private final SliderSetting delay = new SliderSetting(this, "Задержка", "Задержка для действий", () -> !this.antiAfk.isEnabled())
      .min(5.0F)
      .max(60.0F)
      .step(5.0F)
      .currentValue(50.0F);
   private final Timer timerAFK = new Timer();
   private final Timer fishTimer = new Timer();
   private boolean hookFlag;
   private boolean thrown;
   private boolean activeAFK;
   private final EventListener<InternalAttackEvent> onAttackEvent = event -> {
      if (this.noFriendDamage.isEnabled()
         && event.getEntity() instanceof PlayerEntity
         && Rockstar.getInstance().getFriendManager().isFriend(event.getEntity().getName().getString())) {
         event.cancel();
      }
   };

   @Override
   public void tick() {
      if (this.antiAfk.isEnabled()) {
         if (this.timerAFK.finished(10000L)) {
            this.activeAFK = true;
         }

         if (EntityUtility.isPlayerMoving()) {
            this.activeAFK = false;
            this.timerAFK.reset();
         }

         if (this.activeAFK && mc.player.age % this.delay.getCurrentValue() == 5.0F) {
            if (this.chat.isSelected()) {
               mc.player.networkHandler.sendChatMessage("Всем привет " + Math.random() + " !");
            } else if (this.jump.isSelected() && mc.player.isOnGround()) {
               mc.player.jump();
            } else if (this.swing.isSelected()) {
               mc.player.swingHand(Hand.MAIN_HAND);
            }
         }
      }

      if (this.autoRespawn.isEnabled() && mc.currentScreen instanceof DeathScreen && mc.player != null) {
         mc.player.requestRespawn();
         mc.setScreen(null);
      }

      if (this.autoFish.isEnabled() && mc.player.getMainHandStack().getItem() instanceof FishingRodItem) {
         if (mc.player.fishHook != null) {
            this.thrown = true;
            this.fishTimer.reset();
            if (!this.hookFlag && (Boolean)mc.player.fishHook.getDataTracker().get(FishingBobberEntity.CAUGHT_FISH)) {
               this.throwRod();
               this.hookFlag = true;
               this.fishTimer.reset();
            }
         } else if (this.hookFlag && this.fishTimer.finished(600L)) {
            this.throwRod();
            this.hookFlag = false;
            this.thrown = false;
            this.fishTimer.reset();
         } else if (!this.hookFlag && this.thrown && this.fishTimer.finished(3000L)) {
            this.throwRod();
            this.thrown = false;
            this.fishTimer.reset();
         }
      }

      if (mc.player != null && mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.LADDER) && this.fastLadder.isEnabled()) {
         mc.player.setVelocity(mc.player.getVelocity().multiply(1.0, 1.43, 1.0));
      }

      super.tick();
   }

   private void throwRod() {
      mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      mc.player.swingHand(Hand.MAIN_HAND);
   }

   @Override
   public void onDisable() {
      this.hookFlag = false;
   }
}
