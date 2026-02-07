package moscow.rockstar.systems.modules.modules.player;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Blink", category = ModuleCategory.PLAYER)
public class Blink extends BaseModule {
   private final List<Packet<?>> packets = new ArrayList<>();
   private final Timer timer = new Timer();
   private final BooleanSetting pulse = new BooleanSetting(this, "modules.settings.blink.pulse");
   private final SliderSetting time = new SliderSetting(this, "modules.settings.blink.time", () -> !this.pulse.isEnabled())
      .min(1.0F)
      .max(40.0F)
      .step(1.0F)
      .currentValue(12.0F);
   private final BooleanSetting display = new BooleanSetting(this, "modules.settings.blink.display");
   private final BooleanSetting svo = new BooleanSetting(this, "modules.settings.blink.hide_first_person", () -> !this.display.isEnabled());
   private Vec3d lastPos;
   private boolean replaying;
   private final EventListener<SendPacketEvent> sendListener = this::savePacket;
   private final EventListener<Render3DEvent> event3d = e -> {
      if (this.display.isEnabled() && this.lastPos != null && (mc.options.getPerspective() != Perspective.FIRST_PERSON || !this.svo.isEnabled())) {
         MatrixStack ms = e.getMatrices();
         BufferBuilder quadsBuffer = RenderSystem.renderThreadTesselator().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
         ms.push();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Draw3DUtility.renderOutlinedBox(
            ms,
            quadsBuffer,
            mc.player.getBoundingBox().offset(this.lastPos.subtract(mc.player.getPos())).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z),
            ColorRGBA.WHITE.withAlpha(180.0F)
         );
         BuiltBuffer buildQuadsBuffer = quadsBuffer.endNullable();
         if (buildQuadsBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(buildQuadsBuffer);
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         ms.pop();
      }
   };
   private final EventListener<WorldChangeEvent> world = e -> this.disable();

   public void savePacket(SendPacketEvent e) {
      if (!this.replaying && EntityUtility.isInGame()) {
         this.packets.add(e.getPacket());
         e.cancel();
         if (this.pulse.isEnabled() && this.timer.finished((long)(this.time.getCurrentValue() * 50.0F))) {
            this.onDisable();
            this.onEnable();
            this.timer.reset();
         }
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.packets.clear();
         this.lastPos = mc.player.getPos();
         this.timer.reset();
         this.replaying = false;
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         this.replaying = true;

         for (Packet<?> p : this.packets) {
            mc.player.networkHandler.sendPacket(p);
         }

         this.replaying = false;
         this.packets.clear();
         this.lastPos = null;
      }
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public BooleanSetting getPulse() {
      return this.pulse;
   }

   @Generated
   public SliderSetting getTime() {
      return this.time;
   }
}
