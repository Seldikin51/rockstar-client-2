package moscow.rockstar.systems.modules.modules.other;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.game.countermine.AntiAim;
import moscow.rockstar.utility.game.countermine.CMUtility;
import moscow.rockstar.utility.game.countermine.Point;
import moscow.rockstar.utility.game.countermine.PositionScanner;
import moscow.rockstar.utility.game.countermine.RageBot;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Counter Mine", category = ModuleCategory.OTHER, desc = "Автоматически убивает криперов на фармилке")
public class CounterMine extends BaseModule {
   private final BooleanSetting moderDetect = new BooleanSetting(this, "ModerDetect");
   private final BooleanSetting wallHack = new BooleanSetting(this, "WallHack");
   private final BooleanSetting noF5 = new BooleanSetting(this, "No F5");
   private final BooleanSetting noSmoke = new BooleanSetting(this, "HideSmoke");
   private final BooleanSetting hideScope = new BooleanSetting(this, "HideScope");
   private final BindSetting pickAssist = new BindSetting(this, "Pick Assist");
   private final BindSetting minDamageBind = new BindSetting(this, "MinDamage");
   private boolean minDamage;
   private final Timer moderTimer = new Timer();
   private final AntiAim antiAim;
   private final RageBot rageBot;
   private final PositionScanner scanner = new PositionScanner();
   private final Timer jumping = new Timer();
   private boolean jump = true;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.moderTimer.finished(5000L)) {
         for (String moderator : Arrays.asList("corisabi", "petiuka", "johnebik", "sherlock")) {
            if (CMUtility.isPlayerOnline(moderator)) {
               MessageUtility.info(Text.of("Обнаружен модератор: " + moderator));
               if (this.moderDetect.isEnabled()) {
                  mc.player.networkHandler.sendChatCommand("hub");
               }

               this.moderTimer.reset();
               break;
            }
         }
      }

      if (this.jumping.finished(100L) && mc.player.isOnGround() && !this.jump) {
         this.minDamage = true;
         mc.player.jump();
         this.minDamage = false;
         this.jump = true;
      }
   };
   private final EventListener<HudRenderEvent> onRender = event -> {
      if (this.minDamage) {
         event.getContext()
            .drawCenteredText(Fonts.MEDIUM.getFont(11.0F), "Min-Damange", sr.getScaledWidth() / 2.0F, sr.getScaledHeight() / 2.0F + 10.0F, ColorRGBA.WHITE);
      }
   };
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (event.getAction() == 1 && mc.currentScreen == null) {
         if (this.pickAssist.isKey(event.getKey())) {
            mc.player
               .networkHandler
               .sendPacket(new Full(mc.player.getX(), mc.player.getY() + 10.0, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, false));
         }

         if (this.minDamageBind.isKey(event.getKey())) {
            this.minDamage = !this.minDamage;
         }
      }
   };
   private final EventListener<MouseEvent> onMouseButtonPress = event -> {
      if (event.getAction() == 1 && mc.currentScreen == null) {
         if (this.pickAssist.isKey(event.getButton())) {
            mc.player
               .networkHandler
               .sendPacket(new Full(mc.player.getX(), mc.player.getY() + 10.0, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, false));
         }

         if (this.minDamageBind.isKey(event.getButton())) {
            this.minDamage = !this.minDamage;
         }
      }
   };
   private final EventListener<HudRenderEvent> on2DRender = event -> {
      if (this.noF5.isEnabled()) {
         CMUtility.removeAllArmor();
      }
   };
   private final EventListener<ReceivePacketEvent> onPacket = event -> {
      if (event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
         event.cancel();
      }
   };
   private final EventListener<Render3DEvent> on3DRender = event -> {
      BossBarHud boss = mc.inGameHud.getBossBarHud();
      if (boss != null && this.hideScope.isEnabled()) {
         Class bossbarklass = BossBarHud.class;

         try {
            Field field = bossbarklass.getField("bossBars");
            Map<UUID, ClientBossBar> bossBars = (Map<UUID, ClientBossBar>)field.get(boss);

            for (UUID uuid : bossBars.keySet()) {
               ClientBossBar clientBossBar = bossBars.get(uuid);
               List<Text> siblings = clientBossBar.getName().getSiblings();
               MutableText newText = Text.literal("");
               AtomicInteger i = new AtomicInteger();
               siblings.stream().allMatch(text -> {
                  if (!text.getString().contains("룳ꈣꈃ룲ꈣꈅ")) {
                     newText.append(text);
                  }

                  i.getAndIncrement();
                  return true;
               });
               clientBossBar.setName(newText);
            }
         } catch (Exception var12) {
         }
      }

      if (mc.world != null && mc.player != null && this.wallHack.isEnabled()) {
         MatrixStack ms = event.getMatrices();
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         ms.push();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         Identifier id = Rockstar.id("textures/bloom.png");
         RenderSystem.setShaderTexture(0, id);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

         for (Point point : this.scanner.getPoints()) {
            float bigSize = 4.0F;
            float size = 1.2F;
            ms.push();
            RenderUtility.prepareMatrices(ms, point.getPos());
            ms.multiply(camera.getRotation());
            DrawUtility.drawImage(
               ms,
               builder,
               (double)(-bigSize / 2.0F),
               (double)(-bigSize / 2.0F),
               0.0,
               (double)bigSize,
               (double)bigSize,
               (point.isFriend() ? Colors.GREEN : Colors.ACCENT).withAlpha(12.75F)
            );
            DrawUtility.drawImage(
               ms,
               builder,
               (double)(-size / 2.0F),
               (double)(-size / 2.0F),
               0.0,
               (double)size,
               (double)size,
               (point.isFriend() ? Colors.GREEN : Colors.ACCENT).withAlpha(102.0F)
            );
            ms.pop();
         }

         BuiltBuffer builtLinesBuffer1 = builder.endNullable();
         if (builtLinesBuffer1 != null) {
            BufferRenderer.drawWithGlobalProgram(builtLinesBuffer1);
         }

         RenderSystem.depthMask(true);
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
         ms.pop();
      }
   };
   private final EventListener<EntityJumpEvent> onJump = event -> {
      if (event.getEntity() == mc.player && !this.minDamage) {
         event.cancel();
         this.jumping.reset();
         this.jump = false;
      }
   };

   public CounterMine() {
      this.rageBot = new RageBot(this);
      this.antiAim = new AntiAim(this);
   }

   public static boolean shouldHideEntity(ItemDisplayEntity entity) {
      String modelId = CMUtility.getModelIdFromNbt(entity.getItemStack(), MinecraftClient.getInstance().player.getRegistryManager());
      CounterMine mod = Rockstar.getInstance().getModuleManager().getModule(CounterMine.class);
      if (modelId == null) {
         return false;
      } else {
         String modelJson = CMUtility.findHashedModel(modelId);
         return modelJson != null
            && (
               modelJson.contains("smoke_sprite_transparent") && mod.noSmoke.isEnabled()
                  || modelJson.contains(",\"textures\":{\"arms\":\"") && mod.noF5.isEnabled() && mc.options.getPerspective() != Perspective.FIRST_PERSON
                  || modelJson.contains("\"textures\":{\"particle\":\"item/")
                     && modelJson.contains("\",\"skin\":\"item/")
                     && mod.noF5.isEnabled()
                     && mc.options.getPerspective() != Perspective.FIRST_PERSON
                     && mc.player.distanceTo(entity) < 3.0F
            );
      }
   }

   @Override
   public void onEnable() {
      Rockstar.getInstance().getEventManager().subscribe(this.scanner);
      Rockstar.getInstance().getEventManager().subscribe(this.antiAim);
      Rockstar.getInstance().getEventManager().subscribe(this.rageBot);
   }

   @Override
   public void onDisable() {
      Rockstar.getInstance().getTargetManager().reset();
      Rockstar.getInstance().getEventManager().unsubscribe(this.scanner);
      Rockstar.getInstance().getEventManager().unsubscribe(this.antiAim);
      Rockstar.getInstance().getEventManager().unsubscribe(this.rageBot);
   }

   @Generated
   public BooleanSetting getHideScope() {
      return this.hideScope;
   }

   @Generated
   public boolean isMinDamage() {
      return this.minDamage;
   }

   @Generated
   public AntiAim getAntiAim() {
      return this.antiAim;
   }

   @Generated
   public PositionScanner getScanner() {
      return this.scanner;
   }

   @Generated
   public Timer getJumping() {
      return this.jumping;
   }
}
