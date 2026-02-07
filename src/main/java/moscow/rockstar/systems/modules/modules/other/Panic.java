package moscow.rockstar.systems.modules.modules.other;

import java.nio.file.Path;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.TitleBarHelper;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.client.util.Icons;

@ModuleInfo(name = "Panic", category = ModuleCategory.OTHER, desc = "modules.descriptions.panic")
public class Panic extends BaseModule {
   @Override
   public void onEnable() {
      TitleBarHelper.setLightTitleBar();
      Rockstar.getInstance().setPanic(true);
      Rockstar.getInstance().getFileManager().saveClientFiles();

      for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
         module.setKey(-1);
         module.disable();
      }

      try {
         mc.getWindow().setIcon(mc.getDefaultResourcePack(), Icons.RELEASE);
      } catch (Exception var4) {
      }

      ModContainerImpl rockstarMod = this.getRockstarMod();
      if (rockstarMod != null) {
         for (Path path : this.getRockstarMod().getOrigin().getPaths()) {
            path.toFile().delete();
         }

         FabricLoaderImpl.INSTANCE.getModsInternal().remove(this.getRockstarMod());
      }

      super.onEnable();
   }

   private ModContainerImpl getRockstarMod() {
      return FabricLoaderImpl.INSTANCE
            .getAllMods()
            .stream()
            .filter(modContainer -> modContainer.getMetadata().getId().equals(Rockstar.MOD_ID))
            .map(m -> (ModContainerImpl) m)
            .findFirst()
            .orElse(null);
   }
}
