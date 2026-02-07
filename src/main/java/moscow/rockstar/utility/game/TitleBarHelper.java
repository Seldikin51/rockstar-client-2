package moscow.rockstar.utility.game;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.ptr.IntByReference;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFWNativeWin32;

public class TitleBarHelper {
   private static final int DWM_USE_IMMERSIVE_DARK_MODE = 20;

   public static void setDarkTitleBar() {
      applyTitleBarTheme(1);
   }

   public static void setLightTitleBar() {
      applyTitleBarTheme(0);
   }

   private static void applyTitleBarTheme(int themeValue) {
      if (System.getProperty("os.name").toLowerCase().contains("windows")) {
         try {
            long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
            long hwndHandle = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
            HWND hwnd = new HWND(Pointer.createConstant(hwndHandle));
            IntByReference useDarkTheme = new IntByReference(themeValue);
            LPVOID pointerToValue = new LPVOID(useDarkTheme.getPointer());
            DwmApi.INSTANCE.DwmSetWindowAttribute(hwnd, new DWORD(20L), pointerToValue, new DWORD(4L));
         } catch (Exception var8) {
         }
      }
   }
}
