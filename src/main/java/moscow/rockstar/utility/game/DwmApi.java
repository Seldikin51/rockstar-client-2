package moscow.rockstar.utility.game;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface DwmApi extends StdCallLibrary {
   DwmApi INSTANCE = (DwmApi)Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

   HRESULT DwmSetWindowAttribute(HWND var1, DWORD var2, LPVOID var3, DWORD var4);
}
