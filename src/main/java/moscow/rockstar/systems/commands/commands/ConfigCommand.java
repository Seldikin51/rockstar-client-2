package moscow.rockstar.systems.commands.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ParameterValidator;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public final class ConfigCommand {
   private static final ParameterValidator<String> CONFIG_NAME = ValidationResult::ok;

   @Compile
   public Command command() {
      List<String> configNames = Rockstar.getInstance().getConfigManager().getConfigFiles().stream()
            .map(ConfigFile::getFileName).toList();
      return CommandBuilder.begin(
            "config",
            b -> b.aliases("cfg", "кфг", "конфиг")
                  .desc("commands.config.description")
                  .param(
                        "action",
                        p -> p.validator(
                              text -> ConfigCommand.Action.from(text)
                                    .map(a -> (ValidationResult) ValidationResult.ok(a))
                                    .orElseGet(() -> ValidationResult
                                          .error(Localizator.translate("commands.config.invalid_action"))))
                              .suggests(ConfigCommand.Action.allNames()))
                  .param("id", p -> p.optional().validator((ParameterValidator) CONFIG_NAME).suggests(configNames))
                  .handler(this::handle))
            .build();
   }

   @Compile
   private void handle(CommandContext ctx) {
      ConfigCommand.Action action = (ConfigCommand.Action) ctx.arguments().get(0);
      String id = (String) ctx.arguments().get(1);
      action.createHandler().accept(id);
   }

   private static enum Action {
      SAVE("save", "create", "add", "сохранить", "ыфму"),
      REMOVE("delete", "remove", "del", "удалить", "вудуеу"),
      LIST("list", "дшые"),
      LOAD("load", "use", "использовать", "дщфв"),
      DIR("dir", "direction");

      private final List<String> names;

      private Action(String... names) {
         this.names = Arrays.stream(names).map(String::toLowerCase).collect(Collectors.toList());
      }

      @Compile
      private Consumer<String> createHandler() {
         return switch (this) {
            case SAVE -> this::saveConfig;
            case REMOVE -> s -> {
               if (s != null) {
                  Rockstar.getInstance().getConfigManager().getConfig(s).delete();
               }
            };
            case LIST -> s -> Rockstar.getInstance().getConfigManager().listConfigs();
            case LOAD -> s -> {
               Rockstar.getInstance().getConfigManager().refresh();
               if (s != null && Rockstar.getInstance().getConfigManager().getConfig(s) != null) {
                  Rockstar.getInstance().getConfigManager().getConfig(s).load();
               }
            };
            case DIR -> s -> Rockstar.getInstance().getConfigManager().directionConfig();
         };
      }

      @Compile
      private void saveConfig(String configName) {
         if (configName != null) {
            ConfigManager configManager = Rockstar.getInstance().getConfigManager();
            configManager.createConfig(configName);
            MessageUtility.info(Text.of(Localizator.translate("commands.config.saved", configName)));
         }
      }

      @Compile
      static Optional<ConfigCommand.Action> from(String input) {
         String key = input.toLowerCase();
         return Arrays.stream(values()).filter(a -> a.names.contains(key)).findFirst();
      }

      @Compile
      static List<String> allNames() {
         return Arrays.stream(values()).map(a -> a.names.getFirst()).collect(Collectors.toList());
      }
   }
}
