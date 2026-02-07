package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import org.jetbrains.annotations.NotNull;

public class ButtonSetting extends AbstractSetting {
   private Runnable action = System.out::println;

   public ButtonSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public ButtonSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public ButtonSetting(@NotNull SettingsContainer parent, String name, String description) {
      super(parent, name);
   }

   public ButtonSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public ButtonSetting action(Runnable action) {
      this.action = action;
      return this;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive("сука как сделать так чтобы для него не было кфг");
   }

   @Override
   public void load(JsonElement element) {
   }

   @Generated
   public Runnable getAction() {
      return this.action;
   }

   @Generated
   public void setAction(Runnable action) {
      this.action = action;
   }
}
