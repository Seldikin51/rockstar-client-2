package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.target.TargetComparators;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.entity.Entity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

@ModuleInfo(name = "Aim Bot", category = ModuleCategory.COMBAT, desc = "Автоматически наводится луком, трезубцем или арбалетом")
public class AimBot extends BaseModule {
   private SelectSetting.Value bow;
   private SelectSetting.Value crossbow;
   private SelectSetting.Value trident;
   private SliderSetting distance;
   private SliderSetting fov;
   private BooleanSetting prediction;
   private BooleanSetting silent;
   private SelectSetting.Value players;
   private SelectSetting.Value animals;
   private SelectSetting.Value mobs;
   private SelectSetting.Value invisibles;
   private SelectSetting.Value naked;
   private SelectSetting.Value friends;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player != null && mc.world != null && this.isHoldingSelected()) {
         TargetSettings settings = new TargetSettings.Builder()
            .targetPlayers(this.players.isSelected())
            .targetAnimals(this.animals.isSelected())
            .targetMobs(this.mobs.isSelected())
            .targetInvisibles(this.invisibles.isSelected())
            .targetNakedPlayers(this.naked.isSelected())
            .targetFriends(this.friends.isSelected())
            .requiredRange(this.distance.getCurrentValue())
            .sortBy(TargetComparators.FOV)
            .build();
         Rockstar.getInstance().getTargetManager().update(settings);
         Entity targetEntity = Rockstar.getInstance().getTargetManager().getCurrentTarget();
         if (targetEntity != null) {
            Vec3d aimPos = this.getAimPosition(targetEntity);
            Rotation toTarget = this.calculateRotation(aimPos);
            float yaw = toTarget.getYaw();
            float pitch = toTarget.getPitch();
            float deltaYaw = RotationMath.getAngleDifference(yaw, mc.player.getYaw());
            if (!(deltaYaw > this.fov.getCurrentValue()) && MathUtility.canSeen(targetEntity.getPos())) {
               if (this.silent.isEnabled()) {
                  Rockstar.getInstance().getRotationHandler().rotate(toTarget);
               } else {
                  mc.player.setYaw(yaw);
                  mc.player.setPitch(pitch);
                  mc.player.setHeadYaw(yaw);
               }
            }
         }
      }
   };

   public AimBot() {
      this.initialize();
   }

   @VMProtect(type = VMProtectType.VIRTUALIZATION)
   private void initialize() {
      SelectSetting items = new SelectSetting(this, "items");
      this.bow = new SelectSetting.Value(items, "bow").select();
      this.crossbow = new SelectSetting.Value(items, "crossbow");
      this.trident = new SelectSetting.Value(items, "trident");
      this.prediction = new BooleanSetting(this, "predict");
      this.silent = new BooleanSetting(this, "silent_aim");
      this.distance = new SliderSetting(this, "distance", "Макс. расстояние наведения").min(0.0F).max(100.0F).step(1.0F).currentValue(30.0F);
      this.fov = new SliderSetting(this, "fov").min(1.0F).max(180.0F).step(1.0F).currentValue(90.0F);
      SelectSetting targets = new SelectSetting(this, "targets");
      this.players = new SelectSetting.Value(targets, "players").select();
      this.animals = new SelectSetting.Value(targets, "animals").select();
      this.mobs = new SelectSetting.Value(targets, "mobs").select();
      this.invisibles = new SelectSetting.Value(targets, "invisibles").select();
      this.naked = new SelectSetting.Value(targets, "nakedPlayers").select();
      this.friends = new SelectSetting.Value(targets, "friends");
   }

   private boolean isHoldingSelected() {
      if (mc.player != null && !mc.player.isDead()) {
         Item main = mc.player.getMainHandStack().getItem();
         if (main == Items.BOW && this.bow.isSelected()) {
            return mc.player.isUsingItem();
         } else if (main == Items.CROSSBOW && this.crossbow.isSelected()) {
            return CrossbowItem.isCharged(mc.player.getMainHandStack());
         } else {
            return main == Items.TRIDENT && this.trident.isSelected() ? mc.player.isUsingItem() : false;
         }
      } else {
         return false;
      }
   }

   private Rotation calculateRotation(Vec3d targetPos) {
      Vec3d eyes = mc.player.getCameraPosVec(1.0F);
      double dx = targetPos.x - eyes.x;
      double dy = targetPos.y - eyes.y;
      double dz = targetPos.z - eyes.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      return new Rotation(yaw, pitch);
   }

   private Vec3d getAimPosition(Entity target) {
      Vec3d pos = target.getPos();
      if (!this.prediction.isEnabled()) {
         return pos.add(0.0, target.getHeight() / 2.0F, 0.0);
      } else {
         Vec3d motion = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ).multiply(10.0);
         return pos.add(motion).add(0.0, target.getHeight() / 2.0F, 0.0);
      }
   }

   @Override
   public void onDisable() {
      Rockstar.getInstance().getTargetManager().reset();
   }
}
