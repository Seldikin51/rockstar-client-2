package moscow.rockstar.utility.render;

import moscow.rockstar.utility.interfaces.IMinecraft;

public class HookLimiter implements IMinecraft {
   private long lastHookTime;
   private int accumulatedCalls;
   private final boolean useMCFrameRate;
   private int currentFps = 0;
   private long hookIntervalNS = 0L;

   public HookLimiter(boolean useMCFrameRate) {
      this.lastHookTime = System.nanoTime();
      this.useMCFrameRate = useMCFrameRate;
      this.accumulatedCalls = 0;
   }

   public void execute(int fps, IHook... calls) {
      if (this.currentFps != fps) {
         this.hookIntervalNS = 1000000000L / fps;
         this.currentFps = fps;
      }

      long nanoTime = System.nanoTime();
      long elapsed = nanoTime - this.lastHookTime;
      this.accumulatedCalls = this.accumulatedCalls + (int)(elapsed / this.hookIntervalNS);
      this.lastHookTime = this.lastHookTime + this.accumulatedCalls * this.hookIntervalNS;

      for (this.accumulatedCalls = Math.min(this.accumulatedCalls, this.useMCFrameRate ? Math.min(this.currentFps, mc.getCurrentFps()) : this.currentFps);
         this.accumulatedCalls > 0;
         this.accumulatedCalls--
      ) {
         for (IHook call : calls) {
            call.execute();
         }
      }
   }
}
