package moscow.rockstar.utility.math;

import java.util.Random;

public class PerlinNoise {
   private final int[] p = new int[512];

   public PerlinNoise() {
      this(System.currentTimeMillis());
   }

   public PerlinNoise(long seed) {
      Random random = new Random(seed);
      int[] permutation = new int[256];
      int i = 0;

      while (i < 256) {
         permutation[i] = i++;
      }

      for (int ix = 0; ix < 256; ix++) {
         int j = random.nextInt(256 - ix) + ix;
         int temp = permutation[ix];
         permutation[ix] = permutation[j];
         permutation[j] = temp;
      }

      for (int ix = 0; ix < 256; ix++) {
         this.p[ix] = this.p[ix + 256] = permutation[ix];
      }
   }

   public double noise(double x) {
      return this.noise(x, 0.0, 0.0);
   }

   public double noise(double x, double y) {
      return this.noise(x, y, 0.0);
   }

   public double noise(double x, double y, double z) {
      int X = (int)Math.floor(x) & 0xFF;
      int Y = (int)Math.floor(y) & 0xFF;
      int Z = (int)Math.floor(z) & 0xFF;
      x -= Math.floor(x);
      y -= Math.floor(y);
      z -= Math.floor(z);
      double u = fade(x);
      double v = fade(y);
      double w = fade(z);
      int A = this.p[X] + Y;
      int AA = this.p[A] + Z;
      int AB = this.p[A + 1] + Z;
      int B = this.p[X + 1] + Y;
      int BA = this.p[B] + Z;
      int BB = this.p[B + 1] + Z;
      return lerp(
         w,
         lerp(
            v,
            lerp(u, grad(this.p[AA], x, y, z), grad(this.p[BA], x - 1.0, y, z)),
            lerp(u, grad(this.p[AB], x, y - 1.0, z), grad(this.p[BB], x - 1.0, y - 1.0, z))
         ),
         lerp(
            v,
            lerp(u, grad(this.p[AA + 1], x, y, z - 1.0), grad(this.p[BA + 1], x - 1.0, y, z - 1.0)),
            lerp(u, grad(this.p[AB + 1], x, y - 1.0, z - 1.0), grad(this.p[BB + 1], x - 1.0, y - 1.0, z - 1.0))
         )
      );
   }

   private static double fade(double t) {
      return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
   }

   private static double lerp(double t, double a, double b) {
      return a + t * (b - a);
   }

   private static double grad(int hash, double x, double y, double z) {
      int h = hash & 15;
      double u = h < 8 ? x : y;
      double v = h < 4 ? y : (h != 12 && h != 14 ? z : x);
      return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
   }
}
