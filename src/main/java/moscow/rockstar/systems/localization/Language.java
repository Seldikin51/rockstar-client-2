package moscow.rockstar.systems.localization;

import lombok.Generated;

public enum Language {
   EN_US("en_us"),
   RU_RU("ru_ru"),
   UK_UA("uk_ua"),
   PL_PL("pl_pl");

   private final String code;

   private Language(String code) {
      this.code = code;
   }

   @Generated
   public String getCode() {
      return this.code;
   }
}
