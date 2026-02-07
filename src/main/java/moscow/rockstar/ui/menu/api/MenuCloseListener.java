package moscow.rockstar.ui.menu.api;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.MinecraftClient;

public class MenuCloseListener implements IMinecraft {
   private final EventListener<HudRenderEvent> onHudRender = event -> {
      MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
      if (mc.currentScreen == null) {
         if (Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).getModern().isSelected()) {
            if (!(menuScreen instanceof ModernScreen)) {
               Rockstar.getInstance().setMenuScreen(new ModernScreen());
            }
         } else if (!(menuScreen instanceof DropDownScreen)) {
            Rockstar.getInstance().setMenuScreen(new DropDownScreen());
         }
      }

      if (menuScreen != null) {
         menuScreen.getMenuAnimation().update(menuScreen.isClosing() ? 0.0F : 1.0F);
         if (!(mc.currentScreen instanceof MenuScreen) && Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).isEnabled()) {
            Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).setEnabled(false);
         }

         if (menuScreen.getMenuAnimation().getValue() > 0.1F && !(mc.currentScreen instanceof MenuScreen) && menuScreen.isClosing()) {
            UIContext context = UIContext.of(event.getContext(), -1, -1, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            menuScreen.render(context);
         }
      }
   };

   public MenuCloseListener() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }
}
