package moscow.rockstar.ui.hud.impl.island.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.XRay;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.impl.island.ExtandableStatus;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class XrayStatus extends ExtandableStatus implements IMinecraft {
   private static final Item[] ORES = new Item[]{Items.ANCIENT_DEBRIS, Items.DIAMOND_ORE, Items.GOLD_ORE, Items.LAPIS_ORE};
   private static final String[] LABELS = new String[]{"Древние обломки: ", "Алмазная руда: ", "Золотая руда: ", "Лазуритовая руда: "};

   public XrayStatus(SelectSetting setting) {
      super(setting, "xray");
   }

   @Override
   public void draw(CustomDrawContext context) {
      DynamicIsland island = Rockstar.getInstance().getHud().getIsland();
      XRay xRay = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
      if (xRay.isEnabled() && this.haveOres(xRay)) {
         float x = sr.getScaledWidth() / 2.0F - island.getSize().width / 2.0F;
         float y = 7.0F;
         float expHeight = 25 + this.visibleOres(xRay) * 15;
         float expWidth = 114.0F;
         float maxWidth = 90.0F;
         float defaultWidth = 32.0F + Fonts.MEDIUM.getFont(7.0F).width("Найдено алмазов: " + xRay.getDiamonds());
         float width = this.size.width = island.isExtended() ? expWidth : Math.min(defaultWidth, maxWidth);
         float height = this.size.height = island.isExtended() ? expHeight : 15.0F;
         float extending = island.getExtendingAnim().getValue();
         if (extending != 0.0F) {
            if (extending > 0.7F) {
               float baseY = y + 20.0F;
               float alpha = 255.0F * extending;
               int entryCount = 0;
               context.drawText(
                  Fonts.MEDIUM.getFont(7.0F),
                  "Найдено: ",
                  x + 25.0F - 11.0F * this.animation.getValue(),
                  y + 10.0F,
                  Colors.getTextColor().withAlpha(255.0F * extending)
               );

               for (int i = 0; i < ORES.length; i++) {
                  int count = this.getOreCount(xRay, i);
                  if (count != 0) {
                     float entryY = baseY + entryCount * 15;
                     this.drawOreEntry(context, x + 25.0F - 11.0F * this.animation.getValue(), entryY, ORES[i], LABELS[i] + count, alpha * 0.7F);
                     entryCount++;
                  }
               }
            }
         } else {
            context.drawRoundedRect(
               x - 6.0F + 10.0F * this.animation.getValue(), y + 4.0F, 7.0F, 7.0F, BorderRadius.all(3.0F), new ColorRGBA(115.0F, 0.0F, 255.0F)
            );
            context.drawText(
               Fonts.MEDIUM.getFont(7.0F),
               "Найдено алмазов: " + xRay.getDiamonds(),
               x + 25.0F - 10.0F * this.animation.getValue(),
               y + 5.0F,
               Colors.getTextColor()
            );
         }
      }
   }

   private int getOreCount(XRay xRay, int index) {
      return switch (index) {
         case 0 -> xRay.getAncient();
         case 1 -> xRay.getDiamonds();
         case 2 -> xRay.getGold();
         case 3 -> xRay.getLapis();
         default -> 0;
      };
   }

   private int visibleOres(XRay xRay) {
      int count = 0;
      if (xRay.getAncient() > 0) {
         count++;
      }

      if (xRay.getDiamonds() > 0) {
         count++;
      }

      if (xRay.getGold() > 0) {
         count++;
      }

      if (xRay.getLapis() > 0) {
         count++;
      }

      return count;
   }

   private boolean haveOres(XRay xRay) {
      return (xRay.getAncient() > 0 || xRay.getDiamonds() > 0 || xRay.getGold() > 0 || xRay.getLapis() > 0) && xRay.isEnabled();
   }

   private void drawOreEntry(CustomDrawContext context, float x, float y, Item ore, String text, float alpha) {
      context.drawItem(ore, x - 1.0F, y, 0.75F);
      context.drawText(Fonts.MEDIUM.getFont(7.0F), text, x + 15.0F, y + 3.0F, Colors.getTextColor().withAlpha(alpha));
   }

   @Override
   public boolean canShow() {
      XRay xRay = Rockstar.getInstance().getModuleManager().getModule(XRay.class);
      return this.haveOres(xRay);
   }
}
