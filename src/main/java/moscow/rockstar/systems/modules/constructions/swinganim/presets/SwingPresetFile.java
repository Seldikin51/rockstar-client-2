package moscow.rockstar.systems.modules.constructions.swinganim.presets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.text.Text;

public class SwingPresetFile implements IMinecraft {
   private final File file;
   private final String fileName;
   private final Animation hoverAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);
   private final Animation activeAnimation = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

   public SwingPresetFile(String fileName) {
      this.fileName = fileName;
      File configsFolder = new File(FileManager.DIRECTORY + "/presets", "swing");
      if (!configsFolder.exists()) {
         configsFolder.mkdir();
      }

      this.file = new File(configsFolder, fileName + ".%s".formatted("rock"));
   }

   public void load() {
      if (!this.file.exists()) {
         Rockstar.LOGGER.warn("Config file not found: {}", this.file.getAbsolutePath());
      } else {
         try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject animation = jsonObject.getAsJsonObject("animation");

            for (Setting setting : Rockstar.getInstance().getSwingManager().getSharedSettings().getSettings()) {
               if (animation.has(setting.getName())) {
                  setting.load(animation.get(setting.getName()));
               }
            }

            JsonObject startPhase = jsonObject.getAsJsonObject("startPhase");

            for (Setting settingx : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
               if (startPhase.has(settingx.getName())) {
                  settingx.load(startPhase.get(settingx.getName()));
               }
            }

            JsonObject endPhase = jsonObject.getAsJsonObject("endPhase");

            for (Setting settingxx : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
               if (endPhase.has(settingxx.getName())) {
                  settingxx.load(endPhase.get(settingxx.getName()));
               }
            }

            if (!this.fileName.equals("autosave")) {
               Rockstar.getInstance().getSwingPresetManager().setCurrent(this);
            }
         } catch (Exception var10) {
            Rockstar.LOGGER.error("Failed to load config file {}: {}", this.fileName, var10.getMessage());
         }
      }
   }

   public void save() {
      try {
         if (!this.file.exists() && !this.file.createNewFile()) {
            throw new IOException("Failed to create config file: " + this.file.getAbsolutePath());
         }

         JsonObject json = new JsonObject();
         JsonObject animation = new JsonObject();

         for (Setting setting : Rockstar.getInstance().getSwingManager().getSharedSettings().getSettings()) {
            animation.add(setting.getName(), setting.save());
         }

         json.add("animation", animation);
         JsonObject startPhase = new JsonObject();

         for (Setting setting : Rockstar.getInstance().getSwingManager().getStartPhase().getSettings()) {
            startPhase.add(setting.getName(), setting.save());
         }

         json.add("startPhase", startPhase);
         JsonObject endPhase = new JsonObject();

         for (Setting setting : Rockstar.getInstance().getSwingManager().getEndPhase().getSettings()) {
            endPhase.add(setting.getName(), setting.save());
         }

         json.add("endPhase", endPhase);
         FileWriter fileWriter = new FileWriter(this.file);

         try {
            fileWriter.write(FileManager.GSON.toJson(json));
         } catch (Throwable var9) {
            try {
               fileWriter.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         fileWriter.close();
         System.out.println("saved");
         if (!this.fileName.equals("autosave")) {
            Rockstar.getInstance().getSwingPresetManager().setCurrent(this);
         }
      } catch (IOException var10) {
         Rockstar.LOGGER.error("Failed to save config file", var10);
      }
   }

   public void delete() {
      Path filePath = this.file.toPath();

      try {
         Files.delete(filePath);
         Rockstar.getInstance().getSwingPresetManager().getSwingPresetFiles().remove(this);
         Rockstar.LOGGER.info("Config file deleted: {}", filePath);
      } catch (NoSuchFileException var3) {
         Rockstar.LOGGER.warn("Tried to delete a file that does not exist: {}", filePath);
      } catch (IOException var4) {
         MessageUtility.error(Text.of("Произошла ошибка при удалении"));
         Rockstar.LOGGER.warn("Failed to delete config file: {}. Reason: {}", filePath, var4.getMessage());
      }
   }

   private JsonObject getSettingsJsonObject(Module module) {
      JsonObject settingsObject = new JsonObject();

      for (Setting setting : module.getSettings()) {
         settingsObject.add(setting.getName(), setting.save());
      }

      return settingsObject;
   }

   @Generated
   public File getFile() {
      return this.file;
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }

   @Generated
   public Animation getHoverAnimation() {
      return this.hoverAnimation;
   }

   @Generated
   public Animation getActiveAnimation() {
      return this.activeAnimation;
   }
}
