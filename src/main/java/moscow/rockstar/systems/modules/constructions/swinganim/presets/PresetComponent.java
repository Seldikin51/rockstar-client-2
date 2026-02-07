package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.render.ScissorUtility;

public class PresetComponent extends CustomComponent {
   private final Animation addAnim = new Animation(300L, Easing.BAKEK);
   private final ScrollHandler scrollHandler = new ScrollHandler();
   private final TextField textField;
   private final Animation heightAnim = new Animation(300L, Easing.BAKEK_SMALLER);

   public PresetComponent() {
      this.textField = new TextField(Fonts.REGULAR.getFont(8.0F));
      this.textField.setPreview(Localizator.translate("type_name"));
   }

   @Override
   protected void renderComponent(UIContext context) {
      SwingManager swingManager = Rockstar.getInstance().getSwingManager();
      SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
      List<SwingPresetFile> presets = manager.getSwingPresetFiles();
      float x = this.x + 8.0F;
      float y = this.y - 1.0F;
      float width = this.width - 16.0F;
      this.scrollHandler.update();
      context.drawRoundedRect(
         x - 1.0F, y + 7.0F, width + 2.0F, 8.0F + this.height - 46.0F, BorderRadius.all(6.0F), Colors.getBackgroundColor().withAlpha(76.5F)
      );
      ScissorUtility.push(context.getMatrices(), x - 1.0F, y + 7.5F, width + 2.0F, 7.0F + this.height - 46.0F);
      float offset = 0.0F;

      for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
         float elmtY = (float)(y + 14.0F + offset - this.scrollHandler.getValue());
         boolean hover = GuiUtility.isHovered(x - 1.0F, y + 7.5F, width + 2.0F, 7.0F + this.height - 46.0F, context)
            && GuiUtility.isHovered((double)(x - 1.0F), (double)(elmtY - 4.0F), (double)(width + 2.0F), 12.0, context.getMouseX(), context.getMouseY());
         value.getHoverAnimation().update(hover);
         value.getActiveAnimation().update(Objects.equals(value.getName(), swingManager.getCurrent()));
         context.drawFadeoutText(
            Fonts.REGULAR.getFont(7.0F),
            Localizator.translate(value.getName()),
            x + 7.0F,
            elmtY + 0.5F,
            Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * value.getHoverAnimation().getValue() + 0.25F * value.getActiveAnimation().getValue())),
            0.8F,
            1.0F,
            width - 12.0F - value.getActiveAnimation().getValue() * 10.0F
         );
         if (hover) {
            CursorUtility.set(CursorType.HAND);
         }

         if (value.getActiveAnimation().getValue() >= 0.0F) {
            context.drawTexture(
               Rockstar.id("icons/check.png"),
               x + width - 11.0F - value.getActiveAnimation().getValue() * 2.0F,
               elmtY,
               6.0F,
               6.0F,
               Colors.getTextColor().withAlpha(value.getActiveAnimation().getValue() * 255.0F)
            );
         }

         offset += 12.0F;
      }

      for (SwingPresetFile value : presets) {
         if (!value.getFileName().equals("autosave")) {
            float elmtYx = (float)(y + 14.0F + offset - this.scrollHandler.getValue());
            boolean hoverx = GuiUtility.isHovered(x - 1.0F, y + 7.5F, width + 2.0F, 7.0F + this.height - 46.0F, context)
               && GuiUtility.isHovered((double)(x - 1.0F), (double)(elmtYx - 4.0F), (double)(width + 2.0F), 12.0, context.getMouseX(), context.getMouseY());
            value.getHoverAnimation().update(hoverx);
            value.getActiveAnimation().update(Objects.equals(value.getFileName(), swingManager.getCurrent()));
            context.drawFadeoutText(
               Fonts.REGULAR.getFont(7.0F),
               value.getFileName(),
               x + 7.0F + 10.0F * value.getHoverAnimation().getValue(),
               elmtYx + 0.5F,
               Colors.getTextColor().withAlpha(255.0F * (0.75F + 0.25F * value.getHoverAnimation().getValue() + 0.25F * value.getActiveAnimation().getValue())),
               0.8F,
               1.0F,
               width - 12.0F - value.getActiveAnimation().getValue() * 10.0F - 10.0F * value.getHoverAnimation().getValue()
            );
            if (hoverx) {
               CursorUtility.set(CursorType.HAND);
            }

            if (value.getHoverAnimation().getValue() >= 0.0F) {
               context.drawTexture(
                  Rockstar.id("icons/trash.png"),
                  x + 7.0F * value.getHoverAnimation().getValue(),
                  elmtYx,
                  6.0F,
                  6.0F,
                  Colors.getTextColor().withAlpha(value.getHoverAnimation().getValue() * 255.0F)
               );
            }

            if (value.getActiveAnimation().getValue() >= 0.0F) {
               context.drawTexture(
                  Rockstar.id("icons/check.png"),
                  x + width - 11.0F - value.getActiveAnimation().getValue() * 2.0F,
                  elmtYx,
                  6.0F,
                  6.0F,
                  Colors.getTextColor().withAlpha(value.getActiveAnimation().getValue() * 255.0F)
               );
            }

            offset += 12.0F;
         }
      }

      ScissorUtility.pop();
      context.drawRoundedRect(x - 1.0F, y + this.height - 25.0F, width + 2.0F, 20.0F, BorderRadius.all(6.0F), Colors.getBackgroundColor().mulAlpha(0.3F));
      context.drawTexture(
         Rockstar.id("icons/add.png"),
         x + width - 2.0F * this.addAnim.getValue() - 10.0F,
         y + this.height - 25.0F + 6.0F,
         8.0F,
         8.0F,
         Colors.getTextColor().mulAlpha(this.addAnim.getValue())
      );
      this.textField.set(x - 1.0F, y + this.height - 25.0F, width + 2.0F - 12.0F, 20.0F);
      this.textField.setAlpha(1.0F);
      this.textField.render(context);
      this.addAnim.update(!this.textField.getBuiltText().isBlank());
      if (GuiUtility.isHovered(x + width - 2.0F - 10.0F, y + this.height - 25.0F + 6.0F, 8.0, 8.0, context) && this.addAnim.getValue() > 0.0F) {
         CursorUtility.set(CursorType.HAND);
      }

      this.scrollHandler.setMax(-offset + this.height - 20.0F - 25.0F);
   }

   @Override
   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
      this.textField.onMouseClicked(mouseX, mouseY, button);
      SwingManager swingManager = Rockstar.getInstance().getSwingManager();
      SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
      List<SwingPresetFile> presets = manager.getSwingPresetFiles();
      float x = this.x + 8.0F;
      float y = this.y - 1.0F;
      float width = this.width - 16.0F;
      float offset = 0.0F;

      for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
         float elmtY = (float)(y + 14.0F + offset - this.scrollHandler.getValue());
         boolean hover = GuiUtility.isHovered(
               (double)(x - 1.0F), (double)(y + 7.5F), (double)(width + 2.0F), (double)(7.0F + this.height - 46.0F), mouseX, mouseY
            )
            && GuiUtility.isHovered((double)(x - 1.0F), (double)(elmtY - 4.0F), (double)(width + 2.0F), 12.0, mouseX, mouseY);
         if (hover && button == MouseButton.LEFT) {
            swingManager.getBezier().start(value.getBezierStart()).end(value.getBezierEnd());
            swingManager.getBack().enabled(value.isSwingBack());
            swingManager.getSpeed().setCurrentValue(value.getSpeed());
            SwingPhase start = swingManager.getStartPhase();
            start.getAnchorX().setCurrentValue(value.getFrom().anchorX());
            start.getAnchorY().setCurrentValue(value.getFrom().anchorY());
            start.getAnchorZ().setCurrentValue(value.getFrom().anchorZ());
            start.getMoveX().setCurrentValue(value.getFrom().moveX());
            start.getMoveY().setCurrentValue(value.getFrom().moveY());
            start.getMoveZ().setCurrentValue(value.getFrom().moveZ());
            start.getRotateX().setCurrentValue(value.getFrom().rotateX());
            start.getRotateY().setCurrentValue(value.getFrom().rotateY());
            start.getRotateZ().setCurrentValue(value.getFrom().rotateZ());
            SwingPhase end = swingManager.getEndPhase();
            end.getAnchorX().setCurrentValue(value.getTo().anchorX());
            end.getAnchorY().setCurrentValue(value.getTo().anchorY());
            end.getAnchorZ().setCurrentValue(value.getTo().anchorZ());
            end.getMoveX().setCurrentValue(value.getTo().moveX());
            end.getMoveY().setCurrentValue(value.getTo().moveY());
            end.getMoveZ().setCurrentValue(value.getTo().moveZ());
            end.getRotateX().setCurrentValue(value.getTo().rotateX());
            end.getRotateY().setCurrentValue(value.getTo().rotateY());
            end.getRotateZ().setCurrentValue(value.getTo().rotateZ());
            manager.setCurrent(null);
            swingManager.setCurrent(value.getName());
         }

         offset += 12.0F;
      }

      for (SwingPresetFile value : new ArrayList<>(presets)) {
         if (!value.getFileName().equals("autosave")) {
            float elmtY = (float)(y + 14.0F + offset - this.scrollHandler.getValue());
            boolean hover = GuiUtility.isHovered(
                  (double)(x - 1.0F), (double)(y + 7.5F), (double)(width + 2.0F), (double)(7.0F + this.height - 46.0F), mouseX, mouseY
               )
               && GuiUtility.isHovered((double)(x - 1.0F), (double)(elmtY - 4.0F), (double)(width + 2.0F), 12.0, mouseX, mouseY);
            if (hover && GuiUtility.isHovered((double)(x + 7.0F), (double)elmtY, 6.0, 6.0, mouseX, mouseY) && button == MouseButton.LEFT) {
               value.delete();
            } else if (hover && button == MouseButton.LEFT) {
               if (manager.getCurrent() != null) {
                  manager.getCurrent().save();
               }

               swingManager.setCurrent(value.getFileName());
               value.load();
            }

            offset += 12.0F;
         }
      }

      if (GuiUtility.isHovered((double)(x + width - 2.0F - 10.0F), (double)(y + this.height - 25.0F + 6.0F), 8.0, 8.0, mouseX, mouseY)
         && !this.textField.getBuiltText().isBlank()) {
         this.create();
      }
   }

   private void create() {
      SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
      SwingManager swingManager = Rockstar.getInstance().getSwingManager();
      swingManager.getBezier().start(0.5F, 1.0F).end(0.5F, 0.0F);
      swingManager.getBack().enabled(true);
      swingManager.getSpeed().setCurrentValue(2.0F);

      for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
         if (setting instanceof SwingPhase.PhaseSlider slider) {
            slider.setCurrentValue(0.0F);
         }
      }

      for (Setting settingx : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
         if (settingx instanceof SwingPhase.PhaseSlider slider) {
            slider.setCurrentValue(0.0F);
         }
      }

      swingManager.setCurrent(this.textField.getBuiltText());
      manager.createPreset(this.textField.getBuiltText());
      manager.getPreset(this.textField.getBuiltText()).load();
      this.textField.clear();
   }

   @Override
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
      this.textField.onMouseReleased(mouseX, mouseY, button);
   }

   @Override
   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 257 && !this.textField.getBuiltText().isBlank()) {
         this.create();
      } else {
         this.textField.onKeyPressed(keyCode, scanCode, modifiers);
         if (this.isHovered(GuiUtility.getMouse().getX(), GuiUtility.getMouse().getY())) {
            this.scrollHandler.onKeyPressed(keyCode);
         }
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      this.textField.charTyped(chr, modifiers);
      return super.charTyped(chr, modifiers);
   }

   @Override
   public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.scrollHandler.scroll(verticalAmount);
   }

   @Override
   public float getHeight() {
      SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
      List<SwingPresetFile> presets = manager.getSwingPresetFiles();
      return this.height = this.heightAnim
         .update(Math.min(presets.size() * 12 + Rockstar.getInstance().getSwingManager().getPresets().size() * 12 - 12, 182) + 46);
   }
}
