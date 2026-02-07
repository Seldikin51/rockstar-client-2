package moscow.rockstar.utility.game;

import moscow.rockstar.utility.interfaces.IMinecraft;

public class ScaledResolution implements IMinecraft {
   public Number getNumberScaledWidth() {
      return mc.getWindow().getScaledWidth();
   }

   public Number getNumberScaledHeight() {
      return mc.getWindow().getScaledHeight();
   }

   public Number getNumberScaleFactor() {
      return mc.getWindow().getScaleFactor();
   }

   public float getScaledWidth() {
      return mc.getWindow().getScaledWidth();
   }

   public float getScaledHeight() {
      return mc.getWindow().getScaledHeight();
   }

   public double getScaleFactor() {
      return mc.getWindow().getScaleFactor();
   }
}
