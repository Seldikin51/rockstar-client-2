package moscow.rockstar.ui.hud.inline;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.render.VertexFormats;

public class InlineElement extends HudElement {
   protected final SelectSetting elements = new SelectSetting(this, "elements").draggable().min(1);

   public InlineElement(String name, String icon) {
      super(name, icon);
      this.height = 18.0F;
   }

   @Override
   protected void renderComponent(UIContext context) {
      context.drawClientRect(this.x, this.y, this.width, this.height, this.animation.getValue(), this.dragAnim.getValue(), 3.0F);
      ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
      context.drawTexture(Rockstar.id(this.icon), this.x + 5.0F, this.y + 5.0F, 8.0F, 8.0F, Colors.getTextColor());
      float xOffset = 0.0F;
      Batching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, Fonts.MEDIUM);

      for (SelectSetting.Value value : this.elements.getValues()) {
         InlineValue elmt = (InlineValue)value;
         if (elmt.isSelected()) {
            float textWidth = Fonts.MEDIUM.getFont(8.0F).width(elmt.text()) + 8.0F * elmt.copyAnim().getValue();
            boolean hover = GuiUtility.isHovered(this.x + 19.0F + xOffset, this.y + 6.0F, Fonts.MEDIUM.getFont(8.0F).width(elmt.text()), 8.0, context)
               && !elmt.copy().isEmpty();
            if (!hover || elmt.copyTimer().finished(1000L)) {
               elmt.copied(false);
            }

            elmt.copyAnim().update(hover);
            elmt.successAnim().update(elmt.copied());
            context.drawText(
               Fonts.MEDIUM.getFont(8.0F), elmt.text(), this.x + 19.0F + xOffset + 8.0F * elmt.copyAnim().getValue(), this.y + 6.0F, Colors.getTextColor()
            );
            if (!elmt.suffix().isEmpty()) {
               context.drawText(
                  Fonts.MEDIUM.getFont(7.0F), elmt.suffix(), this.x + 19.0F + xOffset + textWidth, this.y + 7.0F, Colors.getTextColor().mulAlpha(0.5F)
               );
            }

            xOffset += textWidth + Fonts.MEDIUM.getFont(7.0F).width(elmt.suffix()) + 10.0F;
         }
      }

      fontBatching.draw();
      xOffset = 0.0F;

      for (SelectSetting.Value valuex : this.elements.getValues()) {
         InlineValue elmt = (InlineValue)valuex;
         if (elmt.isSelected()) {
            float textWidthx = Fonts.MEDIUM.getFont(8.0F).width(elmt.text()) + 8.0F * elmt.copyAnim().getValue();
            if (xOffset != 0.0F) {
               context.drawRoundedRect(
                  this.x + 19.0F + xOffset - 7.0F, this.y + this.height / 2.0F - 1.0F, 2.0F, 2.0F, BorderRadius.all(1.0F), Colors.getTextColor().mulAlpha(0.5F)
               );
            }

            RenderUtility.rotate(context.getMatrices(), this.x + 19.0F + xOffset + 3.0F, this.y + 6.0F + 3.0F, 90.0F * elmt.successAnim().getValue());
            context.drawTexture(
               Rockstar.id("icons/hud/copy.png"),
               this.x + 19.0F + xOffset,
               this.y + 6.0F,
               6.0F,
               6.0F,
               Colors.getTextColor().mulAlpha(elmt.copyAnim().getValue() * (1.0F - elmt.successAnim().getValue()))
            );
            RenderUtility.end(context.getMatrices());
            RenderUtility.rotate(context.getMatrices(), this.x + 19.0F + xOffset + 3.0F, this.y + 6.0F + 3.0F, -90.0F + 90.0F * elmt.successAnim().getValue());
            context.drawTexture(
               Rockstar.id("icons/check.png"),
               this.x + 19.0F + xOffset,
               this.y + 6.0F,
               6.0F,
               6.0F,
               Colors.GREEN.mulAlpha(elmt.copyAnim().getValue() * elmt.successAnim().getValue())
            );
            RenderUtility.end(context.getMatrices());
            xOffset += textWidthx + Fonts.MEDIUM.getFont(7.0F).width(elmt.suffix()) + 10.0F;
         }
      }

      ScissorUtility.pop();
      this.width = 15.0F + xOffset;
      this.getWidthAnim().update(this.width);
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      float xOffset = 0.0F;

      for (SelectSetting.Value value : this.elements.getValues()) {
         InlineValue elmt = (InlineValue)value;
         if (elmt.isSelected()) {
            float textWidth = Fonts.MEDIUM.getFont(8.0F).width(elmt.text());
            boolean hover = GuiUtility.isHovered(
                  (double)(this.x + 19.0F + xOffset), (double)(this.y + 6.0F), (double)Fonts.MEDIUM.getFont(8.0F).width(elmt.text()), 8.0, mouseX, mouseY
               )
               && !elmt.copy().isEmpty();
            if (hover && button == MouseButton.LEFT) {
               TextUtility.copyText(elmt.copy());
               elmt.copyTimer().reset();
               elmt.copied(true);
               return;
            }

            xOffset += textWidth + Fonts.MEDIUM.getFont(7.0F).width(elmt.suffix()) + 10.0F;
         }
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }
}
