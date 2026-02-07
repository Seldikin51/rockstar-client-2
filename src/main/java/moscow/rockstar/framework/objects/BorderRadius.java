package moscow.rockstar.framework.objects;

public record BorderRadius(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
   public static final BorderRadius ZERO = new BorderRadius(0.0F, 0.0F, 0.0F, 0.0F);

   public static BorderRadius all(float radius) {
      return new BorderRadius(radius, radius, radius, radius);
   }

   public static BorderRadius topLeft(float radius) {
      return new BorderRadius(radius, 0.0F, 0.0F, 0.0F);
   }

   public static BorderRadius topRight(float radius) {
      return new BorderRadius(0.0F, radius, 0.0F, 0.0F);
   }

   public static BorderRadius bottomRight(float radius) {
      return new BorderRadius(0.0F, 0.0F, radius, 0.0F);
   }

   public static BorderRadius bottomLeft(float radius) {
      return new BorderRadius(0.0F, 0.0F, 0.0F, radius);
   }

   public static BorderRadius top(float leftRadius, float rightRadius) {
      return new BorderRadius(leftRadius, rightRadius, 0.0F, 0.0F);
   }

   public static BorderRadius bottom(float leftRadius, float rightRadius) {
      return new BorderRadius(0.0F, 0.0F, rightRadius, leftRadius);
   }

   public static BorderRadius left(float topRadius, float bottomRadius) {
      return new BorderRadius(topRadius, 0.0F, 0.0F, bottomRadius);
   }

   public static BorderRadius right(float topRadius, float bottomRadius) {
      return new BorderRadius(0.0F, topRadius, bottomRadius, 0.0F);
   }

   @Override
   public String toString() {
      return "BorderRadius{topLeftRadius="
         + this.topLeftRadius
         + ", topRightRadius="
         + this.topRightRadius
         + ", bottomRightRadius="
         + this.bottomRightRadius
         + ", bottomLeftRadius="
         + this.bottomLeftRadius
         + "}";
   }
}
