package moscow.rockstar.systems.modules.exception;

import lombok.Generated;

public class UnknownModuleException extends RuntimeException {
   private final String moduleName;

   public UnknownModuleException(String moduleName) {
      super("%s is not found!".formatted(moduleName));
      this.moduleName = moduleName;
   }

   @Generated
   public String getModuleName() {
      return this.moduleName;
   }
}
