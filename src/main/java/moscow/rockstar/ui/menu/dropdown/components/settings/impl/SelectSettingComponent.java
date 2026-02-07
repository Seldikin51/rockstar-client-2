package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.penis.PenisPlayer;
import moscow.rockstar.utility.time.Timer;

public class SelectSettingComponent extends MenuSettingComponent<SelectSetting> {
   private AnimatedNumber numberAnim;
   private SelectSetting.Value dragging;
   private final Timer sortTimer = new Timer();
   private boolean initialized;

   public SelectSettingComponent(SelectSetting setting, CustomComponent parent) {
      super(setting, parent);
      List<SelectSetting.Value> enabled = new ArrayList<>();
      setting.getValues().forEach(sel -> {
         if (sel.isSelected()) {
            enabled.add(sel);
         }
      });
      setting.getSelectedValues().clear();
      setting.getSelectedValues().addAll(enabled);
   }

   @Override
   protected void renderComponent(UIContext context) {
      if (!this.initialized) {
         for (SelectSetting.Value value : this.setting.getValues()) {
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
      float nameHeight = Fonts.REGULAR.getFont(7.0F).height();
      float headerHeight = 19.0F;
      this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
      String rightText = String.format(" %s", Localizator.translate("setting_of") + " " + this.setting.getValues().size());
      if (this.numberAnim == null) {
         this.numberAnim = new AnimatedNumber(Fonts.MEDIUM.getFont(7.0F), 5.0F, 500L, Easing.BAKEK);
      }

      context.drawFadeoutText(
         nameFont,
         Localizator.translate(this.setting.getName()),
         this.x + leftPadding,
         y - 1.0F + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight),
         Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue())),
         0.8F,
         1.0F,
         this.getParent().getWidth() - leftPadding - Fonts.REGULAR.getFont(7.0F).width(rightText) - this.numberAnim.getWidth() - 10.0F
      );
      this.numberAnim.settings(false, Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue())));
      this.numberAnim.update(this.setting.getSelectedValues().size());
      this.numberAnim
         .pos(
            x + width - Fonts.REGULAR.getFont(7.0F).width(rightText) - this.numberAnim.getWidth(),
            y - 1.0F + GuiUtility.getMiddleOfBox(nameHeight, headerHeight)
         );
      this.numberAnim.render(context);
      context.drawRightText(
         Fonts.REGULAR.getFont(7.0F),
         rightText,
         x + width,
         y - 1.0F + GuiUtility.getMiddleOfBox(nameHeight, headerHeight),
         Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue()))
      );
      context.drawRoundedRect(
         x - 1.0F, y + 17.0F, width + 2.0F, 8 + this.setting.getValues().size() * 12, BorderRadius.all(6.0F), Colors.getBackgroundColor().withAlpha(76.5F)
      );
      float offset = 0.0F;

      for (SelectSetting.Value valuex : this.setting.getValues()) {
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
            float elmtY = this.dragging == valuex
               ? Math.clamp((float)(context.getMouseY() - 2), y + 18.0F, y + 20.0F + this.setting.getValues().size() * 12)
               : y + 24.0F + offset;
            boolean hover = GuiUtility.isHovered(
               (double)(x - 1.0F), (double)(elmtY - 4.0F), (double)(width + 2.0F), 12.0, context.getMouseX(), context.getMouseY()
            );
            valuex.getYAnim().setEasing(Easing.BAKEK_SMALLER);
            valuex.getYAnim().update(elmtY - y);
            valuex.setYFactor(elmtY);
            if (hover && this.dragging != valuex && !valuex.isAlwaysEnabled()) {
               CursorUtility.set(CursorType.HAND);
            }

            valuex.getHoverAnimation().update(hover);
            valuex.getActiveAnimation().update(valuex.isSelected());
            if (this.setting.isDraggable()) {
               context.drawTexture(Rockstar.id("icons/hud/drag.png"), x + 7.0F, y + valuex.getYAnim().getValue(), 6.0F, 6.0F, Colors.getTextColor());
            }

            if (GuiUtility.isHovered(x, elmtY - 2.0F, 17.0, 10.0, context) || valuex == this.dragging) {
               CursorUtility.set(CursorType.ARROW_VERTICAL);
            }

            context.drawFadeoutText(
               Fonts.REGULAR.getFont(7.0F),
               Localizator.translate(valuex.getName()),
               x + (this.setting.isDraggable() ? 18 : 7),
               y + valuex.getYAnim().getValue() + 0.5F,
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
                  y + valuex.getYAnim().getValue(),
                  6.0F,
                  6.0F,
                  Colors.getTextColor().mulAlpha(0.1F + 0.9F * valuex.getActiveAnimation().getValue())
               );
            }

            offset += 12.0F;
         }
      }

      if (this.sortTimer.finished(100L)) {
         this.setting.getValues().sort(Comparator.comparingDouble(SelectSetting.Value::getYFactor));
         this.sortTimer.reset();
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
         float x = this.x + 9.0F;
         float y = this.y + 1.0F;
         float offset = 0.0F;

         for (SelectSetting.Value value : this.setting.getValues()) {
            if (!value.isHidden()) {
               boolean hover = GuiUtility.isHovered((double)(x - 1.0F), (double)(y + 20.0F + offset), (double)(this.width - 2.0F), 12.0, mouseX, mouseY);
               if (GuiUtility.isHovered((double)x, (double)(y + 22.0F + offset), 17.0, 10.0, mouseX, mouseY) && this.setting.isDraggable()) {
                  this.dragging = value;
               } else if (hover) {
                  value.toggle();
               }

               offset += 12.0F;
            }
         }

         super.onMouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      this.dragging = null;
      super.onMouseReleased(mouseX, mouseY, button);
   }

   @Override
   public float getHeight() {
      return this.height = 31 + this.setting.getValues().size() * 12;
   }
}
