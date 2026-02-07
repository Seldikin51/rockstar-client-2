package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.server.ServerUtility;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@ModuleInfo(name = "Auto Accept", category = ModuleCategory.OTHER, desc = "Автоматически принимает телепортацию")
public class AutoAccept extends BaseModule {
   private final ModeSetting acceptMode = new ModeSetting(this, "Принимать");
   private final ModeSetting.Value acceptAll = new ModeSetting.Value(this.acceptMode, "Всех");
   private final ModeSetting.Value friendsOnly = new ModeSetting.Value(this.acceptMode, "Только друзей");
   private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
      if (event.getPacket() instanceof GameMessageS2CPacket packet
         && mc.player != null
         && packet.content().getString().contains("телепортироваться")
         && !ServerUtility.hasCT
         && this.canAccept(packet.content().getString())) {
         mc.player.networkHandler.sendChatCommand("tpaccept");
      }
   };

   private boolean canAccept(String message) {
      if (this.acceptMode.is(this.acceptAll)) {
         return true;
      } else {
         if (this.acceptMode.is(this.friendsOnly)) {
            if (Rockstar.getInstance().getFriendManager().isFriend(message.split(" ")[1])
               || Rockstar.getInstance()
                  .getFriendManager()
                  .isFriend(message.replace("\u0a77 просит телепортироваться к Вам.\u0a77§l [ੲ§l✔\u0a77§l]\u0a77§l [\u0a7c§l✗\u0a77§l]", "").replace("੶", ""))
               || Rockstar.getInstance().getFriendManager().isFriend(message.replace("➝ Ник: ", ""))) {
               return true;
            }

            if (message.contains("телепортироваться")) {
               String[] parts = message.split(" ");
               return parts.length >= 2 && Rockstar.getInstance().getFriendManager().isFriend(parts[2]);
            }
         }

         return false;
      }
   }
}
