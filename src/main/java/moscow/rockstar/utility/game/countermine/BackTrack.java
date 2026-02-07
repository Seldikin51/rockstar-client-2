package moscow.rockstar.utility.game.countermine;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.modules.modules.other.CounterMine;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.Vec3d;

public class BackTrack implements IMinecraft {
   private final BooleanSetting backTrack;
   private final SliderSetting ticks;
   private final List<Packet<?>> packets = new ArrayList<>();
   private final Timer timer = new Timer();
   private Vec3d lastPos;
   private boolean replaying;
   private boolean unFreeze;
   private final EventListener<SendPacketEvent> sendListener = this::savePacket;
   private final EventListener<WorldChangeEvent> world = e -> {};

   public BackTrack(CounterMine cm) {
      this.backTrack = new BooleanSetting(cm, "BackTrack");
      this.ticks = new SliderSetting(cm, "BackTrack").min(0.0F).max(2000.0F).step(50.0F).currentValue(1000.0F).suffix(number -> " ms");
   }

   public void savePacket(SendPacketEvent e) {
      if (!this.replaying && EntityUtility.isInGame() && this.backTrack.isEnabled()) {
         System.out.println(e.getPacket());
         this.packets.add(e.getPacket());
         e.cancel();
         if (this.timer.finished((long)this.ticks.getCurrentValue())) {
            this.unFreeze = true;
            this.disable();
            this.enable();
            this.timer.reset();
         }
      }
   }

   public void enable() {
      if (mc.player != null) {
         this.packets.clear();
         this.lastPos = mc.player.getPos();
         this.timer.reset();
         this.replaying = false;
      }
   }

   public void disable() {
      if (mc.player != null) {
         this.replaying = true;

         for (Packet<?> p : this.packets) {
            mc.player.networkHandler.sendPacket(p);
         }

         this.replaying = false;
         this.packets.clear();
         this.lastPos = null;
      }
   }

   @Generated
   public BooleanSetting getBackTrack() {
      return this.backTrack;
   }

   @Generated
   public boolean isUnFreeze() {
      return this.unFreeze;
   }

   @Generated
   public void setUnFreeze(boolean unFreeze) {
      this.unFreeze = unFreeze;
   }
}
