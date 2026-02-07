package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class SliderSetting extends AbstractSetting {
   protected float min;
   protected float max;
   protected float step;
   protected float currentValue;
   private SliderSetting.Suffix suffix = number -> "";

   public SliderSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public SliderSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public SliderSetting(@NotNull SettingsContainer parent, String name, String description) {
      super(parent, name);
   }

   public SliderSetting(@NotNull SettingsContainer parent, String id) {
      super(parent, id);
   }

   public SliderSetting min(float min) {
      this.min = min;
      return this;
   }

   public SliderSetting max(float max) {
      this.max = max;
      return this;
   }

   public SliderSetting step(float step) {
      this.step = step;
      return this;
   }

   public SliderSetting suffix(SliderSetting.Suffix suffix) {
      this.suffix = suffix;
      return this;
   }

   public SliderSetting suffix(String suffix) {
      this.suffix = number -> suffix;
      return this;
   }

   public SliderSetting currentValue(float currentValue) {
      this.setCurrentValue(currentValue);
      return this;
   }

   public String getSuffix() {
      return this.suffix.apply(this.getCurrentValue()).contains(" ")
         ? " " + Localizator.translate(this.suffix.apply(this.getCurrentValue()).replace(" ", ""))
         : Localizator.translate(this.suffix.apply(this.getCurrentValue()));
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.currentValue);
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         this.setCurrentValue(element.getAsFloat());
      }
   }

   public void setCurrentValue(float currentValue) {
      this.currentValue = MathHelper.clamp((float)(Math.round(currentValue * (1.0 / this.step)) / (1.0 / this.step)), this.min, this.max);
   }

   @Generated
   public float getMin() {
      return this.min;
   }

   @Generated
   public float getMax() {
      return this.max;
   }

   @Generated
   public float getStep() {
      return this.step;
   }

   @Generated
   public float getCurrentValue() {
      return this.currentValue;
   }

   public interface Suffix {
      String apply(float var1);
   }
}
