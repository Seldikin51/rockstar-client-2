package moscow.rockstar.systems.modules.modules.visuals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.SoundEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Sound ESP", category = ModuleCategory.VISUALS, enabledByDefault = true, desc = "Показывает где был воспроизведен звук")
public class SoundESP extends BaseModule {
   private final SelectSetting select = new SelectSetting(this, "Отображать");
   private final SelectSetting.Value trident = new SelectSetting.Value(this.select, "Трезубец").select();
   private final SelectSetting.Value tnt = new SelectSetting.Value(this.select, "Динамит");
   private final SelectSetting.Value fireworks = new SelectSetting.Value(this.select, "Фейерверки");
   private final Map<String, SoundESP.SoundMarker> markers = new HashMap<>();
   private final Set<String> TARGET_SOUNDS = new HashSet<>(
      Arrays.asList(
         "minecraft:entity.generic.explode", "minecraft:item.trident.throw", "minecraft:item.trident.return", "minecraft:entity.firework_rocket.launch"
      )
   );
   private final EventListener<SoundEvent> onSoundInstanceEvent = event -> {
      SoundInstance sound = event.getSound();
      Identifier soundId = sound.getId();
      String soundIdStr = soundId.toString();
      if (this.TARGET_SOUNDS.contains(soundIdStr)) {
         boolean add = false;
         if (soundIdStr.contains("generic.explode") && this.tnt.isSelected()) {
            add = true;
         } else if ((soundIdStr.contains("trident.throw") || soundIdStr.contains("trident.return")) && this.trident.isSelected()) {
            add = true;
         } else if (soundIdStr.contains("firework_rocket.launch") && this.fireworks.isSelected()) {
            add = true;
         }

         if (add && mc.player != null && mc.world != null) {
            String displayName = this.simplifySoundName(soundIdStr);
            long creationTime = System.currentTimeMillis();
            String key = displayName + "_" + creationTime;
            Vec3d pos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
            this.add(key, displayName, pos.x, pos.y, pos.z);
         }
      }
   };
   private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
      MatrixStack matrices = event.getContext().getMatrices();
      long currentTime = System.currentTimeMillis();
      this.markers.entrySet().removeIf(entry -> {
         SoundESP.SoundMarker markerx = entry.getValue();
         return currentTime - markerx.creationTime > 5000L;
      });
      Batching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());

      for (SoundESP.SoundMarker marker : this.markers.values()) {
         float distance = (float)mc.player.getPos().distanceTo(marker.pos);
         String text = marker.name + " (" + String.format("%.1f", distance) + "m)";
         this.renderBack(event, matrices, text, marker);
      }

      rect.draw();
      FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (SoundESP.SoundMarker marker : this.markers.values()) {
         float distance = (float)mc.player.getPos().distanceTo(marker.pos);
         String text = marker.name + " (" + String.format("%.1f", distance) + "m)";
         this.renderText(event, matrices, text, marker);
      }

      batching.draw();
   };

   private void renderText(PreHudRenderEvent event, MatrixStack matrices, String displayText, SoundESP.SoundMarker marker) {
      Vec3d renderPos = marker.pos;
      Vec3d renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
      Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
      if (screenPos != null) {
         float distance = (float)mc.player.getPos().distanceTo(renderPos);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         String text = marker.name + " (" + String.format("%.1f", distance) + "m)";
         int width = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
         int x = -width / 2;
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), text, x + 16, 5.0F, ColorRGBA.WHITE);
         if (marker.name.toLowerCase().contains("взрыв")) {
            event.getContext().drawItem(Items.TNT, (float)x, 3.0F, 0.75F);
         } else if (marker.name.toLowerCase().contains("трезубец")) {
            event.getContext().drawItem(Items.TRIDENT, (float)x, 3.0F, 0.75F);
         } else if (marker.name.toLowerCase().contains("фейерверк")) {
            event.getContext().drawItem(Items.FIREWORK_ROCKET, (float)x, 3.0F, 0.75F);
         }

         matrices.pop();
      }
   }

   private void renderBack(PreHudRenderEvent event, MatrixStack matrices, String displayText, SoundESP.SoundMarker marker) {
      Vec3d renderPos = marker.pos;
      Vec3d renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
      Vec2f screenPos = Utils.worldToScreen(renderPosAdjusted);
      if (screenPos != null) {
         float distance = (float)mc.player.getPos().distanceTo(renderPos);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(displayText);
         int x = -textWidth / 2;
         int y = 1;
         event.getContext().drawRect(x - 3, y, textWidth + 26, Fonts.MEDIUM.getFont(11.0F).height() + 8.0F, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
         matrices.pop();
      }
   }

   private void add(String key, String displayName, double x, double y, double z) {
      Vec3d pos = new Vec3d(x, y, z);
      this.markers.put(key, new SoundESP.SoundMarker(displayName, pos, System.currentTimeMillis()));
   }

   private String simplifySoundName(String soundId) {
      if (soundId.contains("generic.explode")) {
         return "Взрыв";
      } else if (soundId.contains("trident.throw")) {
         return "Трезубец брошен";
      } else if (soundId.contains("trident.return")) {
         return "Трезубец";
      } else {
         return soundId.contains("firework_rocket.launch") ? "Фейерверк" : soundId.replace("minecraft:", "");
      }
   }

   record SoundMarker(String name, Vec3d pos, long creationTime) {
   }
}
