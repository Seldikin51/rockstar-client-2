package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class RangeSetting extends AbstractSetting {
   private float firstValue;
   private float secondValue;
   private float min;
   private float max;
   private float step;

   public RangeSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public RangeSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public RangeSetting firstValue(float firstValue) {
      this.firstValue = firstValue;
      return this;
   }

   public RangeSetting secondValue(float secondValue) {
      this.secondValue = secondValue;
      return this;
   }

   public RangeSetting min(float min) {
      this.min = min;
      return this;
   }

   public RangeSetting max(float max) {
      this.max = max;
      return this;
   }

   public RangeSetting step(float step) {
      this.step = step;
      return this;
   }

   @Override
   public JsonElement save() {
      JsonObject object = new JsonObject();
      object.addProperty("first", this.firstValue);
      object.addProperty("second", this.secondValue);
      return object;
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();
         if (object.has("first")) {
            this.setFirstValue(object.get("first").getAsFloat());
         }

         if (object.has("second")) {
            this.setSecondValue(object.get("second").getAsFloat());
         }
      }
   }

   public void setFirstValue(float value) {
      this.firstValue = (float)MathHelper.clamp(Math.round(value * (1.0 / this.step)) / (1.0 / this.step), this.min, this.max);
   }

   public void setSecondValue(float value) {
      this.secondValue = (float)MathHelper.clamp(Math.round(value * (1.0 / this.step)) / (1.0 / this.step), this.min, this.max);
   }

   @Generated
   public float getFirstValue() {
      return this.firstValue;
   }

   @Generated
   public float getSecondValue() {
      return this.secondValue;
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
   public void setMin(float min) {
      this.min = min;
   }

   @Generated
   public void setMax(float max) {
      this.max = max;
   }

   @Generated
   public void setStep(float step) {
      this.step = step;
   }
}
