package moscow.rockstar.ui.hud.impl.island.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.TimerStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class EventStatus extends TimerStatus {
   private final List<EventStatus.ActiveEvent> activeEvents = new ArrayList<>();
   private final Timer timer = new Timer();
   private String pendingEvent;
   private int an = -1;
   private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
      if (event.getPacket() instanceof GameMessageS2CPacket packet) {
         String message = packet.content().getString().replaceAll("\\n", " ")
               .replaceAll("[^\\p{L}\\p{N}\\s\\[\\]:.-]", "").replaceAll("\\s{2,}", " ").trim();
         if (packet.content().getString().contains("Появился")) {
            Matcher eventMatcher = Pattern.compile("\\[([^\\]]+)\\]").matcher(message);
            Matcher coordMatcher = Pattern.compile("координатах\\s+(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)").matcher(message);
            if (eventMatcher.find() && coordMatcher.find()) {
               String eventName = eventMatcher.group(1);

               for (EventStatus.EventType value : EventStatus.EventType.values()) {
                  if (eventName.toLowerCase().contains(value.getName().toLowerCase())) {
                     this.activeEvents.removeIf(e -> e.type == value);
                     this.activeEvents.add(new EventStatus.ActiveEvent(value, value.getTime()));
                     this.an = ServerUtility.ftAn;
                     this.timer.reset();
                     if (this.an == ServerUtility.ftAn) {
                        Rockstar.getInstance()
                              .getWayPointsManager()
                              .add(
                                    this.activeEvents.getFirst().type.getName(),
                                    Integer.parseInt(coordMatcher.group(1)),
                                    Integer.parseInt(coordMatcher.group(2)),
                                    Integer.parseInt(coordMatcher.group(3)));
                     }
                     break;
                  }
               }
            }
         } else {
            for (EventStatus.EventType valuex : EventStatus.EventType.values()) {
               if (message.equalsIgnoreCase(valuex.getName())) {
                  this.pendingEvent = valuex.getName();
                  break;
               }
            }

            if (message.toLowerCase().startsWith("координаты")) {
               Matcher coordMatcher = Pattern.compile("координаты:?\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)", 2)
                     .matcher(message);
               if (coordMatcher.find() && this.pendingEvent != null) {
                  for (EventStatus.EventType valuexx : EventStatus.EventType.values()) {
                     if (this.pendingEvent.equalsIgnoreCase(valuexx.getName())) {
                        this.activeEvents.removeIf(e -> e.type == valuexx);
                        this.activeEvents.add(new EventStatus.ActiveEvent(valuexx, valuexx.getTime()));
                        this.timer.reset();
                        Rockstar.getInstance()
                              .getWayPointsManager()
                              .add(
                                    valuexx.getName(),
                                    Integer.parseInt(coordMatcher.group(1)),
                                    Integer.parseInt(coordMatcher.group(2)),
                                    Integer.parseInt(coordMatcher.group(3)));
                        break;
                     }
                  }

                  this.pendingEvent = null;
               }
            }
         }
      }
   };
   private final EventListener<WorldChangeEvent> worldChangeEvent = event -> {
      if (ServerUtility.ftAn != this.an) {
         this.activeEvents.forEach(e -> Rockstar.getInstance().getWayPointsManager().del(e.type().getName()));
         this.activeEvents.clear();
      }
   };

   public EventStatus(SelectSetting setting) {
      super(setting, "events");
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   @Override
   public void draw(CustomDrawContext context) {
      this.activeEvents.removeIf(event -> {
         if (this.timer.getElapsedTime() >= event.type().getTime()) {
            Rockstar.getInstance().getWayPointsManager().del(event.type().getName());
            return true;
         } else {
            return false;
         }
      });
      if (!this.activeEvents.isEmpty()) {
         EventStatus.ActiveEvent currentEvent = this.activeEvents.getFirst();
         long remaining = currentEvent.type.getTime() - this.timer.getElapsedTime();
         if (remaining > 0L) {
            int timer = (int) (remaining / 1000L);
            int min = timer / 60;
            int sec = timer % 60;
            String time = String.format("%d:%02d", min, sec);
            ColorRGBA color = this.getEventColor(currentEvent.type);
            this.update(Integer.parseInt(time.split(":")[0]) + ":", "", Integer.parseInt(time.split(":")[1]),
                  currentEvent.type.name, color);
            super.draw(context);
         }
      }
   }

   private ColorRGBA getEventColor(EventStatus.EventType eventType) {
      return switch (eventType) {
         case ALTAR -> new ColorRGBA(138.0F, 43.0F, 226.0F);
         case BEACON -> new ColorRGBA(255.0F, 69.0F, 0.0F);
         case VULCAN -> new ColorRGBA(255.0F, 140.0F, 0.0F);
         case METEOR -> new ColorRGBA(70.0F, 130.0F, 180.0F);
         case PACKAGE -> new ColorRGBA(243.0F, 196.0F, 82.0F);
         case BOSS -> new ColorRGBA(139.0F, 222.0F, 221.0F);
         case CONTAINER -> new ColorRGBA(141.0F, 99.0F, 184.0F);
         case GRUZ -> new ColorRGBA(41.0F, 253.0F, 5.0F);
         case MYSTERIOUS_SHIP -> new ColorRGBA(90.0F, 158.0F, 152.0F);
      };
   }

   @Override
   public boolean canShow() {
      return !this.activeEvents.isEmpty();
   }

   private record ActiveEvent(EventStatus.EventType type, long time) {
   }

   static enum EventType {
      ALTAR("Мистический Алтарь", 360000L),
      BEACON("Маяк Убийца", 360000L),
      VULCAN("Вулкан", 300000L),
      METEOR("Метеоритный дождь", 180000L),
      PACKAGE("Посылка", 180000L),
      BOSS("Босс", 180000L),
      CONTAINER("Контейнер", 180000L),
      GRUZ("Груз", 180000L),
      MYSTERIOUS_SHIP("Таинственный корабль", 300000L);

      final String name;
      final long time;

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public long getTime() {
         return this.time;
      }

      @Generated
      private EventType(final String name, final long time) {
         this.name = name;
         this.time = time;
      }
   }
}
