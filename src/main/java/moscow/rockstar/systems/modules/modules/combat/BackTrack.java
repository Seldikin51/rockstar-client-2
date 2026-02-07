package moscow.rockstar.systems.modules.modules.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.mixins.BacktrackableEntity;
import moscow.rockstar.utility.render.Draw3DUtility;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Back Track", desc = "Задерживает хитбокс", category = ModuleCategory.COMBAT)
public class BackTrack extends BaseModule {
   private final BooleanSetting visual = new BooleanSetting(this, "Визуализировать");
   private final SliderSetting delay = new SliderSetting(this, "время").min(50.0F).max(1000.0F).step(50.0F).currentValue(100.0F);
   private final EventListener<ClientPlayerTickEvent> updateEvent = event -> {
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player
            && !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())
            && player instanceof BacktrackableEntity backtrackableEntity
            && backtrackableEntity.rockstar2_0$getBackTracks().size() > 2) {
            backtrackableEntity.rockstar2_0$getBackTracks().removeFirst();
         }
      }
   };
   private final EventListener<Render3DEvent> event3d = e -> {
      if (this.visual.isEnabled()) {
         MatrixStack ms = e.getMatrices();
         Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

         for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player
               && !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())
               && player instanceof BacktrackableEntity backtrackable) {
               List<BackTrack.Position> backTracks = backtrackable.rockstar2_0$getBackTracks();
               if (!backTracks.isEmpty()) {
                  long now = System.currentTimeMillis();
                  backTracks.removeIf(pos -> (float)(now - pos.time()) > this.delay.getCurrentValue());
                  BackTrack.Position last = backTracks.getLast();
                  Vec3d lastPos = last.pos();
                  BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                  ms.push();
                  RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                  RenderSystem.disableCull();
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  Draw3DUtility.renderOutlinedBox(
                     ms,
                     buffer,
                     player.getBoundingBox().offset(lastPos.subtract(player.getPos())).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z),
                     ColorRGBA.WHITE.withAlpha(180.0F)
                  );
                  BuiltBuffer built = buffer.endNullable();
                  if (built != null) {
                     BufferRenderer.drawWithGlobalProgram(built);
                  }

                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  ms.pop();
               }
            }
         }
      }
   };

   public record Position(Vec3d pos, long time) {
   }
}
