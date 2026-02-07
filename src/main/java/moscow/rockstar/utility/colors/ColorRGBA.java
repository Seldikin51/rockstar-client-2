package moscow.rockstar.utility.colors;

import java.nio.ByteBuffer;
import java.util.Objects;
import lombok.Generated;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ColorRGBA {
   public static final ColorRGBA WHITE = new ColorRGBA(255.0F, 255.0F, 255.0F);
   public static final ColorRGBA BLACK = new ColorRGBA(0.0F, 0.0F, 0.0F);
   public static final ColorRGBA GREEN = new ColorRGBA(0.0F, 255.0F, 0.0F);
   public static final ColorRGBA RED = new ColorRGBA(255.0F, 0.0F, 0.0F);
   public static final ColorRGBA BLUE = new ColorRGBA(0.0F, 0.0F, 255.0F);
   public static final ColorRGBA YELLOW = new ColorRGBA(255.0F, 255.0F, 0.0F);
   private transient float[] hsbValues;
   private final float red;
   private final float green;
   private final float blue;
   private final float alpha;
   private static final ByteBuffer PIXEL_BUFFER = ByteBuffer.allocateDirect(4);

   public ColorRGBA(float red, float green, float blue) {
      this(red, green, blue, 255.0F);
   }

   public ColorRGBA(float red, float green, float blue, float alpha) {
      red = MathHelper.clamp(red, 0.0F, 255.0F);
      green = MathHelper.clamp(green, 0.0F, 255.0F);
      blue = MathHelper.clamp(blue, 0.0F, 255.0F);
      alpha = MathHelper.clamp(alpha, 0.0F, 255.0F);
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.alpha = alpha;
   }

   public int getRGB() {
      int a = Math.round(this.clamp(this.alpha));
      int r = Math.round(this.clamp(this.red));
      int g = Math.round(this.clamp(this.green));
      int b = Math.round(this.clamp(this.blue));
      return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
   }

   public String toHex() {
      return String.format(
         "#%02x%02x%02x%02x",
         Math.round(this.clamp(this.red)),
         Math.round(this.clamp(this.green)),
         Math.round(this.clamp(this.blue)),
         Math.round(this.clamp(this.alpha))
      );
   }

   private float clamp(float value) {
      return Math.max(0.0F, Math.min(255.0F, value));
   }

   public static ColorRGBA fromHex(String hex) {
      String sanitized = hex.startsWith("#") ? hex.substring(1) : hex;
      if (sanitized.length() != 6 && sanitized.length() != 8) {
         throw new IllegalArgumentException("Hex color must be in the format #RRGGBB or #RRGGBBAA");
      } else {
         float red = Integer.parseInt(sanitized.substring(0, 2), 16);
         float green = Integer.parseInt(sanitized.substring(2, 4), 16);
         float blue = Integer.parseInt(sanitized.substring(4, 6), 16);
         float alpha = sanitized.length() == 8 ? Integer.parseInt(sanitized.substring(6, 8), 16) : 255.0F;
         return new ColorRGBA(red, green, blue, alpha);
      }
   }

   public static ColorRGBA fromInt(int colorInt) {
      float alpha = colorInt >> 24 & 0xFF;
      float red = colorInt >> 16 & 0xFF;
      float green = colorInt >> 8 & 0xFF;
      float blue = colorInt & 0xFF;
      return new ColorRGBA(red, green, blue, alpha);
   }

   public ColorRGBA withAlpha(float newAlpha) {
      return new ColorRGBA(this.red, this.green, this.blue, newAlpha);
   }

   public ColorRGBA mulAlpha(float percent) {
      return this.withAlpha(this.alpha * percent);
   }

   public ColorRGBA mix(ColorRGBA color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return new ColorRGBA(
         MathUtility.interpolate(this.getRed(), color2.getRed(), amount),
         MathUtility.interpolate(this.getGreen(), color2.getGreen(), amount),
         MathUtility.interpolate(this.getBlue(), color2.getBlue(), amount),
         MathUtility.interpolate(this.getAlpha(), color2.getAlpha(), amount)
      );
   }

   public static ColorRGBA fromHSB(float hue, float saturation, float brightness) {
      if (saturation == 0.0F) {
         int grayValue = (int)(brightness * 255.0F + 0.5F);
         return new ColorRGBA(grayValue, grayValue, grayValue);
      } else {
         float h = (hue - (float)Math.floor(hue)) * 6.0F;
         float f = h - (float)Math.floor(h);
         float p = brightness * (1.0F - saturation);
         float q = brightness * (1.0F - saturation * f);
         float t = brightness * (1.0F - saturation * (1.0F - f));
         float r = 0.0F;
         float g = 0.0F;
         float b = 0.0F;
         switch ((int)h) {
            case 0:
               r = brightness;
               g = t;
               b = p;
               break;
            case 1:
               r = q;
               g = brightness;
               b = p;
               break;
            case 2:
               r = p;
               g = brightness;
               b = t;
               break;
            case 3:
               r = p;
               g = q;
               b = brightness;
               break;
            case 4:
               r = t;
               g = p;
               b = brightness;
               break;
            case 5:
               r = brightness;
               g = p;
               b = q;
         }

         return new ColorRGBA(r * 255.0F, g * 255.0F, b * 255.0F);
      }
   }

   public float getHue() {
      return this.getHSBValues()[0];
   }

   public float getSaturation() {
      return this.getHSBValues()[2];
   }

   public float getBrightness() {
      return this.getHSBValues()[1];
   }

   private float[] getHSBValues() {
      if (this.hsbValues == null) {
         this.hsbValues = this.calculateHSB();
      }

      return this.hsbValues;
   }

   private float[] calculateHSB() {
      float r = this.red / 255.0F;
      float g = this.green / 255.0F;
      float b = this.blue / 255.0F;
      float maxC = Math.max(r, Math.max(g, b));
      float minC = Math.min(r, Math.min(g, b));
      float delta = maxC - minC;
      float hue = 0.0F;
      if (delta != 0.0F) {
         if (maxC == r) {
            hue = (g - b) / delta;
         } else if (maxC == g) {
            hue = (b - r) / delta + 2.0F;
         } else {
            hue = (r - g) / delta + 4.0F;
         }

         hue /= 6.0F;
         if (hue < 0.0F) {
            hue++;
         }
      }

      float saturation = maxC == 0.0F ? 0.0F : delta / maxC;
      return new float[]{hue, saturation, maxC};
   }

   public static ColorRGBA fromPixel(float pixelX, float pixelY) {
      PIXEL_BUFFER.clear();
      GL11.glReadPixels((int)pixelX, (int)pixelY, 1, 1, 6408, 5121, PIXEL_BUFFER);
      int red = PIXEL_BUFFER.get(0) & 255;
      int green = PIXEL_BUFFER.get(1) & 255;
      int blue = PIXEL_BUFFER.get(2) & 255;
      return new ColorRGBA(red, green, blue);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ColorRGBA colorRGBA = (ColorRGBA)o;
         return Float.compare(this.red, colorRGBA.red) == 0
            && Float.compare(this.green, colorRGBA.green) == 0
            && Float.compare(this.blue, colorRGBA.blue) == 0
            && Float.compare(this.alpha, colorRGBA.alpha) == 0;
      } else {
         return false;
      }
   }

   public float difference(ColorRGBA colorRGBA) {
      return Math.abs(this.getHue() - colorRGBA.getHue())
         + Math.abs(this.getBrightness() - colorRGBA.getBrightness())
         + Math.abs(this.getSaturation() - colorRGBA.getSaturation());
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.red, this.green, this.blue, this.alpha);
   }

   @Override
   public String toString() {
      return String.format("RGBA(%.1f, %.1f, %.1f, %.1f)", this.red, this.green, this.blue, this.alpha);
   }

   @Generated
   public float getRed() {
      return this.red;
   }

   @Generated
   public float getGreen() {
      return this.green;
   }

   @Generated
   public float getBlue() {
      return this.blue;
   }

   @Generated
   public float getAlpha() {
      return this.alpha;
   }
}
