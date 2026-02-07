package moscow.rockstar.systems.modules.modules.player;

import java.util.function.Predicate;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.FireworkEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.ElytraTarget;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.mixins.ArmorItemAddition;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Elytra Utils", category = ModuleCategory.PLAYER, desc = "Помощник с элитрами")
public class ElytraUtils extends BaseModule {
   private final BindSetting swapKey = new BindSetting(this, "Клавиша свапа");
   private final BindSetting fireworkKey = new BindSetting(this, "Клавиша фейерверка");
   private final BooleanSetting automat = new BooleanSetting(this, "Авто взлёт", "Автоматически взлетает на элитрах").enable();
   private final BooleanSetting withUse = new BooleanSetting(
         this, "Авто использование", "Автоматически использует фейерверк для взлета", () -> !this.automat.isEnabled()
      )
      .enable();
   private final BooleanSetting unEquip = new BooleanSetting(this, "Грудак на земле", "Автоматически надевает нагрудник или снимает элитры при приземлении")
      .enable();
   private final BooleanSetting boost = new BooleanSetting(this, "Ускорять", "Ускоряет движение на элитре");
   private boolean wasFlying;
   private ElytraUtils.SwapTask swapTask;
   private final EventListener<ClientPlayerTickEvent> onUpdate = event -> {
      if (mc.player.isGliding()) {
         this.wasFlying = true;
      }

      ItemSlot chestplateSlot = InventoryUtility.getChestplateSlot();
      SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory()).and(SlotGroups.offhand());
      ItemSlot chestplateItemSlot = group.findItem(
         (Predicate<ItemStack>)(itemStack -> itemStack.getItem() instanceof ArmorItem armorItem
            && ((ArmorItemAddition)armorItem).rockstar$getType() == EquipmentType.CHESTPLATE)
      );
      ItemSlot slot = group.findItem(Items.FIREWORK_ROCKET);
      boolean isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
      if (this.swapTask != null) {
         switch (this.swapTask.stage) {
            case 0:
            case 2:
               InventoryUtility.hotbarSwap(this.swapTask.from.getIdForServer(), 40);
               break;
            case 1:
               InventoryUtility.hotbarSwap(this.swapTask.chest.getIdForServer(), 40);
         }

         if (this.swapTask.stage++ >= 2) {
            this.swapTask = null;
         }
      }

      if (this.automat.isEnabled() && isElytraEquipped && !mc.player.isGliding() && !mc.player.isOnGround() && !mc.player.isInFluid()) {
         mc.player.startGliding();
         mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
         if (this.withUse.isEnabled() && slot != null) {
            EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
         }
      } else if (mc.player.isOnGround() && this.automat.isEnabled() && isElytraEquipped && !mc.player.isInFluid()) {
         mc.player.jump();
      }

