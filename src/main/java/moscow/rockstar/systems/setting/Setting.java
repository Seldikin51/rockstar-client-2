package moscow.rockstar.systems.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

public interface Setting {
   String getName();

   String getDescription();

   BooleanSupplier getHideCondition();

   void register(SettingsContainer var1);

   default boolean isVisible() {
      return !this.getHideCondition().getAsBoolean();
   }

   JsonElement save();

   void load(JsonElement var1);
}
