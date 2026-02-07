package moscow.rockstar.utility.time;

import lombok.Generated;

public class Timer {
   private long millis;

   public Timer() {
      this.reset();
   }

   public boolean finished(long delay) {
      return System.currentTimeMillis() - delay >= this.millis;
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }
}
