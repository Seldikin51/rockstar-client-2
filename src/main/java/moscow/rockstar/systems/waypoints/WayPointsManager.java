package moscow.rockstar.systems.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class WayPointsManager {
   private final Map<String, Vec3d> waypoints = new HashMap<>();

   public void add(String name, int x, int y, int z) {
      Vec3d pos = new Vec3d(x, y, z);
      if (this.waypoints.containsKey(name)) {
         MessageUtility.error(Text.of(Localizator.translate("modules.waypoints.exists", name)));
      } else {
         this.waypoints.put(name, pos);
         MessageUtility.info(Text.of(Localizator.translate("modules.waypoints.added", name, x, y, z)));
      }
   }

   public void del(String name) {
      if (this.waypoints.remove(name) != null) {
         MessageUtility.info(Text.of(Localizator.translate("modules.waypoints.deleted", name)));
      } else {
         MessageUtility.info(Text.of(Localizator.translate("modules.waypoints.not_found", name)));
      }
   }

   public void clear() {
      this.waypoints.clear();
      MessageUtility.info(Text.of(Localizator.translate("modules.waypoints.cleared")));
   }

   public boolean contains(String name) {
      return this.waypoints.containsKey(name);
   }

   public Set<Entry<String, Vec3d>> getEntries() {
      return this.waypoints.entrySet();
   }
}
