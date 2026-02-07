package moscow.rockstar.utility.animation.types;

import lombok.NonNull;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;

public class ColorAnimation {
   private static final Easing DEFAULT_EASING = Easing.CUBIC_IN_OUT;
   private final long duration;
   private final Animation r;
   private final Animation g;
   private final Animation b;
   private final Animation a;

   public ColorAnimation(long duration, Easing easing) {
      this.duration = duration;
      this.r = new Animation(duration, easing);
      this.g = new Animation(duration, easing);
      this.b = new Animation(duration, easing);
      this.a = new Animation(duration, easing);
   }

   public ColorAnimation(long duration) {
      this(duration, DEFAULT_EASING);
   }

   public ColorAnimation(long duration, ColorRGBA initalColor, Easing easing) {
      this.duration = duration;
      this.r = new Animation(duration, initalColor.getRed(), easing);
      this.g = new Animation(duration, initalColor.getGreen(), easing);
      this.b = new Animation(duration, initalColor.getBlue(), easing);
      this.a = new Animation(duration, initalColor.getAlpha(), easing);
   }

   public ColorAnimation(long duration, ColorRGBA initalColor) {
      this(duration, initalColor, DEFAULT_EASING);
   }

   public void update(@NonNull ColorRGBA targetColor) {
      if (targetColor == null) {
         throw new NullPointerException("targetColor is marked non-null but is null");
      } else {
         this.r.update(targetColor.getRed());
         this.g.update(targetColor.getGreen());
         this.b.update(targetColor.getBlue());
         this.a.update(targetColor.getAlpha());
      }
   }

   public ColorRGBA getColor() {
      return new ColorRGBA((int)this.r.getValue(), (int)this.g.getValue(), (int)this.b.getValue(), (int)this.a.getValue());
   }

   public void setEasing(Easing easing) {
      this.r.setEasing(easing);
      this.g.setEasing(easing);
      this.b.setEasing(easing);
      this.a.setEasing(easing);
   }

   public void setDuration(long duration) {
      this.r.setDuration(duration);
      this.g.setDuration(duration);
      this.b.setDuration(duration);
      this.a.setDuration(duration);
   }

   public void setColor(@NonNull ColorRGBA color) {
      if (color == null) {
         throw new NullPointerException("color is marked non-null but is null");
      } else {
         this.r.setValue(color.getRed());
         this.g.setValue(color.getGreen());
         this.b.setValue(color.getBlue());
         this.a.setValue(color.getAlpha());
      }
   }
}
