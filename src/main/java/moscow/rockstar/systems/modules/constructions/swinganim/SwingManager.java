package moscow.rockstar.systems.modules.constructions.swinganim;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.setting.settings.BezierSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class SwingManager {
   private final List<SwingPreset> presets = new ArrayList<>();
   private String current = "autosave";
   private final SwingSettings sharedSettings = new SwingSettings();
   private final SwingPhase startPhase = new SwingPhase();
   private final SwingPhase endPhase = new SwingPhase();
   private final BezierSetting bezier = new BezierSetting(this.sharedSettings, "animation").start(0.5F, 1.0F).end(0.5F, 0.0F);
   private final BooleanSetting back = new BooleanSetting(this.sharedSettings, "swing.back").enable();
   private final SliderSetting speed = new SliderSetting(this.sharedSettings, "swing.wing_speed").step(1.0F).min(1.0F).max(5.0F).currentValue(2.0F);

   @VMProtect(type = VMProtectType.MUTATION)
   @CompileBytecode
   private void initialize() {
      this.presets
         .add(
            new SwingPreset(
               "swings.block_hit",
               new Vec2f(0.5F, 1.0F),
               new Vec2f(0.5F, 0.0F),
               true,
               2.0F,
               new SwingTransformations(0.0F, -0.05F, -0.7F, 1.0500001F, -0.7F, -1.1F, -120.0F, -135.0F, -60.0F),
               new SwingTransformations(0.0F, -0.05F, -0.7F, 1.0500001F, -0.7F, -1.1F, -120.0F, -180.0F, -60.0F)
            )
         );
      this.presets
         .add(
            new SwingPreset(
               "swings.bonk",
               new Vec2f(0.40131578F, 0.53543305F),
               new Vec2f(0.0F, -0.24409449F),
               true,
               2.0F,
               new SwingTransformations(0.0F, -0.4F, -0.65000004F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
               new SwingTransformations(0.0F, -0.4F, -0.65000004F, 0.0F, 0.0F, 0.0F, -45.0F, 0.0F, 0.0F)
            )
         );
      this.presets
         .add(
            new SwingPreset(
               "swings.rotate_360",
               new Vec2f(0.43421054F, 0.61417323F),
               new Vec2f(0.04605263F, -0.26771653F),
               false,
               2.0F,
               new SwingTransformations(0.0F, -0.4F, -0.65000004F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
               new SwingTransformations(0.0F, -0.4F, -0.65000004F, 0.0F, 0.0F, 0.0F, -360.0F, 0.0F, 0.0F)
            )
         );
      this.presets
         .add(
            new SwingPreset(
               "swings.from_me",
               new Vec2f(0.42105263F, 0.87401575F),
               new Vec2f(0.3881579F, -0.4566929F),
               true,
               2.0F,
               new SwingTransformations(0.0F, 0.0F, -1.1F, 0.2F, 0.0F, -0.1F, -135.0F, 45.0F, 60.0F),
               new SwingTransformations(0.0F, 0.0F, -1.1F, 0.2F, 0.0F, -0.3F, -180.0F, 45.0F, 60.0F)
            )
         );
   }

   public SwingManager() {
      this.initialize();
   }

   public SwingTransformations transformations(float progress) {
      progress = this.bezier.easing().ease(progress, 0.0F, 1.0F, 1.0F);
      if (this.back.isEnabled()) {
         progress = MathHelper.sin(MathHelper.sqrt(progress) * (float) Math.PI);
      }

      return new SwingTransformations(
         this.get(this.startPhase.getAnchorX(), this.endPhase.getAnchorX(), progress),
         this.get(this.startPhase.getAnchorY(), this.endPhase.getAnchorY(), progress),
         this.get(this.startPhase.getAnchorZ(), this.endPhase.getAnchorZ(), progress),
         this.get(this.startPhase.getMoveX(), this.endPhase.getMoveX(), progress),
         this.get(this.startPhase.getMoveY(), this.endPhase.getMoveY(), progress),
         this.get(this.startPhase.getMoveZ(), this.endPhase.getMoveZ(), progress),
         this.get(this.startPhase.getRotateX(), this.endPhase.getRotateX(), progress),
         this.get(this.startPhase.getRotateY(), this.endPhase.getRotateY(), progress),
         this.get(this.startPhase.getRotateZ(), this.endPhase.getRotateZ(), progress)
      );
   }

   private float get(SliderSetting start, SliderSetting end, float progress) {
      return MathUtility.interpolate(start.getCurrentValue(), end.getCurrentValue(), progress);
   }

   public String getCurrent() {
      SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
      return manager.getCurrent() != null ? manager.getCurrent().getFileName() : this.current;
   }

   @Generated
   public List<SwingPreset> getPresets() {
      return this.presets;
   }

   @Generated
   public SwingSettings getSharedSettings() {
      return this.sharedSettings;
   }

   @Generated
   public SwingPhase getStartPhase() {
      return this.startPhase;
   }

   @Generated
   public SwingPhase getEndPhase() {
      return this.endPhase;
   }

   @Generated
   public BezierSetting getBezier() {
      return this.bezier;
   }

   @Generated
   public BooleanSetting getBack() {
      return this.back;
   }

   @Generated
   public SliderSetting getSpeed() {
      return this.speed;
   }

   @Generated
   public void setCurrent(String current) {
      this.current = current;
   }
}
