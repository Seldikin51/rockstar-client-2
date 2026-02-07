package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BezierSetting extends AbstractSetting {
   private Vec2f start = Vec2f.ZERO;
   private Vec2f end = new Vec2f(1.0F, 1.0F);

   public BezierSetting(@NotNull SettingsContainer parent, String name, String description, @Nullable BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public BezierSetting(@NotNull SettingsContainer parent, String name, @Nullable BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public BezierSetting(@NotNull SettingsContainer parent, String name, String description) {
      super(parent, name);
   }

   public BezierSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public BezierSetting start(float startX, float startY) {
      this.start = new Vec2f(startX, startY);
      return this;
   }

   public BezierSetting end(float endX, float endY) {
      this.end = new Vec2f(endX, endY);
      return this;
   }

   public BezierSetting start(Vec2f start) {
      this.start = start;
      return this;
   }

   public BezierSetting end(Vec2f end) {
      this.end = end;
      return this;
   }

   public Easing easing() {
      return Easing.generate(this.start.x, 1.0F - this.start.y, this.end.x, 1.0F - this.end.y);
   }

   @Override
   public JsonElement save() {
      JsonObject object = new JsonObject();
      object.addProperty("start_x", this.start.x);
      object.addProperty("start_y", this.start.y);
      object.addProperty("end_x", this.end.x);
      object.addProperty("end_y", this.end.y);
      return object;
   }

   @Override
   public void load(JsonElement element) {
      if (element.isJsonObject()) {
         JsonObject object = element.getAsJsonObject();
         if (object.has("start_x") && object.has("start_y")) {
            this.start(new Vec2f(object.get("start_x").getAsFloat(), object.get("start_y").getAsFloat()));
         }

         if (object.has("end_x") && object.has("end_y")) {
            this.end(new Vec2f(object.get("end_x").getAsFloat(), object.get("end_y").getAsFloat()));
         }
      }
   }

   @Generated
   public Vec2f start() {
      return this.start;
   }

   @Generated
   public Vec2f end() {
      return this.end;
   }
}
