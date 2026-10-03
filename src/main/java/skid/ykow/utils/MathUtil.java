package skid.ykow.utils;

public final class MathUtil {
   public static double roundToNearest(double value, double step) {
      return step * Math.round(value / step);
   }

   public static double smoothStep(double factor, double start, double end) {
      double clampedFactor = Math.max(0.0, Math.min(1.0, factor));
      return start + (end - start) * (clampedFactor * clampedFactor * (3.0 - 2.0 * clampedFactor));
   }

   public static double approachValue(float speed, double current, double target) {
      double stepSize = Math.ceil(Math.abs(target - current) * speed);
      return current < target ? Math.min(current + (int)stepSize, target) : Math.max(current - (int)stepSize, target);
   }

   public static double linearInterpolate(double factor, double start, double end) {
      return start + (end - start) * factor;
   }

   public static double exponentialInterpolate(double start, double end, double base, double exponent) {
      return linearInterpolate(1.0F - (float)Math.pow(base, exponent), start, end);
   }

   public static double clampValue(double value, double min, double max) {
      return Math.max(min, Math.min(value, max));
   }

   public static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   public static int randomInt(int min, int max) {
      return (int)(Math.random() * (max - min + 1)) + min;
   }
}
