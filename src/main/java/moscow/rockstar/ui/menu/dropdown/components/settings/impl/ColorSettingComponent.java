package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ColorSettingComponent extends MenuSettingComponent<ColorSetting> {
   private ColorPicker picker;

   public ColorSettingComponent(ColorSetting setting, CustomComponent parent) {
      super(setting, parent);
   }

   @Override
   public void onInit() {
      this.width = 13.0F;
      this.height = 8.0F;
      super.onInit();
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
      if (this.isHovered(context.getMouseX(), context.getMouseY())) {
         CursorUtility.set(CursorType.HAND);
      }

      float checkWidth = 13.0F;
      Font nameFont = Fonts.REGULAR.getFont(8.0F);
      float leftPadding = 10.0F;
      float headerHeight = 19.0F;
      context.drawFadeoutText(
         nameFont,
         Localizator.translate(this.setting.getName()),
         this.x + leftPadding,
         this.y + GuiUtility.getMiddleOfBox(nameFont.height(), headerHeight) - 0.5F,
         Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * this.hoverAnimation.getValue())),
         0.7F,
         0.99F,
         this.width - checkWidth - 20.0F
      );
      context.drawRoundedRect(this.x + this.width - leftPadding - 8.0F, this.y + 5.0F, 8.0F, 8.0F, BorderRadius.all(4.5F), Colors.getOutlineColor());
      context.drawRoundedRect(this.x + this.width - leftPadding - 7.0F, this.y + 6.0F, 6.0F, 6.0F, BorderRadius.all(4.5F), this.setting.getColor());
      if (this.picker != null) {
         this.setting.color(this.picker.built());
      }
   }

   @Override
   public void drawSplit(UIContext context) {
      float separatorHeight = 0.5F;
      context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1F));
   }

   @Compile
   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (this.isHovered(mouseX, mouseY) && button == MouseButton.LEFT) {
         if (Rockstar.getInstance().getMenuScreen() instanceof DropDownScreen dropDownScreen) {
            dropDownScreen.getColorPickers()
               .add(
                  this.picker = new ColorPicker(
                     (float)mouseX, (float)mouseY, 6.0F, this.setting.isAlpha(), this.setting.getColor(), Localizator.translate(this.setting.getName())
                  )
               );
         } else if (Rockstar.getInstance().getMenuScreen() instanceof ModernScreen modernScreen) {
            modernScreen.getColorPickers()
               .add(
                  this.picker = new ColorPicker(
                     (float)mouseX, (float)mouseY, 6.0F, this.setting.isAlpha(), this.setting.getColor(), Localizator.translate(this.setting.getName())
                  )
               );
         }
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   @Override
   public float getHeight() {
      return this.height = 18.0F;
   }
}
