package moscow.rockstar.systems.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.file.ClientFile;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.file.api.FileInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.modules.modules.other.AutoAuth;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;

@FileInfo(name = "client")
public class ClientDataFile extends ClientFile implements IMinecraft {
   @Override
   public void write() {
      JsonObject json = new JsonObject();
      json.addProperty("username", mc.getSession().getUsername());
      json.addProperty("theme", Rockstar.getInstance().getThemeManager().getCurrentTheme().name());
      json.addProperty("swing", Rockstar.getInstance().getSwingManager().getCurrent());
      json.add("hudElements", this.getHudElementsJsonArray());
      json.add("friends", this.getFriendsJsonArray());
      json.add("colorPickerPresets", this.getColorPickerPresetsJsonArray());
      json.add("password", this.getPassword());
      ConfigFile currentConfig = Rockstar.getInstance().getConfigManager().getCurrent();
      if (currentConfig != null) {
         json.addProperty("lastConfig", currentConfig.getFileName());
      }

      try (FileWriter writer = new FileWriter(this.file)) {
         writer.write(FileManager.GSON.toJson(json));
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   @Override
   public void read() {
      try (FileReader reader = new FileReader(this.getFile())) {
         JsonObject object = (JsonObject)FileManager.GSON.fromJson(reader, JsonObject.class);
         if (object.has("username")) {
            String username = object.get("username").getAsString();
            new Session(username, UUID.randomUUID(), "", Optional.empty(), Optional.empty(), AccountType.MOJANG);
         }

         if (object.has("password")) {
            this.loadPass(object.getAsJsonArray("password"));
         }

         if (object.has("swing")) {
            String swing = object.get("swing").getAsString();
            SwingManager swingManager = Rockstar.getInstance().getSwingManager();
            SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();

            for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
               if (value.getName().equals(swing)) {
                  swingManager.getBezier().start(value.getBezierStart()).end(value.getBezierEnd());
                  swingManager.getBack().enabled(value.isSwingBack());
                  swingManager.getSpeed().setCurrentValue(value.getSpeed());
                  SwingPhase start = swingManager.getStartPhase();
                  start.getAnchorX().setCurrentValue(value.getFrom().anchorX());
                  start.getAnchorY().setCurrentValue(value.getFrom().anchorY());
                  start.getAnchorZ().setCurrentValue(value.getFrom().anchorZ());
                  start.getMoveX().setCurrentValue(value.getFrom().moveX());
                  start.getMoveY().setCurrentValue(value.getFrom().moveY());
                  start.getMoveZ().setCurrentValue(value.getFrom().moveZ());
                  start.getRotateX().setCurrentValue(value.getFrom().rotateX());
                  start.getRotateY().setCurrentValue(value.getFrom().rotateY());
                  start.getRotateZ().setCurrentValue(value.getFrom().rotateZ());
                  SwingPhase end = swingManager.getEndPhase();
                  end.getAnchorX().setCurrentValue(value.getTo().anchorX());
                  end.getAnchorY().setCurrentValue(value.getTo().anchorY());
                  end.getAnchorZ().setCurrentValue(value.getTo().anchorZ());
                  end.getMoveX().setCurrentValue(value.getTo().moveX());
                  end.getMoveY().setCurrentValue(value.getTo().moveY());
                  end.getMoveZ().setCurrentValue(value.getTo().moveZ());
                  end.getRotateX().setCurrentValue(value.getTo().rotateX());
                  end.getRotateY().setCurrentValue(value.getTo().rotateY());
                  end.getRotateZ().setCurrentValue(value.getTo().rotateZ());
                  swingManager.setCurrent(swing);
               }
            }
         }

         if (object.has("theme")) {
            String themeName = object.get("theme").getAsString();

            try {
               Theme theme = Theme.valueOf(themeName);
               Rockstar.getInstance().getThemeManager().setCurrentTheme(theme);
            } catch (IllegalArgumentException var16) {
               Rockstar.getInstance().getThemeManager().setCurrentTheme(Theme.DARK);
            }
         }

         if (object.has("friends")) {
            JsonArray friendsArray = object.getAsJsonArray("friends");
            Rockstar.getInstance().getFriendManager().clear();

            for (JsonElement friendElement : friendsArray) {
               Rockstar.getInstance().getFriendManager().add(friendElement.getAsString());
            }
         }

         if (object.has("colorPickerPresets")) {
            this.loadColorPickerPresets(object.getAsJsonArray("colorPickerPresets"));
         }

         if (object.has("hudElements")) {
            for (JsonElement elemObj : object.getAsJsonArray("hudElements")) {
               JsonObject elementObject = elemObj.getAsJsonObject();
               String name = elementObject.get("name").getAsString();
               float x = elementObject.get("x").getAsFloat();
               float y = elementObject.get("y").getAsFloat();
               boolean showing = elementObject.get("showing").getAsBoolean();
               HudElement element = Rockstar.getInstance().getHud().getElementByName(name);
               if (element != null) {
                  element.setX(x);
                  element.setY(y);
                  element.setShowing(showing);
                  if (elementObject.has("settings")) {
                     JsonObject settingsObject = elementObject.getAsJsonObject("settings");

                     for (Setting setting : element.getSettings()) {
                        if (settingsObject.has(setting.getName())) {
                           setting.load(settingsObject.get(setting.getName()));
                        }
                     }
                  }
               }
            }
         }

         if (object.has("lastConfig")) {
            String configName = object.get("lastConfig").getAsString();
            ConfigFile config = Rockstar.getInstance().getConfigManager().getConfig(configName);
            if (config != null) {
               config.load();
            }
         }
      } catch (Exception var18) {
         var18.printStackTrace();
      }
   }

   private JsonArray getHudElementsJsonArray() {
      JsonArray hudElementsArray = new JsonArray();

      for (HudElement element : Rockstar.getInstance().getHud().getElements()) {
         JsonObject elementObject = new JsonObject();
         elementObject.addProperty("name", element.getName());
         elementObject.addProperty("x", element.getX());
         elementObject.addProperty("y", element.getY());
         elementObject.addProperty("showing", element.isShowing());
         elementObject.add("settings", this.getSettingsJsonObject(element));
         hudElementsArray.add(elementObject);
      }

      return hudElementsArray;
   }

   private JsonObject getSettingsJsonObject(HudElement element) {
      JsonObject settingsObject = new JsonObject();

      for (Setting setting : element.getSettings()) {
         settingsObject.add(setting.getName(), setting.save());
      }

      return settingsObject;
   }

   private JsonArray getFriendsJsonArray() {
      JsonArray friendsJsonArray = new JsonArray();

      for (String friendsName : Rockstar.getInstance().getFriendManager().listFriends()) {
         friendsJsonArray.add(friendsName);
      }

      return friendsJsonArray;
   }

   private JsonArray getPassword() {
      JsonArray passwordJsonArray = new JsonArray();

      for (Entry<String, String> pass : Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).listPassword().entrySet()) {
         JsonObject passObject = new JsonObject();
         passObject.addProperty("nick", pass.getValue());
         passObject.addProperty("pass", pass.getKey());
         passwordJsonArray.add(passObject);
      }

      return passwordJsonArray;
   }

