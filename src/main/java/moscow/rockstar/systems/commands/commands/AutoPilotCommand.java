package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap.Type;

public class AutoPilotCommand implements IMinecraft {
   private final Timer timer = new Timer();
   private Vec3d target;
   private boolean active;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.active && mc.player != null && mc.world != null) {
         Vec3d currentTarget = this.target;
         if (currentTarget != null) {
            if (mc.player.isGliding()) {
               Vec3d vec = currentTarget.subtract(mc.player.getEyePos()).normalize();
               float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.x, vec.z));
               int highestY = (int)mc.player.getY();
               int highestX = (int)currentTarget.x;
               int highestZ = (int)currentTarget.z;
               int iterations = 80;

               for (int x = -iterations; x < iterations; x++) {
                  for (int z = -iterations; z < iterations; z++) {
                     int height = mc.world.getTopY(Type.WORLD_SURFACE, (int)(mc.player.getX() + x), (int)(mc.player.getZ() + z)) + 5;
                     if (height > highestY && height > mc.player.getY()) {
                        highestY = height;
                        highestX = (int)(mc.player.getX() + x);
                        highestZ = (int)(mc.player.getZ() + z);
                     }
                  }
               }

               mc.options.sprintKey.setPressed(true);
               mc.options.forwardKey.setPressed(true);
               if (EntityUtility.getVelocity() < 1.46 && this.timer.finished(1700L)) {
                  mc.interactionManager
                     .sendSequencedPacket(
                        mc.world,
                        sequence -> new PlayerInteractItemC2SPacket(
                           Hand.OFF_HAND,
                           sequence,
                           Rockstar.getInstance().getRotationHandler().getServerRotation().getYaw(),
                           Rockstar.getInstance().getRotationHandler().getServerRotation().getPitch()
                        )
                     );
                  this.timer.reset();
               }

               Vec3d vecHeight = new Vec3d(highestX, highestY + 23, highestZ).subtract(mc.player.getEyePos()).normalize();
               float rawPitch = (float)Math.clamp(Math.toDegrees(Math.asin(-vecHeight.y)), -89.0, 89.0);

               for (int i = mc.world.getTopY(Type.WORLD_SURFACE, (int)mc.player.getX(), (int)mc.player.getZ()) - 10; i < mc.player.getY(); i++) {
                  if (!mc.world.getBlockState(new BlockPos((int)mc.player.getX(), i, (int)mc.player.getZ())).getFluidState().isEmpty()
                     && mc.player.getY() - i < 5.0) {
                     rawPitch -= 11.0F;
                     break;
                  }
               }

               if (mc.player.getPos().squaredDistanceTo(currentTarget) < 20.0) {
                  MessageUtility.info(Text.of(Localizator.translate("commands.autopilot.stopped")));
                  this.stopAutoPilot();
               }

               RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
               Rotation targetRotation = new Rotation(rawYaw, rawPitch + 13.0F);
               rotationHandler.rotate(targetRotation);
            }
         }
      }
   };

   private void initialize() {
      Rockstar.getInstance().getEventManager().subscribe(this);
   }

   public AutoPilotCommand() {
      this.initialize();
   }

   public Command command() {
      return CommandBuilder.begin("autopilot")
         .aliases("ap", "pilot", "автопилот", "пилот")
         .desc("commands.autopilot.description")
         .param("x", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate))
         .param("y", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate))
         .param("z", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate))
         .handler(this::handle)
         .build();
   }

   private static ValidationResult verifyCoordinate(String input) {
      try {
         Double.parseDouble(input);
         return ValidationResult.ok(input);
      } catch (NumberFormatException var2) {
         return ValidationResult.error(Localizator.translate("commands.autopilot.invalid"));
      }
   }

   private void handle(CommandContext ctx) {
      String x = (String)ctx.arguments().get(0);
      String y = (String)ctx.arguments().get(1);
      String z = (String)ctx.arguments().get(2);
      if (x != null && y != null && z != null) {
         try {
            this.target = new Vec3d(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
            this.active = true;
            MessageUtility.info(Text.of(Localizator.translate("commands.autopilot.start", this.target.getX(), this.target.getY(), this.target.getZ())));
         } catch (NumberFormatException var6) {
            MessageUtility.error(Text.of(Localizator.translate("commands.autopilot.invalid")));
         }
      } else {
         if (this.active) {
            this.stopAutoPilot();
            MessageUtility.info(Text.of(Localizator.translate("commands.autopilot.stopping")));
         } else {
            MessageUtility.error(Text.of(Localizator.translate("commands.autopilot.not_active")));
         }
      }
   }

   private void stopAutoPilot() {
      this.active = false;
      this.target = null;
      Rockstar.getInstance().getEventManager().unsubscribe(this);
      mc.options.sprintKey.setPressed(false);
      mc.options.forwardKey.setPressed(false);
   }
}
