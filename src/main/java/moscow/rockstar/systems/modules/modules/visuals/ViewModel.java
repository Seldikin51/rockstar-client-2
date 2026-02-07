package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

@ModuleInfo(name = "View Model", category = ModuleCategory.VISUALS, desc = "modules.descriptions.view_model")
public class ViewModel extends BaseModule {
   private final SliderSetting mainTranslateX = new SliderSetting(this, "modules.settings.view_model.main_translate_x")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting mainTranslateY = new SliderSetting(this, "modules.settings.view_model.main_translate_y")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting mainTranslateZ = new SliderSetting(this, "modules.settings.view_model.main_translate_z")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting mainRotateX = new SliderSetting(this, "modules.settings.view_model.main_rotate_x")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final SliderSetting mainRotateY = new SliderSetting(this, "modules.settings.view_model.main_rotate_y")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final SliderSetting mainRotateZ = new SliderSetting(this, "modules.settings.view_model.main_rotate_z")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final SliderSetting offTranslateX = new SliderSetting(this, "modules.settings.view_model.off_translate_x")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting offTranslateY = new SliderSetting(this, "modules.settings.view_model.off_translate_y")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting offTranslateZ = new SliderSetting(this, "modules.settings.view_model.off_translate_z")
      .min(-2.0F)
      .max(2.0F)
      .currentValue(0.0F)
      .step(0.05F);
   private final SliderSetting offRotateX = new SliderSetting(this, "modules.settings.view_model.off_rotate_x")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final SliderSetting offRotateY = new SliderSetting(this, "modules.settings.view_model.off_rotate_y")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final SliderSetting offRotateZ = new SliderSetting(this, "modules.settings.view_model.off_rotate_z")
      .min(-180.0F)
      .max(180.0F)
      .currentValue(0.0F)
      .step(1.0F);
   private final EventListener<HandRenderEvent> onHandRender = event -> {
      MatrixStack matrices = event.getMatrices();
      boolean isMain = event.getArm() == Arm.RIGHT;
      float translateX = isMain ? this.mainTranslateX.getCurrentValue() : this.offTranslateX.getCurrentValue();
      float translateY = isMain ? this.mainTranslateY.getCurrentValue() : this.offTranslateY.getCurrentValue();
      float translateZ = isMain ? this.mainTranslateZ.getCurrentValue() : this.offTranslateZ.getCurrentValue();
      float rotateX = isMain ? this.mainRotateX.getCurrentValue() : this.offRotateX.getCurrentValue();
      float rotateY = isMain ? this.mainRotateY.getCurrentValue() : this.offRotateY.getCurrentValue();
      float rotateZ = isMain ? this.mainRotateZ.getCurrentValue() : this.offRotateZ.getCurrentValue();
      float direction = isMain ? 1.0F : -1.0F;
      matrices.translate(translateX * direction, translateY, translateZ);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateX));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateY));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZ));
   };
}
