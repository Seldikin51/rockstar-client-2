package moscow.rockstar.systems.commands.commands;

import java.util.Map.Entry;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class WaypointsCommand implements IMinecraft, IScaledResolution {
   private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
      MatrixStack matrices = event.getContext().getMatrices();
      Batching rect = new RectBatching(VertexFormats.POSITION_COLOR, event.getContext().getMatrices());
      this.renderBack(event, matrices);
      rect.draw();
      FontBatching batching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);
      this.renderText(event, matrices);
      batching.draw();
   };

   public WaypointsCommand() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   public Command command() {
      return CommandBuilder.begin("waypoint")
         .aliases("way")
         .desc("Метки")
         .param("action", p -> p.literal("add", "del", "clear"))
         .param("name", p -> p.optional().validator(ValidationResult::ok))
         .param("x", p -> p.optional().validator(this::verifyCoordinate))
         .param("y", p -> p.optional().validator(this::verifyCoordinate))
         .param("z", p -> p.optional().validator(this::verifyCoordinate))
         .handler(this::handle)
         .build();
   }

   private ValidationResult verifyCoordinate(String input) {
      try {
         Integer.parseInt(input);
         return ValidationResult.ok(input);
      } catch (NumberFormatException var3) {
         return ValidationResult.error("Не правильное число");
      }
   }

   private void handle(CommandContext ctx) {
      String action = (String)ctx.arguments().get(0);
      String name = (String)ctx.arguments().get(1);
      String x = (String)ctx.arguments().get(2);
      String y = (String)ctx.arguments().get(3);
      String z = (String)ctx.arguments().get(4);
      WayPointsManager wayPointsManager = Rockstar.getInstance().getWayPointsManager();
      String var8 = action.toLowerCase();
      switch (var8) {
         case "add":
            if (name == null || x == null || y == null || z == null) {
               MessageUtility.error(Text.of("Укажите название и координаты (.way add \"Название\" x y z)"));
               return;
            }

            try {
               wayPointsManager.add(name, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
            } catch (NumberFormatException var11) {
               MessageUtility.error(Text.of("Координаты должны быть числами"));
            }
            break;
         case "del":
            if (name == null) {
               MessageUtility.error(Text.of("Укажите название (.way del \"Название\")"));
               return;
            }

            wayPointsManager.del(name);
            break;
         case "clear":
            wayPointsManager.clear();
      }
   }

   private void renderBack(HudRenderEvent event, MatrixStack matrices) {
      for (Entry<String, Vec3d> entry : Rockstar.getInstance().getWayPointsManager().getEntries()) {
         String name = entry.getKey();
         Vec3d pos = entry.getValue();
         Vec3d renderPos = pos.add(0.0, 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(renderPos);
         if (screenPos != null) {
            float distance = (float)mc.player.getPos().distanceTo(pos.add(0.5, 0.5, 0.5));
            float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0F);
            matrices.scale(scale, scale, 1.0F);
            int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(name + " " + String.format("%.1f", mc.player.getPos().distanceTo(pos)) + "m");
            int x = -textWidth / 2;
            int y = 5;
            event.getContext().drawRect(x - 3, y - 3, textWidth + 8, Fonts.MEDIUM.getFont(11.0F).height() + 6.0F, new ColorRGBA(0.0F, 0.0F, 0.0F, 100.0F));
            matrices.pop();
         }
      }
   }

   private void renderText(HudRenderEvent event, MatrixStack matrices) {
      for (Entry<String, Vec3d> entry : Rockstar.getInstance().getWayPointsManager().getEntries()) {
         String name = entry.getKey();
         Vec3d pos = entry.getValue();
         Vec3d renderPos = pos.add(0.0, 0.5, 0.0);
         Vec2f screenPos = Utils.worldToScreen(renderPos);
         if (screenPos != null) {
            float distance = (float)mc.player.getPos().distanceTo(pos.add(0.5, 0.5, 0.5));
            float scale = MathHelper.clamp(1.0F - distance / 20.0F, 0.5F, 1.0F);
            matrices.push();
            matrices.translate(screenPos.x, screenPos.y, 0.0F);
            matrices.scale(scale, scale, 1.0F);
            int textWidth = (int)Fonts.MEDIUM.getFont(11.0F).width(name + " " + String.format("%.1f", mc.player.getPos().distanceTo(pos)) + "m");
            int x = -textWidth / 2;
            int y = 5;
            event.getContext()
               .drawText(Fonts.MEDIUM.getFont(11.0F), name + " " + String.format("%.1f", mc.player.getPos().distanceTo(pos)) + "m", x, y, ColorRGBA.WHITE);
            matrices.pop();
         }
      }
   }
}
