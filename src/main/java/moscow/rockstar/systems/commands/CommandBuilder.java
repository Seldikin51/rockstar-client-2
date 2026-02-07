package moscow.rockstar.systems.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandBuilder {
   private final List<String> names = new ArrayList<>();
   private String desc = "";
   private final List<Parameter<?>> params = new ArrayList<>();
   private final List<Command> subs = new ArrayList<>();
   private boolean executable = true;
   private CommandHandler handler;

   private CommandBuilder(String name) {
      this.names.add(name);
   }

   public static CommandBuilder begin(String name) {
      return new CommandBuilder(name);
   }

   public static CommandBuilder begin(String name, Consumer<CommandBuilder> cfg) {
      CommandBuilder b = new CommandBuilder(name);
      cfg.accept(b);
      return b;
   }

   public CommandBuilder aliases(String... a) {
      this.names.addAll(Arrays.asList(a));
      return this;
   }

   public CommandBuilder desc(String d) {
      this.desc = d;
      return this;
   }

   public <T> CommandBuilder param(String name, Consumer<ParameterBuilder<T>> cfg) {
      ParameterBuilder<T> pb = ParameterBuilder.create(name);
      cfg.accept(pb);
      this.params.add(pb.build());
      return this;
   }

   public CommandBuilder subcommand(Command c) {
      this.subs.add(c);
      return this;
   }

   public CommandBuilder hub() {
      this.executable = false;
      return this;
   }

   public CommandBuilder handler(CommandHandler h) {
      this.handler = h;
      return this;
   }

   public Command build() {
      if (this.executable && this.handler == null) {
         throw new IllegalStateException("Executable command requires handler");
      } else if (!this.executable && this.handler != null) {
         throw new IllegalStateException("Hub command cannot have handler");
      } else {
         return new CommandBuilder.SimpleCommand(this.names, this.desc, this.params, this.subs, this.executable, this.handler);
      }
   }

   private record SimpleCommand(
      List<String> names, String description, List<Parameter<?>> parameters, List<Command> subcommands, boolean executable, CommandHandler handler
   ) implements Command {
   }
}
