package moscow.rockstar.systems.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.exception.UnknownModuleException;

public class ParameterBuilder<T> {
   private final String name;
   private boolean required = true;
   private boolean vararg = false;
   private ParameterValidator<T> validator;
   private List<String> suggestions;
   public static final ParameterValidator<Integer> INTEGER = text -> {
      try {
         return ValidationResult.ok(Integer.parseInt(text));
      } catch (NumberFormatException var2) {
         return ValidationResult.error("'" + text + "' is not a number");
      }
   };
   public static final ParameterValidator<Module> MODULE = text -> {
      try {
         Module module = Rockstar.getInstance().getModuleManager().getModule(text);
         return ValidationResult.ok(module);
      } catch (UnknownModuleException var2) {
         return ValidationResult.error("Module with name '%s' was not found".formatted(text));
      }
   };

   private ParameterBuilder(String name) {
      this.name = name;
   }

   public static <T> ParameterBuilder<T> create(String name) {
      return new ParameterBuilder<>(name);
   }

   public ParameterBuilder<T> optional() {
      this.required = false;
      return this;
   }

   public ParameterBuilder<T> vararg() {
      this.vararg = true;
      return this;
   }

   public ParameterBuilder<T> validator(ParameterValidator<T> validator) {
      this.validator = validator;
      return this;
   }

   public void suggests(String... suggestions) {
      this.suggestions = List.of(suggestions[0]);
   }

   public void suggests(List<String> suggestions) {
      this.suggestions = suggestions;
   }

   public ParameterBuilder<T> literal(String... literals) {
      final String opts = Arrays.stream(literals).map(s -> "'" + s + "'").collect(Collectors.joining(", "));
      this.validator = new ParameterValidator<T>() {
         @Override
         public ValidationResult validate(String text) {
            for (String l : literals) {
               if (l.equalsIgnoreCase(text)) {
                  return ValidationResult.ok(text);
               }
            }

            return ValidationResult.error("Expected one of: " + opts);
         }

         @Override
         public List<String> suggestions(String partial) {
            return Arrays.stream(literals).filter(l -> l.toLowerCase().startsWith(partial.toLowerCase())).toList();
         }
      };
      this.suggestions = List.of(literals);
      return this;
   }

   public Parameter<T> build() {
      ParameterValidator<T> resultValidator = this.validator;
      if (this.suggestions != null) {
         final ParameterValidator<T> orig = resultValidator;
         resultValidator = new ParameterValidator<T>() {
            @Override
            public ValidationResult validate(String text) {
               return orig.validate(text);
            }

            @Override
            public List<String> suggestions(String partial) {
               String low = partial.toLowerCase();
               return ParameterBuilder.this.suggestions.stream().filter(s -> s.toLowerCase().startsWith(low)).toList();
            }
         };
      }

      return new Parameter<>(this.name, this.required, this.vararg, resultValidator);
   }
}
