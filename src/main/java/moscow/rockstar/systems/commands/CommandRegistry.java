package moscow.rockstar.systems.commands;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import moscow.rockstar.systems.commands.commands.AuthCommand;
import moscow.rockstar.systems.commands.commands.AutoPilotCommand;
import moscow.rockstar.systems.commands.commands.BindCommand;
import moscow.rockstar.systems.commands.commands.CatCommand;
import moscow.rockstar.systems.commands.commands.ConfigCommand;
import moscow.rockstar.systems.commands.commands.FakePlayerCommand;
import moscow.rockstar.systems.commands.commands.FriendCommand;
import moscow.rockstar.systems.commands.commands.HelpCommand;
import moscow.rockstar.systems.commands.commands.InventoryCommand;
import moscow.rockstar.systems.commands.commands.PrefixCommand;
import moscow.rockstar.systems.commands.commands.ReHubCommand;
import moscow.rockstar.systems.commands.commands.TargetCommand;
import moscow.rockstar.systems.commands.commands.ToggleCommand;
import moscow.rockstar.systems.commands.commands.VclipCommand;
import moscow.rockstar.systems.commands.commands.WaypointsCommand;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class CommandRegistry {
   private final List<Command> commands = new ArrayList<>();
   private String prefix = ".";

   public void register(Command command) {
      this.commands.add(command);
   }

   @Compile
   @VMProtect(type = VMProtectType.MUTATION)
   @Initialization
   public void initCommands() {
      this.register(new CatCommand().command());
      this.register(new AutoPilotCommand().command());
      this.register(new ConfigCommand().command());
      this.register(new FakePlayerCommand().command());
      this.register(new FriendCommand().command());
      this.register(new HelpCommand().command());
      this.register(new InventoryCommand().command());
      this.register(new PrefixCommand().command());
      this.register(new ReHubCommand().command());
      this.register(new TargetCommand().command());
      this.register(new VclipCommand().command());
      this.register(new AuthCommand().command());
      this.register(new ToggleCommand().command());
      this.register(new WaypointsCommand().command());
      this.register(new BindCommand().command());
   }

   public List<Command> commands() {
      return Collections.unmodifiableList(this.commands);
   }

   public boolean dispatch(String line) {
      if (!line.startsWith(this.prefix)) {
         return false;
      } else {
         String[] toks = line.substring(this.prefix.length()).split("\\s+");
         List<String> args = Arrays.asList(toks);
         CommandRegistry.Pair<Command, Integer> pair = this.findSub(args, null, 0);
         if (pair == null) {
            return false;
         } else {
            Command cmd = pair.command();
            int idx = pair.index();
            if (!cmd.executable()) {
               return false;
            } else {
               List<Object> parsed = this.parseArgs(cmd, toks, idx);
               if (parsed == null) {
                  return true;
               } else {
                  cmd.handler().execute(new CommandContext(cmd, parsed));
                  return true;
               }
            }
         }
      }
   }

   private CommandRegistry.Pair<Command, Integer> findSub(List<String> args, Command parent, int idx) {
      Collection<Command> pool = parent == null ? this.commands : parent.subcommands();
      if (idx >= args.size()) {
         return this.createResultOrNull(parent, idx - 1);
      } else {
         String current = args.get(idx);

         for (Command cmd : pool) {
            for (String name : cmd.names()) {
               if (name.equalsIgnoreCase(current)) {
                  CommandRegistry.Pair<Command, Integer> deeper = this.findSub(args, cmd, idx + 1);
                  if (deeper != null) {
                     return deeper;
                  }

                  return new CommandRegistry.Pair<>(cmd, idx);
               }
            }
         }

         return this.createResultOrNull(parent, idx - 1);
      }
   }

   private CommandRegistry.Pair<Command, Integer> createResultOrNull(Command parent, int index) {
      return parent != null ? new CommandRegistry.Pair<>(parent, index) : null;
   }

   private List<Object> parseArgs(Command cmd, String[] tok, int startIdx) {
      List<Parameter<?>> params = cmd.parameters();
      List<Object> parsed = new ArrayList<>();
      int argCursor = startIdx + 1;
      int tokLen = tok.length;

      for (Parameter<?> p : params) {
         if (p.vararg()) {
            List<Object> vararg = new ArrayList<>();

            for (int j = argCursor; j < tokLen; j++) {
               ValidationResult result = p.validator().validate(tok[j]);
               if (result instanceof ValidationResult.Error) {
                  return null;
               }

               vararg.add(((ValidationResult.Ok)result).value());
            }

            parsed.add(vararg);
            return parsed;
         }

         if (argCursor >= tokLen) {
            if (p.required()) {
               return null;
            }

            parsed.add(null);
         } else {
            ValidationResult result = p.validator().validate(tok[argCursor]);
            if (result instanceof ValidationResult.Error) {
               return null;
            }

            parsed.add(((ValidationResult.Ok)result).value());
            argCursor++;
         }
      }

      return parsed;
   }

   public CompletableFuture<Suggestions> autoComplete(String orig, int cursor) {
      if (orig.startsWith(this.prefix) && cursor >= this.prefix.length()) {
         String text = orig.substring(0, Math.min(cursor, orig.length()));
         String afterPrefix = text.substring(this.prefix.length());
         boolean trailingSpace = afterPrefix.endsWith(" ");
         String trimmed = afterPrefix.trim();
         String[] tokens = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
         List<Command> pool = this.commands;
         Command current = null;
         int argStart = 0;

         for (int i = 0; i < tokens.length; i++) {
            Command matched = this.findExact(pool, tokens[i]);
            if (matched == null) {
               break;
            }

            current = matched;
            argStart = i + 1;
            pool = matched.subcommands();
            if (pool.isEmpty()) {
               break;
            }
         }

         int argsCount = tokens.length - argStart;
         String partial = !trailingSpace && tokens.length > 0 ? tokens[tokens.length - 1] : "";
         int start = Math.max(this.prefix.length(), orig.lastIndexOf(32, Math.max(0, cursor - 1)) + 1);
         StringRange range = StringRange.between(start, cursor);
         List<Suggestion> suggestions = new ArrayList<>();
         if (current == null) {
            String check = partial.toLowerCase();

            for (Command c : pool) {
               String name = c.names().getFirst();
               if (name.toLowerCase().startsWith(check)) {
                  suggestions.add(new Suggestion(range, name));
               }
            }
         } else if (!pool.isEmpty() && argsCount == 0) {
            String check = partial.toLowerCase();

            for (Command cx : pool) {
               String name = cx.names().getFirst();
               if (name.toLowerCase().startsWith(check)) {
                  suggestions.add(new Suggestion(range, name));
               }
            }
         } else {
            List<Parameter<?>> params = current.parameters();
            int paramIndex = argsCount - (trailingSpace ? 0 : 1);
            if (paramIndex < 0) {
               paramIndex = 0;
            }

            Parameter<?> param = null;
            if (paramIndex >= params.size()) {
               if (!params.isEmpty() && params.getLast().vararg()) {
                  param = params.getLast();
               }
            } else {
               param = params.get(paramIndex);
            }

            if (param != null) {
               String check = partial.toLowerCase();

               for (String s : param.validator().suggestions(check)) {
                  suggestions.add(new Suggestion(range, s));
               }
            }
         }

         return !suggestions.isEmpty() ? CompletableFuture.completedFuture(new Suggestions(range, suggestions)) : Suggestions.empty();
      } else {
         return Suggestions.empty();
      }
   }

   private Command findExact(List<Command> pool, String token) {
      for (Command c : pool) {
         for (String n : c.names()) {
            if (n.equalsIgnoreCase(token)) {
               return c;
            }
         }
      }

      return null;
   }

   @Generated
   public String getPrefix() {
      return this.prefix;
   }

   @Generated
   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   private record Pair<T, U>(T command, U index) {
   }
}
