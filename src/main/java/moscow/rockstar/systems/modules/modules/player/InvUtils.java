package moscow.rockstar.systems.modules.modules.player;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.time.Timer;

@ModuleInfo(name = "Inventory Utils", category = ModuleCategory.PLAYER)
public class InvUtils extends BaseModule {
   private final SelectSetting util = new SelectSetting(this, "targets");
   private final SelectSetting.Value scroller = new SelectSetting.Value(this.util, "Item Scroller").select();
   private final SelectSetting.Value slotLock = new SelectSetting.Value(this.util, "Slot Lock").select();
   private final SelectSetting lock = new SelectSetting(this, "modules.settings.slot_lock.lock", () -> !this.slotLock.isSelected());
   private final SelectSetting.Value slot1 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot1").select();
   private final SelectSetting.Value slot2 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot2");
   private final SelectSetting.Value slot3 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot3");
   private final SelectSetting.Value slot4 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot4");
   private final SelectSetting.Value slot5 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot5");
   private final SelectSetting.Value slot6 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot6");
   private final SelectSetting.Value slot7 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot7");
   private final SelectSetting.Value slot8 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot8");
   private final SelectSetting.Value slot9 = new SelectSetting.Value(this.lock, "modules.settings.slot_lock.lock.slot9");
   private final SliderSetting scrollDelay = new SliderSetting(this, "delay", () -> !this.scroller.isSelected())
      .currentValue(0.0F)
      .max(150.0F)
      .min(0.0F)
      .step(1.0F);
   private final Timer timer = new Timer();
   private final Timer healTimer = new Timer();
   private float lastHealth = -1.0F;
   private boolean eating;

   public boolean isLocked(int slot) {
      SelectSetting.Value[] slots = new SelectSetting.Value[]{
         this.slot1, this.slot2, this.slot3, this.slot4, this.slot5, this.slot6, this.slot7, this.slot8, this.slot9
      };
      return slot >= 0 && slot < slots.length && slots[slot].isSelected() && this.isEnabled();
   }

   @Generated
   public SelectSetting getUtil() {
      return this.util;
   }

   @Generated
   public SelectSetting.Value getScroller() {
      return this.scroller;
   }

   @Generated
   public SelectSetting.Value getSlotLock() {
      return this.slotLock;
   }

   @Generated
   public SelectSetting getLock() {
      return this.lock;
   }

   @Generated
   public SelectSetting.Value getSlot1() {
      return this.slot1;
   }

   @Generated
   public SelectSetting.Value getSlot2() {
      return this.slot2;
   }

   @Generated
   public SelectSetting.Value getSlot3() {
      return this.slot3;
   }

   @Generated
   public SelectSetting.Value getSlot4() {
      return this.slot4;
   }

   @Generated
   public SelectSetting.Value getSlot5() {
      return this.slot5;
   }

   @Generated
   public SelectSetting.Value getSlot6() {
      return this.slot6;
   }

   @Generated
   public SelectSetting.Value getSlot7() {
      return this.slot7;
   }

   @Generated
   public SelectSetting.Value getSlot8() {
      return this.slot8;
   }

   @Generated
   public SelectSetting.Value getSlot9() {
      return this.slot9;
   }

   @Generated
   public SliderSetting getScrollDelay() {
      return this.scrollDelay;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public Timer getHealTimer() {
      return this.healTimer;
   }

   @Generated
   public float getLastHealth() {
      return this.lastHealth;
   }

   @Generated
   public boolean isEating() {
      return this.eating;
   }
}
