package moscow.rockstar.systems.commands;

import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface ParameterValidator<T> {
   ValidationResult validate(String var1);

   default List<String> suggestions(String partial) {
      return Collections.emptyList();
   }
}
