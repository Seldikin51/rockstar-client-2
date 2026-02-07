package moscow.rockstar.utility.game.server;

import java.util.ArrayList;
import java.util.Arrays;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public final class ServerUtility implements IMinecraft {
   public static boolean hasCT;
   public static int ctTime;
   public static int ftAn = -1;

   public static boolean isCM() {
      return is("cherry.pizza");
   }

   public static boolean isST() {
      return is("spooky");
   }

   public static boolean isFT() {
      return is("funtime") || is("playft");
   }

   public static boolean isRW() {
      return is("reallyworld") || is("playrw");
   }

   public static boolean isFS() {
      return is("funsky");
   }

   public static boolean isHW() {
      return is("holy") || is("holly") || is("playhw");
   }

   public static boolean isSunWay() {
      return is("sunw");
   }

   public static boolean isSaturn() {
      return is("saturn");
   }

   public static boolean isSR() {
      return is("sunmc");
   }

   public static boolean isIntave() {
      return is("mineblaze") || is("dexland");
   }

   public static boolean isServerForHPFix() {
      return isFT() || isRW() || isFS();
   }

   public static boolean isPastaFT() {
      return isFT() || isST() || isFS();
   }

   public static String getIP() {
      return mc.player != null && mc.player.networkHandler.getServerInfo() != null ? mc.player.networkHandler.getServerInfo().address : "single";
   }

   public static boolean is(String ip) {
      return getIP().toLowerCase().contains(ip.toLowerCase());
   }

   public static String getServerName(boolean shortName) {
      String ip = getIP();
      String[] parts = ip.split("\\.");
      if (mc.isInSingleplayer()) {
         return applyCase(ip, shortName);
      } else if (parts.length == 3) {
         return applyCase(parts[1], shortName);
      } else if (parts.length == 2) {
         return applyCase(parts[0], shortName);
      } else {
         return ip.contains(":") ? ip.split(":")[0] : ip;
      }
   }

   private static String applyCase(String server, boolean shortName) {
      server = server.replace("-", "");
      ArrayList<ServerUtility.Data> datas = new ArrayList<>();
      String[] suffixes = new String[]{
         "legacy",
         "bars",
         "world",
         "best",
         "times",
         "time",
         "shine",
         "sky",
         "lands",
         "land",
         "trainer",
         "server",
         "blaze",
         "mine",
         "lord",
         "cube",
         "grief",
         "craft",
         "rise",
         "force",
         "project",
         "lite"
      };
      Arrays.stream(suffixes).forEach(suffix -> datas.add(genData(suffix)));
      Arrays.stream(
            new ServerUtility.Data[]{
               new ServerUtility.Data("mc", "MC", "-MC"), new ServerUtility.Data("hvh", "HVH", "-HVH"), new ServerUtility.Data("pvp", "PVP", "PVP")
            }
         )
         .forEach(datas::add);
      if (mc.isInSingleplayer() && !shortName) {
         server = "LocalHost";
      }

      if (isSR()) {
         server = shortName ? "SR" : "SunRise";
      }

      if (isSaturn()) {
         server = shortName ? "S-X" : "SaturnX";
      }

      if (isSunWay()) {
         server = shortName ? "SW" : "SunWay";
      }

      for (ServerUtility.Data data : datas) {
         if (server.contains(data.orig)) {
            if (shortName) {
               server = server.substring(0, 1).toUpperCase() + data.small;
            } else {
               server = server.replace(data.orig, data.big);
               server = server.substring(0, 1).toUpperCase() + server.substring(1);
            }

            return server;
         }
      }

      return server.substring(0, 1).toUpperCase() + server.substring(1);
   }

   public static boolean spawn() {
      if (mc.player != null && mc.world != null) {
         BlockPos pos = mc.player.getBlockPos();
         Block blockBelow = mc.world.getBlockState(pos.down(1)).getBlock();
         Block blockAtZero = mc.world.getBlockState(new BlockPos(pos.getX(), 0, pos.getZ())).getBlock();
         if (isFT() && mc.world.getDifficulty() == Difficulty.NORMAL) {
            return false;
         } else if (!isFT() && !isST()) {
            return mc.world.getRegistryKey() != World.OVERWORLD ? false : blockAtZero == Blocks.BEDROCK || blockBelow == Blocks.BEDROCK;
         } else {
            return blockBelow == Blocks.AIR || blockAtZero == Blocks.AIR;
         }
      } else {
         return false;
      }
   }

   private static ServerUtility.Data genData(String full) {
      return new ServerUtility.Data(full, full.substring(0, 1).toUpperCase() + full.substring(1), full.substring(0, 1).toUpperCase());
   }

   @Generated
   private ServerUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static void setHasCT(boolean hasCT) {
      ServerUtility.hasCT = hasCT;
   }

   @Generated
   public static void setCtTime(int ctTime) {
      ServerUtility.ctTime = ctTime;
   }

   record Data(String orig, String big, String small) {
   }
}
