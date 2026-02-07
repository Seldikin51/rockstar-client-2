package moscow.rockstar.ui.menu.dropdown.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.sounds.ClientSounds;

public class MenuPanel extends CustomComponent implements IScaledResolution {
   private final MenuCategory category;
   private final Animation swapping = new Animation(500L, Easing.BAKEK_PAGES);
   private final Animation sizing = new Animation(500L, Easing.BAKEK_SMALLER);
   private final List<ModuleComponent> moduleComponents = new ArrayList<>();
   private final ScrollHandler modulesScroll = new ScrollHandler();
   private final ScrollHandler settingsScroll = new ScrollHandler();
   private Font titleFont;
   private ModuleComponent lastSelected;
   private ModuleComponent selectedModuleComponent;

   public MenuPanel(MenuCategory category) {
      this.category = category;
   }

   @Override
   public void onInit() {
      for (Module module : Rockstar.getInstance()
         .getModuleManager()
         .getModules()
         .stream()
         .sorted(Comparator.comparing(Module::getName))
         .filter(modulex -> modulex.getCategory().equals(this.category.getCategory()))
         .toList()) {
         ModuleComponent component = new ModuleComponent(module, this);
         component.setWidth(this.width);
         component.setHeight(20.0F);
         this.moduleComponents.add(component);
         component.onInit();
      }

      this.titleFont = Fonts.SEMIBOLD.getFont(9.0F);
      this.modulesScroll.reset();
      this.settingsScroll.reset();
      super.onInit();
   }

   @Override
   public void update(UIContext context) {
      super.update(context);
   }

   private void updateScale(UIContext context) {
      if (Interface.glassSelected()) {
         this.sizing.setEasing(Easing.BAKEK_MANY);
      } else {
         this.sizing.setEasing(Easing.BAKEK_SMALLER);
      }

      this.sizing.setDuration(500L);
      this.sizing
         .update(
            Rockstar.getInstance().getMenuScreen().isClosing()
               ? 2.0F
               : (
                  Math.abs(sr.getScaledWidth() / 2.0F - this.x) / 1500.0F * 3.0F < Rockstar.getInstance().getMenuScreen().getMenuAnimation().getValue()
                     ? 1.0F
                     : 0.0F
               )
         );
   }

   public void scale(UIContext context) {
      if (Interface.glassSelected()) {
         RenderUtility.scale(context.getMatrices(), sr.getScaledWidth() / 2.0F, this.y + this.height / 2.0F, 2.0F - this.sizing.getValue());
      } else {
         RenderUtility.scale(context.getMatrices(), this.x + this.width / 2.0F, this.y + this.height / 2.0F, 2.0F - this.sizing.getValue());
      }
   }

