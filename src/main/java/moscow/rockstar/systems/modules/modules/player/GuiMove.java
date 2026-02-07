package moscow.rockstar.systems.modules.modules.player;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(name = "Gui Move", category = ModuleCategory.PLAYER, enabledByDefault = true)
public class GuiMove extends BaseModule {
   private final List<Packet<?>> packets = new ArrayList<>();
   private final List<ClickSlotC2SPacket> pickupPackets = new ArrayList<>();
   private final ModeSetting mode = new ModeSetting(this, "Режим");
   private final ModeSetting.Value noBypass = new ModeSetting.Value(this.mode, "Без обхода");
   private final ModeSetting.Value auto = new ModeSetting.Value(this.mode, "Автоматический").select();
   private final ModeSetting.Value custom = new ModeSetting.Value(this.mode, "Настраиваемый");
   private final ModeSetting containers = new ModeSetting(this, "В контейнерах", this.auto::isSelected);
   private final ModeSetting.Value notWork = new ModeSetting.Value(this.containers, "Стоять");
   private final ModeSetting.Value vanillaCon = new ModeSetting.Value(this.containers, "Двигаться").select();
   private final ModeSetting.Value shift = new ModeSetting.Value(this.containers, "Шифт");
   private final BooleanSetting cancelClose = new BooleanSetting(this, "Отменять закрытие", this.auto::isSelected).enable();
   private final BooleanSetting jump = new BooleanSetting(this, "Учитывать прыжок", this.auto::isSelected);
   private final ModeSetting bypassMode = new ModeSetting(this, "Обход", this.auto::isSelected);
   private final ModeSetting.Value vanilla = new ModeSetting.Value(this.bypassMode, "Без обхода");
   private final ModeSetting.Value slow = new ModeSetting.Value(this.bypassMode, "Замедление").select();
   private final ModeSetting.Value slowClose = new ModeSetting.Value(this.bypassMode, "При закрытии");
   private final ModeSetting.Value close = new ModeSetting.Value(this.bypassMode, "Фейк закрытие");
   private final BooleanSetting ground = new BooleanSetting(this, "Без обхода на земле", () -> this.vanilla.isSelected() || this.auto.isSelected());
   private final SliderSetting cooldown = new SliderSetting(
         this, "Задержка", () -> this.vanilla.isSelected() || this.close.isSelected() || this.auto.isSelected()
      )
      .min(50.0F)
      .max(500.0F)
      .step(50.0F)
      .currentValue(100.0F)
      .suffix(" ms");
   private final Timer staying = new Timer();
   private final Timer grounding = new Timer();
   private boolean stay;
   private boolean sending;
   private int screenId = 0;
   private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
      if (this.auto.isSelected()) {
         if (ServerUtility.isST()) {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.enable();
            this.slow.select();
            this.ground.enable();
            this.cooldown.setCurrentValue(500.0F);
         } else if (ServerUtility.isFT()) {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.setEnabled(false);
            this.slowClose.select();
            this.ground.setEnabled(false);
            this.cooldown.setCurrentValue(100.0F);
         } else if (ServerUtility.isFT()) {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.setEnabled(false);
            this.slowClose.select();
            this.ground.setEnabled(false);
            this.cooldown.setCurrentValue(100.0F);
         } else if (ServerUtility.isIntave()) {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.setEnabled(false);
            this.slow.select();
            this.ground.setEnabled(false);
            this.cooldown.setCurrentValue(150.0F);
         } else if (ServerUtility.isHW()) {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.setEnabled(false);
            this.slow.select();
            this.ground.setEnabled(false);
            this.cooldown.setCurrentValue(100.0F);
         } else {
            this.notWork.select();
            this.cancelClose.enable();
            this.jump.setEnabled(false);
            this.slow.select();
            this.ground.setEnabled(false);
            this.cooldown.setCurrentValue(100.0F);
         }
      }

      if (mc.player.currentScreenHandler != null) {
         this.screenId = mc.player.currentScreenHandler.syncId;
      } else {
         this.screenId = 0;
      }

      if (this.slowClose.isSelected() && mc.currentScreen == null && !this.packets.isEmpty()) {
         this.stay = true;
      }

      if (this.canSend()) {
         this.sendPackets();
         this.stay = false;
      }

      if (!mc.player.isOnGround()) {
         this.grounding.reset();
      }

      if (mc.player.currentScreenHandler != null
         && (mc.player.currentScreenHandler.getCursorStack().isEmpty() || mc.player.currentScreenHandler.getCursorStack() == ItemStack.EMPTY)
         && !this.pickupPackets.isEmpty()
         && !this.sending) {
         this.stay = true;
         if (mc.player.isOnGround() && this.ground.isEnabled() || this.staying.finished((long)this.cooldown.getCurrentValue())) {
            this.sendPickupPackets();
         }
      }

