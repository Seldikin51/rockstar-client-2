package moscow.rockstar.systems.commands.commands;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class FakePlayerCommand implements IMinecraft {
   private OtherClientPlayerEntity fakePlayer;
   private float moveForward = 0.0F;
   private float moveStrafe = 0.0F;
   private final EventListener<AttackEvent> onAttackEvent = event -> {
      if (this.fakePlayer != null && event.getEntity() == this.fakePlayer && this.fakePlayer.hurtTime == 0) {
         mc.world
            .playSound(
               mc.player,
               this.fakePlayer.getX(),
               this.fakePlayer.getY(),
               this.fakePlayer.getZ(),
               SoundEvents.ENTITY_PLAYER_HURT,
               SoundCategory.PLAYERS,
               1.0F,
               1.0F
            );
         if (mc.player.fallDistance > 0.0F) {
            mc.world
               .playSound(
                  mc.player,
                  this.fakePlayer.getX(),
                  this.fakePlayer.getY(),
                  this.fakePlayer.getZ(),
                  SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F
               );
         } else {
            mc.world
               .playSound(
                  mc.player,
                  this.fakePlayer.getX(),
                  this.fakePlayer.getY(),
                  this.fakePlayer.getZ(),
                  SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                  SoundCategory.PLAYERS,
                  1.0F,
                  1.0F
               );
         }

         this.fakePlayer.onDamaged(mc.world.getDamageSources().generic());
         this.fakePlayer.setHealth(this.fakePlayer.getHealth() + this.fakePlayer.getAbsorptionAmount() - 1.0F);
         if (this.fakePlayer.isDead()) {
            this.fakePlayer.setHealth(10.0F);
            new EntityStatusS2CPacket(this.fakePlayer, (byte)35).apply(mc.player.networkHandler);
         }
      }
   };
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (this.fakePlayer != null && mc.currentScreen == null) {
         int key = event.getKey();
         int action = event.getAction();
         if (key == 265) {
            this.moveForward = action != 1 && action != 2 ? 0.0F : 1.0F;
         } else if (key == 264) {
            this.moveForward = action != 1 && action != 2 ? 0.0F : -1.0F;
         } else if (key == 263) {
            this.moveStrafe = action != 1 && action != 2 ? 0.0F : 1.0F;
         } else if (key == 262) {
            this.moveStrafe = action != 1 && action != 2 ? 0.0F : -1.0F;
         }
      }
   };
   private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
      if (this.fakePlayer != null && mc.player != null) {
         if (this.moveForward == 0.0F && this.moveStrafe == 0.0F) {
            this.fakePlayer.setSprinting(false);
            this.fakePlayer.setVelocity(0.0, this.fakePlayer.getVelocity().y, 0.0);
            this.fakePlayer.limbAnimator.setSpeed(0.0F);
         } else {
            float yaw = mc.player.getYaw();
            double speed = 0.2;
            double motionX = this.moveStrafe * Math.cos(Math.toRadians(yaw)) - this.moveForward * Math.sin(Math.toRadians(yaw));
            double motionZ = this.moveForward * Math.cos(Math.toRadians(yaw)) + this.moveStrafe * Math.sin(Math.toRadians(yaw));
            Vec3d velocity = new Vec3d(motionX * speed, this.fakePlayer.getVelocity().y, motionZ * speed);
            this.fakePlayer.setVelocity(velocity);
            this.fakePlayer.move(MovementType.SELF, velocity);
            this.fakePlayer.setSprinting(true);
         }
      }
   };

   public FakePlayerCommand() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   public Command command() {
      return CommandBuilder.begin("fakeplayer")
         .aliases("fp")
         .desc("commands.fakeplayer.description")
         .param("action", p -> p.literal("add", "del"))
         .handler(this::handle)
         .build();
   }

   private void handle(CommandContext ctx) {
      String action = (String)ctx.arguments().getFirst();
      String var3 = action.toLowerCase();
      switch (var3) {
         case "add":
            this.add();
            break;
         case "del":
            this.del();
      }
   }

   public void add() {
      if (this.fakePlayer != null) {
         this.fakePlayer.discard();
         this.fakePlayer = null;
      }

      this.fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), "FakePlayer"));
      this.fakePlayer.copyPositionAndRotation(mc.player);
      this.fakePlayer.setStackInHand(Hand.MAIN_HAND, mc.player.getMainHandStack().copy());
      this.fakePlayer.setStackInHand(Hand.OFF_HAND, mc.player.getOffHandStack().copy());
      this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
      this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
      this.fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
      mc.world.addEntity(this.fakePlayer);
      Rockstar.getInstance()
         .getNotificationManager()
         .addNotificationOther(
            NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.added")
         );
   }

   public void del() {
      if (this.fakePlayer == null) {
         Rockstar.getInstance()
            .getNotificationManager()
            .addNotificationOther(
               NotificationType.ERROR, Localizator.translate("commands.fakeplayer.error"), Localizator.translate("commands.fakeplayer.not_exists")
            );
      } else {
         this.fakePlayer.discard();
         this.fakePlayer = null;
         Rockstar.getInstance()
            .getNotificationManager()
            .addNotificationOther(
               NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.removed")
            );
      }
   }
}
