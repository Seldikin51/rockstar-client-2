package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.time.Timer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

@ModuleInfo(name = "Auto Bot", category = ModuleCategory.PLAYER)
public class AutoBot extends BaseModule {
   private final Timer scanTimer = new Timer();
   private AutoBot.Action action = AutoBot.Action.IDLE;

   @Override
   public void onEnable() {
      if (!FabricLoader.getInstance().isModLoaded("baritone")) {
         MessageUtility.info(Text.of("Для работы " + this.getName() + " нужен мод baritone"));
         this.toggle();
      } else {
         this.action = AutoBot.Action.IDLE;
         this.msg("#allowBreak true");
      }
   }

   @Override
   public void onDisable() {
      this.msg("#stop");
      this.msg("#sel clear");
   }

   @Override
   public void tick() {
      if (this.scanTimer.finished(200L)) {
         BlockPos player = mc.player.getBlockPos();
         int radius = 10;

         for (int x = 0; x < radius * 2; x++) {
            for (int z = 0; z < radius * 2; z++) {
               for (int y = 0; y < radius * 2; y++) {
                  BlockPos offset = new BlockPos((x % 2 == 0 ? -x : x) / 2, (y % 2 == 0 ? -y : y) / 2, (z % 2 == 0 ? -z : z) / 2);
                  BlockPos pos = player.add(offset);
                  if (this.logic(pos)) {
                     return;
                  }
               }
            }
         }

         this.scanTimer.reset();
      }
   }

   private boolean logic(BlockPos obsidian) {
      BlockPos target = obsidian.up();
      if (mc.world.getBlockState(obsidian).getBlock() == Blocks.OBSIDIAN
         && mc.world.getBlockState(obsidian.east()).getBlock() == Blocks.OBSIDIAN
         && mc.world.getBlockState(obsidian.north()).getBlock() == Blocks.OBSIDIAN
         && mc.world.getBlockState(obsidian.south()).getBlock() == Blocks.OBSIDIAN
         && mc.world.getBlockState(obsidian.west()).getBlock() == Blocks.OBSIDIAN
         && mc.world.getBlockState(target.east()).getBlock() == Blocks.AIR
         && mc.world.getBlockState(target.north()).getBlock() == Blocks.AIR
         && mc.world.getBlockState(target.south()).getBlock() == Blocks.AIR
         && mc.world.getBlockState(target.west()).getBlock() == Blocks.AIR) {
         if (target.equals(mc.player.getBlockPos())) {
            if (mc.world.getBlockState(target).getBlock() == Blocks.TORCH) {
               if (this.action != AutoBot.Action.BREAK) {
                  this.select(target);
                  this.msg("#sel cleararea");
                  this.action = AutoBot.Action.BREAK;
                  this.scanTimer.reset();
                  return true;
               }
            } else if (this.action != AutoBot.Action.PLACE) {
               this.select(target);
               this.msg("#sel fill " + Registries.BLOCK.getId(Blocks.TORCH));
               this.action = AutoBot.Action.PLACE;
               this.scanTimer.reset();
               return true;
            }
         } else if (this.action != AutoBot.Action.FOLLOW && this.action != AutoBot.Action.PLACE) {
            this.msg(String.format("#goto %s.5 %s %s.5", target.getX(), target.getY(), target.getZ()));
            this.action = AutoBot.Action.FOLLOW;
            this.scanTimer.reset();
            return true;
         }

         return false;
      } else {
         return false;
      }
   }

   private void select(BlockPos pos) {
      this.msg("#sel clear");
      this.msg(String.format("#sel pos1 %s %s %s", pos.getX(), pos.getY(), pos.getZ()));
      this.msg(String.format("#sel pos2 %s %s %s", pos.getX(), pos.getY(), pos.getZ()));
   }

   private void msg(String msg) {
      mc.player.networkHandler.sendChatMessage(msg);
   }

   static enum Action {
      IDLE,
      FOLLOW,
      PLACE,
      BREAK;
   }
}