      if (!this.isTyping() && this.invCheck()) {
         long windowHandle = mc.getWindow().getHandle();
         KeyBinding[] movementKeys = new KeyBinding[]{mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey};

         for (KeyBinding key : movementKeys) {
            int keyCode = InputUtil.fromTranslationKey(key.getBoundKeyTranslationKey()).getCode();
            key.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }

         if (mc.player.getAbilities().flying) {
            int keyCode = InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode();
            mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(windowHandle, keyCode));
         }
      }
   };
   private final EventListener<SendPacketEvent> eventEventListener = event -> {
      if (!(mc.currentScreen instanceof GenericContainerScreen container && container.getTitle().getString().toLowerCase().contains("выбор"))) {
         if (mc.player != null
            && !this.isTyping()
            && this.invCheck()
            && !this.vanilla.isSelected()
            && !this.sending
            && (!mc.player.isOnGround() || !this.ground.isEnabled())
            && !this.noBypass.isSelected()) {
            if (event.getPacket() instanceof ClickSlotC2SPacket packet) {
               if (this.ground.isEnabled() && mc.player.isOnGround()) {
                  this.grounding.reset();
               }

               if (packet.getActionType() == SlotActionType.PICKUP
                  || packet.getActionType() == SlotActionType.PICKUP_ALL
                  || packet.getActionType() == SlotActionType.CLONE
                  || packet.getActionType() == SlotActionType.QUICK_CRAFT) {
                  this.pickupPackets.add(packet);
                  event.cancel();
                  return;
               }

               if (this.slow.isSelected()) {
                  this.packets.add(packet);
                  event.cancel();
                  this.stay = true;
               } else if (this.slowClose.isSelected()) {
                  this.packets.add(packet);
                  event.cancel();
                  if (mc.currentScreen instanceof GenericContainerScreen
                     || ServerUtility.isPastaFT() && packet.getStack().getItem() instanceof BlockItem item && item.getBlock() instanceof ShulkerBoxBlock
                     || packet.getActionType() == SlotActionType.THROW) {
                     this.stay = true;
                  }
               } else if (this.close.isSelected()) {
                  mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.screenId));
               }
            }
         }
      }
   };
   private final EventListener<InputEvent> onInput = event -> {
      if (mc.currentScreen instanceof GenericContainerScreen && this.shift.isSelected()) {
         event.setSneak(true);
      }

      if (this.stay) {
         event.setForward(0.0F);
         event.setStrafe(0.0F);
         if (this.jump.isEnabled()) {
            event.setJump(false);
         }
      }

      if (this.jump.isEnabled() && event.isJump() || event.getStrafe() != 0.0F || event.getForward() != 0.0F) {
         this.staying.reset();
      }
   };
   private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
      if (this.cancelClose.isEnabled() && event.getPacket() instanceof CloseScreenS2CPacket) {
         event.cancel();
      }
   };

   private boolean isTyping() {
      return mc.currentScreen instanceof ChatScreen
         || mc.currentScreen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused()
         || mc.currentScreen instanceof SignEditScreen
         || mc.currentScreen instanceof AnvilScreen
         || mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreen.selectedTab == ItemGroups.getSearchGroup();
   }

   private boolean invCheck() {
      return !this.notWork.isSelected()
         || this.ground.isEnabled() && mc.player.isOnGround()
         || mc.currentScreen instanceof InventoryScreen
         || mc.currentScreen instanceof CreativeInventoryScreen
         || mc.currentScreen == null;
   }

   private void sendPackets() {
      if (!this.packets.isEmpty()) {
         this.sending = true;
         this.packets.forEach(mc.player.networkHandler::sendPacket);
         this.packets.clear();
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(this.screenId));
         this.sending = false;
      }
   }

   public boolean canSend() {
      return this.isEnabled()
         && this.stay
         && (mc.player.isOnGround() && this.ground.isEnabled() && this.grounding.finished(500L) || this.staying.finished((long)this.cooldown.getCurrentValue()));
   }

   private void sendPickupPackets() {
      if (!this.pickupPackets.isEmpty()) {
         this.sending = true;
         this.pickupPackets.forEach(mc.player.networkHandler::sendPacket);
         this.pickupPackets.clear();
         this.sending = false;
      }
   }

   public boolean slowing() {
      return this.slow.isSelected() || this.slowClose.isSelected();
   }

   @Generated
   public SliderSetting getCooldown() {
      return this.cooldown;
   }

   @Generated
   public void setStay(boolean stay) {
      this.stay = stay;
   }

   @Generated
   public void setSending(boolean sending) {
      this.sending = sending;
   }
}
