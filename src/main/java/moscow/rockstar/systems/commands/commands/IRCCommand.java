package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
// import moscow.rockstar.systems.commands.CommandContext;
// import pw.lucent.bridge.client.core.Network;
// import pw.lucent.bridge.shared.packet.impl.client.community.C2SPacketIRCMessage;

// import java.util.List;

public class IRCCommand {
   public Command command() {
      // Disabled due to missing dependency pw.lucent
      return CommandBuilder.begin("irc", b -> b.desc("Коммуникации между пользователями").handler(ctx -> {
      })).build();
   }

   // private void handle(CommandContext ctx) {
   // List<String> msgParts = (List<String>)ctx.arguments().get(0);
   // String msg = String.join(" ", msgParts);
   // Network.getInstance().getClient().sendMessage(new
   // C2SPacketIRCMessage(msg.replace("&", "§").replace("\\n", "\n")));
   // }
}
