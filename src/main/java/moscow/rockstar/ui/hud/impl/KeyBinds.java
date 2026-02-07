package moscow.rockstar.ui.hud.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.VertexFormats;

public class KeyBinds extends HudList {
   int lastSize = -1;
   private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");

   public KeyBinds() {
      super("hud.keybinds", "icons/hud/keybinds.png");
   }

   @Override
   public void update(UIContext context) {
      this.width = 92.0F;
      this.height = 18.0F;

      for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
         boolean forward = module.isEnabled() && module.getKey() != -1;
         module.getKeybindsAnimation().update(forward);
         module.getKeybindsAnimation().setEasing(Easing.BAKEK);
         if (module.getKeybindsAnimation().getValue() > 0.0F) {
            this.width = Math.max(Fonts.REGULAR.getFont(7.0F).width(module.getName() + TextUtility.getKeyName(module.getKey())) + 20.0F, this.width);
         }

         this.height = this.height + 18.0F * module.getKeybindsAnimation().getValue();
      }

      if (this.height > 18.0F) {
         this.height += 5.0F;
      }

      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      Font font = Fonts.REGULAR.getFont(7.0F);
      List<Module> modules = new ArrayList<>(Rockstar.getInstance().getModuleManager().getModules());
      if (this.lastSize == modules.size()) {
         modules.sort(Comparator.comparingDouble(m -> font.width(m.getName())));
         this.lastSize = modules.size();
      }

      float offset = 22.0F;
      super.renderComponent(context);

      for (Module module : modules) {
         Animation anim = module.getKeybindsAnimation();
         if (anim.getValue() != 0.0F && offset != 22.0F) {
            float off = -4.5F + 4.5F * anim.getValue();
            context.drawRect(this.x, this.y + offset + off, this.width, 0.5F, Colors.getTextColor().withAlpha(5.1F));
            offset += 18.0F * anim.getValue();
         }
      }

      Batching fontBatching = new FontBatching(VertexFormats.POSITION_TEXTURE_COLOR, font.getFont());
      offset = 22.0F;

      for (Module modulex : modules) {
         Animation anim = modulex.getKeybindsAnimation();
         if (anim.getValue() != 0.0F) {
            float off = -4.5F + 4.5F * anim.getValue();
            context.drawText(
               font,
               modulex.getName(),
               this.x + 7.0F * anim.getValue(),
               this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0F),
               Colors.getTextColor().withAlpha(255.0F * anim.getValue())
            );
            context.drawRightText(
               font,
               TextUtility.getKeyName(modulex.getKey()),
               this.x + this.width - 7.0F * anim.getValue(),
               this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0F),
               Colors.getTextColor().withAlpha(255.0F * anim.getValue())
            );
            offset += 18.0F * anim.getValue();
         }
      }

      fontBatching.draw();
   }

   @Override
   public boolean show() {
      return !Rockstar.getInstance().getModuleManager().getModules().stream().filter(module -> module.isEnabled() && module.getKey() != -1).toList().isEmpty()
         || mc.currentScreen instanceof ChatScreen
         || this.alwaysDisplay.isEnabled();
   }
}
