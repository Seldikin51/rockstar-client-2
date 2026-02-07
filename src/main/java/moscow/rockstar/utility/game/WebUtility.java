package moscow.rockstar.utility.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.client.texture.NativeImage;

public final class WebUtility {
   public static String fetchJson(String usd) throws IOException {
      URL url = new URL(usd);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();
      connection.setRequestProperty("User-Agent", "Mozilla/5.0");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(10000);

      String var4;
      try (InputStream in = connection.getInputStream()) {
         var4 = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      }

      return var4;
   }

   public static String extractImageUrl(String json) {
      Pattern pattern = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
      Matcher matcher = pattern.matcher(json);
      return matcher.find() ? matcher.group(1).replace("\\/", "/") : null;
   }

   public static NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage, boolean avatar) {
      int width = bufferedImage.getWidth();
      int height = bufferedImage.getHeight();
      NativeImage nativeImage = new NativeImage(width, height, true);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int argb = bufferedImage.getRGB(x, y);
            nativeImage.setColorArgb(x, y, argb);
         }
      }

      return nativeImage;
   }

   @Generated
   private WebUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
