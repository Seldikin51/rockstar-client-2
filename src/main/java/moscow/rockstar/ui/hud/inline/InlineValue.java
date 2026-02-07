package moscow.rockstar.ui.hud.inline;

import lombok.Generated;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.time.Timer;

public class InlineValue extends SelectSetting.Value {
   private String text = "?";
   private String copy = "";
   private final String suffix;
   private boolean copied;
   private final Timer copyTimer = new Timer();
   private final Animation copyAnim = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation successAnim = new Animation(500L, 0.0F, Easing.BAKEK_SIZE);

   public InlineValue(SelectSetting parent, String name, String suffix) {
      super(parent, name);
      this.select();
      this.suffix = " " + suffix;
   }

   public InlineValue(SelectSetting parent, String name) {
      super(parent, name);
      this.select();
      this.suffix = "";
   }

   public void update(String text, String copy) {
      if (this.isSelected()) {
         this.text = text;
         this.copy = copy;
      }
   }

   public void update(String text) {
      if (this.isSelected()) {
         this.text = text;
      }
   }

   @Generated
   public String text() {
      return this.text;
   }

   @Generated
   public String copy() {
      return this.copy;
   }

   @Generated
   public String suffix() {
      return this.suffix;
   }

   @Generated
   public boolean copied() {
      return this.copied;
   }

   @Generated
   public Timer copyTimer() {
      return this.copyTimer;
   }

   @Generated
   public Animation copyAnim() {
      return this.copyAnim;
   }

   @Generated
   public Animation successAnim() {
      return this.successAnim;
   }

   @Generated
   public InlineValue copied(boolean copied) {
      this.copied = copied;
      return this;
   }
}
