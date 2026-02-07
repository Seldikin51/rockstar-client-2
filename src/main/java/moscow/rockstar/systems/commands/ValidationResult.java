package moscow.rockstar.systems.commands;

import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;

public sealed interface ValidationResult permits ValidationResult.Ok, ValidationResult.Error {
   static <T> ValidationResult.Ok<T> ok(T value) {
      return new ValidationResult.Ok<>(value);
   }

   static ValidationResult.Error error(String msg) {
      MessageUtility.error(Text.of(msg));
      return new ValidationResult.Error(msg);
   }

   public record Error(String message) implements ValidationResult {
   }

   public record Ok<T>(T value) implements ValidationResult {
   }
}
