package moscow.rockstar.systems.modules.modules.other;

import java.util.function.BooleanSupplier;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler.ConfirmServerResourcePackScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

@ModuleInfo(name = "Assist", category = ModuleCategory.OTHER, desc = "Помощник для разных серверов")
public class Assist extends BaseModule {
   private final BooleanSupplier rwCondition = () -> ServerUtility.isHW()
      || ServerUtility.isPastaFT()
      || ServerUtility.isCM()
      || ServerUtility.isSaturn()
      || ServerUtility.isIntave();
   private final BooleanSupplier ftCondition = () -> ServerUtility.isHW()
      || ServerUtility.isRW()
      || ServerUtility.isCM()
      || ServerUtility.isSaturn()
      || ServerUtility.isIntave();
   private final BooleanSupplier hwCondition = () -> ServerUtility.isPastaFT()
      || ServerUtility.isRW()
      || ServerUtility.isCM()
      || ServerUtility.isSaturn()
      || ServerUtility.isIntave();
   private final BooleanSetting spoof = new BooleanSetting(this, "Спуф рп", "Позволяет зайти на сервер без скачивания ресурс пака", this.rwCondition);
   private final BooleanSetting closeMenu = new BooleanSetting(this, "Закрывать меню", "Автоматически закрывает меню при заходе на гриф", this.rwCondition)
      .enable();
   private final BooleanSetting autoFix = new BooleanSetting(this, "Автопочинка", this.rwCondition);
   private final BooleanSetting warnArmor = new BooleanSetting(this, "Поломка брони", "Предупреждает если броня поломана");
   private final BooleanSetting fly = new BooleanSetting(this, "Драгон флай", "Ускоряет флай из креатива").enable();
   private final SliderSetting flySpeedXZ = new SliderSetting(this, "Ускорять по XZ", () -> !this.fly.isEnabled())
      .currentValue(1.0F)
      .max(5.0F)
      .min(1.0F)
      .step(0.5F)
      .currentValue(5.0F);
   private final SliderSetting flySpeedY = new SliderSetting(this, "Ускорять по Y", () -> !this.fly.isEnabled())
      .currentValue(1.0F)
      .max(5.0F)
      .min(1.0F)
      .step(0.5F)
      .currentValue(5.0F);
   private final BooleanSetting autoPiona = new BooleanSetting(
         this, "Авто пиона", "Автоматически прописывает /piona при заходе на Funtime", ServerUtility::isRW
      )
      .enable();
   private final BindSetting dezorentKey = new BindSetting(this, "Дезориентация", this.ftCondition);
   private final BindSetting trapkaKey = new BindSetting(this, "Трапка", this.ftCondition);
   private final BindSetting smerchKey = new BindSetting(this, "Огненный смерч", this.ftCondition);
   private final BindSetting plastKey = new BindSetting(this, "Пласт", this.ftCondition);
   private final BindSetting auraKey = new BindSetting(this, "Божья аура", this.ftCondition);
   private final BindSetting pilbKey = new BindSetting(this, "Явная пыль", this.ftCondition);
   private final BooleanSetting autoZako = new BooleanSetting(this, "Авто /zako", this.hwCondition);
   private final BooleanSetting autoStop = new BooleanSetting(this, "Авто-стоп", this.hwCondition);
   private final BindSetting stanKey = new BindSetting(this, "Стан", this.hwCondition);
   private final BindSetting snowKey = new BindSetting(this, "Ком снега", this.hwCondition);
   private final BindSetting bombKey = new BindSetting(this, "Взрывная штучка", this.hwCondition);
   private final BindSetting hwTrapKey = new BindSetting(this, "Трапка", this.hwCondition);
   private final BindSetting boomTrapKey = new BindSetting(this, "Взрывная трапка", this.hwCondition);
   private final BindSetting goolKey = new BindSetting(this, "Прощальный гул", this.hwCondition);
   private final BindSetting backpackKey = new BindSetting(this, "Рюкзак", this.hwCondition);
   private final Timer timer = new Timer();
   private final Timer timerStop = new Timer();
   private boolean stopHandle;
   private boolean zakoCommandSent = true;
   private boolean pionaCommandSent = true;
   private boolean visible;
   private final EventListener<MouseEvent> onMouseEvent = event -> this.handleButtonPress(event.getButton());
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (event.getAction() == 1) {
         this.handleButtonPress(event.getKey());
      }
   };
   private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
      if (this.autoPiona.isEnabled() && event.getPacket() instanceof GameMessageS2CPacket packet) {
         String message = packet.content().getString().toLowerCase();
         if (message.contains("10,000 было начислено вам") || message.contains("повторите текст еще раз")) {
            this.pionaCommandSent = false;
            this.timer.reset();
         }
      }

      if (this.autoZako.isEnabled() && event.getPacket() instanceof GameMessageS2CPacket packetx) {
         String message = packetx.content().getString();
         if (message.contains("Вы уже активировали этот промокод")) {
            this.zakoCommandSent = false;
            this.timer.reset();
         } else if (message.contains("Прямо сейчас идет набор")) {
            this.zakoCommandSent = true;
         }
      }

      if (this.autoStop.isEnabled() && event.getPacket() instanceof GameMessageS2CPacket packetxx) {
         String message = packetxx.content().getString();
         if (message.contains("Телепортация принята")) {
            this.stopHandle = true;
         }
      }

      if (this.closeMenu.isEnabled() && event.getPacket() instanceof OpenScreenS2CPacket packetxxx) {
         String title = packetxxx.getName().getString();
         if (title.contains("Меню") || title.contains("ꈁꀀꈂꌁꈂꀁ")) {
            mc.player.closeScreen();
            event.cancel();
         }
      }
   };
   private final EventListener<InputEvent> inputEvent = event -> {
      if (this.autoStop.isEnabled() && this.stopHandle && !this.timerStop.finished(3200L)) {
         event.setForward(0.0F);
         event.setJump(false);
         event.setStrafe(0.0F);
         this.timerStop.reset();
      }
   };
   private Timer shootTimer = new Timer();
   private Timer chargeTimer = new Timer();
   private ItemSlot previousSlot = null;
   private boolean isCharging = false;
   private boolean needsSlotSwapBack = false;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {};

   @Override
   public void tick() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (this.warnArmor.isEnabled()) {
            float armorPoint = 1.0F;

            for (ItemStack stack : mc.player.getAllArmorItems()) {
               if (!stack.isEmpty()) {
                  float maxDamage = stack.getMaxDamage();
                  float currentDamage = maxDamage - stack.getDamage();
                  armorPoint = currentDamage / maxDamage;
               }
            }

            if (armorPoint < 0.36) {
               if (this.visible) {
                  Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "Поломка", "Ваша броня на грани поломки");
                  this.visible = false;
               }
            } else {
               this.visible = true;
            }
         }

         if (this.fly.isEnabled() && mc.player.getAbilities().flying) {
            if (!mc.player.isSneaking() && mc.options.jumpKey.isPressed()) {
               mc.player.setVelocity(mc.player.getVelocity().x, this.flySpeedY.getCurrentValue(), mc.player.getVelocity().z);
            } else if (mc.options.sneakKey.isPressed()) {
               mc.player.setVelocity(mc.player.getVelocity().x, -this.flySpeedY.getCurrentValue(), mc.player.getVelocity().z);
            }

            EntityUtility.setSpeed(this.flySpeedXZ.getCurrentValue());
         }

         if (this.autoFix.isEnabled() && !ServerUtility.hasCT) {
            PlayerInventory inventory = mc.player.getInventory();

            for (int i = 0; i < inventory.size(); i++) {
               ItemStack stackx = inventory.getStack(i);
               if (!stackx.isEmpty() && stackx.isDamageable()) {
                  float maxDamage = stackx.getMaxDamage();
                  float currentDamage = maxDamage - stackx.getDamage();
                  if (currentDamage / maxDamage > 0.5F && mc.player.age % 25 == 0) {
                     mc.player.networkHandler.sendChatCommand("fix all");
                     break;
                  }
               }
            }
         }

         if (this.spoof.isEnabled() && this.spoof.isVisible() && mc.player.age > 20 && mc.currentScreen instanceof ConfirmServerResourcePackScreen) {
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), Status.ACCEPTED));
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), Status.SUCCESSFULLY_LOADED));
            mc.player.closeScreen();
         }

         if (this.autoPiona.isEnabled()) {
            if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler && mc.currentScreen.getTitle().getString().contains("Вам подарок")) {
               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, mc.player);
            }

            if (this.timer.finished(1000L) && !this.pionaCommandSent) {
               this.pionaCommandSent = true;
               mc.player.networkHandler.sendChatCommand("piona");
            }
         }

         if (this.autoZako.isEnabled() && this.timer.finished(500L) && this.zakoCommandSent) {
            this.zakoCommandSent = false;
            mc.player.networkHandler.sendChatCommand("zako");
         }

         super.tick();
      }
   }

   private void handleButtonPress(int button) {
      if (this.dezorentKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.ENDER_EYE);
      } else if (this.trapkaKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.NETHERITE_SCRAP);
      } else if (this.smerchKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.FIRE_CHARGE);
      } else if (this.stanKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.NETHER_STAR);
      } else if (this.plastKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.DRIED_KELP);
      } else if (this.auraKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.PHANTOM_MEMBRANE);
      } else if (this.pilbKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.SUGAR);
      } else if (this.snowKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.SNOWBALL);
      } else if (this.bombKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.FIRE_CHARGE);
      } else if (this.hwTrapKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.POPPED_CHORUS_FRUIT);
      } else if (this.boomTrapKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.PRISMARINE_SHARD);
      } else if (this.goolKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_STAR);
      } else if (this.backpackKey.isKey(button)) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.MAGENTA_SHULKER_BOX);
      }
   }

   private void attemptShoot() {
      SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory());
      ItemSlot crossbowSlot = group.findItem(Items.CROSSBOW);
      if (crossbowSlot != null) {
         if (this.previousSlot == null) {
            this.previousSlot = InventoryUtility.getCurrentHotbarSlot();
         }

         if (crossbowSlot instanceof HotbarSlot hotbarSlot && InventoryUtility.getCurrentHotbarSlot().item() != Items.CROSSBOW) {
            InventoryUtility.selectHotbarSlot(hotbarSlot);
         }

         ItemStack crossbowStack = InventoryUtility.getCurrentHotbarSlot().itemStack();
         if (!this.isCrossbowCharged(crossbowStack)) {
            this.startCharging();
         } else {
            this.shoot();
            this.startCharging();
         }
      }
   }

   private boolean isCrossbowCharged(ItemStack crossbow) {
      if (!crossbow.isEmpty() && crossbow.getItem() == Items.CROSSBOW) {
         NbtCompound nbt = ItemUtility.getNBT(crossbow);
         return nbt != null && nbt.getBoolean("Charged");
      } else {
         return false;
      }
   }

   private void startCharging() {
      if (!this.isCharging) {
         this.isCharging = true;
         this.chargeTimer.reset();
         MinecraftClient.getInstance().options.useKey.setPressed(true);
      }
   }

   private void finishCharging() {
      if (this.isCharging) {
         this.isCharging = false;
         MinecraftClient.getInstance().options.useKey.setPressed(false);
         this.needsSlotSwapBack = true;
      }
   }

   private void shoot() {
      if (mc.interactionManager != null) {
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      }
   }

   public void stop() {
      if (this.isCharging) {
         MinecraftClient.getInstance().options.useKey.setPressed(false);
         this.isCharging = false;
      }

      if (this.previousSlot != null && this.previousSlot instanceof HotbarSlot hotbarSlot) {
         InventoryUtility.selectHotbarSlot(hotbarSlot);
         this.previousSlot = null;
      }

      this.needsSlotSwapBack = false;
   }
}
