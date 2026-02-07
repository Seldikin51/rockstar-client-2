package moscow.rockstar.systems.modules.constructions.swinganim;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class SwingPhase extends SwingSettings {
   private final SliderSetting anchorX = new SwingPhase.PhaseSlider(this, "swing.anchorX").step(0.05F).min(-5.0F).max(5.0F).currentValue(0.0F);
   private final SliderSetting anchorY = new SwingPhase.PhaseSlider(this, "swing.anchorY").step(0.05F).min(-5.0F).max(5.0F).currentValue(0.0F);
   private final SliderSetting anchorZ = new SwingPhase.PhaseSlider(this, "swing.anchorZ").step(0.05F).min(-5.0F).max(5.0F).currentValue(0.0F);
   private final SliderSetting moveX = new SwingPhase.PhaseSlider(this, "swing.moveX").step(0.05F).min(-5.0F).max(5.0F).currentValue(0.0F);
   private final SliderSetting moveY = new SwingPhase.PhaseSlider(this, "swing.moveY").step(0.05F).min(-5.0F).max(5.0F).currentValue(0.0F);
   private final SliderSetting moveZ = new SwingPhase.PhaseSlider(this, "swing.moveZ").step(0.05F).min(-3.0F).max(3.0F).currentValue(0.0F);
   private final SliderSetting rotateX = new SwingPhase.PhaseSlider(this, "swing.rotateX").step(15.0F).min(-360.0F).max(360.0F).currentValue(0.0F);
   private final SliderSetting rotateY = new SwingPhase.PhaseSlider(this, "swing.rotateY").step(15.0F).min(-360.0F).max(360.0F).currentValue(0.0F);
   private final SliderSetting rotateZ = new SwingPhase.PhaseSlider(this, "swing.rotateZ").step(15.0F).min(-360.0F).max(360.0F).currentValue(0.0F);

   @Generated
   public SliderSetting getAnchorX() {
      return this.anchorX;
   }

   @Generated
   public SliderSetting getAnchorY() {
      return this.anchorY;
   }

   @Generated
   public SliderSetting getAnchorZ() {
      return this.anchorZ;
   }

   @Generated
   public SliderSetting getMoveX() {
      return this.moveX;
   }

   @Generated
   public SliderSetting getMoveY() {
      return this.moveY;
   }

   @Generated
   public SliderSetting getMoveZ() {
      return this.moveZ;
   }

   @Generated
   public SliderSetting getRotateX() {
      return this.rotateX;
   }

   @Generated
   public SliderSetting getRotateY() {
      return this.rotateY;
   }

   @Generated
   public SliderSetting getRotateZ() {
      return this.rotateZ;
   }

   public static class PhaseSlider extends SliderSetting {
      public PhaseSlider(@NotNull SettingsContainer parent, String name) {
         super(parent, name);
      }

      @Override
      public void setCurrentValue(float currentValue) {
         super.setCurrentValue(currentValue);
         if (SwingAnimScreen.hasShiftDown()) {
            for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
               if (setting.getName().equals(this.getName()) && setting instanceof SwingPhase.PhaseSlider slider) {
                  slider.silentSet(currentValue);
               }
            }

            for (Setting settingx : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
               if (settingx.getName().equals(this.getName()) && settingx instanceof SwingPhase.PhaseSlider slider) {
                  slider.silentSet(currentValue);
               }
            }
         }
      }

      private void silentSet(float value) {
         this.currentValue = MathHelper.clamp((float)(Math.round(value * (1.0 / this.step)) / (1.0 / this.step)), this.min, this.max);
      }
   }
}