   private JsonArray getColorPickerPresetsJsonArray() {
      JsonArray presetsArray = new JsonArray();

      for (ColorPicker.Preset preset : ColorPicker.COLOR_PRESETS) {
         if (preset.isShowing()) {
            JsonObject presetObject = new JsonObject();
            ColorRGBA color = preset.getColor();
            presetObject.addProperty("red", color.getRed());
            presetObject.addProperty("green", color.getGreen());
            presetObject.addProperty("blue", color.getBlue());
            presetObject.addProperty("alpha", color.getAlpha());
            presetsArray.add(presetObject);
         }
      }

      return presetsArray;
   }

   private void loadColorPickerPresets(JsonArray presetsArray) {
      List<ColorPicker.Preset> loadedPresets = new ArrayList<>();

      for (JsonElement presetElement : presetsArray) {
         JsonObject presetObject = presetElement.getAsJsonObject();
         float red = presetObject.get("red").getAsFloat();
         float green = presetObject.get("green").getAsFloat();
         float blue = presetObject.get("blue").getAsFloat();
         float alpha = presetObject.get("alpha").getAsFloat();
         ColorRGBA color = new ColorRGBA(red, green, blue, alpha);
         loadedPresets.add(new ColorPicker.Preset(color));
      }

      ColorPicker.setColorPresets(loadedPresets);
   }

   private void loadPass(JsonArray password) {
      for (JsonElement passElement : password) {
         JsonObject passObject = passElement.getAsJsonObject();
         String nick = passObject.get("nick").getAsString();
         String pass = passObject.get("pass").getAsString();
         Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).put(nick, pass);
      }
   }
}
