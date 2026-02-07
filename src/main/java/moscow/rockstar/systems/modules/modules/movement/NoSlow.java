package moscow.rockstar.systems.modules.modules.movement;

import java.util.Random;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.SlowDownEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

@ModuleInfo(name = "No Slow", category = ModuleCategory.MOVEMENT)
public class NoSlow extends BaseModule {
   private int ticks;
   private final moscow.rockstar.utility.time.Timer timer = new moscow.rockstar.utility.time.Timer();
   private final Random random = new Random();
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.noslow.mode");
   private final ModeSetting.Value grim = new ModeSetting.Value(this.mode, "modules.settings.noslow.grim");
   private final ModeSetting.Value spooky = new ModeSetting.Value(this.mode, "modules.settings.noslow.spooky");
   private final ModeSetting.Value holyWorld = new ModeSetting.Value(this.mode, "modules.settings.noslow.holly");
   private final EventListener<SlowDownEvent> onSlowEventEvent = event -> {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (!mc.player.isUsingItem()) {
            this.ticks = 0;
         } else {
            if (this.spooky.isSelected() || this.holyWorld.isSelected()) {
               this.ticks++;
            }

            if ((
                  mc.player.getMainHandStack().getUseAction() != UseAction.BLOCK && mc.player.getOffHandStack().getUseAction() != UseAction.EAT
                     || mc.player.getActiveHand() != Hand.MAIN_HAND
               )
               && mc.player.isUsingItem()) {
               mc.player.setSprinting(true);
               if (mc.player.getActiveHand() == Hand.MAIN_HAND && !this.spooky.isSelected()) {
                  mc.interactionManager
                     .sendSequencedPacket(
                        mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, mc.player.getYaw(), mc.player.getPitch())
                     );
                  event.cancel();
               } else {
                  Hand hand = mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                  if (!this.spooky.isSelected() && !this.holyWorld.isSelected()) {
                     mc.interactionManager
                        .sendSequencedPacket(
                           mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch())
                        );
                  }

                  if (this.ticks >= 2 || this.grim.isSelected() || this.holyWorld.isSelected()) {
                     event.cancel();
                     this.ticks = 0;
                  }
               }
            }
         }
      }
   };
   private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {};
   private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
      if (mc.player != null && mc.player.getItemCooldownManager() != null && mc.getNetworkHandler() != null && this.grim.isSelected()) {
         if (!mc.player.getItemCooldownManager().isCoolingDown(mc.player.getMainHandStack().getItem().getDefaultStack())
            && !mc.player.getItemCooldownManager().isCoolingDown(mc.player.getOffHandStack().getItem().getDefaultStack())
            && mc.player.isUsingItem()
            && mc.player.fallDistance < 1.0F
            && mc.player.getActiveHand() == Hand.OFF_HAND) {
         }
      }
   };
}
