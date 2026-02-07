package moscow.rockstar.systems.commands;

import java.util.List;

public record CommandContext(Command command, List<Object> arguments) {
}
