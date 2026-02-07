package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name = "Auto Duels", category = ModuleCategory.OTHER, desc = "Автоматически кидает дуэли")
public class AutoDuels extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "Предпочитать");
   private final ModeSetting.Value soft = new ModeSetting.Value(this.mode, "Софтеров");
   private final ModeSetting.Value anSoft = new ModeSetting.Value(this.mode, "Ансофтеров");
   private final ModeSetting.Value random = new ModeSetting.Value(this.mode, "Рандом");
   private final ModeSetting kit = new ModeSetting(this, "Кит");
   private final ModeSetting.Value shield = new ModeSetting.Value(this.kit, "Щит");
   private final ModeSetting.Value shipi = new ModeSetting.Value(this.kit, "Шипы 3");
   private final ModeSetting.Value bow = new ModeSetting.Value(this.kit, "Лук");
   private final ModeSetting.Value totem = new ModeSetting.Value(this.kit, "Тотем");
   private final ModeSetting.Value noDebaff = new ModeSetting.Value(this.kit, "НоуДебаф");
   private final ModeSetting.Value balls = new ModeSetting.Value(this.kit, "Шары");
   private final ModeSetting.Value classik = new ModeSetting.Value(this.kit, "Классик");
   private final ModeSetting.Value cheats = new ModeSetting.Value(this.kit, "Читерский рай");
   private final ModeSetting.Value nezer = new ModeSetting.Value(this.kit, "Незер");
   private final Timer count = new Timer();
   private final List<String> sent = new ArrayList<>();
   private final EventListener<ReceivePacketEvent> onReceive = event -> {
      if (event.getPacket() instanceof GameMessageS2CPacket packet) {
         String msg = packet.content().getString();
         if (msg.contains("принял") && !msg.contains("не принял") || msg.contains("команды")) {
            this.sent.clear();
            this.toggle();
         }

         if (msg.contains("Баланс") || msg.contains("отключил запросы")) {
            event.cancel();
         }
      }
   };
   private final EventListener<WorldChangeEvent> world = e -> this.disable();

   @Override
   public void tick() {
      List<String> playerNames = new ArrayList<>();

      for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
         playerNames.add(entry.getProfile().getName());
      }

      if (this.random.isSelected()) {
         Collections.shuffle(playerNames);
      } else if (this.soft.isSelected()) {
         Collections.reverse(playerNames);
      }

      for (String name : playerNames) {
         if (this.count.finished(750L) && !this.sent.contains(name) && !name.equals(mc.player.getNameForScoreboard())) {
            mc.player.networkHandler.sendChatCommand("duel " + name);
            this.sent.add(name);
            this.count.reset();
         }
      }

      if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
         String title = mc.currentScreen.getTitle().getString();
         if (title.contains("Выбор набора")) {
            mc.interactionManager
               .clickSlot(
                  mc.player.currentScreenHandler.syncId, this.kit.getValues().indexOf(this.kit.getRandomEnabledElement()), 0, SlotActionType.PICKUP, mc.player
               );
            mc.player.currentScreenHandler.onSlotClick(this.kit.getValues().indexOf(this.kit.getRandomEnabledElement()), 0, SlotActionType.PICKUP, mc.player);
         } else if (title.contains("Настройка поединка")) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 0, SlotActionType.PICKUP, mc.player);
            mc.player.currentScreenHandler.onSlotClick(0, 0, SlotActionType.PICKUP, mc.player);
         }
      }

      super.tick();
   }

   @Override
   public void onEnable() {
      this.count.reset();
      super.onEnable();
   }
}
