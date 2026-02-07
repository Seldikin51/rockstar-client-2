package moscow.rockstar.ui.hud.impl.island.impl;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.TimerStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class MineStatus extends TimerStatus implements IMinecraft {
   private Vec3d vec = new Vec3d(-52.0, 87.0, 3.0);

   public MineStatus(SelectSetting setting) {
      super(setting, "mine");
   }

   @Override
   public void draw(CustomDrawContext context) {
      if (mc.world != null && mc.player != null && ServerUtility.spawn()) {
         String time = "";
         String mineType = "";
         List<String> hw_types = List.of("обычная", "редкая", "эпическая", "легендарная", "мифическая");
         List<ArmorStandEntity> nearbyStands = new ArrayList<>();

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand && this.near(armorStand, new Vec3d(-52.0, 87.0, 3.0))) {
               nearbyStands.add(armorStand);
            }
         }

         nearbyStands.sort((a, b) -> Double.compare(b.getY(), a.getY()));

         for (int i = 0; i < nearbyStands.size(); i++) {
            ArmorStandEntity armorStand = nearbyStands.get(i);
            Text customName = armorStand.getCustomName();
            if (customName != null) {
               String name = customName.getString().trim();
               if (name.matches("\\d{1,2}:\\d{2}")) {
                  time = name.replaceFirst("^0", "");
               } else if (name.contains("осталось:")) {
                  int index = name.indexOf(58);
                  if (index != -1 && index + 2 < name.length()) {
                     String timeStr = name.substring(index + 2).trim();
                     int minIndex = timeStr.indexOf(" мин.");
                     int secIndex = timeStr.indexOf(" сек.");
                     if (minIndex != -1 && secIndex != -1) {
                        int min = Integer.parseInt(timeStr.substring(0, minIndex).trim());
                        int sec = Integer.parseInt(timeStr.substring(minIndex + 5, secIndex).trim());
                        time = String.format("%d:%02d", min, sec);
                     }
                  }
               } else if (name.startsWith("Следующая:")) {
                  int index = name.indexOf(58);
                  if (index != -1 && index + 2 < name.length()) {
                     mineType = name.substring(index + 2).trim();
                  }
               } else if (name.equals("Следующая шахта:") && i + 1 < nearbyStands.size()) {
                  ArmorStandEntity nextStand = nearbyStands.get(i + 1);
                  Text nextName = nextStand.getCustomName();
                  if (nextName != null) {
                     String nextNameStr = nextName.getString().trim();
                     if (hw_types.contains(nextNameStr.toLowerCase().trim())) {
                        mineType = nextNameStr;
                     }
                  }
               }

               if (!time.isEmpty() && !mineType.isEmpty()) {
                  break;
               }
            }
         }

         if (!time.isEmpty() && !mineType.isEmpty()) {
            ColorRGBA color;
            if (ServerUtility.is("holyworld")) {
               String var19 = mineType.trim().toLowerCase();

               color = switch (var19) {
                  case "легендарная" -> new ColorRGBA(0.0F, 128.0F, 250.0F);
                  case "эпическая" -> new ColorRGBA(231.0F, 0.0F, 250.0F);
                  default -> new ColorRGBA(243.0F, 151.0F, 250.0F);
               };
            } else {
               String var20 = mineType.trim().toLowerCase();

               color = switch (var20) {
                  case "легендарная" -> new ColorRGBA(84.0F, 152.0F, 152.0F);
                  case "мифическая" -> new ColorRGBA(252.0F, 84.0F, 252.0F);
                  default -> new ColorRGBA(252.0F, 168.0F, 0.0F);
               };
            }

            this.update(Integer.parseInt(time.split(":")[0]) + ":", "", Integer.parseInt(time.split(":")[1]), mineType, color);
            super.draw(context);
            this.timeAnim.settings(true, ColorRGBA.WHITE);
         }
      }
   }

   @Override
   public boolean canShow() {
      if (mc.world != null && mc.player != null && ServerUtility.spawn()) {
         if (ServerUtility.is("holyworld")) {
            this.vec = new Vec3d(23.0, 41.0, -156.0);
         } else {
            this.vec = new Vec3d(-52.0, 87.0, 3.0);
         }

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand && armorStand.isAlive() && this.near(armorStand, this.vec)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean near(ArmorStandEntity a, Vec3d v) {
      return Math.abs(a.getX() - v.x) <= 2.0 && Math.abs(a.getY() - v.y) <= 2.0 && Math.abs(a.getZ() - v.z) <= 2.0;
   }
}
