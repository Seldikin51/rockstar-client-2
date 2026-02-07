package moscow.rockstar.ui.components.popup.list;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.components.popup.PopupComponent;
import moscow.rockstar.utility.colors.Colors;

public class Separator extends PopupComponent {
   @Override
   protected void renderComponent(UIContext context) {
      context.drawRect(this.x, this.y, this.width, this.height, Colors.getSeparatorColor());
   }

   @Override
   public float getHeight() {
      return this.height = 4.0F;
   }
}
