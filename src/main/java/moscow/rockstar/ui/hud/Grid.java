package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IScaledResolution;

public class Grid {
   private final List<GridLine> lines = new ArrayList<>();

   public void draw(CustomDrawContext context) {
      for (GridLine line : this.lines) {
         if (line.isActive()) {
            float x = line.getType() == GridLine.Type.VERTICAL ? line.getPos() : 0.0F;
            float y = line.getType() == GridLine.Type.HORIZONTAL ? line.getPos() : 0.0F;
            float width = line.getType() == GridLine.Type.VERTICAL ? 1.0F : IScaledResolution.sr.getScaledWidth();
            float height = line.getType() == GridLine.Type.HORIZONTAL ? 1.0F : IScaledResolution.sr.getScaledHeight();
            context.drawRect(x, y, width, height, ColorRGBA.WHITE.mulAlpha(0.3F));
         }
      }
   }

   public void update() {
      this.lines.clear();
      this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, 5.0F));
      this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, IScaledResolution.sr.getScaledHeight() - 5.0F));
      this.lines.add(new GridLine(GridLine.Type.VERTICAL, 4.0F));
      this.lines.add(new GridLine(GridLine.Type.VERTICAL, IScaledResolution.sr.getScaledWidth() - 5.0F));
      this.lines.add(new GridLine(GridLine.Type.VERTICAL, IScaledResolution.sr.getScaledWidth() / 2.0F - 1.0F));
      this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, IScaledResolution.sr.getScaledHeight() / 2.0F - 0.5F));
      this.lines.add(new GridLine(GridLine.Type.VERTICAL, IScaledResolution.sr.getScaledWidth() / 4.0F - 0.5F));
      this.lines.add(new GridLine(GridLine.Type.VERTICAL, IScaledResolution.sr.getScaledWidth() / 4.0F * 3.0F - 0.5F));
      this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, IScaledResolution.sr.getScaledHeight() / 4.0F - 0.5F));
      this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, IScaledResolution.sr.getScaledHeight() / 4.0F * 3.0F - 0.5F));

      for (HudElement element : Rockstar.getInstance().getHud().getElements()) {
         if (!element.isDragging() && !(element instanceof DynamicIsland)) {
            this.lines.add(new GridLine(GridLine.Type.HORIZONTAL, element.y));
            if (element.x + element.width / 2.0F > IScaledResolution.sr.getScaledWidth() / 2.0F) {
               this.lines.add(new GridLine(GridLine.Type.VERTICAL, element.x + element.width));
            } else {
               this.lines.add(new GridLine(GridLine.Type.VERTICAL, element.x));
            }
         }
      }
   }

   @Generated
   public List<GridLine> getLines() {
      return this.lines;
   }
}
