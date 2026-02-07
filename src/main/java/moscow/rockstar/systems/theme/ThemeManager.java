package moscow.rockstar.systems.theme;

import lombok.Generated;
import moscow.rockstar.systems.modules.modules.visuals.Interface;

public class ThemeManager {
   private Theme currentTheme = Theme.DARK;

   public void switchTheme() {
      this.currentTheme = this.currentTheme == Theme.DARK ? Theme.LIGHT : Theme.DARK;
   }

   public Theme getCurrentTheme() {
      return Interface.glassSelected() ? Theme.DARK : this.currentTheme;
   }

   @Generated
   public void setCurrentTheme(Theme currentTheme) {
      this.currentTheme = currentTheme;
   }
}
