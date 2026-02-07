package moscow.rockstar.systems.notifications;

import lombok.Generated;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.time.Timer;

public class Notification {
   private final NotificationType type;
   private final String text;
   private final Timer timer = new Timer();
   private final long duration;
   private final Animation animation = new Animation(300L, Easing.EXPO_OUT);
   private final Animation showing = new Animation(300L, Easing.BAKEK_SIZE);

   public Notification(NotificationType type, String text) {
      this.type = type;
      this.text = text;
      this.duration = 1000L;
   }

   public void update() {
      this.animation.update(this.timer.finished(this.duration) ? 0.0F : 1.0F);
   }

   public boolean isFinished() {
      return this.animation.getValue() == 0.0F && this.timer.finished(this.duration);
   }

   @Generated
   public NotificationType getType() {
      return this.type;
   }

   @Generated
   public String getText() {
      return this.text;
   }

   @Generated
   public Timer getTimer() {
      return this.timer;
   }

   @Generated
   public long getDuration() {
      return this.duration;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public Animation getShowing() {
      return this.showing;
   }
}
