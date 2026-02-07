package moscow.rockstar.systems.modules.modules.combat;

import java.util.Comparator;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.prediction.EntityPredictor;
import moscow.rockstar.utility.game.prediction.FallPredictor;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.Nullable;

@ModuleInfo(name = "Auto Totem", category = ModuleCategory.COMBAT, desc = "modules.descriptions.auto_totem")
public class AutoTotem extends BaseModule {
   private final SliderSetting health = new SliderSetting(this, "modules.settings.auto_totem.health").min(1.0F).max(20.0F).step(0.5F).currentValue(6.0F);
   private final SliderSetting healthElytra = new SliderSetting(this, "modules.settings.auto_totem.elytra_health")
      .min(1.0F)
      .max(20.0F)
      .step(0.5F)
      .currentValue(6.0F);
   private final SelectSetting get = new SelectSetting(this, "modules.settings.auto_totem.select_with");
   private final SelectSetting.Value fall = new SelectSetting.Value(this.get, "modules.settings.auto_totem.select_with.fall");
   private final SelectSetting.Value crystal = new SelectSetting.Value(this.get, "modules.settings.auto_totem.select_with.crystal");
   private final SelectSetting.Value tnt = new SelectSetting.Value(this.get, "modules.settings.auto_totem.select_with.tnt");
   private final BooleanSetting deathTNT = new BooleanSetting(this, "modules.settings.auto_totem.death_tnt", () -> !this.tnt.isSelected());
   private int returnSlot = -1;
   private int totemCount = 0;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player != null && mc.world != null) {
         this.updateTotemCount();
         boolean needsTotem = this.checkTotemCondition();
         boolean hasTotemInOffhand = this.findBestTotemSlot() != null && this.findBestTotemSlot().getIdForServer() == 45;
         if (needsTotem && !hasTotemInOffhand) {
            this.equipTotem();
         }

         if (!needsTotem && this.returnSlot != -1) {
            this.returnPreviousItem();
         }
      }
   };
   private final EventListener<HudRenderEvent> onHud = event -> {
      if (this.totemCount > 0) {
         CustomDrawContext ctx = event.getContext();
         Font font = Fonts.BOLD.getFont(7.0F);
         boolean isLeftHanded = mc.options.getMainArm().getValue() == Arm.LEFT;
         float x = isLeftHanded ? sr.getScaledWidth() / 2.0F - 103.0F : sr.getScaledWidth() / 2.0F + 103.0F;
         float y = sr.getScaledHeight() - 21.5F;
         ctx.pushMatrix();
         ctx.drawText(font, Text.of(String.valueOf(this.totemCount)), x - font.width(Text.of(String.valueOf(this.totemCount))) / 2.0F + 8.1F, y + 15.3F);
         ctx.drawItem(Items.TOTEM_OF_UNDYING, x - 8.0F, y + 3.0F, 1.0F);
         ctx.popMatrix();
      }
   };

   private boolean checkTotemCondition() {
      if (mc.player != null && mc.world != null) {
         if (mc.player.getHealth() + mc.player.getAbsorptionAmount()
            <= (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA ? this.healthElytra.getCurrentValue() : this.health.getCurrentValue())
            )
          {
            return true;
         } else {
            if (this.crystal.isSelected()) {
               float maxDmg = 0.0F;

               for (EndCrystalEntity c : mc.world.getEntitiesByClass(EndCrystalEntity.class, mc.player.getBoundingBox().expand(6.0), e -> true)) {
                  maxDmg = Math.max(maxDmg, EntityPredictor.predictDamage(c, mc.player));
               }

               if (maxDmg >= mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                  return true;
               }
            }

            if (this.tnt.isSelected()) {
               if (!this.deathTNT.isEnabled()) {
                  return !mc.world.getEntitiesByClass(TntEntity.class, mc.player.getBoundingBox().expand(6.0), e -> true).isEmpty();
               }

               float maxDMG = 0.0F;

               for (TntEntity tntEntity : mc.world.getEntitiesByClass(TntEntity.class, mc.player.getBoundingBox().expand(6.0), e -> true)) {
                  maxDMG = Math.max(maxDMG, EntityPredictor.predictDamage(tntEntity, mc.player));
               }

               if (maxDMG >= mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
                  return true;
               }
            }

            if (this.fall.isSelected()) {
               float predicted = FallPredictor.predictFallDamage(mc.player, 120);
               return predicted >= mc.player.getHealth() + mc.player.getAbsorptionAmount();
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private void equipTotem() {
      ItemSlot totemSlot = this.findBestTotemSlot();
      if (totemSlot != null) {
         this.returnSlot = totemSlot.getIdForServer();
         InventoryUtility.hotbarSwap(this.returnSlot, 40);
      }
   }

   private void returnPreviousItem() {
      if (this.returnSlot != -1) {
         InventoryUtility.hotbarSwap(this.returnSlot, 40);
         this.returnSlot = -1;
      }
   }

   @Nullable
   private ItemSlot findBestTotemSlot() {
      SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.inventory()).and(SlotGroups.hotbar());
      return slotsToSearch.findItems(Items.TOTEM_OF_UNDYING).stream().min(Comparator.comparingInt(s -> ItemUtility.totemFactor(s.itemStack()))).orElse(null);
   }

   private void updateTotemCount() {
      this.totemCount = SlotGroups.inventory()
         .and(SlotGroups.hotbar())
         .and(SlotGroups.offhand())
         .and(SlotGroups.armor())
         .findItems(Items.TOTEM_OF_UNDYING)
         .size();
   }
}
