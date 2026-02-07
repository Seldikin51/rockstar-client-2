package moscow.rockstar.systems.modules.modules.combat;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.player.GuiMove;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.rotations.RotationPriority;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

@ModuleInfo(name = "Auto Potion", category = ModuleCategory.COMBAT, desc = "Автоматически бросает зелья")
public class AutoPotion extends BaseModule {
   private final SelectSetting potions = new SelectSetting(this, "modules.settings.auto_potion.potions");
   private final SelectSetting.Value health;
   private final SelectSetting.Value cerka;
   private SliderSetting healthHealth = null;
   private SliderSetting cerkaHealth = null;
   private final SelectSetting allow = new SelectSetting(this, "modules.settings.auto_potion.allow");
   private final SelectSetting.Value up = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.throw_up").select();
   private final SelectSetting.Value walls = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.throw_walls").select();
   private final SelectSetting.Value don = new SelectSetting.Value(this.allow, "modules.settings.auto_potion.allow.donate");
   private final Timer staying = new Timer();
   private final Timer ground = new Timer();
   private final Timer wall = new Timer();
   private final Timer roof = new Timer();
   private AutoPotion.UseTask task;
   private final EventListener<InputEvent> onInput = event -> {
      if (this.task != null) {
         GuiMove guiMove = Rockstar.getInstance().getModuleManager().getModule(GuiMove.class);
         boolean hotbar = this.task.slot instanceof HotbarSlot;
         if (guiMove.isEnabled() && guiMove.slowing() && !hotbar) {
            event.setForward(0.0F);
            event.setStrafe(0.0F);
         }
      }
   };
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      Rotation rotation = this.getRotation();
      GuiMove guiMove = Rockstar.getInstance().getModuleManager().getModule(GuiMove.class);
      if (this.task != null) {
         Rockstar.getInstance().getRotationHandler().rotate(rotation, RotationPriority.USE_ITEM);
         boolean hotbar = this.task.slot instanceof HotbarSlot;
         int offset = guiMove.isEnabled() && guiMove.slowing() && !hotbar ? 1 : 0;
         if (hotbar) {
            switch (this.task.stage) {
               case 0:
                  InventoryUtility.selectHotbarSlot(this.task.slot.getIdForServer() - 36);
                  break;
               case 1:
                  this.usePotion();
                  break;
               case 2:
                  InventoryUtility.selectHotbarSlot(this.task.prevSlot);
            }
         } else {
            switch (this.task.stage - offset) {
               case 0:
                  InventoryUtility.hotbarSwap(this.task.slot.getIdForServer(), this.task.prevSlot);
                  InventoryUtility.selectHotbarSlot(this.task.prevSlot);
                  break;
               case 1:
                  this.usePotion();
                  break;
               case 2:
                  InventoryUtility.hotbarSwap(this.task.slot.getIdForServer(), this.task.prevSlot);
            }
         }

         if (this.task.stage < 2 + offset) {
            this.task.stage++;
            return;
         }

         this.task = null;
      }

