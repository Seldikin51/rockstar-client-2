package moscow.rockstar.systems.commands.commands;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.ParameterBuilder;
import moscow.rockstar.systems.commands.ParameterValidator;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ToggleCommand {
      @Compile
      public Command command() {
            List<String> moduleNames = Rockstar.getInstance().getModuleManager().getModules().stream()
                        .map(module -> module.getName().replace(" ", "")).toList();
            return CommandBuilder.begin("toggle")
                        .aliases("t")
                        .desc("commands.toggle.description")
                        .param("module",
                                    p -> p.validator(
                                                (moscow.rockstar.systems.commands.ParameterValidator) ParameterBuilder.MODULE)
                                                .suggests(moduleNames))
                        .handler(context -> {
                              Module module = (Module) context.arguments().getFirst();
                              module.toggle();
                              MessageUtility.info(Text.of(Localizator
                                          .translate("commands.toggle." + (module.isEnabled() ? "enabled" : "disabled"),
                                                      module.getName())));
                        })
                        .build();
      }
}
