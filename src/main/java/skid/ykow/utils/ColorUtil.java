package skid.ykow.utils;

import java.awt.Color;

public final class ColorUtil {
   public static Color getRainbowColor(int offset, int alpha) {
      Color hsbColor = Color.getHSBColor((float)((System.currentTimeMillis() * 3L + offset * 175) % 7200L) / 7200.0F, 0.6F, 1.0F);
      return new Color(hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), alpha);
   }

   public static Color getBreathingColor(Color baseColor, int offset, int speed) {
      float[] hsbValues = new float[3];
      Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), hsbValues);
      hsbValues[2] = 0.25F + 0.75F * Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)offset / speed * 2.0F) % 2.0F - 1.0F) % 2.0F;
      int rgbValue = Color.HSBtoRGB(hsbValues[0], hsbValues[1], hsbValues[2]);
      return new Color(rgbValue >> 16 & 0xFF, rgbValue >> 8 & 0xFF, rgbValue & 0xFF, baseColor.getAlpha());
   }

   public static Color interpolateColors(float interpolation, Color color1, Color color2) {
      return new Color(
         (int)MathUtil.approachValue(interpolation, color2.getRed(), color1.getRed()),
         (int)MathUtil.approachValue(interpolation, color2.getGreen(), color1.getGreen()),
         (int)MathUtil.approachValue(interpolation, color2.getBlue(), color1.getBlue())
      );
   }

   public static Color interpolateAlpha(float interpolation, int targetAlpha, Color color) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)MathUtil.approachValue(interpolation, color.getAlpha(), targetAlpha));
   }

   public static Color smoothInterpolate(Color color1, Color color2, float interpolation) {
      return new Color(
         clampValue(Math.round(color1.getRed() + interpolation * (color2.getRed() - color1.getRed())), 0, 255),
         clampValue(Math.round(color1.getGreen() + interpolation * (color2.getGreen() - color1.getGreen())), 0, 255),
         clampValue(Math.round(color1.getBlue() + interpolation * (color2.getBlue() - color1.getBlue())), 0, 255),
         clampValue(Math.round(color1.getAlpha() + interpolation * (color2.getAlpha() - color1.getAlpha())), 0, 255)
      );
   }

   private static int clampValue(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   public static Color keyCodec(int offset, int alpha) {
      return getRainbowColor(offset, alpha);
   }

   public static Color keyCodec(float interpolation, Color color1, Color color2) {
      return interpolateColors(interpolation, color1, color2);
   }

   public static Color keyCodec(float interpolation, int targetAlpha, Color color) {
      return interpolateAlpha(interpolation, targetAlpha, color);
   }

   public static Color keyCodec(Color color1, Color color2, float interpolation) {
      return smoothInterpolate(color1, color2, interpolation);
   }
}
