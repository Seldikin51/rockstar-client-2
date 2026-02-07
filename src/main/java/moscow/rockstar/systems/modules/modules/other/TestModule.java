package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.event.impl.window.ChatTypeEvent;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@ModuleInfo(name = "Test", category = ModuleCategory.OTHER)
public class TestModule extends BaseModule {
   private final RangeSetting testBoolean = new RangeSetting(this, "Name").min(1.0F).max(10.0F).step(1.0F).firstValue(3.0F).secondValue(6.0F);
   private Popup popup = new Popup(100.0F, 100.0F);
   private AnimatedNumber time;
   private final EventListener<ChatTypeEvent> onChat = event -> {};
   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (mc.player != null) {
         ;
      }
   };
   private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
      UIContext context = UIContext.of(
         event.getContext(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getX(),
         mc.currentScreen == null ? -1 : (int)GuiUtility.getMouse().getY(),
         MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)
      );
   };

   @Override
   public void tick() {
      if (mc.player != null) {
         Entity entity = mc.targetedEntity;
         boolean isAnimal = entity instanceof AnimalEntity;
         boolean isMob = entity instanceof MobEntity;
         MessageUtility.info(Text.of("Is Animal: " + isAnimal + " | IsMob: " + isMob));
         super.tick();
      }
   }

   @Override
   public void onEnable() {
      this.popup = new Popup(100.0F, 100.0F, 90.0F)
         .text("Sosalin1337")
         .separator()
         .checkbox("Друг", false)
         .checkbox("Враг", true)
         .checkbox("Главный враг всех народов", false)
         .separator()
         .checkbox("Glabos", true)
         .checkbox("Sosia", true)
         .checkbox("x, x, x", false);

      for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
         System.out.println(String.format("modules.descriptions.%s=%s", module.getName().toLowerCase().replace(" ", "_"), module.getDescription()));
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   private Vec3d getRenderPos() {
      float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
      return new Vec3d(
         MathHelper.lerp(tickDelta, mc.player.prevX, mc.player.getX()),
         MathHelper.lerp(
            tickDelta, mc.player.prevY + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose())
         ),
         MathHelper.lerp(tickDelta, mc.player.prevZ, mc.player.getZ())
      );
   }

   private void renderTexture(MatrixStack matrices, Identifier identifier, ColorRGBA color, float size) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      builder.vertex(matrix, 0.0F, -size, 0.0F).texture(0.0F, 0.0F).color(color.getRGB());
      builder.vertex(matrix, -size, -size, 0.0F).texture(0.0F, 1.0F).color(color.getRGB());
      builder.vertex(matrix, -size, 0.0F, 0.0F).texture(1.0F, 1.0F).color(color.getRGB());
      builder.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(color.getRGB());
      BufferRenderer.drawWithGlobalProgram(builder.end());
   }
}
