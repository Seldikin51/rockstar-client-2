package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "TNT Timer", category = ModuleCategory.VISUALS, desc = "modules.descriptions.tnt_timer")
public class TNTTimer extends BaseModule {
   private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
      MatrixStack matrices = event.getContext().getMatrices();
      Batching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());

      for (Entity entity : mc.world.getEntities()) {
         if (entity instanceof TntEntity tnt) {
            this.renderBack(event, matrices, tnt);
         }
      }

      rect.draw();
      FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (Entity entityx : mc.world.getEntities()) {
         if (entityx instanceof TntEntity tnt) {
            this.renderText(event, matrices, tnt);
         }
      }

      batching.draw();
   };

   private void renderBack(PreHudRenderEvent event, MatrixStack matrices, TntEntity entity) {
      int fuse = entity.getFuse();
      float seconds = fuse / 20.0F;
      String text = Localizator.translate("modules.tnt_timer.format", seconds);
      Vec3d renderPos = entity.getLerpedPos(event.getTickDelta()).add(0.0, 0.5, 0.0);
      Vec2f screenPos = Utils.worldToScreen(renderPos);
      if (screenPos != null) {
         float distance = (float)mc.player.getPos().distanceTo(renderPos);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x - 6.0F, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int width = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
         int x = -width / 2;
         event.getContext().drawRect(x - 3, 1.0F, width + 26, Fonts.MEDIUM.getFont(11.0F).height() + 8.0F, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
         matrices.pop();
      }
   }

   private void renderText(PreHudRenderEvent event, MatrixStack matrices, TntEntity entity) {
      int fuse = entity.getFuse();
      float seconds = fuse / 20.0F;
      String text = Localizator.translate("modules.tnt_timer.format", seconds);
      Vec3d renderPos = entity.getLerpedPos(event.getTickDelta()).add(0.0, 0.5, 0.0);
      Vec2f screenPos = Utils.worldToScreen(renderPos);
      if (screenPos != null) {
         float distance = (float)mc.player.getPos().distanceTo(renderPos);
         float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
         matrices.push();
         matrices.translate(screenPos.x - 6.0F, screenPos.y, 0.0F);
         matrices.scale(scale, scale, 1.0F);
         int width = (int)Fonts.MEDIUM.getFont(11.0F).width(text);
         int x = -width / 2;
         event.getContext().drawText(Fonts.MEDIUM.getFont(11.0F), text, x + 16, 5.0F, ColorRGBA.WHITE);
         event.getContext().drawItem(Items.TNT, (float)x, 3.0F, 0.75F);
         matrices.pop();
      }
   }
}
