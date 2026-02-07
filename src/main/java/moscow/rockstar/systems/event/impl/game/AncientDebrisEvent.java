package moscow.rockstar.systems.event.impl.game;

import java.util.Collections;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AncientDebrisEvent extends Event {
   private final List<BlockPos> positions;
   private final Vec3d explosionCenter;

   public AncientDebrisEvent(List<BlockPos> positions, Vec3d center) {
      this.positions = Collections.unmodifiableList(positions);
      this.explosionCenter = center;
   }

   @Generated
   public List<BlockPos> getPositions() {
      return this.positions;
   }

   @Generated
   public Vec3d getExplosionCenter() {
      return this.explosionCenter;
   }
}
