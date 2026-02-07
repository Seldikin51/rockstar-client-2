package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin implements IMinecraft {
   @Inject(method = "onKey", at = @At("HEAD"))
   public void triggerKeyEvent(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (key != -1) {
         Rockstar.getInstance().getEventManager().triggerEvent(new KeyPressEvent(action, key));
         if (mc.currentScreen == null) {
            if (key == 46 && action == 1) {
               mc.setScreen(new ChatScreen(""));
            }
         }
      }
   }
}
