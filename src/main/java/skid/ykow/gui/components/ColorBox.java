package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ColorSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public final class ColorBox extends Component {
   private final ColorSetting setting;
   private float hoverAnimation;
   private Color currentColor;
   private final Color TEXT_COLOR;
   private final Color HOVER_COLOR;
   private final Color COLOR_BORDER;
   private final float CORNER_RADIUS = 4.0F;
   private final float HOVER_ANIMATION_SPEED = 0.25F;
   private boolean colorPickerOpen = false;
   private final Color[] colorPalette = new Color[]{
      Color.RED,
      Color.GREEN,
      Color.BLUE,
      Color.YELLOW,
      Color.CYAN,
      Color.MAGENTA,
      Color.ORANGE,
      Color.PINK,
      Color.WHITE,
      Color.GRAY,
      Color.DARK_GRAY,
      Color.BLACK,
      new Color(255, 100, 100),
      new Color(100, 255, 100),
      new Color(100, 100, 255),
      new Color(255, 255, 100),
      new Color(255, 100, 255),
      new Color(100, 255, 255)
   };

   public ColorBox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.hoverAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.COLOR_BORDER = new Color(60, 60, 65);
      this.setting = (ColorSetting)setting;
   }

   @Override
   public void onUpdate() {
      Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
      if (this.currentColor == null) {
         this.currentColor = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 0);
      } else {
         this.currentColor = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), this.currentColor.getAlpha());
      }

      if (this.currentColor.getAlpha() != 255) {
         this.currentColor = ColorUtil.keyCodec(0.05F, 255, this.currentColor);
      }

      super.onUpdate();
   }

   @Override
   public void render(DrawContext drawContext, int n, int n2, float n3) {
      super.render(drawContext, n, n2, n3);
      this.updateAnimations(n, n2, n3);
      if (!this.parent.parent.dragging) {
         drawContext.fill(
            this.parentX(),
            this.parentY() + this.parentOffset() + this.offset,
            this.parentX() + this.parentWidth(),
            this.parentY() + this.parentOffset() + this.offset + this.parentHeight(),
            new Color(
                  this.HOVER_COLOR.getRed(), this.HOVER_COLOR.getGreen(), this.HOVER_COLOR.getBlue(), (int)(this.HOVER_COLOR.getAlpha() * this.hoverAnimation)
               )
               .getRGB()
         );
      }

      int n4 = this.parentX() + 5;
      int n5 = this.parentY() + this.parentOffset() + this.offset + this.parentHeight() / 2;
      TextRenderer.drawString(String.valueOf(this.setting.getName()), drawContext, n4, n5 - 8, this.TEXT_COLOR.getRGB());
      int n6 = n4 + TextRenderer.getWidth(this.setting.getName() + ": ") + 5;
      int n7 = n5 - 11;
      int colorBoxSize = 20;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.COLOR_BORDER, n6, n7, n6 + 20, n7 + 20, 4.0, 4.0, 4.0, 4.0, 50.0);
      Color settingColor = this.setting.getValue();
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), settingColor, n6 + 1, n7 + 1, n6 + 20 - 1, n7 + 20 - 1, 3.5, 3.5, 3.5, 3.5, 50.0);
      int n8 = n6 + 20 + 10;
      int n9 = n5 - 8;
      String rgbText = String.format("RGB(%d,%d,%d)", settingColor.getRed(), settingColor.getGreen(), settingColor.getBlue());
      TextRenderer.drawString(rgbText, drawContext, n8, n9, this.TEXT_COLOR.getRGB());
      if (this.colorPickerOpen) {
         int pickerX = this.parentX() + this.parentWidth() + 5;
         int pickerY = this.parentY() + this.parentOffset() + this.offset;
         int pickerWidth = 160;
         int pickerHeight = 70;
         if (pickerX + pickerWidth > this.mc.getWindow().getWidth() - 10) {
            pickerX = this.parentX() - pickerWidth - 5;
         }

         if (pickerY + pickerHeight > this.mc.getWindow().getHeight() - 10) {
            pickerY = this.mc.getWindow().getHeight() - pickerHeight - 10;
         }

         if (pickerY < 10) {
            pickerY = 10;
         }

         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(),
            new Color(20, 20, 25, 255),
            pickerX - 2,
            pickerY - 2,
            pickerX + pickerWidth + 2,
            pickerY + pickerHeight + 2,
            6.0,
            6.0,
            6.0,
            6.0,
            50.0
         );
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(), new Color(40, 40, 45, 255), pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerHeight, 4.0, 4.0, 4.0, 4.0, 50.0
         );
         int colorsPerRow = 8;
         int colorSize = 16;
         int spacing = 2;

         for (int i = 0; i < this.colorPalette.length; i++) {
            int row = i / colorsPerRow;
            int col = i % colorsPerRow;
            int colorX = pickerX + 2 + col * (colorSize + spacing);
            int colorY = pickerY + 2 + row * (colorSize + spacing);
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), this.colorPalette[i], colorX, colorY, colorX + colorSize, colorY + colorSize, 2.0, 2.0, 2.0, 2.0, 50.0
            );
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(), new Color(80, 80, 85, 255), colorX, colorY, colorX + colorSize, colorY + colorSize, 2.0, 2.0, 2.0, 2.0, 50.0
            );
         }
      }
   }

   private void updateAnimations(int n, int n2, float n3) {
      float n4;
      if (this.isHovered(n, n2) && !this.parent.parent.dragging) {
         n4 = 1.0F;
      } else {
         n4 = 0.0F;
      }

      this.hoverAnimation = (float)MathUtil.exponentialInterpolate(this.hoverAnimation, n4, 0.25, n3 * 0.05F);
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (this.colorPickerOpen) {
         int pickerX = this.parentX() + this.parentWidth() + 5;
         int pickerY = this.parentY() + this.parentOffset() + this.offset;
         int pickerWidth = 160;
         int pickerHeight = 70;
         if (pickerX + pickerWidth > this.mc.getWindow().getWidth() - 10) {
            pickerX = this.parentX() - pickerWidth - 5;
         }

         if (pickerY + pickerHeight > this.mc.getWindow().getHeight() - 10) {
            pickerY = this.mc.getWindow().getHeight() - pickerHeight - 10;
         }

         if (pickerY < 10) {
            pickerY = 10;
         }

         if (n >= pickerX && n <= pickerX + pickerWidth && n2 >= pickerY && n2 <= pickerY + pickerHeight) {
            int colorsPerRow = 8;
            int colorSize = 16;
            int spacing = 2;
            int relativeX = (int)(n - pickerX);
            int relativeY = (int)(n2 - pickerY);
            int colorIndex = relativeY / (colorSize + spacing) * colorsPerRow + relativeX / (colorSize + spacing);
            if (colorIndex >= 0 && colorIndex < this.colorPalette.length) {
               this.setting.setValue(this.colorPalette[colorIndex]);
               this.colorPickerOpen = false;
            }
         } else {
            this.colorPickerOpen = false;
         }
      } else {
         if (this.isHovered(n, n2)) {
            if (n3 == 0) {
               Color currentColor = this.setting.getValue();
               Color newColor;
               if (currentColor.equals(Color.WHITE)) {
                  newColor = Color.RED;
               } else if (currentColor.equals(Color.RED)) {
                  newColor = Color.GREEN;
               } else if (currentColor.equals(Color.GREEN)) {
                  newColor = Color.BLUE;
               } else if (currentColor.equals(Color.BLUE)) {
                  newColor = Color.YELLOW;
               } else if (currentColor.equals(Color.YELLOW)) {
                  newColor = Color.CYAN;
               } else if (currentColor.equals(Color.CYAN)) {
                  newColor = Color.MAGENTA;
               } else {
                  newColor = Color.WHITE;
               }

               this.setting.setValue(newColor);
            } else if (n3 == 1) {
               int n4 = this.parentX() + 5;
               int n5 = this.parentY() + this.parentOffset() + this.offset + this.parentHeight() / 2;
               int n6 = n4 + TextRenderer.getWidth(this.setting.getName() + ": ") + 5;
               int n7 = n5 - 11;
               int colorBoxSize = 20;
               if (n >= n6 && n <= n6 + 20 && n2 >= n7 && n2 <= n7 + 20) {
                  int currentIndex = this.parent.settings.indexOf(this);
                  if (currentIndex >= 0 && currentIndex < this.parent.settings.size() - 1) {
                     Component nextComponent = this.parent.settings.get(currentIndex + 1);
                     if (nextComponent instanceof ColorBox nextColorBox) {
                        Color currentColor = nextColorBox.setting.getValue();
                        Color newColor;
                        if (currentColor.equals(Color.WHITE)) {
                           newColor = Color.RED;
                        } else if (currentColor.equals(Color.RED)) {
                           newColor = Color.GREEN;
                        } else if (currentColor.equals(Color.GREEN)) {
                           newColor = Color.BLUE;
                        } else if (currentColor.equals(Color.BLUE)) {
                           newColor = Color.YELLOW;
                        } else if (currentColor.equals(Color.YELLOW)) {
                           newColor = Color.CYAN;
                        } else if (currentColor.equals(Color.CYAN)) {
                           newColor = Color.MAGENTA;
                        } else {
                           newColor = Color.WHITE;
                        }

                        nextColorBox.setting.setValue(newColor);
                     } else if (nextComponent.setting instanceof NumberSetting numberSetting) {
                        double currentValue = numberSetting.getValue();
                        double newValue = Math.max(numberSetting.getMin(), currentValue - numberSetting.getFormat());
                        numberSetting.setValue(newValue);
                     } else if (nextComponent.setting instanceof BooleanSetting booleanSetting) {
                        booleanSetting.toggle();
                     } else if (nextComponent.setting instanceof ModeSetting<?> modeSetting) {
                        modeSetting.cycleDown();
                     }
                  }
               } else {
                  this.colorPickerOpen = true;
               }
            }
         }
      }
   }
}
