package moscow.rockstar.systems.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.impl.AbstractSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import org.jetbrains.annotations.NotNull;

public class ModeSetting extends AbstractSetting {
   private final List<ModeSetting.Value> values = new ArrayList<>();
   private ModeSetting.Value value;

   public ModeSetting(@NotNull SettingsContainer parent, String name, String description, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public ModeSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      super(parent, name, hideCondition);
   }

   public ModeSetting(@NotNull SettingsContainer parent, String name, String description) {
      super(parent, name);
   }

   public ModeSetting(@NotNull SettingsContainer parent, String name) {
      super(parent, name);
   }

   public void addMode(ModeSetting.Value mode) {
      this.values.add(mode);
      if (this.value == null) {
         this.value = mode;
      }
   }

   public boolean is(ModeSetting.Value otherValue) {
      return this.value == otherValue;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value.getName());
   }

   public ModeSetting.Value getRandomEnabledElement() {
      List<ModeSetting.Value> enableValue = this.values.stream().filter(ModeSetting.Value::isSelected).toList();
      if (!enableValue.isEmpty()) {
         Random random = new Random();
         return enableValue.get(random.nextInt(enableValue.size()));
      } else {
         return null;
      }
   }

   @Override
   public void load(JsonElement element) {
      String name = element.getAsString();

      for (ModeSetting.Value value : this.values) {
         if (value.getName().equalsIgnoreCase(name)) {
            this.value = value;
            break;
         }
      }
   }

   @Generated
   public List<ModeSetting.Value> getValues() {
      return this.values;
   }

   @Generated
   public ModeSetting.Value getValue() {
      return this.value;
   }

   @Generated
   public void setValue(ModeSetting.Value value) {
      this.value = value;
   }

   public static class Value {
      private final ModeSetting parent;
      private final String name;
      private final String description;
      private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
      private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
      private final BooleanSupplier hideCondition;
      private PenisPlayer enablePenis;
      private PenisPlayer disablePenis;
      private PenisPlayer currentPenis;
      private boolean lastState;

      public Value(ModeSetting parent, String name) {
         this(parent, name, "", () -> false);
      }

      public Value(ModeSetting parent, String name, String description) {
         this(parent, name, description, () -> false);
      }

      public Value(ModeSetting parent, String name, String description, BooleanSupplier hideCondition) {
         this.parent = parent;
         this.name = name;
         this.description = description;
         this.hideCondition = hideCondition;
         parent.addMode(this);
      }

      public boolean isHidden() {
         return this.hideCondition != null && this.hideCondition.getAsBoolean();
      }

      public ModeSetting.Value select() {
         this.parent.setValue(this);
         return this;
      }

      public boolean isSelected() {
         return this.parent.getValue() == this;
      }

      @Override
      public String toString() {
         return this.name;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            ModeSetting.Value that = (ModeSetting.Value)obj;
            return Objects.equals(this.parent, that.parent) && Objects.equals(this.name, that.name) && Objects.equals(this.description, that.description);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.parent, this.name, this.description);
      }

      @Generated
      public void setEnablePenis(PenisPlayer enablePenis) {
         this.enablePenis = enablePenis;
      }

      @Generated
      public void setDisablePenis(PenisPlayer disablePenis) {
         this.disablePenis = disablePenis;
      }

      @Generated
      public void setCurrentPenis(PenisPlayer currentPenis) {
         this.currentPenis = currentPenis;
      }

      @Generated
      public void setLastState(boolean lastState) {
         this.lastState = lastState;
      }

      @Generated
      public ModeSetting getParent() {
         return this.parent;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public String getDescription() {
         return this.description;
      }

      @Generated
      public Animation getHoverAnimation() {
         return this.hoverAnimation;
      }

      @Generated
      public Animation getActiveAnimation() {
         return this.activeAnimation;
      }

      @Generated
      public BooleanSupplier getHideCondition() {
         return this.hideCondition;
      }

      @Generated
      public PenisPlayer getEnablePenis() {
         return this.enablePenis;
      }

      @Generated
      public PenisPlayer getDisablePenis() {
         return this.disablePenis;
      }

      @Generated
      public PenisPlayer getCurrentPenis() {
         return this.currentPenis;
      }

      @Generated
      public boolean isLastState() {
         return this.lastState;
      }
   }
}