   public void renderBackground(UIContext context) {
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing()
         ? Rockstar.getInstance().getMenuScreen().getMenuAnimation().getValue()
         : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      this.scale(context);
      context.drawRoundedRect(
         this.x + 1.0F,
         this.y + 1.0F,
         this.width - 2.0F,
         this.height - 2.0F,
         BorderRadius.all(10.0F),
         Colors.getBackgroundColor().withAlpha(255.0F * (Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK ? 0.55F : 0.7F))
      );
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void renderShadow(UIContext context) {
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing()
         ? Rockstar.getInstance().getMenuScreen().getMenuAnimation().getValue()
         : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      this.scale(context);
      context.drawShadow(this.x, this.y, this.width, this.height, 25.0F, BorderRadius.all(10.0F), ColorRGBA.BLACK.withAlpha(51.0F));
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void renderBlur(UIContext context) {
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      this.scale(context);
      if (Interface.showMinimalizm()) {
         context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0F, 10.0F, BorderRadius.all(10.0F), ColorRGBA.WHITE.withAlpha(255.0F * alpha));
      }

      if (Interface.showGlass()) {
         context.drawLiquidGlass(this.x, this.y, this.width, this.height, 10.0F, 0.08F, BorderRadius.all(10.0F), ColorRGBA.WHITE.withAlpha(255.0F * alpha));
      }

      RenderUtility.end(context.getMatrices());
   }

   public void push(UIContext context) {
      float headerHeight = 24.0F;
      float separatorHeight = 4.0F;
      float offset = Interface.glass() * 2.0F;
      if (this.selectedModuleComponent != null) {
         ScissorUtility.push(
            context.getMatrices(),
            this.x + offset,
            this.y + headerHeight * 2.0F + separatorHeight + offset,
            this.width - offset * 2.0F,
            this.height - headerHeight * 2.0F - separatorHeight - 0.5F - offset * 2.0F
         );
      } else {
         ScissorUtility.push(
            context.getMatrices(),
            this.x + offset,
            this.y + headerHeight + separatorHeight + offset,
            this.width - offset * 2.0F,
            this.height - headerHeight - separatorHeight - 0.5F - offset * 2.0F
         );
      }
   }

   @Override
   protected void renderComponent(UIContext context) {
      this.modulesScroll.update();
      this.settingsScroll.update();
      float headerHeight = 24.0F;
      this.updateScale(context);
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      this.scale(context);
      boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      context.drawSquircle(
         this.x,
         this.y,
         this.width,
         this.height,
         10.0F,
         BorderRadius.all(10.0F),
         Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.9F - 0.7F * Interface.glass() : 0.7F))
      );
      float separatorHeight = 4.0F;
      float titleLeftPadding = 10.0F;
      float titleHeight = this.titleFont.height();
      context.drawText(
         this.titleFont,
         this.category.getName(),
         this.x + titleLeftPadding,
         this.y + GuiUtility.getMiddleOfBox(titleHeight, headerHeight) + 0.5F,
         Colors.getTextColor()
      );
      if (Interface.showMinimalizm()) {
         context.drawRect(
            this.x,
            this.y + headerHeight,
            this.width,
            separatorHeight,
            Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm())
         );
      }

      if (this.selectedModuleComponent != null) {
         this.lastSelected = this.selectedModuleComponent;
      }

      this.swapping.update(this.selectedModuleComponent != null ? 1.0F : 0.0F);
      if (this.swapping.getValue() != 1.0F) {
         float x = this.x + -this.width * this.swapping.getValue();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * (1.0F - this.swapping.getValue()));
         ScissorUtility.push(
            context.getMatrices(), this.x, this.y + headerHeight + separatorHeight, this.width, this.height - headerHeight - separatorHeight - 0.5F
         );
         float offset = 0.0F;

         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.searchCheck(moduleComponent)) {
               moduleComponent.setX(x);
               moduleComponent.setY((float)(this.y + offset - this.modulesScroll.getValue()) + headerHeight + separatorHeight - 1.0F);
               moduleComponent.render(context);
               offset += moduleComponent.getHeight();
               this.modulesScroll.setMax(-offset + this.height - headerHeight - separatorHeight);
            }
         }

         ScissorUtility.pop();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      }

      if (this.swapping.getValue() != 0.0F) {
         float x = this.x + this.width * (1.0F - this.swapping.getValue());
         float y = this.y + headerHeight + separatorHeight;
         float leftPadding = 6.0F;
         float arrowIconSize = 8.0F;
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha * this.swapping.getValue());
         ScissorUtility.push(context.getMatrices(), this.x, this.y, this.width, this.height);
         if (GuiUtility.isHovered((double)x, (double)(this.y + 28.0F), (double)this.width, 20.0, context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
         }

         context.drawTexture(
            Rockstar.id("icons/arrow.png"),
            x + leftPadding,
            y + GuiUtility.getMiddleOfBox(arrowIconSize, headerHeight) - 2.0F,
            arrowIconSize,
            arrowIconSize,
            Colors.getTextColor()
         );
         context.drawText(
            Fonts.REGULAR.getFont(8.0F),
            this.lastSelected.getModule().getName(),
            x + arrowIconSize + 8.0F,
            y + GuiUtility.getMiddleOfBox(arrowIconSize, headerHeight) - 1.0F,
            Colors.getTextColor().withAlpha(255.0F)
         );
         if (Interface.showMinimalizm()) {
            context.drawRect(
               x,
               y + headerHeight - separatorHeight,
               this.width,
               separatorHeight,
               Colors.getSeparatorColor().withAlpha(Colors.getSeparatorColor().getAlpha() * Interface.minimalizm())
            );
         }

         ScissorUtility.pop();
         ScissorUtility.push(
            context.getMatrices(), this.x, y + headerHeight, this.width, this.height - headerHeight * 2.0F - separatorHeight - 0.5F - Interface.glass() * 5.0F
         );
         float settingsY = y + headerHeight;
         float offset = 0.0F;

         for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, settingComponent.getOpacity() * this.swapping.getValue());
            settingComponent.getVisibilityAnimation().update(settingComponent.getSetting().isVisible() ? 1.0F : 0.0F);
            settingComponent.setX(x);
            settingComponent.setY((float)(settingsY + offset - this.settingsScroll.getValue()));
            settingComponent.setWidth(this.width);
            context.pushMatrix();
            context.getMatrices().translate(0.0F, (-settingComponent.getHeight() + settingComponent.getHeight() * settingComponent.getOpacity()) / 2.0F, 0.0F);
            settingComponent.render(context);
            context.popMatrix();
            offset += settingComponent.getHeight() * settingComponent.getOpacity();
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
         this.settingsScroll.setMax(-offset + this.height - headerHeight * 2.0F - separatorHeight - (Interface.glassSelected() ? 5 : 0));
         ScissorUtility.pop();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      }

      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void drawRegular8(UIContext context) {
      float headerHeight = 24.0F;
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      float separatorHeight = 4.0F;
      if (this.selectedModuleComponent != null) {
         this.lastSelected = this.selectedModuleComponent;
      }

      if (this.swapping.getValue() != 1.0F) {
         float x = this.x + -this.width * this.swapping.getValue();

         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.searchCheck(moduleComponent)
               && (
                  GuiUtility.isHovered(
                        (double)x, (double)this.y, (double)this.width, (double)this.height, (double)moduleComponent.getX(), (double)moduleComponent.getY()
                     )
                     || GuiUtility.isHovered(
                        (double)x,
                        (double)this.y,
                        (double)this.width,
                        (double)this.height,
                        (double)moduleComponent.getX(),
                        (double)(moduleComponent.getY() + moduleComponent.getHeight())
                     )
               )) {
               moduleComponent.drawRegular8(context);
            }
         }
      }

      if (this.swapping.getValue() != 0.0F) {
         float x = this.x + this.width * (1.0F - this.swapping.getValue());
         float y = this.y + headerHeight + separatorHeight;

         for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()
               && (
                  GuiUtility.isHovered(
                        (double)x, (double)y, (double)this.width, (double)this.height, (double)settingComponent.getX(), (double)settingComponent.getY()
                     )
                     || GuiUtility.isHovered(
                        (double)x,
                        (double)y,
                        (double)this.width,
                        (double)this.height,
                        (double)settingComponent.getX(),
                        (double)(settingComponent.getY() + settingComponent.getHeight())
                     )
               )) {
               settingComponent.drawRegular8(context);
            }
         }
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void drawIcons(UIContext context) {
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      if (this.selectedModuleComponent != null) {
         this.lastSelected = this.selectedModuleComponent;
      }

      if (this.swapping.getValue() != 1.0F) {
         float x = this.x + -this.width * this.swapping.getValue();

         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.searchCheck(moduleComponent)
               && (
                  GuiUtility.isHovered(
                        (double)x, (double)this.y, (double)this.width, (double)this.height, (double)moduleComponent.getX(), (double)moduleComponent.getY()
                     )
                     || GuiUtility.isHovered(
                        (double)x,
                        (double)this.y,
                        (double)this.width,
                        (double)this.height,
                        (double)moduleComponent.getX(),
                        (double)(moduleComponent.getY() + moduleComponent.getHeight())
                     )
               )) {
               moduleComponent.drawIcons(context);
            }
         }
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void drawType(UIContext context) {
      float headerHeight = 24.0F;
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      this.scale(context);
      float iconScale = 8.0F;
      float rightPadding = 10.0F;
      context.drawSprite(
         this.category.getMenuSprite(),
         this.x + this.width - rightPadding - iconScale,
         this.y + GuiUtility.getMiddleOfBox(iconScale, headerHeight) + 0.5F,
         iconScale,
         iconScale,
         Colors.getTextColor().withAlpha(255.0F * alpha)
      );
      RenderUtility.end(context.getMatrices());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void drawSplit(UIContext context) {
      float headerHeight = 24.0F;
      float alpha = Rockstar.getInstance().getMenuScreen().isClosing() ? 2.0F - this.sizing.getValue() : this.sizing.getValue();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      float separatorHeight = 4.0F;
      if (this.selectedModuleComponent != null) {
         this.lastSelected = this.selectedModuleComponent;
      }

      if (this.swapping.getValue() != 1.0F) {
         float x = this.x + -this.width * this.swapping.getValue();

         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.searchCheck(moduleComponent)
               && (
                  GuiUtility.isHovered(
                        (double)x, (double)this.y, (double)this.width, (double)this.height, (double)moduleComponent.getX(), (double)moduleComponent.getY()
                     )
                     || GuiUtility.isHovered(
                        (double)x,
                        (double)this.y,
                        (double)this.width,
                        (double)this.height,
                        (double)moduleComponent.getX(),
                        (double)(moduleComponent.getY() + moduleComponent.getHeight())
                     )
               )) {
               moduleComponent.drawSplit(context);
            }
         }
      }

      if (this.swapping.getValue() != 0.0F) {
         float x = this.x + this.width * (1.0F - this.swapping.getValue());
         float y = this.y + headerHeight + separatorHeight;

         for (MenuSettingComponent<?> settingComponent : this.lastSelected.getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()
               && (
                  GuiUtility.isHovered(
                        (double)x, (double)y, (double)this.width, (double)this.height, (double)settingComponent.getX(), (double)settingComponent.getY()
                     )
                     || GuiUtility.isHovered(
                        (double)x,
                        (double)y,
                        (double)this.width,
                        (double)this.height,
                        (double)settingComponent.getX(),
                        (double)(settingComponent.getY() + settingComponent.getHeight())
                     )
               )) {
               settingComponent.drawSplit(context);
            }
         }
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      if (this.selectedModuleComponent != null) {
         if (GuiUtility.isHovered((double)this.x, (double)(this.y + 52.0F), (double)this.width, (double)(this.height - 52.0F), mouseX, mouseY)) {
            for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
               if (settingComponent.getSetting().isVisible()) {
                  settingComponent.onMouseClicked(mouseX, mouseY, button);
               }
            }
         }

         float y = this.y + 28.0F;
         if (GuiUtility.isHovered((double)this.x, (double)y, (double)this.width, 20.0, mouseX, mouseY) && button == MouseButton.LEFT) {
            this.selectedModuleComponent = null;
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
               ClientSounds.CLICKGUI_OPEN.play(0.8F, 1.2F);
            }
         }
      } else if (GuiUtility.isHovered((double)this.x, (double)(this.y + 28.0F), (double)this.width, (double)(this.height - 28.0F), mouseX, mouseY)) {
         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.searchCheck(moduleComponent)) {
               moduleComponent.onMouseClicked(mouseX, mouseY, button);
            }
         }
      }

      super.onMouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      if (this.selectedModuleComponent != null) {
         for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()) {
               settingComponent.onMouseReleased(mouseX, mouseY, button);
            }
         }
      } else {
         for (ModuleComponent moduleComponent : this.moduleComponents) {
            moduleComponent.onMouseReleased(mouseX, mouseY, button);
         }
      }

      super.onMouseReleased(mouseX, mouseY, button);
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.isHovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
         if (this.selectedModuleComponent != null) {
            this.settingsScroll.onKeyPressed(keyCode);
         } else {
            this.modulesScroll.onKeyPressed(keyCode);
         }
      }

      if (this.selectedModuleComponent != null) {
         for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()) {
               settingComponent.onKeyPressed(keyCode, scanCode, modifiers);
            }
         }
      } else {
         for (ModuleComponent moduleComponent : this.moduleComponents) {
            moduleComponent.onKeyPressed(keyCode, scanCode, modifiers);
         }
      }

      super.onKeyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (this.selectedModuleComponent != null) {
         for (MenuSettingComponent<?> settingComponent : this.selectedModuleComponent.getSettingComponents()) {
            if (settingComponent.getSetting().isVisible()) {
               settingComponent.charTyped(chr, modifiers);
            }
         }
      }

      return super.charTyped(chr, modifiers);
   }

   private boolean searchCheck(ModuleComponent component) {
      if (!(Rockstar.getInstance().getMenuScreen() instanceof DropDownScreen dropDownScreen)) {
         return true;
      } else {
         TextField search = dropDownScreen.getSearchField();
         return search != null
            && !search.getBuiltText().isBlank()
            && !component.getModule().getName().toLowerCase().contains(search.getBuiltText().toLowerCase())
            && !component.getModule().getName().replace(" ", "").toLowerCase().contains(search.getBuiltText().toLowerCase());
      }
   }

   @Override
   public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.isHovered(mouseX, mouseY)) {
         if (this.selectedModuleComponent != null) {
            this.settingsScroll.scroll(verticalAmount);
         } else {
            this.modulesScroll.scroll(verticalAmount);
         }
      }
   }

   @Generated
   public MenuCategory getCategory() {
      return this.category;
   }

   @Generated
   public Animation getSwapping() {
      return this.swapping;
   }

   @Generated
   public Animation getSizing() {
      return this.sizing;
   }

   @Generated
   public List<ModuleComponent> getModuleComponents() {
      return this.moduleComponents;
   }

   @Generated
   public ScrollHandler getModulesScroll() {
      return this.modulesScroll;
   }

   @Generated
   public ScrollHandler getSettingsScroll() {
      return this.settingsScroll;
   }

   @Generated
   public Font getTitleFont() {
      return this.titleFont;
   }

   @Generated
   public ModuleComponent getLastSelected() {
      return this.lastSelected;
   }

   @Generated
   public ModuleComponent getSelectedModuleComponent() {
      return this.selectedModuleComponent;
   }

   @Generated
   public void setTitleFont(Font titleFont) {
      this.titleFont = titleFont;
   }

   @Generated
   public void setLastSelected(ModuleComponent lastSelected) {
      this.lastSelected = lastSelected;
   }

   @Generated
   public void setSelectedModuleComponent(ModuleComponent selectedModuleComponent) {
      this.selectedModuleComponent = selectedModuleComponent;
   }
}
