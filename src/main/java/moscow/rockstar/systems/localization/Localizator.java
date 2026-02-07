package moscow.rockstar.systems.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public final class Localizator {
   private static final Language DEFAULT_LANG = Language.RU_RU;
   private static Language currentLanguage = DEFAULT_LANG;
   private static final Map<String, String> translations = new HashMap<>();

   public static void loadTranslations() {
      String langFile = "/assets/" + Rockstar.MOD_ID + "/lang/" + currentLanguage.getCode() + ".lang";

      try {
         InputStream inputStream = Localizator.class.getResourceAsStream(langFile);
         if (inputStream == null) {
            throw new RuntimeException("Language file not found: " + langFile);
         } else {
            translations.clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int lineNumber = 0;

            String line;
            while ((line = reader.readLine()) != null) {
               lineNumber++;
               line = removeComments(line).trim();
               if (!line.isEmpty()) {
                  parseLine(line, lineNumber, langFile);
               }
            }

            reader.close();
            inputStream.close();
         }
      } catch (IOException var5) {
         throw new RuntimeException("Failed to load translations for language: " + currentLanguage.getCode(), var5);
      }
   }

   public static void setLanguage(@Nonnull Language lang) {
      currentLanguage = lang;
      loadTranslations();
   }

   public static String translate(String key) {
      return translations.getOrDefault(key, key);
   }

   public static String translate(String key, Object... args) {
      String format = translations.getOrDefault(key, key);
      return String.format(format, args);
   }

   public static String translateOrEmpty(String key) {
      return translations.getOrDefault(key, " ");
   }

   @VMProtect(type = VMProtectType.MUTATION)
   private static void parseLine(String line, int lineNumber, String fileName) {
      int equalIndex = line.indexOf(61);
      if (equalIndex == -1) {
         Rockstar.LOGGER.warn("Warning: Invalid line format at line {} in {}: {}", new Object[]{lineNumber, fileName, line});
      } else {
         String key = line.substring(0, equalIndex).trim();
         String value = line.substring(equalIndex + 1).trim();
         if (key.isEmpty()) {
            Rockstar.LOGGER.warn("Warning: Empty key at line {} in {}", lineNumber, fileName);
         } else {
            translations.put(key, value);
         }
      }
   }

   private static String removeComments(String line) {
      int commentIndex = line.indexOf("#");
      return commentIndex != -1 ? line.substring(0, commentIndex) : line;
   }

   @Generated
   private Localizator() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static Language getCurrentLanguage() {
      return currentLanguage;
   }
}
