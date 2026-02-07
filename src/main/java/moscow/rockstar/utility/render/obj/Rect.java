package moscow.rockstar.utility.render.obj;

import lombok.Generated;
import moscow.rockstar.utility.gui.GuiUtility;

public class Rect {
   public static final Rect EMPTY = new Rect(0.0F, 0.0F, 0.0F, 0.0F);
   protected float x;
   protected float y;
   protected float width;
   protected float height;

   public void set(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public Rect x(float x) {
      return new Rect(x, this.y, this.width, this.height);
   }

   public Rect y(float y) {
      return new Rect(this.x, y, this.width, this.height);
   }

   public Rect width(float width) {
      return new Rect(this.x, this.y, width, this.height);
   }

   public Rect height(float height) {
      return new Rect(this.x, this.y, this.width, height);
   }

   public Rect size(float off) {
      return new Rect(this.x + off, this.y + off, this.width - off * 2.0F, this.height - off * 2.0F);
   }

   public static Rect interpolate(Rect oldValue, Rect newValue, double interpolationValue) {
      float interpolatedX = (float)(oldValue.x + (newValue.x - oldValue.x) * interpolationValue);
      float interpolatedY = (float)(oldValue.y + (newValue.y - oldValue.y) * interpolationValue);
      float interpolatedWidth = (float)(oldValue.width + (newValue.width - oldValue.width) * interpolationValue);
      float interpolatedHeight = (float)(oldValue.height + (newValue.height - oldValue.height) * interpolationValue);
      return new Rect(interpolatedX, interpolatedY, interpolatedWidth, interpolatedHeight);
   }

   public boolean contains(Rect parent) {
      return this.contains(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight());
   }

   public boolean contains(float x, float y, float width, float height) {
      return this.x + this.width > x && this.x < x + width && this.y + this.height > y && this.y < y + height;
   }

   public boolean inside(Rect parent) {
      return this.inside(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight());
   }

   public boolean inside(float x, float y, float width, float height) {
      return this.x > x && this.x + this.width < x + width && this.y > y && this.y + this.height < y + height;
   }

   public boolean hovered(double mouseX, double mouseY) {
      return GuiUtility.isHovered((double)this.x, (double)this.y, (double)this.width, (double)this.height, mouseX, mouseY);
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

   @Generated
   public Rect() {
   }

   @Generated
   public Rect(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }
}
