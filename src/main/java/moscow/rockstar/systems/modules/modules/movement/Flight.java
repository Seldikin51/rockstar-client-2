package moscow.rockstar.systems.modules.modules.movement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEndEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Flight", category = ModuleCategory.MOVEMENT)
public class Flight extends BaseModule {
   private final ModeSetting mode = new ModeSetting(this, "modules.settings.flight.mode");
   private final ModeSetting.Value vanilla = new ModeSetting.Value(this.mode, "modules.settings.flight.vanilla");
   private final ModeSetting.Value elytraY = new ModeSetting.Value(this.mode, "ElytraY");
   private final ModeSetting.Value elytraGlide = new ModeSetting.Value(this.mode, "Elytra Glide").select();
   private final ModeSetting.Value grimGlide = new ModeSetting.Value(this.mode, "Grim Glide");
   private boolean wasFlyingAllowed = false;
   private boolean wasFlying = false;
   private final SliderSetting speed = new SliderSetting(this, "modules.settings.flight.speed", () -> !this.vanilla.isSelected())
      .currentValue(1.0F)
      .max(10.0F)
      .min(0.1F)
      .step(0.1F);
   private int ticks;
   private moscow.rockstar.utility.time.Timer ticksTimer = new moscow.rockstar.utility.time.Timer();
   int ticksTwo = 0;
   private final EventListener<ClientPlayerTickEndEvent> tickEnd = event -> {
      if (this.grimGlide.isSelected() && mc.player.isGliding()) {
         this.ticksTwo++;
         Vec3d pos = mc.player.getPos();
         float yaw = mc.player.getYaw();
         double forward = 0.085F;
         double motion = getBps(mc.player, 1);
         if (motion >= 52.0) {
            forward = 0.0;
         }

         double dx = -Math.sin(Math.toRadians(yaw)) * forward;
         double dz = Math.cos(Math.toRadians(yaw)) * forward;
         mc.player.setVelocity(dx * MathUtility.random(1.1F, 1.21F), mc.player.getVelocity().y - 0.01F, dz * MathUtility.random(1.1F, 1.21F));
         if (this.ticksTimer.finished(45L)) {
            mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            this.ticksTimer.reset();
         }

         mc.player.setVelocity(dx * MathUtility.random(1.1F, 1.21F), mc.player.getVelocity().y + 0.015F, dz * MathUtility.random(1.1F, 1.21F));
      }
   };

   @Override
   public void onEnable() {
      if (mc.player != null) {
         if (this.vanilla.isSelected()) {
            mc.player.getAbilities().allowFlying = true;
         }

         super.onEnable();
      }
   }

   @Override
   public void tick() {
      if (mc.player != null) {
         if (this.vanilla.isSelected()) {
            this.wasFlyingAllowed = mc.player.getAbilities().allowFlying;
            this.wasFlying = mc.player.getAbilities().flying;
            mc.player.getAbilities().allowFlying = true;
            mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0F);
            super.tick();
         } else if (this.elytraY.isSelected()) {
            if (mc.player.isGliding()) {
               mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + 0.05999999761581421, mc.player.getVelocity().z);
            }
         } else if (this.elytraGlide.isSelected()) {
            SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
            HotbarSlot slot = slotsToSearch.findItem(Items.ELYTRA);
            if (slot != null && mc.player.age % 10 != 0) {
               HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
               InventoryUtility.selectHotbarSlot(slot);
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               ((Slot)mc.player.currentScreenHandler.slots.get(6)).setStack(new ItemStack(Items.ELYTRA));
               if (mc.player.isSprinting() && mc.player.input.hasForwardMovement() && mc.player.checkGliding()) {
                  mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
               }

               InventoryUtility.selectHotbarSlot(currentItem);
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
         }
      }
   }

   public static double getBps(Entity entity, int decimal) {
      double x = entity.getX() - entity.prevX;
      double y = entity.getY() - entity.prevY;
      double z = entity.getZ() - entity.prevZ;
      double speed = Math.sqrt(x * x + y * y + z * z) * 20.0;
      return roundHalfUp(speed, decimal);
   }

   public static double roundHalfUp(double num, double increment) {
      double v = Math.round(num / increment) * increment;
      BigDecimal bd = new BigDecimal(v);
      bd = bd.setScale(2, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         if (this.vanilla.isSelected()) {
            mc.player.getAbilities().setFlySpeed(0.05F);
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
         }

         super.onDisable();
      }
   }
}
