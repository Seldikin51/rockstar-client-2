package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Friend Markers", desc = "Выделяет друзей", category = ModuleCategory.VISUALS)
public class FriendMarkers extends BaseModule {
   private final ModeSetting setting = new ModeSetting(this, "modules.settings.friends_markers.setting");
   private final ModeSetting.Value heads = new ModeSetting.Value(this.setting, "modules.settings.friends_markers.heads");
   private final ModeSetting.Value sims = new ModeSetting.Value(this.setting, "Sims");
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (this.sims.isSelected()) {
         RenderUtility.setupRender3D(true);
         MatrixStack ms = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         ColorRGBA color = new ColorRGBA(52.0F, 199.0F, 89.0F);
         BufferBuilder builder = CrystalRenderer.createBuffer();

         for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            if (Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
               ms.push();
               RenderUtility.prepareMatrices(ms, Utils.getInterpolatedPos(player, event.getTickDelta()));
               float size = 0.1F;
               CrystalRenderer.render(ms, builder, 0.0F, player.getHeight() + 0.4F, 0.0F, size, color.withAlpha(255.0F));
               ms.pop();
            }
         }

         BuiltBuffer built = builder.endNullable();
         if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
         }

         RenderUtility.endRender3D();
      }
   };

   @Generated
   public ModeSetting.Value getHeads() {
      return this.heads;
   }
}
