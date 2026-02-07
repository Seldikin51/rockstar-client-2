package moscow.rockstar;

import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import lombok.Generated;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.systems.ai.AIPredict;
import moscow.rockstar.systems.commands.CommandRegistry;
import moscow.rockstar.systems.config.ConfigDropHandler;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.systems.discord.DiscordManager;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventManager;
import moscow.rockstar.systems.event.handlers.ServerConnectionHandler;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.friends.FriendManager;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.ModuleManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.modules.listeners.ModuleTickListener;
import moscow.rockstar.systems.modules.listeners.ModuleWidgetRenderer;
import moscow.rockstar.systems.notifications.NotificationManager;
import moscow.rockstar.systems.poshalko.PoshalkoHandler;
import moscow.rockstar.systems.target.TargetManager;
import moscow.rockstar.systems.theme.ThemeManager;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.ui.hud.Hud;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.utility.game.TitleBarHelper;
import moscow.rockstar.utility.game.WebUtility;
import moscow.rockstar.utility.game.server.TPSHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.calculator.ChatListener;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationUpdateListener;
import moscow.rockstar.utility.sounds.MusicTracker;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public enum Rockstar implements IMinecraft {
   INSTANCE;

   public static final String NAME = "Rockstar";
   public static final String BUILD_TYPE = "Beta";
   public static final String VERSION = "2.0";
   public static final String MOD_ID = "Rockstar".toLowerCase();
   public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
   private EventManager eventManager;
   private ThemeManager themeManager;
   private ModuleManager moduleManager;
   private CommandRegistry commandManager;
   private FriendManager friendManager;
   private DiscordManager discordManager;
   private RotationHandler rotationHandler;
   private TargetManager targetManager;
   private MusicTracker musicTracker;
   private FileManager fileManager;
   private NotificationManager notificationManager;
   private ConfigManager configManager;
   private SwingManager swingManager;
   private TPSHandler tpsHandler;
   private AIPredict ai;
   private Hud hud;
   private ServerConnectionHandler serverConnectionHandler;
   private PoshalkoHandler poshalkoHandler;
   private WayPointsManager wayPointsManager;
   private SwingPresetManager swingPresetManager;
   private MenuScreen menuScreen;
   private ChatListener chatListener;
   private boolean panic;

   @Compile
   @Initialization
   public void initialize() {
      LOGGER.info("Initializing {}...", "Rockstar");
      this.musicTracker = new MusicTracker();
      this.wayPointsManager = new WayPointsManager();
      this.eventManager = new EventManager();
      this.friendManager = new FriendManager();
      this.themeManager = new ThemeManager();
      this.discordManager = new DiscordManager();
      this.rotationHandler = new RotationHandler(new RotationUpdateListener());
      this.targetManager = new TargetManager();
      this.fileManager = new FileManager();
      this.moduleManager = new ModuleManager(new ModuleTickListener(), new ModuleWidgetRenderer());
      this.hud = new Hud();
      this.tpsHandler = new TPSHandler();
      this.notificationManager = new NotificationManager();
      this.fileManager.registerClientFiles();
      this.moduleManager.registerModules();
      this.moduleManager.enableModules();
      this.configManager = new ConfigManager();
      this.configManager.handle();
      this.commandManager = new CommandRegistry();
      this.commandManager.initCommands();
      this.swingManager = new SwingManager();
      this.swingPresetManager = new SwingPresetManager();
      this.swingPresetManager.handle();
      this.fileManager.loadClientFiles();
      ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Rockstar.id("after_shader_load");
         }

         public void reload(ResourceManager manager) {
            GlProgram.loadAndSetupPrograms();
         }
      });
      DrawUtility.initializeShaders();
      Localizator.loadTranslations();
      this.chatListener = new ChatListener();
      this.serverConnectionHandler = new ServerConnectionHandler();
      this.poshalkoHandler = new PoshalkoHandler();
      String osName = System.getProperty("os.name");
      String pcName = System.getProperty("user.name");
      if (osName.toLowerCase().contains("windows") && !pcName.equals("sheluvparis")) {
         this.discordManager.connect();
      }

      ConfigDropHandler.init();
      TitleBarHelper.setDarkTitleBar();
      new EventIntegration();
      this.createAvatar();
      LOGGER.info("{} initialized", "Rockstar");
   }

   public void shutdown() {
      LOGGER.info("Shutting down...");
      this.fileManager.saveClientFiles();
      if (!this.isPanic()) {
         this.configManager.getAutoSaveConfig().save();
      }

      if (!this.isPanic()) {
         this.swingPresetManager.getAutoSavePreset().save();
      }

      this.setPanic(false);
   }

   public static Rockstar getInstance() {
      return INSTANCE;
   }

   public static Identifier id(String path) {
      return Identifier.of(MOD_ID, path);
   }

   @CompileBytecode
   private void createAvatar() {
      try {
         BufferedImage bufferedImage = ImageIO.read(new URL("https://rockstar.pub/api/avatars/ConeTin.jpg?t=1754613855632"));
         if (bufferedImage == null) {
            return;
         }

         Identifier id = id("temp/avatar");
         mc.getTextureManager().registerTexture(id, new NativeImageBackedTexture(WebUtility.bufferedImageToNativeImage(bufferedImage, true)));
      } catch (Exception var3) {
      }
   }

   @Generated
   public EventManager getEventManager() {
      return this.eventManager;
   }

   @Generated
   public ThemeManager getThemeManager() {
      return this.themeManager;
   }

   @Generated
   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   @Generated
   public CommandRegistry getCommandManager() {
      return this.commandManager;
   }

   @Generated
   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   @Generated
   public DiscordManager getDiscordManager() {
      return this.discordManager;
   }

   @Generated
   public RotationHandler getRotationHandler() {
      return this.rotationHandler;
   }

   @Generated
   public TargetManager getTargetManager() {
      return this.targetManager;
   }

   @Generated
   public MusicTracker getMusicTracker() {
      return this.musicTracker;
   }

   @Generated
   public FileManager getFileManager() {
      return this.fileManager;
   }

   @Generated
   public NotificationManager getNotificationManager() {
      return this.notificationManager;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public SwingManager getSwingManager() {
      return this.swingManager;
   }

   @Generated
   public TPSHandler getTpsHandler() {
      return this.tpsHandler;
   }

   @Generated
   public AIPredict getAi() {
      return this.ai;
   }

   @Generated
   public Hud getHud() {
      return this.hud;
   }

   @Generated
   public ServerConnectionHandler getServerConnectionHandler() {
      return this.serverConnectionHandler;
   }

   @Generated
   public PoshalkoHandler getPoshalkoHandler() {
      return this.poshalkoHandler;
   }

   @Generated
   public WayPointsManager getWayPointsManager() {
      return this.wayPointsManager;
   }

   @Generated
   public SwingPresetManager getSwingPresetManager() {
      return this.swingPresetManager;
   }

   @Generated
   public MenuScreen getMenuScreen() {
      return this.menuScreen;
   }

   @Generated
   public ChatListener getChatListener() {
      return this.chatListener;
   }

   @Generated
   public boolean isPanic() {
      return this.panic;
   }

   @Generated
   public void setMenuScreen(MenuScreen menuScreen) {
      this.menuScreen = menuScreen;
   }

   @Generated
   public void setPanic(boolean panic) {
      this.panic = panic;
   }
}
