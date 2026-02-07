package moscow.rockstar.framework.msdf;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import moscow.rockstar.Rockstar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ResourceProvider {
   private static final ResourceManager RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
   private static final Gson GSON = new Gson();

   public static Identifier getShaderIdentifier(String name) {
      return Rockstar.id("core/" + name);
   }

   public static <T> T fromJsonToInstance(Identifier identifier, Class<T> clazz) {
      return (T)GSON.fromJson(toString(identifier), clazz);
   }

   public static String toString(Identifier identifier) {
      return toString(identifier, "\n");
   }

   public static String toString(Identifier identifier, String delimiter) {
      try {
         String var4;
         try (
            InputStream inputStream = RESOURCE_MANAGER.open(identifier);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         ) {
            var4 = reader.lines().collect(Collectors.joining(delimiter));
         }

         return var4;
      } catch (IOException var10) {
         throw new RuntimeException(var10);
      }
   }
}
