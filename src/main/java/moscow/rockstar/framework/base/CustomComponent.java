package moscow.rockstar.framework.base;

import lombok.Generated;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;

public abstract class CustomComponent implements IMinecraft {
   protected float x;
   protected float y;
   protected float width;
   protected float height;

   protected CustomComponent(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   protected CustomComponent() {
      this(0.0F, 0.0F, 0.0F, 0.0F);
   }

   public void render(UIContext context) {
      this.update(context);
      this.renderComponent(context);
   }

   protected abstract void renderComponent(UIContext var1);

   public void onInit() {
   }

   public void update(UIContext context) {
   }

   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
   }

   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
   }

   public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
   }

   public boolean charTyped(char chr, int modifiers) {
      return false;
   }

   public void onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
   }

   public void pos(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public void set(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public boolean isHovered(float mouseX, float mouseY) {
      return GuiUtility.isHovered((double)this.x, (double)this.y, (double)this.width, (double)this.height, (double)mouseX, (double)mouseY);
   }

   public boolean isHovered(double mouseX, double mouseY) {
      return GuiUtility.isHovered((double)this.x, (double)this.y, (double)this.width, (double)this.height, mouseX, mouseY);
   }

   public boolean isHovered(UIContext context) {
      return this.isHovered((float)context.getMouseX(), (float)context.getMouseY());
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public void setX(float x) {
      this.x = x;
   }

   @Generated
   public void setY(float y) {
      this.y = y;
   }

   @Generated
   public void setWidth(float width) {
      this.width = width;
   }

   @Generated
   public void setHeight(float height) {
      this.height = height;
   }
}
