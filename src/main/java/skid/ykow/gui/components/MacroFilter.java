package skid.ykow.gui.components;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.MacroSetting;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MacroFilter extends Screen {
   private final MacroSetting setting;
   private String newCommand;
   private int cursorPosition;
   private long lastCursorBlink;
   private boolean cursorVisible;
   private final int CURSOR_BLINK_SPEED = 530;
   private int scrollOffset;
   private final int COMMANDS_PER_PAGE = 6;
   final MacroBox this$0;

   public MacroFilter(MacroBox this$0, MacroSetting setting) {
      super(Text.empty());
      this.this$0 = this$0;
      this.newCommand = "";
      this.cursorPosition = 0;
      this.lastCursorBlink = 0L;
      this.cursorVisible = true;
      this.scrollOffset = 0;
      this.setting = setting;
   }

   public void render(DrawContext drawContext, int n, int n2, float n3) {
      RenderUtils.unscaledProjection();
      int n4 = n * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      int n5 = n2 * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
      super.render(drawContext, n4, n5, n3);
      int width = this.this$0.mc.getWindow().getWidth();
      int height = this.this$0.mc.getWindow().getHeight();
      int a;
      if (Ykow.renderBackground.getValue()) {
         a = 180;
      } else {
         a = 0;
      }

      drawContext.fill(0, 0, width, height, new Color(0, 0, 0, a).getRGB());
      double scale = Ykow.guiScale.getValue();
      int baseWidth = (int)(600.0 * scale);
      int baseHeight = (int)(450.0 * scale);
      int n6 = (this.this$0.mc.getWindow().getWidth() - baseWidth) / 2;
      int n7 = (this.this$0.mc.getWindow().getHeight() - baseHeight) / 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(30, 30, 35, 240), n6, n7, n6 + baseWidth, n7 + baseHeight, 8.0, 8.0, 8.0, 8.0, 20.0);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 45, 255), n6, n7, n6 + baseWidth, n7 + 30, 8.0, 8.0, 0.0, 0.0, 20.0);
      drawContext.fill(n6, n7 + 30, n6 + baseWidth, n7 + 31, Utils.getMainColor(255, 1).getRGB());
      TextRenderer.drawCenteredString(
         "Macro Editor: " + this.setting.getName(), drawContext, n6 + baseWidth / 2, n7 + 8, new Color(245, 245, 245, 255).getRGB()
      );
      int n8 = n6 + 20;
      int n9 = n7 + 50;
      int inputWidth = baseWidth - 40;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(20, 20, 25, 255), n8, n9, n8 + inputWidth, n9 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      RenderUtils.renderRoundedOutline(drawContext, new Color(60, 60, 65, 255), n8, n9, n8 + inputWidth, n9 + 30, 5.0, 5.0, 5.0, 5.0, 2.0, 20.0);
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis - this.lastCursorBlink > 530L) {
         this.cursorVisible = !this.cursorVisible;
         this.lastCursorBlink = currentTimeMillis;
      }

      String displayText = this.newCommand;
      if (this.cursorVisible) {
         displayText = displayText + "|";
      }

      TextRenderer.drawString("Add Command: " + displayText, drawContext, n8 + 10, n9 + 8, new Color(50, 50, 50, 255).getRGB());
      int n10 = n8 + inputWidth - 80;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(50, 205, 50, 255), n10, n9, n10 + 80, n9 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Add", drawContext, n10 + 40, n9 + 8, new Color(245, 245, 245, 255).getRGB());
      int n11 = n7 + 100;
      int n12 = n8;
      int n13 = n9 + 30 + 20;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 45, 255), n8, n13, n8 + inputWidth, n13 + 25, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString(
         "Commands (" + this.setting.getCommands().size() + ")", drawContext, n8 + inputWidth / 2, n13 + 5, new Color(245, 245, 245, 255).getRGB()
      );
      List<String> commands = this.setting.getCommands();
      int startIndex = this.scrollOffset * 6;
      int endIndex = Math.min(startIndex + 6, commands.size());

      for (int i = startIndex; i < endIndex; i++) {
         int commandY = n13 + 30 + (i - startIndex) * 35;
         String command = commands.get(i);
         int commandWidth = inputWidth - 100;
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(), new Color(255, 255, 255, 200), n12, commandY, n12 + commandWidth, commandY + 30, 5.0, 5.0, 5.0, 5.0, 20.0
         );
         RenderUtils.renderRoundedOutline(
            drawContext, new Color(60, 60, 65, 255), n12, commandY, n12 + commandWidth, commandY + 30, 5.0, 5.0, 5.0, 5.0, 1.0, 20.0
         );
         TextRenderer.drawString(i + 1 + ". " + command, drawContext, n12 + 10, commandY + 8, new Color(50, 50, 50, 255).getRGB());
         int deleteX = n12 + commandWidth + 5;
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(), new Color(220, 20, 60, 255), deleteX, commandY, deleteX + 75, commandY + 30, 5.0, 5.0, 5.0, 5.0, 20.0
         );
         TextRenderer.drawCenteredString("Delete", drawContext, deleteX + 37, commandY + 8, new Color(245, 245, 245, 255).getRGB());
      }

      int n14 = n7 + baseHeight - 45;
      int n15 = n6 + baseWidth - 80 - 20;
      int n16 = n15 - 80 - 10;
      int n17 = n16 - 80 - 10;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(50, 205, 50, 255), n15, n14, n15 + 80, n14 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Save", drawContext, n15 + 40, n14 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(128, 128, 128, 255), n16, n14, n16 + 80, n14 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Cancel", drawContext, n16 + 40, n14 + 8, new Color(245, 245, 245, 255).getRGB());
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(220, 20, 60, 255), n17, n14, n17 + 80, n14 + 30, 5.0, 5.0, 5.0, 5.0, 20.0);
      TextRenderer.drawCenteredString("Clear All", drawContext, n17 + 40, n14 + 8, new Color(245, 245, 245, 255).getRGB());
      if (commands.size() > 6) {
         int arrowY = n13 + 30 + 6 * 35 + 10;
         if (this.scrollOffset > 0) {
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), new Color(255, 105, 180, 255), n12, arrowY, n12 + 40, arrowY + 20, 5.0, 5.0, 5.0, 5.0, 20.0
            );
            TextRenderer.drawCenteredString("←", drawContext, n12 + 20, arrowY + 5, new Color(245, 245, 245, 255).getRGB());
         }

         if ((this.scrollOffset + 1) * 6 < commands.size()) {
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), new Color(255, 105, 180, 255), n12 + inputWidth - 40, arrowY, n12 + inputWidth, arrowY + 20, 5.0, 5.0, 5.0, 5.0, 20.0
            );
            TextRenderer.drawCenteredString("→", drawContext, n12 + inputWidth - 20, arrowY + 5, new Color(245, 245, 245, 255).getRGB());
         }
      }

      RenderUtils.scaledProjection();
   }

   public boolean mouseClicked(double n, double n2, int n3) {
      double n4 = n * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double n5 = n2 * MinecraftClient.getInstance().getWindow().getScaleFactor();
      double scale = Ykow.guiScale.getValue();
      int baseWidth = (int)(600.0 * scale);
      int baseHeight = (int)(450.0 * scale);
      int n6 = (this.this$0.mc.getWindow().getWidth() - baseWidth) / 2;
      int n7 = (this.this$0.mc.getWindow().getHeight() - baseHeight) / 2;
      int n8 = n7 + baseHeight - 45;
      int n9 = n6 + baseWidth - 80 - 20;
      int n10 = n9 - 80 - 10;
      int n11 = n10 - 80 - 10;
      if (this.isInBounds(n4, n5, n9, n8, 80, 30)) {
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (this.isInBounds(n4, n5, n10, n8, 80, 30)) {
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (this.isInBounds(n4, n5, n11, n8, 80, 30)) {
         this.setting.clearCommands();
         return true;
      } else {
         int n12 = n6 + 20;
         int n13 = n7 + 50;
         int inputWidth = baseWidth - 40;
         if (this.isInBounds(n4, n5, n12 + inputWidth - 80, n13, 80, 30)) {
            if (!this.newCommand.trim().isEmpty()) {
               this.setting.addCommand(this.newCommand.trim());
               this.newCommand = "";
               this.cursorPosition = 0;
            }

            return true;
         } else {
            List<String> commands = this.setting.getCommands();
            int startIndex = this.scrollOffset * 6;
            int endIndex = Math.min(startIndex + 6, commands.size());

            for (int i = startIndex; i < endIndex; i++) {
               int commandY = n7 + 100 + 30 + 20 + 30 + (i - startIndex) * 35;
               int commandWidth = inputWidth - 100;
               int deleteX = n6 + 20 + commandWidth + 5;
               if (this.isInBounds(n4, n5, deleteX, commandY, 75, 30)) {
                  this.setting.removeCommand(i);
                  return true;
               }
            }

            if (commands.size() > 6) {
               int arrowY = n7 + 100 + 30 + 20 + 30 + 6 * 35 + 10;
               if (this.scrollOffset > 0 && this.isInBounds(n4, n5, n6 + 20, arrowY, 40, 20)) {
                  this.scrollOffset--;
                  return true;
               }

               if ((this.scrollOffset + 1) * 6 < commands.size() && this.isInBounds(n4, n5, n6 + 20 + inputWidth - 40, arrowY, 40, 20)) {
                  this.scrollOffset++;
                  return true;
               }
            }

            return super.mouseClicked(n4, n5, n3);
         }
      }
   }

   public boolean keyPressed(int n, int n2, int n3) {
      this.cursorVisible = true;
      this.lastCursorBlink = System.currentTimeMillis();
      if (n == 256) {
         this.this$0.mc.setScreen(skid.ykow.Ykow.INSTANCE.GUI);
         return true;
      } else if (n == 257) {
         if (!this.newCommand.trim().isEmpty()) {
            this.setting.addCommand(this.newCommand.trim());
            this.newCommand = "";
            this.cursorPosition = 0;
         }

         return true;
      } else if (n == 259) {
         if (this.cursorPosition > 0) {
            this.newCommand = this.newCommand.substring(0, this.cursorPosition - 1) + this.newCommand.substring(this.cursorPosition);
            this.cursorPosition--;
         }

         return true;
      } else if (n == 262) {
         if (this.cursorPosition < this.newCommand.length()) {
            this.cursorPosition++;
         }

         return true;
      } else if (n == 263) {
         if (this.cursorPosition > 0) {
            this.cursorPosition--;
         }

         return true;
      } else {
         return super.keyPressed(n, n2, n3);
      }
   }

   public boolean charTyped(char c, int n) {
      if (Character.isISOControl(c)) {
         return false;
      } else {
         this.newCommand = this.newCommand.substring(0, this.cursorPosition) + c + this.newCommand.substring(this.cursorPosition);
         this.cursorPosition++;
         return true;
      }
   }

   private boolean isInBounds(double n, double n2, int n3, int n4, int n5, int n6) {
      return n >= n3 && n <= n3 + n5 && n2 >= n4 && n2 <= n4 + n6;
   }
}