      if (this.unEquip.isEnabled() && mc.player.isOnGround() && isElytraEquipped && this.wasFlying && mc.player.getGlidingTicks() > 18) {
         if (chestplateItemSlot != null) {
            chestplateItemSlot.swapTo(chestplateSlot);
         } else {
            mc.interactionManager.clickSlot(0, 6, 0, SlotActionType.QUICK_MOVE, mc.player);
         }

         this.wasFlying = false;
      }
   };
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (this.swapKey.isKey(event.getKey()) && event.getAction() == 1 && mc.currentScreen == null) {
         this.swapElytraChestplate();
      }

      if (this.fireworkKey.isKey(event.getKey()) && event.getAction() == 1 && mc.currentScreen == null) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
      }
   };
   private final EventListener<MouseEvent> onMouseButtonPress = event -> {
      if (this.swapKey.isKey(event.getButton()) && event.getAction() == 1 && mc.currentScreen == null) {
         this.swapElytraChestplate();
      }

      if (this.fireworkKey.isKey(event.getButton()) && event.getAction() == 1 && mc.currentScreen == null) {
         EventIntegration.SWAP_INTEGRATION.useItem(Items.FIREWORK_ROCKET);
      }
   };
   private final EventListener<FireworkEvent> onFirework = event -> {
      if (this.boost.isEnabled() && event.getEntity() == mc.player) {
         if (Rockstar.getInstance().getTargetManager().getLivingTarget() instanceof PlayerEntity player && !ElytraPredictionSystem.isLeaving(player)) {
         }

         double boostPower = 1.5 * this.getAdvancedBoost();
         RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
         Vec3d rotationVector = rotationHandler.isIdling()
            ? rotationHandler.getPlayerRotation().getRotationVector()
            : rotationHandler.getCurrentRotation().getRotationVector();
         Vec3d currentVelocity = event.getVelocity();
         Vec3d newVelocity = currentVelocity.add(
            rotationVector.x * 0.1 + (rotationVector.x * boostPower - currentVelocity.x) * 0.5,
            rotationVector.y * 0.1 + (rotationVector.y * boostPower - currentVelocity.y) * 0.5,
            rotationVector.z * 0.1 + (rotationVector.z * boostPower - currentVelocity.z) * 0.5
         );
         event.setVelocity(newVelocity);
      }
   };
   private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
      if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
         Rotation rot = rotationHandler.isIdling() ? rotationHandler.getPlayerRotation() : rotationHandler.getCurrentRotation();
         System.out.println(String.format("ELYTRA BOOSTER HUETA. ANGLES: yaw(%s) pitch(%s) speed(%s)", rot.getYaw(), rot.getPitch(), this.getAdvancedBoost()));
      }
   };

   private void swapElytraChestplate() {
      ItemSlot chestplateSlot = InventoryUtility.getChestplateSlot();
      SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
      ItemSlot elytraItemSlot = slotsToSearch.findItem(
         (Predicate<ItemStack>)(itemStack -> itemStack.getItem() == Items.ELYTRA && !itemStack.willBreakNextUse())
      );
      ItemSlot chestplateItemSlot = slotsToSearch.findItem(
         (Predicate<ItemStack>)(itemStack -> itemStack.getItem() instanceof ArmorItem armorItem
            && ((ArmorItemAddition)armorItem).rockstar$getType() == EquipmentType.CHESTPLATE)
      );
      boolean isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
      if (!isElytraEquipped && elytraItemSlot != null) {
         this.swapTask = new ElytraUtils.SwapTask(elytraItemSlot, chestplateSlot);
      } else if (chestplateItemSlot != null) {
         this.swapTask = new ElytraUtils.SwapTask(chestplateItemSlot, chestplateSlot);
      }
   }

   private double getAdvancedBoost() {
      if (Rockstar.getInstance().getModuleManager().getModule(ElytraTarget.class).isDefensiveActive()) {
      }

      RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
      Rotation rot = rotationHandler.isIdling() ? rotationHandler.getPlayerRotation() : rotationHandler.getCurrentRotation();
      float playerYaw = rot.getYaw();
      float playerPitch = rot.getPitch();
      double A = 0.239037;
      double B = 4.489648;
      double C = 1.236087;
      double MAX_ACCELERATION_YAW = 1.47;
      double YAW_TOLERANCE = 7.9;
      double MAX_PITCH_BOOST = 1.01;
      double MAX_PITCH = -45.0;
      double MIN_PITCH = 10.0;
      double effectiveYaw = Math.abs(playerYaw) % 90.0;
      double yawAcceleration;
      if (Math.abs(effectiveYaw - 45.0) <= 7.9) {
         yawAcceleration = 1.47;
      } else {
         double argument = 4.489648 * (effectiveYaw - 45.0);
         yawAcceleration = 0.239037 * Math.cos(Math.toRadians(argument)) + 1.236087;
      }

      if (playerPitch >= 10.0F) {
         return Math.abs(effectiveYaw - 45.0) <= 5.0 ? 1.8 : yawAcceleration;
      } else if (playerPitch >= 0.0F) {
         return 1.0;
      } else if (playerPitch < -80.0F) {
         return 1.0;
      } else {
         double pitchRatio = Math.min(1.0, Math.abs(playerPitch) / Math.abs(-45.0));
         double pitchMultiplier = 1.0 + 0.010000000000000009 * pitchRatio;
         double totalAcceleration = yawAcceleration * pitchMultiplier;
         return Math.min(totalAcceleration, 1.49);
      }
   }

   private static int findClosestVector(float lastYaw, int[] vectors) {
      int index = 0;
      int minDistIndex = -1;
      float minDist = Float.MAX_VALUE;

      for (int vector : vectors) {
         float dist = Math.abs(MathHelper.wrapDegrees(lastYaw) - vector);
         if (dist < minDist) {
            minDist = dist;
            minDistIndex = index;
         }

         index++;
      }

      return minDistIndex;
   }

   private double calculateDynamicBoostPower(LivingEntity player) {
      float yaw = player.getYaw();
      float pitch = player.getPitch();
      double minSpeed = 1.4;
      double maxSpeed = 1.9;
      double yawFactor = this.calculateYawFactor(yaw);
      double pitchFactor = this.calculatePitchFactor(pitch);
      double combinedFactor = yawFactor * pitchFactor;
      double boostPower = minSpeed + (maxSpeed - minSpeed) * combinedFactor;
      return Math.max(minSpeed, Math.min(maxSpeed, boostPower));
   }

   private double calculateYawFactor(float yaw) {
      yaw = (yaw % 360.0F + 360.0F) % 360.0F;
      double[] diagonalAngles = new double[]{45.0, 135.0, 225.0, 315.0};
      double minDistanceToDiagonal = Double.MAX_VALUE;

      for (double diagonal : diagonalAngles) {
         double distance = Math.min(Math.abs(yaw - diagonal), Math.min(Math.abs(yaw - diagonal + 360.0), Math.abs(yaw - diagonal - 360.0)));
         minDistanceToDiagonal = Math.min(minDistanceToDiagonal, distance);
      }

      return minDistanceToDiagonal <= 45.0 ? 1.0 - minDistanceToDiagonal / 45.0 * 0.85 : 0.15;
   }

   private double calculatePitchFactor(float pitch) {
      float absPitch = Math.abs(pitch);
      if (absPitch <= 10.0F) {
         return 1.0;
      } else if (absPitch <= 30.0F) {
         return 1.0 - (absPitch - 10.0F) / 20.0 * 0.3;
      } else {
         return absPitch <= 60.0F ? 0.7 - (absPitch - 30.0F) / 30.0 * 0.4 : 0.3;
      }
   }

   @Override
   public void onDisable() {
      this.wasFlying = false;
   }

   @Override
   public void onEnable() {
      this.wasFlying = false;
   }

   private static class SwapTask {
      int stage;
      final ItemSlot from;
      final ItemSlot chest;

      SwapTask(ItemSlot from, ItemSlot chest) {
         this.from = from;
         this.chest = chest;
      }
   }
}
