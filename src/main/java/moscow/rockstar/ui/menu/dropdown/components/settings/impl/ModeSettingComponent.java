package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.penis.PenisPlayer;

public class ModeSettingComponent extends MenuSettingComponent<ModeSetting> {
   private boolean initialized;

   public ModeSettingComponent(ModeSetting setting, CustomComponent parent) {
      super(setting, parent);
   }

   @Override
   protected void renderComponent(UIContext context) {
      if (!this.initialized) {
         for (ModeSetting.Value value : this.setting.getValues()) {
            value.setEnablePenis(new PenisPlayer(Rockstar.id("penises/check_enable.penis")));
            value.setDisablePenis(new PenisPlayer(Rockstar.id("penises/check_disable.penis")));
            value.setLastState(value.isSelected());
            value.setCurrentPenis(value.isLastState() ? value.getEnablePenis() : value.getDisablePenis());
            if (value.isLastState()) {
               value.getEnablePenis().playOnce();
            } else {
               value.getDisablePenis().setFrame(0);
               value.getDisablePenis().stop();
            }
         }

         this.initialized = true;
      }

      float x = this.x + 9.0F;
      float y = this.y + 1.0F;
      float width = this.width - 18.0F;
      Font nameFont = Fonts.REGULAR.getFont(8.0F);
      float leftPadding = 10.0F;
      float headerHeight = 19.0F;
      this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
      context.drawFadeoutText(
         nameFont,
         Localizator.translate(this.getSetting().getName()),
         this.x + leftPadding,
         y - 1.0F + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight),
         Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue())),
         0.8F,
         1.0F,
         this.getParent().getWidth() - leftPadding
      );
      context.drawRoundedRect(
         x - 1.0F, y + 17.0F, width + 2.0F, 8 + this.setting.getValues().size() * 12, BorderRadius.all(6.0F), Colors.getBackgroundColor().withAlpha(76.5F)
      );
      float offset = 0.0F;

      for (ModeSetting.Value valuex : this.setting.getValues()) {
         if (!valuex.isHidden()) {
            boolean currentState = valuex.isSelected();
            if (currentState != valuex.isLastState()) {
               if (currentState) {
                  valuex.setCurrentPenis(valuex.getEnablePenis());
               } else {
                  valuex.setCurrentPenis(valuex.getDisablePenis());
               }

               valuex.getCurrentPenis().playOnce();
               valuex.setLastState(currentState);
            }

            valuex.getCurrentPenis().update();
            boolean hover = GuiUtility.isHovered(
               (double)(x - 1.0F), (double)(y + 20.0F + offset), (double)(width + 2.0F), 12.0, context.getMouseX(), context.getMouseY()
            );
            if (hover) {
               CursorUtility.set(CursorType.HAND);
            }

            valuex.getHoverAnimation().update(hover);
            valuex.getActiveAnimation().update(valuex.isSelected());
            context.drawFadeoutText(
               Fonts.REGULAR.getFont(7.0F),
               Localizator.translate(valuex.getName()),
               x + 7.0F,
               y + 24.5F + offset,
               Colors.getTextColor()
                  .withAlpha(255.0F * (0.75F + 0.25F * valuex.getHoverAnimation().getValue() + 0.25F * valuex.getActiveAnimation().getValue())),
               0.8F,
               1.0F,
               width - 12.0F - valuex.getActiveAnimation().getValue() * 10.0F
            );
            if (valuex.getActiveAnimation().getValue() > 0.0F || valuex.getCurrentPenis().isPlaying()) {
               DrawUtility.drawAnimationSprite(
                  context.getMatrices(),
                  valuex.getCurrentPenis().getCurrentSprite(),
                  x + width - 11.0F - valuex.getActiveAnimation().getValue() * 2.0F,
                  y + 24.0F + offset,
                  6.0F,
                  6.0F,
                  Colors.getTextColor().mulAlpha(0.1F + 0.9F * valuex.getActiveAnimation().getValue())
               );
            }

            offset += 12.0F;
         }
      }
   }

   @Override
   public void drawSplit(UIContext context) {
      float separatorHeight = 0.5F;
      context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1F));
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (button == MouseButton.LEFT) {
         float offset = 0.0F;

         for (ModeSetting.Value value : this.setting.getValues()) {
            if (!value.isHidden()) {
               boolean hover = GuiUtility.isHovered(
                  (double)(this.x - 1.0F), (double)(this.y + 20.0F + offset), (double)(this.width - 2.0F), 12.0, mouseX, mouseY
               );
               if (hover) {
                  value.select();
               }

               offset += 12.0F;
            }
         }

         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public float getHeight() {
      return this.height = 31 + this.setting.getValues().size() * 12;
   }
}
