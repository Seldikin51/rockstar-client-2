package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrappedChestBlock;

@ModuleInfo(name = "No Interact", category = ModuleCategory.PLAYER)
public class NoInteract extends BaseModule {
   private final SelectSetting blocks = new SelectSetting(this, "modules.settings.no_interact.blocks");
   private final SelectSetting.Value craftingTables = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.craftingTables").select();
   private final SelectSetting.Value enchantingTables = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.enchantingTables").select();
   private final SelectSetting.Value beds = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.beds").select();
   private final SelectSetting.Value chests = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.chests").select();
   private final SelectSetting.Value enderChests = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.enderChests").select();
   private final SelectSetting.Value trappedChests = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.trappedChests").select();
   private final SelectSetting.Value furnaces = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.furnaces").select();
   private final SelectSetting.Value barrels = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.barrels").select();
   private final SelectSetting.Value shulkers = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.shulkers").select();
   private final SelectSetting.Value droppers = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.droppers").select();
   private final SelectSetting.Value dispensers = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.dispensers").select();
   private final SelectSetting.Value hoppers = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.hoppers").select();
   private final SelectSetting.Value anvils = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.anvils").select();
   private final SelectSetting.Value cauldrons = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.cauldrons").select();
   private final SelectSetting.Value signs = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.signs").select();
   private final SelectSetting.Value bells = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.bells").select();
   private final SelectSetting.Value composters = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.composters").select();
   private final SelectSetting.Value brewingStands = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.brewingStands").select();
   private final SelectSetting.Value jukeBoxes = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.jukeBoxes").select();
   private final SelectSetting.Value commandBlocks = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.commandBlocks").select();
   private final SelectSetting.Value beacons = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.beacons").select();
   private final SelectSetting.Value respawnAnchors = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.respawnAnchors").select();
   private final SelectSetting.Value grindstones = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.grindstones").select();
   private final SelectSetting.Value lecterns = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.lecterns").select();
   private final SelectSetting.Value cartographyTables = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.cartographyTables").select();
   private final SelectSetting.Value looms = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.looms").select();
   private final SelectSetting.Value smithingTables = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.smithingTables").select();
   private final SelectSetting.Value stonecutters = new SelectSetting.Value(this.blocks, "modules.settings.no_interact.stonecutter").select();
   private final BooleanSetting onlyAura = new BooleanSetting(this, "modules.settings.no_interact.onlyAura");

   public boolean shouldPreventInteract(Block block) {
      if (this.onlyAura.isEnabled()) {
         return Rockstar.getInstance().getModuleManager().getModule(Aura.class).isEnabled();
      } else if (block instanceof CraftingTableBlock && this.craftingTables.isSelected()) {
         return true;
      } else if (block instanceof EnchantingTableBlock && this.enchantingTables.isSelected()) {
         return true;
      } else if (block instanceof BedBlock && this.beds.isSelected()) {
         return true;
      } else if (block instanceof ChestBlock && this.chests.isSelected()) {
         return true;
      } else if (block instanceof EnderChestBlock && this.enderChests.isSelected()) {
         return true;
      } else if (block instanceof TrappedChestBlock && this.trappedChests.isSelected()) {
         return true;
      } else if (block instanceof AbstractFurnaceBlock && this.furnaces.isSelected()) {
         return true;
      } else if (block instanceof BarrelBlock && this.barrels.isSelected()) {
         return true;
      } else if (block instanceof ShulkerBoxBlock && this.shulkers.isSelected()) {
         return true;
      } else if (block instanceof DropperBlock && this.droppers.isSelected()) {
         return true;
      } else if (block instanceof DispenserBlock && this.dispensers.isSelected()) {
         return true;
      } else if (block instanceof HopperBlock && this.hoppers.isSelected()) {
         return true;
      } else if (block instanceof AnvilBlock && this.anvils.isSelected()) {
         return true;
      } else if (block instanceof AbstractCauldronBlock && this.cauldrons.isSelected()) {
         return true;
      } else if (block instanceof AbstractSignBlock && this.signs.isSelected()) {
         return true;
      } else if (block instanceof BellBlock && this.bells.isSelected()) {
         return true;
      } else if (block instanceof ComposterBlock && this.composters.isSelected()) {
         return true;
      } else if (block instanceof BrewingStandBlock && this.brewingStands.isSelected()) {
         return true;
      } else if (block instanceof JukeboxBlock && this.jukeBoxes.isSelected()) {
         return true;
      } else if (block instanceof CommandBlock && this.commandBlocks.isSelected()) {
         return true;
      } else if (block instanceof BeaconBlock && this.beacons.isSelected()) {
         return true;
      } else if (block instanceof RespawnAnchorBlock && this.respawnAnchors.isSelected()) {
         return true;
      } else if (block instanceof GrindstoneBlock && this.grindstones.isSelected()) {
         return true;
      } else if (block instanceof LecternBlock && this.lecterns.isSelected()) {
         return true;
      } else if (block instanceof CartographyTableBlock && this.cartographyTables.isSelected()) {
         return true;
      } else if (block instanceof LoomBlock && this.looms.isSelected()) {
         return true;
      } else {
         return block instanceof SmithingTableBlock && this.smithingTables.isSelected()
            ? true
            : block instanceof StonecutterBlock && this.stonecutters.isSelected();
      }
   }
}
