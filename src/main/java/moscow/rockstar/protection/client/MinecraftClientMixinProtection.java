package moscow.rockstar.protection.client;

import moscow.rockstar.Rockstar;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class MinecraftClientMixinProtection {
   @VMProtect(type = VMProtectType.MUTATION)
   public static void init() {
      Rockstar.INSTANCE.initialize();
   }

   @VMProtect(type = VMProtectType.MUTATION)
   public static void shutdown() {
      Rockstar.INSTANCE.shutdown();
   }

   public static void updateTitle(CallbackInfoReturnable<String> cir) {
      if (!Rockstar.INSTANCE.isPanic()) {
         String title = "%s %s (%s)".formatted("Rockstar", "2.0", "Beta");
         cir.setReturnValue(title);
      }
   }
}
