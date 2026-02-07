package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import org.jetbrains.annotations.NotNull;

public class BindSetting extends AbstractSetting {
   private int key = -1;

   public BindSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public BindSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public BindSetting key(int key) {
      this.key = key;
      return this;
   }

   public boolean isKey(int key) {
      return this.key == key && this.isVisible();
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.key);
   }

   @Override
   public void load(JsonElement element) {
      if (element != null && element.isJsonPrimitive()) {
         this.setKey(-1);
         int value = element.getAsInt();
         if (value != -1) {
            this.setKey(value);
         }
      }
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }

   @Generated
   public int getKey() {
      return this.key;
   }
}
