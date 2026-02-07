package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;

@ModuleInfo(name = "Removals", category = ModuleCategory.VISUALS, enabledByDefault = true, desc = "modules.descriptions.removals")
public class Removals extends BaseModule {
   private double oldFovEffectScale;
   private final SelectSetting effects = new SelectSetting(this, "modules.settings.removals.effects");
   private final SelectSetting.Value hurtCam = new SelectSetting.Value(this.effects, "modules.settings.removals.hurtCam").select();
   private final SelectSetting.Value scoreboard = new SelectSetting.Value(this.effects, "modules.settings.removals.scoreboard");
   private final SelectSetting.Value bossBar = new SelectSetting.Value(this.effects, "modules.settings.removals.bossBar");
   private final SelectSetting.Value portal = new SelectSetting.Value(this.effects, "modules.settings.removals.portal").select();
   private final SelectSetting.Value fire = new SelectSetting.Value(this.effects, "modules.settings.removals.fire").select();
   private final SelectSetting.Value clip = new SelectSetting.Value(this.effects, "modules.settings.removals.clip").select();
   private final SelectSetting.Value breakParticles = new SelectSetting.Value(this.effects, "modules.settings.removals.breakParticles");
   private final SelectSetting.Value water = new SelectSetting.Value(this.effects, "modules.settings.removals.water");
   private final SelectSetting.Value nausea = new SelectSetting.Value(this.effects, "modules.settings.removals.nausea").select();
   private final SelectSetting.Value blindness = new SelectSetting.Value(this.effects, "modules.settings.removals.blindness");
   private final SelectSetting.Value pumpkin = new SelectSetting.Value(this.effects, "modules.settings.removals.pumpkin");
   private final SelectSetting.Value fov = new SelectSetting.Value(this.effects, "modules.settings.removals.fov").select();
   private final SelectSetting.Value weather = new SelectSetting.Value(this.effects, "modules.settings.removals.weather");
   private final SelectSetting sounds = new SelectSetting(this, "modules.settings.removals.sounds");
   private final SelectSetting.Value beacon = new SelectSetting.Value(this.sounds, "modules.settings.removals.beacon");
   private final SelectSetting.Value phantoms = new SelectSetting.Value(this.sounds, "modules.settings.removals.phantoms");
   private final SelectSetting.Value weatherSound = new SelectSetting.Value(this.sounds, "modules.settings.removals.weatherSound");
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (this.fov.isSelected()) {
         mc.options.getFovEffectScale().setValue(0.0);
      }
   };

   @Override
   public void onEnable() {
      this.oldFovEffectScale = (Double)mc.options.getFovEffectScale().getValue();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      mc.options.getFovEffectScale().setValue(this.oldFovEffectScale);
      super.onDisable();
   }

   @Generated
   public double getOldFovEffectScale() {
      return this.oldFovEffectScale;
   }

   @Generated
   public SelectSetting getEffects() {
      return this.effects;
   }

   @Generated
   public SelectSetting.Value getHurtCam() {
      return this.hurtCam;
   }

   @Generated
   public SelectSetting.Value getScoreboard() {
      return this.scoreboard;
   }

   @Generated
   public SelectSetting.Value getBossBar() {
      return this.bossBar;
   }

   @Generated
   public SelectSetting.Value getPortal() {
      return this.portal;
   }

   @Generated
   public SelectSetting.Value getFire() {
      return this.fire;
   }

   @Generated
   public SelectSetting.Value getClip() {
      return this.clip;
   }

   @Generated
   public SelectSetting.Value getBreakParticles() {
      return this.breakParticles;
   }

   @Generated
   public SelectSetting.Value getWater() {
      return this.water;
   }

   @Generated
   public SelectSetting.Value getNausea() {
      return this.nausea;
   }

   @Generated
   public SelectSetting.Value getBlindness() {
      return this.blindness;
   }

   @Generated
   public SelectSetting.Value getPumpkin() {
      return this.pumpkin;
   }

   @Generated
   public SelectSetting.Value getFov() {
      return this.fov;
   }

   @Generated
   public SelectSetting.Value getWeather() {
      return this.weather;
   }

   @Generated
   public SelectSetting getSounds() {
      return this.sounds;
   }

   @Generated
   public SelectSetting.Value getBeacon() {
      return this.beacon;
   }

   @Generated
   public SelectSetting.Value getPhantoms() {
      return this.phantoms;
   }

   @Generated
   public SelectSetting.Value getWeatherSound() {
      return this.weatherSound;
   }

   @Generated
   public EventListener<ClientPlayerTickEvent> getOnUpdateEvent() {
      return this.onUpdateEvent;
   }
}
