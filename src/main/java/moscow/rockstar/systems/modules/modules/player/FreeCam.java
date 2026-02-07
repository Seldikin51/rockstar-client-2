package moscow.rockstar.systems.modules.modules.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.FakePlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;

@ModuleInfo(name = "Free Camera", category = ModuleCategory.PLAYER, desc = "Свободная камера")
public class FreeCam extends BaseModule {
   private boolean wasFlyingAllowed = false;
   private boolean wasFlying = false;
   private float oldFlyingSpeed = 0.0F;
   private FakePlayerEntity dummy = null;
   private GameMode prevGameMode;
   private final SliderSetting speed = new SliderSetting(this, "Скорость").currentValue(1.0F).max(15.0F).min(0.1F).step(0.1F).currentValue(3.0F);
   private final BooleanSetting display = new BooleanSetting(this, "Отображать координаты");
   private int x;
   private int y;
   private int z;
   private final EventListener<HudRenderEvent> render2d = event -> {
      if (this.display.isEnabled()) {
         BlockPos diff = mc.player.getBlockPos().subtract(new Vec3i(this.x, this.y, this.z));
         String pos = "X: " + diff.getX() + " Y: " + diff.getY() + " Z: " + diff.getZ();
         Font bold = Fonts.BOLD.getFont(8.0F);
         event.getContext().drawText(bold, Text.of(pos), sr.getScaledWidth() / 2.0F - bold.width(pos) / 2.0F + 8.0F, sr.getScaledHeight() / 2.0F - 20.0F);
      }
   };
   private final EventListener<SendPacketEvent> onSendPacket = event -> {
      if (event.getPacket() instanceof PlayerMoveC2SPacket) {
         event.cancel();
      }
   };

   @Override
   public void tick() {
      if (mc.player != null) {
         mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0F);
         mc.player.getAbilities().flying = true;
         super.tick();
      }
   }

   @Override
   public void onEnable() {
      if (mc.player != null && mc.world != null) {
         this.wasFlyingAllowed = mc.player.getAbilities().allowFlying;
         this.wasFlying = mc.player.getAbilities().flying;
         this.oldFlyingSpeed = mc.player.getAbilities().getFlySpeed();
         this.prevGameMode = mc.interactionManager.getCurrentGameMode();
         this.dummy = new FakePlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.getSession().getUsername()));
         this.dummy.copyFrom(mc.player);
         this.dummy.copyPositionAndRotation(mc.player);
         this.dummy.spawn();
         mc.player.getAbilities().allowFlying = true;
         mc.player.getAbilities().flying = true;
         mc.interactionManager.setGameMode(GameMode.SPECTATOR);
         mc.player.getAbilities().setFlySpeed(this.speed.getCurrentValue() / 10.0F);
         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      if (this.dummy != null && mc.world != null && mc.player != null && mc.getNetworkHandler() != null) {
         mc.player.copyPositionAndRotation(this.dummy);
         mc.getNetworkHandler()
            .sendPacket(new PositionAndOnGround(this.dummy.getX(), this.dummy.getY(), this.dummy.getZ(), false, mc.player.horizontalCollision));
         mc.player.getAbilities().allowFlying = this.wasFlyingAllowed;
         mc.player.getAbilities().flying = this.wasFlying;
         mc.player.getAbilities().setFlySpeed(this.oldFlyingSpeed);
         mc.interactionManager.setGameMode(this.prevGameMode);
         this.dummy.remove();
         this.dummy = null;
         mc.player.setVelocity(0.0, 0.0, 0.0);
         super.onDisable();
      }
   }
}