      if (rotation.getYaw() != -1.0F && rotation.getPitch() != -1.0F) {
         for (SelectSetting.Value selectedValue : this.potions.getSelectedValues()) {
            AutoPotion.PotionValue potionValue = (AutoPotion.PotionValue)selectedValue;
            if (!mc.player.hasStatusEffect(potionValue.effect) && potionValue.throwTimer.finished(2000L) && potionValue.canThrow()) {
               SlotGroup<ItemSlot> search = SlotGroups.inventory().and(SlotGroups.hotbar());
               ItemSlot slot = search.findItem(this.potionPredicate(potionValue.effect));
               if (slot != null) {
                  this.task = new AutoPotion.UseTask(slot, InventoryUtility.getCurrentHotbarSlot().getSlotId());
                  Rockstar.getInstance().getRotationHandler().rotate(rotation, MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.USE_ITEM);
                  potionValue.throwTimer.reset();
                  break;
               }
            }
         }
      }
   };

   public AutoPotion() {
      new AutoPotion.PotionValue(this.potions, "modules.settings.auto_potion.potions.strength", StatusEffects.STRENGTH).select();
      new AutoPotion.PotionValue(this.potions, "modules.settings.auto_potion.potions.speed", StatusEffects.SPEED).select();
      new AutoPotion.PotionValue(this.potions, "modules.settings.auto_potion.potions.fire_resistance", StatusEffects.FIRE_RESISTANCE).select();
      this.health = new AutoPotion.PotionValue(
            this.potions,
            "modules.settings.auto_potion.potions.heal",
            StatusEffects.INSTANT_HEALTH,
            () -> mc.player.getHealth() <= this.healthHealth.getCurrentValue()
         )
         .select();
      this.cerka = new AutoPotion.PotionValue(
            this.potions,
            "modules.settings.auto_potion.potions.heal.cerka",
            StatusEffects.WEAKNESS,
            () -> {
               SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
               ItemSlot health = slotsToSearch.findItem(
                  (Predicate<ItemStack>)(stack -> PotionUtility.hasEffect(stack, StatusEffects.INSTANT_HEALTH)
                     && !mc.player.getItemCooldownManager().isCoolingDown(stack))
               );
               ItemSlot golden = slotsToSearch.findItem(
                  (Predicate<ItemStack>)(stack -> stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE && !mc.player.getItemCooldownManager().isCoolingDown(stack))
               );
               boolean near = false;

               for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                  if (mc.player.distanceTo(player) < 5.0F && player != mc.player) {
                     near = true;
                  }
               }

               return mc.player.getHealth() + mc.player.getAbsorptionAmount() <= this.cerkaHealth.getCurrentValue() && golden == null && health == null && near;
            }
         )
         .select();
      this.healthHealth = new SliderSetting(this, "modules.settings.auto_potion.potions.heal_health", () -> !this.health.isSelected())
         .min(1.0F)
         .max(19.0F)
         .step(0.5F)
         .currentValue(6.0F);
      this.cerkaHealth = new SliderSetting(this, "modules.settings.auto_potion.potions.cerka_health", () -> !this.cerka.isSelected())
         .min(1.0F)
         .max(19.0F)
         .step(0.5F)
         .currentValue(6.0F);
   }

   private void usePotion() {
      Rotation rotation = this.getRotation();
      mc.interactionManager
         .sendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
   }

   private Rotation getRotation() {
      boolean canThrow = false;
      float yaw = mc.player.getYaw();
      float pitch = 90.0F;
      if (this.cerka.isSelected() && ((AutoPotion.PotionValue)this.cerka).canThrow()) {
         for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (mc.player.distanceTo(player) < 5.0F && player != mc.player) {
               Vec3d eyes = mc.player.getCameraPosVec(1.0F);
               double dx = player.getPos().x - eyes.x;
               double dy = player.getPos().y - eyes.y;
               double dz = player.getPos().z - eyes.z;
               double dist = Math.sqrt(dx * dx + dz * dz);
               return new Rotation((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0), (float)(-Math.toDegrees(Math.atan2(dy, dist))));
            }
         }
      }

      if (this.up.isSelected()) {
         if (mc.player.getVelocity().x == 0.0
            && mc.player.getVelocity().z == 0.0
            && Math.abs(mc.player.getVelocity().y) < 0.1F
            && EntityUtility.getBlockAbove(mc.player) == Blocks.AIR) {
            if (this.staying.finished(500L)) {
               pitch = -90.0F;
               return new Rotation(yaw, pitch);
            }
         } else {
            this.staying.reset();
         }
      }

      if (mc.player.isOnGround()) {
         if (this.ground.finished(300L)) {
            BlockHitResult groundResult = mc.world
               .raycast(
                  new RaycastContext(
                     mc.player.getEyePos(),
                     mc.player.getEyePos().add(mc.player.getRotationVector(pitch, yaw).multiply(2.0)),
                     ShapeType.COLLIDER,
                     FluidHandling.NONE,
                     mc.player
                  )
               );
            if (groundResult.getType() == Type.BLOCK) {
               canThrow = true;
            } else {
               pitch = 76.0F;

               for (int i = 0; i < 360; i += 45) {
                  BlockHitResult result = mc.world
                     .raycast(
                        new RaycastContext(
                           mc.player.getEyePos(),
                           mc.player.getEyePos().add(mc.player.getRotationVector(pitch, i).multiply(2.0)),
                           ShapeType.COLLIDER,
                           FluidHandling.NONE,
                           mc.player
                        )
                     );
                  if (result.getType() == Type.BLOCK) {
                     yaw = RotationMath.adjustAngle(mc.player.getYaw(), i);
                     canThrow = true;
                  }
               }
            }
         }
      } else {
         this.ground.reset();
         if (this.walls.isSelected()) {
            boolean wallThrow = false;
            pitch = 5.0F;

            for (int ix = 0; ix < 360; ix += 45) {
               BlockHitResult result = mc.world
                  .raycast(
                     new RaycastContext(
                        mc.player.getEyePos(),
                        mc.player.getEyePos().add(mc.player.getRotationVector(pitch, ix).multiply(0.5)),
                        ShapeType.COLLIDER,
                        FluidHandling.NONE,
                        mc.player
                     )
                  );
               if (result.getType() == Type.BLOCK) {
                  yaw = RotationMath.adjustAngle(mc.player.getYaw(), ix);
                  wallThrow = true;
               }
            }

            if (!wallThrow) {
               this.wall.reset();
            } else if (this.wall.finished(300L)) {
               canThrow = true;
            }

            if (!canThrow) {
               boolean roofThrow = false;
               pitch = -90.0F;
               BlockHitResult result = mc.world
                  .raycast(
                     new RaycastContext(
                        mc.player.getEyePos(),
                        mc.player.getEyePos().add(mc.player.getRotationVector(pitch, mc.player.getYaw()).multiply(0.5)),
                        ShapeType.COLLIDER,
                        FluidHandling.NONE,
                        mc.player
                     )
                  );
               if (result.getType() == Type.BLOCK) {
                  yaw = mc.player.getYaw();
                  roofThrow = true;
               }

               if (!roofThrow) {
                  this.roof.reset();
               } else if (this.roof.finished(300L)) {
                  canThrow = true;
               }
            }
         }
      }

      return canThrow ? new Rotation(yaw, pitch) : new Rotation(-1.0F, -1.0F);
   }

   private Predicate<ItemStack> potionPredicate(RegistryEntry<StatusEffect> type) {
      return stack -> {
         if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
            List<StatusEffectInstance> effects = PotionUtility.effects(stack);
            return !this.don.isSelected() && type != StatusEffects.WEAKNESS
               ? effects.size() == 1 && effects.getFirst().getEffectType() == type
               : effects.stream().anyMatch(effect -> effect.getEffectType() == type);
         } else {
            return false;
         }
      };
   }

   static class PotionValue extends SelectSetting.Value {
      final RegistryEntry<StatusEffect> effect;
      final Timer throwTimer = new Timer();
      Supplier<Boolean> canThrow = () -> true;

      public PotionValue(SelectSetting parent, String name, RegistryEntry<StatusEffect> effect) {
         super(parent, name);
         this.effect = effect;
      }

      public PotionValue(SelectSetting parent, String name, RegistryEntry<StatusEffect> effect, Supplier<Boolean> canThrow) {
         super(parent, name);
         this.effect = effect;
         this.canThrow = canThrow;
      }

      public boolean canThrow() {
         return this.canThrow.get();
      }
   }

   static class UseTask {
      int stage;
      final ItemSlot slot;
      final int prevSlot;

      @Generated
      public UseTask(ItemSlot slot, int prevSlot) {
         this.slot = slot;
         this.prevSlot = prevSlot;
      }
   }
}
