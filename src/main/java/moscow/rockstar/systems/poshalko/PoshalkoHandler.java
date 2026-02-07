package moscow.rockstar.systems.poshalko;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.util.Identifier;

public class PoshalkoHandler implements IMinecraft {
   private static final Animation animation = new Animation(1000L, Easing.CUBIC_IN_OUT);
   private static boolean removing = true;
   private static boolean Z_PRESSED = false;
   private static boolean V_PRESSED = false;
   private final EventListener<KeyPressEvent> onKeyPress = event -> {
      int key = event.getKey();
      int action = event.getAction();
      if (key == 90) {
         Z_PRESSED = action != 0;
      } else if (key == 86) {
         V_PRESSED = action != 0;
      }

      if (Z_PRESSED && V_PRESSED) {
         removing = false;
         animation.update(1.0F);
      }
   };
   private final EventListener<PreHudRenderEvent> onPreHudRender = event -> {
      if (animation.getValue() == 1.0 && !removing) {
         removing = true;
      }

      animation.update(removing ? 0.0F : 1.0F);
      if (animation.getValue() != 0.0F || !removing) {
         float textureScale = 200.0F;
         float textureX = (mc.getWindow().getScaledWidth() - textureScale) / 2.0F;
         float textureY = (mc.getWindow().getScaledHeight() - textureScale) / 2.0F;
         Identifier poshalkoTexture = Rockstar.id("icons/poshalko.png");
         event.getContext().drawTexture(poshalkoTexture, textureX, textureY, textureScale, textureScale, Colors.WHITE.withAlpha(255.0F * animation.getValue()));
      }
   };

   public PoshalkoHandler() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }
}
