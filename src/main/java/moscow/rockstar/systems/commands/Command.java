package moscow.rockstar.systems.commands;

import java.util.List;

public interface Command {
   List<String> names();

   String description();

   List<Parameter<?>> parameters();

   List<Command> subcommands();

   boolean executable();

   CommandHandler handler();
}
