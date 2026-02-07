package moscow.rockstar.utility.colors;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.types.ColorAnimation;

public final class Colors {
   public static final ColorRGBA RED = new ColorRGBA(255.0F, 0.0F, 0.0F);
   public static final ColorRGBA GREEN = new ColorRGBA(0.0F, 255.0F, 0.0F);
   public static final ColorRGBA BLUE = new ColorRGBA(0.0F, 0.0F, 255.0F);
   public static final ColorRGBA WHITE = new ColorRGBA(255.0F, 255.0F, 255.0F);
   public static final ColorRGBA BLACK = new ColorRGBA(0.0F, 0.0F, 0.0F);
   public static final ColorRGBA ACCENT = new ColorRGBA(151.0F, 71.0F, 255.0F);
   private static final long ANIMATION_DURATION = 500L;
   private static final ColorAnimation BACKGROUND_COLOR_ANIMATION = new ColorAnimation(500L);
   private static final ColorAnimation ADDITIONAL_COLOR_ANIMATION = new ColorAnimation(500L);
   private static final ColorAnimation TEXT_COLOR_ANIMATION = new ColorAnimation(500L);
   private static final ColorAnimation OUTLINE_COLOR_ANIMATION = new ColorAnimation(500L);
   private static final ColorAnimation FLAT_COLOR_ANIMATION = new ColorAnimation(500L);

   private static Theme getTheme() {
      return Rockstar.getInstance().getThemeManager().getCurrentTheme();
   }

   public static ColorRGBA getBackgroundColor() {
      return getAnimatedColor(BACKGROUND_COLOR_ANIMATION, getTheme().getBackgroundColor());
   }

   public static ColorRGBA getAdditionalColor() {
      return getAnimatedColor(ADDITIONAL_COLOR_ANIMATION, getTheme().getAdditionalColor());
   }

   public static ColorRGBA getTextColor() {
      return getAnimatedColor(TEXT_COLOR_ANIMATION, getTheme().getTextColor());
   }

   public static ColorRGBA getOutlineColor() {
      return getAnimatedColor(OUTLINE_COLOR_ANIMATION, getTheme().getOutlineColor());
   }

   public static ColorRGBA getFlatColor() {
      return getAnimatedColor(FLAT_COLOR_ANIMATION, getTheme().getFlatColor());
   }

   public static ColorRGBA getSeparatorColor() {
      return ColorRGBA.BLACK.withAlpha(255.0F * (getTheme() == Theme.DARK ? 0.08F : 0.05F));
   }

   private static ColorRGBA getAnimatedColor(ColorAnimation animation, ColorRGBA color) {
      animation.update(color);
      return animation.getColor();
   }

   @Generated
   private Colors() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
