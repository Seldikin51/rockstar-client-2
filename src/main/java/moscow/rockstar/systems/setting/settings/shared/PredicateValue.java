package moscow.rockstar.systems.setting.settings.shared;

import java.util.function.Predicate;
import moscow.rockstar.systems.setting.settings.SelectSetting;

public class PredicateValue<T> extends SelectSetting.Value {
   private final Predicate<T> predicate;

   public PredicateValue(SelectSetting parent, String name, Predicate<T> predicate) {
      super(parent, name);
      this.predicate = predicate;
   }

   public PredicateValue(SelectSetting parent, String name, String description, Predicate<T> predicate) {
      super(parent, name, description);
      this.predicate = predicate;
   }

   public boolean predicated(T t) {
      return this.predicate.test(t) && this.isSelected();
   }
}
