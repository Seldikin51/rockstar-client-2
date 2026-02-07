package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.player.Blink;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.IslandStatus;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;

public class BlinkStatus extends IslandStatus {
   public BlinkStatus(SelectSetting setting) {
      super(setting, "blink");
   }

   @Override
   public void draw(CustomDrawContext context) {
      DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
      Blink blink = this.blink();
      Font font = Fonts.MEDIUM.getFont(7.0F);
      float x = sr.getScaledWidth() / 2.0F - island.getSize().width / 2.0F;
      float y = 7.0F;
      float width = this.size.width = 80.0F;
      float height = this.size.height = 15.0F;
      context.drawText(font, "Blink", x - 4.0F + 10.0F * this.animation.getValue(), y + 5.0F, Colors.getTextColor());
      if (!blink.getPulse().isEnabled()) {
         context.drawRightText(
            font,
            TextUtility.formatNumber((float)blink.getTimer().getElapsedTime() / 1000.0F) + " сек",
            x + width + 4.0F - 10.0F * this.animation.getValue(),
            y + 5.0F,
            Colors.getTextColor()
         );
      } else {
         float blinkWidth = width - font.width("Blink") - 14.0F;
         float progress = blinkWidth
            * ((blink.getTime().getCurrentValue() * 50.0F - (float)blink.getTimer().getElapsedTime()) / (blink.getTime().getCurrentValue() * 50.0F));
         context.drawRoundedRect(x + width - 5.0F - blinkWidth, y + 4.5F, blinkWidth, 6.0F, BorderRadius.all(2.5F), Colors.getAdditionalColor());
         context.drawRoundedRect(x + width - 5.0F - progress, y + 4.5F, progress, 6.0F, BorderRadius.all(2.5F), Colors.ACCENT);
      }
   }

   @Override
   public boolean canShow() {
      return this.blink().isEnabled();
   }

   private Blink blink() {
      return Rockstar.getInstance().getModuleManager().getModule(Blink.class);
   }
}
