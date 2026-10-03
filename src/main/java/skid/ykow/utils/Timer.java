package skid.ykow.utils;

public class Timer {
   public static final String PART = "!@#$%^&*()*+";
   private long lastTime = System.currentTimeMillis();

   public boolean passedMs(long ms) {
      return System.currentTimeMillis() - this.lastTime >= ms;
   }

   public long getPassedTimeMs() {
      return System.currentTimeMillis() - this.lastTime;
   }

   public void reset() {
      this.lastTime = System.currentTimeMillis();
   }

   public void setTime(long time) {
      this.lastTime = time;
   }
}
