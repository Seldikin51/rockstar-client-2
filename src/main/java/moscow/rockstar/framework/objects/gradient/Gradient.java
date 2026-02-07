package moscow.rockstar.framework.objects.gradient;

import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;

public class Gradient {
   protected final ColorRGBA topLeftColor;
   protected final ColorRGBA bottomLeftColor;
   protected final ColorRGBA topRightColor;
   protected final ColorRGBA bottomRightColor;

   protected Gradient(ColorRGBA topLeftColor, ColorRGBA bottomLeftColor, ColorRGBA topRightColor, ColorRGBA bottomRightColor) {
      this.topLeftColor = topLeftColor;
      this.bottomLeftColor = bottomLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
   }

   public static Gradient of(ColorRGBA topLeftColor, ColorRGBA bottomLeftColor, ColorRGBA topRightColor, ColorRGBA bottomRightColor) {
      return new Gradient(topLeftColor, bottomLeftColor, topRightColor, bottomRightColor);
   }

   public Gradient rotate() {
      return this;
   }

   @Generated
   public ColorRGBA getTopLeftColor() {
      return this.topLeftColor;
   }

   @Generated
   public ColorRGBA getBottomLeftColor() {
      return this.bottomLeftColor;
   }

   @Generated
   public ColorRGBA getTopRightColor() {
      return this.topRightColor;
   }

   @Generated
   public ColorRGBA getBottomRightColor() {
      return this.bottomRightColor;
   }
}
