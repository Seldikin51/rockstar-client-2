package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingAnimScreen;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.combat.Aura;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Arm;
import org.joml.Quaternionf;

@ModuleInfo(name = "Swing Animation", category = ModuleCategory.VISUALS, desc = "Изменяет анимации рук при взмахе")
public class SwingAnimation extends BaseModule {
   private final BooleanSetting onlyAura = new BooleanSetting(this, "swing.only_aura");
   private final ButtonSetting button = new ButtonSetting(this, "swing.open_menu").action(() -> mc.setScreen(new SwingAnimScreen()));
   private final EventListener<HandRenderEvent> onHandRender = event -> {
      if (this.shouldApplyAnimation(event.getItemStack()) && event.getArm() == Arm.RIGHT) {
         MatrixStack matrices = event.getMatrices();
         float swingProgress = event.getSwingProgress();
         float equipProgress = event.getEquipProgress();
         SwingTransformations trans = Rockstar.getInstance().getSwingManager().transformations(swingProgress);
         matrices.translate(trans.anchorX(), trans.anchorY(), trans.anchorZ());
         matrices.translate(trans.moveX(), trans.moveY(), trans.moveZ());
         matrices.multiply(
            new Quaternionf()
               .rotationXYZ((float)Math.toRadians(trans.rotateX()), (float)Math.toRadians(trans.rotateY()), (float)Math.toRadians(trans.rotateZ()))
         );
         matrices.translate(-trans.anchorX(), -trans.anchorY(), -trans.anchorZ());
         event.cancel();
      }
   };

   public boolean shouldApplyAnimation(ItemStack itemStack) {
      Aura auraModule = Rockstar.getInstance().getModuleManager().getModule(Aura.class);
      Entity target = Rockstar.getInstance().getTargetManager().getCurrentTarget();
      Item item = itemStack.getItem();
      return !this.onlyAura.isEnabled() || auraModule.isEnabled() && target != null
         ? item != Items.AIR
            && item != Items.FILLED_MAP
            && item != Items.CROSSBOW
            && item != Items.BOW
            && item != Items.TRIDENT
            && item.getUseAction(itemStack) != UseAction.DRINK
            && item.getUseAction(itemStack) != UseAction.EAT
         : false;
   }
}
