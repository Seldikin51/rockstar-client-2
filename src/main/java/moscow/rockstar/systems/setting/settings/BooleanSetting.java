package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import moscow.rockstar.utility.interfaces.Toggleable;
import org.jetbrains.annotations.NotNull;

public class BooleanSetting extends AbstractSetting implements Toggleable {
   private boolean enabled;

   public BooleanSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public BooleanSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public BooleanSetting(@NotNull SettingsContainer parent, String name, String description) {
      super(parent, name);
   }

   public BooleanSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public BooleanSetting enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   public BooleanSetting enable() {
      this.enabled = true;
      return this;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.enabled);
   }

   @Override
   public void load(JsonElement element) {
      this.setEnabled(element.getAsBoolean());
   }

   @Override
   public void toggle() {
      this.enabled = !this.enabled;
   }

   @Override
   public void onEnable() {
   }

   @Override
   public void onDisable() {
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
