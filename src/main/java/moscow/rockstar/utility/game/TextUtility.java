package moscow.rockstar.utility.game;

import com.ibm.icu.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import lombok.Generated;
import moscow.rockstar.systems.localization.Language;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TextUtility implements IMinecraft {
   private static final String STAR_TOKEN = "[★]";
   private static final List<String> prefixes = Arrays.asList(
      "The",
      "Super",
      "Mega",
      "Ultra",
      "Power",
      "Master",
      "Great",
      "Hyper",
      "Quantum",
      "Atomic",
      "Cosmic",
      "Turbo",
      "Mighty",
      "Fantastic",
      "Legendary",
      "Epic",
      "Glorious",
      "Incredible",
      "Marvelous",
      "Supreme",
      "Stellar",
      "Dynamic",
      "Heroic",
      "Valiant",
      "Brave",
      "Noble",
      "Radiant",
      "Brilliant",
      "Bold",
      "Fearless",
      "Fierce",
      "Savage",
      "Infinite",
      "Storm",
      "Thunder",
      "Lightning",
      "Solar",
      "Lunar",
      "Galactic",
      "Nebula",
      "Phoenix",
      "Titan",
      "Colossal",
      "Majestic",
      "Regal",
      "Royal",
      "Sovereign",
      "Auroral",
      "Divine",
      "Ethereal",
      "Fiery",
      "Flaming",
      "Gigahertz",
      "Hypersonic",
      "Infernal",
      "Jovial",
      "Kaleidoscopic",
      "Luminous",
      "Magnetic",
      "Nebulous",
      "Olympian",
      "Pulsar",
      "Quasar",
      "Radiant",
      "Spectral",
      "Stellar",
      "Tachyon",
      "Umbra",
      "Vortex",
      "Warp",
      "Xenon",
      "Yellowstone",
      "Zephyr",
      "Alena",
      "Karina",
      "Eva"
   );
   private static final List<String> adjectives = Arrays.asList(
      "Swift",
      "Fierce",
      "Sneaky",
      "Brave",
      "Savage",
      "Fearless",
      "Stealthy",
      "Valiant",
      "Bold",
      "Cunning",
      "Mighty",
      "Noble",
      "Resolute",
      "Vigilant",
      "Relentless",
      "Intrepid",
      "Daring",
      "Gallant",
      "Tenacious",
      "Ferocious",
      "Unyielding",
      "Audacious",
      "Courageous",
      "Indomitable",
      "Dauntless",
      "Unstoppable",
      "Determined",
      "Invincible",
      "Unbreakable",
      "Epic",
      "Legendary",
      "Mythic",
      "Heroic",
      "Glorious",
      "Triumphant",
      "Fearsome",
      "Imposing",
      "Stalwart",
      "Stout",
      "Steadfast",
      "Grim",
      "Resolute",
      "Fateful",
      "Loyal",
      "Trusty",
      "Staunch",
      "Hardy",
      "Doughty",
      "Unflinching",
      "Unfaltering",
      "Brisk",
      "Keen",
      "Alert",
      "Quick",
      "Agile",
      "Nimble",
      "Lithe",
      "Spry",
      "Energetic",
      "Vibrant",
      "Dynamic",
      "Lively",
      "Sprightly",
      "Active",
      "Forceful",
      "Vigorous",
      "Spirited",
      "Animated",
      "Robust",
      "Brawny",
      "Muscular",
      "Husky",
      "Strong",
      "Tough",
      "Solid",
      "Sturdy",
      "Hefty",
      "Powerful",
      "Mighty",
      "Colossal",
      "Gigantic",
      "Mammoth",
      "Titanic",
      "Towering",
      "Massive",
      "Monumental",
      "Heroic",
      "Bravehearted",
      "Gutsy",
      "Doughty",
      "Unyielding",
      "Unwavering",
      "Ironwilled",
      "Strong-willed",
      "Unshakeable",
      "Elfie"
   );
   private static final List<String> animals = Arrays.asList(
      "Wolf",
      "Tiger",
      "Lion",
      "Eagle",
      "Panther",
      "Dragon",
      "Phoenix",
      "Bear",
      "Leopard",
      "Hawk",
      "Falcon",
      "Cheetah",
      "Jaguar",
      "Griffin",
      "Raven",
      "Fox",
      "Shark",
      "Viper",
      "Cobra",
      "Falcon",
      "Crocodile",
      "Raptor",
      "Condor",
      "Lynx",
      "Ocelot",
      "Cougar",
      "Puma",
      "Hound",
      "Bison",
      "Mammoth",
      "Rhino",
      "Buffalo",
      "Stallion",
      "Mustang",
      "Pegasus",
      "Wyvern",
      "Cerberus",
      "Minotaur",
      "Chimera",
      "Hydra",
      "Kraken",
      "Basilisk",
      "Manticore",
      "Unicorn",
      "Sphinx",
      "Grizzly",
      "Kodiak",
      "Polar Bear",
      "Sabertooth",
      "Direwolf",
      "Orca",
      "Narwhal",
      "Walrus",
      "Beluga",
      "Elephant",
      "Hippo",
      "Gorilla",
      "Orangutan",
      "Chimpanzee",
      "Baboon",
      "Mongoose",
      "Ferret",
      "Weasel",
      "Otter",
      "Badger",
      "Wolverine",
      "Honey Badger",
      "Lizard",
      "Iguana",
      "Gecko",
      "Komodo Dragon",
      "Monitor Lizard",
      "Tortoise",
      "Turtle",
      "Alligator",
      "Caiman",
      "Anaconda",
      "Python",
      "Boa",
      "Eel",
      "Swordfish",
      "Marlin",
      "Barracuda",
      "Piranha",
      "Penguin",
      "Albatross",
      "Seagull",
      "Pelican",
      "Stork",
      "Heron",
      "Flamingo",
      "MasTyp6ek",
      "Masha",
      "Tigr",
      "Legacy",
      ""
   );
   private static final List<String> suffixes = Arrays.asList(
      "Gamer",
      "Player",
      "Ninja",
      "Warrior",
      "Champion",
      "Legend",
      "Hero",
      "Master",
      "Conqueror",
      "Slayer",
      "Guardian",
      "Knight",
      "Paladin",
      "Crusader",
      "Ranger",
      "Assassin",
      "Mage",
      "Sorcerer",
      "Wizard",
      "Enchanter",
      "Necromancer",
      "Berserker",
      "Gladiator",
      "Samurai",
      "Viking",
      "Pirate",
      "Outlaw",
      "Mercenary",
      "Hunter",
      "Scout",
      "Rogue",
      "Thief",
      "Sentinel",
      "Protector",
      "Savior",
      "Defender",
      "Avenger",
      "Warlord",
      "Commander",
      "Captain",
      "General",
      "Marshal",
      "Overlord",
      "Monarch",
      "Emperor",
      "King",
      "Queen",
      "Prince",
      "Princess",
      "Duke",
      "Duchess",
      "Baron",
      "Baroness",
      "Lord",
      "Lady",
      "Warden",
      "Sentinel",
      "Crusader",
      "Champion",
      "Virtuoso",
      "Adept",
      "Prodigy",
      "Savant",
      "Genius",
      "Maven",
      "Whiz",
      "Ace",
      "Virtuoso",
      "Expert",
      "Specialist",
      "Technician",
      "Strategist",
      "Tactician",
      "Operative",
      "Agent",
      "Spy",
      "Infiltrator",
      "Saboteur",
      "Shadow",
      "Phantom",
      "Specter",
      "Shade",
      "Mystic",
      "Seer",
      "Oracle",
      "Prophet",
      "Visionary",
      "Dreamer",
      "Illusionist",
      "Conjurer",
      "Invoker",
      "Diviner",
      "Alchemist",
      "Shaman",
      "Druid",
      "Elementalist",
      "Geomancer",
      "Pyromancer",
      "Hydromancer",
      "Aeromancer",
      "Archon",
      "Brawler",
      "Catalyst",
      "Dynamo",
      "Energizer",
      "Flux",
      "Fusion",
      "Gizmo",
      "Hacker",
      "Innovator",
      "Juggernaut",
      "Kinetix",
      "Luminary",
      "Marauder",
      "Nomad",
      "Operator",
      "Pioneer",
      "Quickshot",
      "Rascal",
      "Slasher",
      "Titan",
      "Umbra",
      "Vanguard",
      "Warden",
      "Pro",
      "Xenon",
      "Yokai",
      "Zealot",
      "Zorro",
      "Zoltar"
   );
   private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
   private static final Random random = new Random();

   public static String getRandomNick() {
      String prefix = getRandomElement(prefixes);
      String adjective = getRandomElement(adjectives);
      String animal = getRandomElement(animals);
      String suffix = getRandomElement(suffixes);
      String year = random.nextInt(100) < 30 ? String.valueOf(2000 + random.nextInt(26)) : "";
      List<String> parts = new ArrayList<>();
      if (random.nextBoolean()) {
         parts.add(prefix);
      }

      if (random.nextBoolean()) {
         parts.add(adjective);
      }

      if (random.nextBoolean()) {
         parts.add(animal);
      }

      if (random.nextBoolean()) {
         parts.add(suffix);
      }

      if (parts.isEmpty()) {
         parts.add(prefix);
      }

      if (parts.size() < 2) {
         parts.add(random.nextBoolean() ? adjective : animal);
      }

      String nickname = String.join("", parts) + year;
      if (random.nextInt(100) < 20) {
         nickname = nickname + (random.nextBoolean() ? "52" : "69");
      } else {
         nickname = nickname + generateNumbers(2 + random.nextInt(3));
      }

      if (nickname.length() > 16) {
         nickname = nickname.substring(nickname.length() - 16);
      }

      return nickname;
   }

   public static String formatNumberClean(double number) {
      if (number == (int)number) {
         return String.valueOf((int)number);
      } else {
         String formatted = String.format("%.1f", number).replace(",", ".").replaceAll("\\.?0+$", "");
         return formatted.endsWith(".") ? formatted.replace(".", "") : formatted;
      }
   }

   public static String formatNumber(double number) {
      return String.format("%.1f", number).replace(",", ".");
   }

   private static String getRandomElement(List<String> list) {
      return list.get(random.nextInt(list.size()));
   }

   private static String generateNumbers(int length) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < length; i++) {
         sb.append(random.nextInt(10));
      }

      return sb.toString();
   }

   public static String makeGender(String parent) {
      if (parent.endsWith("а")) {
         return "а";
      } else if (parent.endsWith("a")) {
         return "а";
      } else if (parent.endsWith("y")) {
         return "о";
      } else if (parent.endsWith("ю")) {
         return "o";
      } else if (parent.endsWith("u")) {
         return "o";
      } else if (parent.endsWith("я")) {
         return "а";
      } else if (parent.endsWith("ы")) {
         return "ы";
      } else {
         return parent.endsWith("и") ? "ы" : "";
      }
   }

   public static String makeCount(float count) {
      double abs = Math.abs(count);
      long integerPart = (long)Math.floor(abs);
      double frac = abs - integerPart;
      if (frac > 1.0E-9) {
         return "а";
      } else {
         int n = (int)(integerPart % 100L);
         if (n >= 11 && n <= 14) {
            return "ов";
         } else {
            return switch (n % 10) {
               case 1 -> "";
               case 2, 3, 4 -> "а";
               default -> "ов";
            };
         }
      }
   }

   public static String makeCountTranslated(float count) {
      Language currentLanguage = Localizator.getCurrentLanguage();

      return switch (currentLanguage) {
         case RU_RU -> makeCountRu(count);
         case UK_UA -> makeCountUa(count);
         case PL_PL -> makeCountPl(count);
         case EN_US -> makeCountEn(count);
      };
   }

   private static String makeCountRu(float count) {
      double abs = Math.abs(count);
      long integerPart = (long)Math.floor(abs);
      double frac = abs - integerPart;
      if (frac > 1.0E-9) {
         return "а";
      } else {
         int n = (int)(integerPart % 100L);
         if (n >= 11 && n <= 14) {
            return "ов";
         } else {
            return switch (n % 10) {
               case 1 -> "";
               case 2, 3, 4 -> "а";
               default -> "ов";
            };
         }
      }
   }

   private static String makeCountUa(float count) {
      double abs = Math.abs(count);
      long integerPart = (long)Math.floor(abs);
      double frac = abs - integerPart;
      if (frac > 1.0E-9) {
         return "и";
      } else {
         int n = (int)(integerPart % 100L);
         if (n >= 11 && n <= 14) {
            return "ів";
         } else {
            return switch (n % 10) {
               case 1 -> "";
               case 2, 3, 4 -> "и";
               default -> "ів";
            };
         }
      }
   }

   private static String makeCountPl(float count) {
      double abs = Math.abs(count);
      long integerPart = (long)Math.floor(abs);
      double frac = abs - integerPart;
      if (frac > 1.0E-9) {
         return "y";
      } else if (integerPart == 1L) {
         return "";
      } else {
         return integerPart >= 2L && integerPart <= 4L ? "y" : "ów";
      }
   }

   private static String makeCountEn(float count) {
      double abs = Math.abs(count);
      return abs == 1.0 ? "" : "s";
   }

   public static String getKeyName(int key) {
      if (key >= 0 && key <= 7) {
         return switch (key) {
            case 0 -> "ЛКМ";
            case 1 -> "ПКМ";
            case 2 -> "Колесико";
            case 3 -> "MOUSE4";
            case 4 -> "MOUSE5";
            case 5 -> "MOUSE6";
            case 6 -> "MOUSE7";
            case 7 -> "MOUSE8";
            default -> "MOUSE" + key;
         };
      } else if (key <= -1) {
         return "NONE";
      } else {
         String name = InputUtil.fromKeyCode(key, -1).getTranslationKey();
         name = name.replace("key.keyboard.", "")
            .replace("key.", "")
            .replace(".", "")
            .replace("left", "l")
            .replace("right", "r")
            .replace("printscreen", "prtsc")
            .replace("graveaccent", "grave")
            .replace("control", "ctrl");
         return name.toUpperCase();
      }
   }

   public static String getCurrentTime() {
      return sdf.format(new Date());
   }

   public static String getFormattedDate() {
      LocalDate currentDate = LocalDate.now();
      String[] daysOfWeek = new String[]{
         "time.days.monday", "time.days.tuesday", "time.days.wednesday", "time.days.thursday", "time.days.friday", "time.days.saturday", "time.days.sunday"
      };
      String[] months = new String[]{
         "time.months.january",
         "time.months.february",
         "time.months.march",
         "time.months.april",
         "time.months.may",
         "time.months.june",
         "time.months.july",
         "time.months.august",
         "time.months.september",
         "time.months.october",
         "time.months.november",
         "time.months.december"
      };
      DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
      String russianDay = Localizator.translate(daysOfWeek[dayOfWeek.getValue() - 1]);
      int dayOfMonth = currentDate.getDayOfMonth();
      Month month = currentDate.getMonth();
      String russianMonth = Localizator.translate(months[month.getValue() - 1]);
      return String.format("%s, %d %s", russianDay, dayOfMonth, russianMonth);
   }

   public static void copyText(String text) {
      mc.keyboard.setClipboard(text);
   }

   public static MutableText formatTalisman(String input) {
      if (input.startsWith("[★]")) {
         String rest = input.substring("[★]".length());
         MutableText redStar = Text.literal("[★]").formatted(Formatting.RED);
         MutableText orangeText = Text.literal(rest).formatted(Formatting.GOLD);
         return Text.literal("").append(redStar).append(orangeText);
      } else {
         return Text.literal(input);
      }
   }

   @Generated
   private TextUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
