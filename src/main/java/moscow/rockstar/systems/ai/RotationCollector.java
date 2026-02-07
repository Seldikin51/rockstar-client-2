package moscow.rockstar.systems.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

@ModuleInfo(name = "Recorder", category = ModuleCategory.OTHER)
public class RotationCollector extends BaseModule {
   private static final long LOG_WINDOW_MS = 1500L;
   private final List<HashMap<String, Object>> dataset = new ArrayList<>();
   private float lastYaw;
   private float lastPitch;
   private float prevYaw;
   private float prevPitch;
   private float prevTargetYaw;
   private float prevTargetPitch;
   private float prevDistance;
   private long lastSwingTimeMs;
   private Entity lastTarget;
   private final EventListener<AttackEvent> onAttackEvent = event -> {
      long now = System.currentTimeMillis();
      this.lastSwingTimeMs = now;
      Entity target = event.getEntity();
      if (target != null) {
         this.lastTarget = target;
         ClientPlayerEntity p = MinecraftClient.getInstance().player;
         if (p != null) {
            this.lastYaw = p.getYaw();
            this.lastPitch = p.getPitch();
         }
      } else {
         this.lastTarget = null;
      }
   };
   private final EventListener<HudRenderEvent> onHudRender = event -> {
      long now = System.currentTimeMillis();
      if (this.lastTarget != null
         && now - this.lastSwingTimeMs <= 1500L
         && this.lastTarget.isAlive()
         && mc.world.getPlayers().contains(this.lastTarget)
         && mc.player.distanceTo(this.lastTarget) < 5.0F) {
         ClientPlayerEntity p = mc.player;
         float yaw = p.getYaw();
         float diffYaw = this.normalizeAngle(yaw - this.lastYaw);
         float targetYaw = this.calcTargetDeltaYaw(p, this.lastTarget);
         if (Math.abs(diffYaw) < 50.0F || Math.signum(diffYaw) == Math.signum(targetYaw)) {
            event.getContext().drawCenteredText(Fonts.MEDIUM.getFont(8.0F), "Recording", sr.getScaledWidth() / 2.0F, 40.0F, ColorRGBA.WHITE);
         }
      }
   };
   private final EventListener<ClientPlayerTickEvent> onPlayerTick = event -> {
      ClientPlayerEntity p = MinecraftClient.getInstance().player;
      if (p != null) {
         long now = System.currentTimeMillis();
         float yaw = p.getYaw();
         float pitch = p.getPitch();
         float diffYaw = this.normalizeAngle(yaw - this.lastYaw);
         float diffPitch = this.normalizeAngle(pitch - this.lastPitch);
         float distance = -999.0F;
         float targetYaw = -999.0F;
         float targetPitch = -999.0F;
         if (this.lastTarget != null
            && now - this.lastSwingTimeMs <= 1500L
            && this.lastTarget.isAlive()
            && mc.world.getPlayers().contains(this.lastTarget)
            && mc.player.distanceTo(this.lastTarget) < 5.0F) {
            distance = p.distanceTo(this.lastTarget);
            targetYaw = this.calcTargetDeltaYaw(p, this.lastTarget);
            targetPitch = this.calcTargetDeltaPitch(p, this.lastTarget);
            float diffY = (float)(mc.player.getY() - this.lastTarget.getY());
            if (Math.abs(targetYaw) < 30.0F || Math.signum(diffYaw) == Math.signum(targetYaw)) {
               HashMap<String, Object> rec = new HashMap<>();
               rec.put("deltaYaw", diffYaw);
               rec.put("deltaPitch", diffPitch);
               rec.put("timeSinceLastHitMs", now - this.lastSwingTimeMs);
               rec.put("distance", distance);
               rec.put("fallDistance", mc.player.fallDistance);
               rec.put("diffY", diffY);
               rec.put("targetDeltaYaw", targetYaw);
               rec.put("targetDeltaPitch", targetPitch);
               rec.put("prevTargetYaw", this.prevTargetYaw == -999.0F ? targetYaw : this.prevTargetYaw);
               rec.put("prevTargetPitch", this.prevTargetPitch == -999.0F ? targetPitch : this.prevTargetPitch);
               rec.put("prevYaw", this.prevYaw);
               rec.put("prevPitch", this.prevPitch);
               rec.put("prevDistance", this.prevDistance == -999.0F ? distance : this.prevDistance);
               this.dataset.add(rec);
            }
         }

         this.prevTargetYaw = targetYaw;
         this.prevTargetPitch = targetPitch;
         this.prevYaw = diffYaw;
         this.prevPitch = diffPitch;
         this.prevDistance = distance;
         if (this.lastTarget != null && now - this.lastSwingTimeMs > 1500L && !this.dataset.isEmpty()) {
            this.dumpToJson();
            this.dataset.clear();
            this.lastTarget = null;
         }

         this.lastYaw = p.getYaw();
         this.lastPitch = p.getPitch();
      }
   };

   private void dumpToJson() {
      Path out = MinecraftClient.getInstance().runDirectory.toPath().resolve("C:/Rockstar/kill_aura_dataset.json");

      try (FileWriter writer = new FileWriter(out.toFile(), true)) {
         Gson gson = new GsonBuilder().create();
         writer.write(gson.toJson(this.dataset));
         writer.write("\n");
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   private float normalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle > 180.0F) {
         angle -= 360.0F;
      }

      if (angle < -180.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   private float calcTargetDeltaYaw(ClientPlayerEntity p, Entity t) {
      double dx = t.getX() - p.getX();
      double dz = t.getZ() - p.getZ();
      float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      return this.normalizeAngle(targetYaw - p.getYaw());
   }

   private float calcTargetDeltaPitch(ClientPlayerEntity p, Entity t) {
      double dx = t.getX() - p.getX();
      double dz = t.getZ() - p.getZ();
      double dy = t.getEyeY() - p.getEyeY();
      double dist = Math.sqrt(dx * dx + dz * dz);
      float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return this.normalizeAngle(targetPitch - p.getPitch());
   }
}
