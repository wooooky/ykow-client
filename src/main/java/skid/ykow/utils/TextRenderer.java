package skid.ykow.utils;

import skid.ykow.font.Fonts;
import skid.ykow.module.modules.client.Ykow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public final class TextRenderer {
   public static void drawString(CharSequence text, DrawContext drawContext, int x, int y, int color) {
      if (Ykow.useCustomFont.getValue() && Fonts.FONT != null) {
         Fonts.FONT.drawString(drawContext.getMatrices(), text, (float)x, (float)y, color);
      } else {
         drawLargeString(text, drawContext, x, y, color);
      }
   }

   public static int getWidth(CharSequence text) {
      return Ykow.useCustomFont.getValue() && Fonts.FONT != null
         ? Fonts.FONT.getStringWidth(text)
         : skid.ykow.Ykow.mc.textRenderer.getWidth(text.toString()) * 2;
   }

   public static void drawCenteredString(CharSequence text, DrawContext drawContext, int x, int y, int color) {
      if (Ykow.useCustomFont.getValue() && Fonts.FONT != null) {
         Fonts.FONT.drawString(drawContext.getMatrices(), text, (float)(x - Fonts.FONT.getStringWidth(text) / 2), (float)y, color);
      } else {
         drawCenteredMinecraftText(text, drawContext, x, y, color);
      }
   }

   public static void drawLargeString(CharSequence text, DrawContext drawContext, int x, int y, int color) {
      MatrixStack matrices = drawContext.getMatrices();
      matrices.push();
      matrices.scale(2.0F, 2.0F, 2.0F);
      drawContext.drawText(skid.ykow.Ykow.mc.textRenderer, text.toString(), x / 2, y / 2, color, false);
      matrices.scale(1.0F, 1.0F, 1.0F);
      matrices.pop();
   }

   public static void drawCenteredMinecraftText(CharSequence text, DrawContext drawContext, int x, int y, int color) {
      MatrixStack matrices = drawContext.getMatrices();
      matrices.push();
      matrices.scale(2.0F, 2.0F, 2.0F);
      drawContext.drawText(
         skid.ykow.Ykow.mc.textRenderer, (String)text, x / 2 - skid.ykow.Ykow.mc.textRenderer.getWidth((String)text) / 2, y / 2, color, false
      );
      matrices.scale(1.0F, 1.0F, 1.0F);
      matrices.pop();
   }
}
