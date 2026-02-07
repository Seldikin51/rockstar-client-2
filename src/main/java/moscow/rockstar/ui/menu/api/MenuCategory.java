package moscow.rockstar.ui.menu.api;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.utility.render.obj.CustomSprite;

public enum MenuCategory {
   COMBAT("Combat", ModuleCategory.COMBAT, CustomSprite.COMBAT, CustomSprite.BIG_COMBAT),
   MOVEMENT("Movement", ModuleCategory.MOVEMENT, CustomSprite.MOVEMENT, CustomSprite.BIG_MOVEMENT),
   VISUALS("Visuals", ModuleCategory.VISUALS, CustomSprite.VISUALS, CustomSprite.BIG_VISUALS),
   PLAYER("Player", ModuleCategory.PLAYER, CustomSprite.PLAYER, CustomSprite.BIG_PLAYER),
   OTHER("Other", ModuleCategory.OTHER, CustomSprite.OTHER, CustomSprite.BIG_OTHER);

   private final String name;
   private final ModuleCategory category;
   private final CustomSprite menuSprite;
   private final CustomSprite bigMenuSprite;

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public ModuleCategory getCategory() {
      return this.category;
   }

   @Generated
   public CustomSprite getMenuSprite() {
      return this.menuSprite;
   }

   @Generated
   public CustomSprite getBigMenuSprite() {
      return this.bigMenuSprite;
   }

   @Generated
   private MenuCategory(final String name, final ModuleCategory category, final CustomSprite menuSprite, final CustomSprite bigMenuSprite) {
      this.name = name;
      this.category = category;
      this.menuSprite = menuSprite;
      this.bigMenuSprite = bigMenuSprite;
   }
}
