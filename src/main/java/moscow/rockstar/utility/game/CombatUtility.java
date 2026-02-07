package moscow.rockstar.utility.game;

import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.modules.modules.combat.Criticals;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class CombatUtility implements IMinecraft {
   public static HotbarSlot getMace() {
      SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
      boolean useWindBurst = mc.player.fallDistance > 2.0F;
      RegistryKey<Enchantment> targetEnchantment = useWindBurst ? Enchantments.WIND_BURST : Enchantments.BREACH;
      HotbarSlot slot = slotsToSearch.findItem(
         (Predicate<ItemStack>)(stack -> !(stack.getItem() instanceof MaceItem) ? false : EnchantmentUtility.getEnchantmentLevel(stack, targetEnchantment) > 0)
      );
      if (slot == null) {
         slot = slotsToSearch.findItem(Items.MACE);
      }

      return slot;
   }

   public static float getFallDistance(LivingEntity target) {
      if (mc.player.getMainHandStack().getItem() instanceof MaceItem) {
      }

      SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
      HotbarSlot slot = slotsToSearch.findItem(Items.MACE);
      if (slot != null) {
         return 0.7F;
      } else {
         return !ServerUtility.isFS() && !ServerUtility.isST()
            ? 0.0F
            : (
               Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 10 == 0
                  ? 0.4F
                  : (Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0 ? 0.2F : 0.0F)
            );
      }
   }

   public static boolean canPerformCriticalHit(LivingEntity target, boolean ignoreSprint) {
      if (mc.world != null && mc.player != null) {
         Block blockAboveHead = mc.world.getBlockState(mc.player.getBlockPos().up(2)).getBlock();
         Aura aura = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
         return mc.player.isClimbing()
            || EntityUtility.getBlock(0.0, 2.0, 0.0) != Blocks.AIR
               && (
                  Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 10 == 0
                     || Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0
               )
               && (ServerUtility.isST() || ServerUtility.isFS())
            || mc.currentScreen instanceof InventoryScreen
            || mc.player.isTouchingWater() && EntityUtility.getBlock(0.0, 1.0, 0.0) == Blocks.WATER && mc.player.fallDistance <= 0.0F
            || mc.player.isSwimming()
            || mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.COBWEB)
            || mc.player.isInLava()
            || aura.getFastPvp().isSelected()
            || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
            || mc.player.hasVehicle()
            || (mc.player.getMainHandStack().getItem() instanceof MaceItem ? mc.player.fallDistance > 1.0F : mc.player.fallDistance > getFallDistance(target))
            || ServerUtility.isST()
               && Rockstar.getInstance().getModuleManager().getModule(Aura.class).getAttacks() % 5 == 0
               && EntityUtility.getBlock(0.0, 2.0, 0.0) != Blocks.AIR
            || Rockstar.getInstance().getModuleManager().getModule(Criticals.class).canCritical();
      } else {
         return false;
      }
   }

   public static boolean canBreakShield(LivingEntity target) {
      if (mc.player == null || mc.player.isDead()) {
         return false;
      } else if (target.isDead()) {
         return false;
      } else {
         HotbarSlot axeSlot = SlotGroups.hotbar().findItem((Predicate<ItemStack>)(itemStack -> itemStack.getItem() instanceof AxeItem));
         if (axeSlot == null) {
            return false;
         } else {
            Vec3d facingVector = target.getRotationVector();
            Vec3d deltaPos = new Vec3d(target.getPos().getX() - mc.player.getPos().getX(), 0.0, target.getPos().getZ() - mc.player.getPos().getZ());
            return deltaPos.dotProduct(facingVector) < 0.0;
         }
      }
   }

   public static boolean shouldBreakShield(LivingEntity target) {
      return target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem;
   }

   public static void tryBreakShield(LivingEntity target) {
      if (mc.player != null && mc.interactionManager != null) {
         SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
         HotbarSlot slot = slotsToSearch.findItem((Predicate<ItemStack>)(item -> item.getItem() instanceof AxeItem));
         if (slot != null && target instanceof PlayerEntity && target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot.getSlotId()));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
         }
      }
   }

   public static boolean stalin(LivingEntity target) {
      Vec3d pos = target.getPos();
      Box hitbox = target.getBoundingBox();
      float off = 0.05F;
      return !isAir(hitbox.minX - off, pos.y, hitbox.minZ - off)
         || !isAir(hitbox.maxX + off, pos.y, hitbox.minZ - off)
         || !isAir(hitbox.minX - off, pos.y, hitbox.maxZ + off)
         || !isAir(hitbox.maxX + off, pos.y, hitbox.maxZ + off);
   }

   private static boolean isAir(double x, double y, double z) {
      return mc.world.getBlockState(new BlockPos((int)x, (int)y, (int)z)).getBlock() == Blocks.AIR;
   }

   @Generated
   private CombatUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
