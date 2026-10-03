package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class NumberBox extends Component {
   public boolean dragging;
   public double offsetX;
   public double lerpedOffsetX;
   private float hoverAnimation;
   private final NumberSetting setting;
   public Color currentColor1;
   private Color currentAlpha;
   private final Color TEXT_COLOR;
   private final Color HOVER_COLOR;
   private final Color TRACK_BG_COLOR;
   private final float TRACK_HEIGHT = 4.0F;
   private final float TRACK_RADIUS = 2.0F;
   private final float ANIMATION_SPEED = 0.25F;

   public NumberBox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.lerpedOffsetX = 0.0;
      this.hoverAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.TRACK_BG_COLOR = new Color(60, 60, 65);
      this.setting = (NumberSetting)setting;
   }

   @Override
   public void onUpdate() {
      Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
      if (this.currentColor1 == null) {
         this.currentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 0);
      } else {
         this.currentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), this.currentColor1.getAlpha());
      }

      if (this.currentColor1.getAlpha() != 255) {
         this.currentColor1 = ColorUtil.keyCodec(0.05F, 255, this.currentColor1);
      }

      super.onUpdate();
   }

   @Override
   public void render(DrawContext drawContext, int n, int n2, float n3) {
      super.render(drawContext, n, n2, n3);
      this.updateAnimations(n, n2, n3);
      this.offsetX = (this.setting.getValue() - this.setting.getMin()) / (this.setting.getMax() - this.setting.getMin()) * this.parentWidth();
      this.lerpedOffsetX = MathUtil.approachValue((float)(0.5 * n3), this.lerpedOffsetX, this.offsetX);
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

      int n4 = this.parentY() + this.offset + this.parentOffset() + 25;
      int n5 = this.parentX() + 5;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.TRACK_BG_COLOR, n5, n4, n5 + (this.parentWidth() - 10), n4 + 4.0F, 2.0, 2.0, 2.0, 2.0, 50.0);
      if (this.lerpedOffsetX > 2.5) {
         if (this.currentColor1 == null) {
            Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
            this.currentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 255);
         }

         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(), this.currentColor1, n5, n4, n5 + Math.max(this.lerpedOffsetX - 5.0, 0.0), n4 + 4.0F, 2.0, 2.0, 2.0, 2.0, 50.0
         );
      }

      String displayValue = this.getDisplayValue();
      TextRenderer.drawString(
         this.setting.getName(), drawContext, this.parentX() + 5, this.parentY() + this.parentOffset() + this.offset + 9, this.TEXT_COLOR.getRGB()
      );
      if (this.currentColor1 == null) {
         Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
         this.currentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 255);
      }

      TextRenderer.drawString(
         displayValue,
         drawContext,
         this.parentX() + this.parentWidth() - TextRenderer.getWidth(displayValue) - 5,
         this.parentY() + this.parentOffset() + this.offset + 9,
         this.currentColor1.getRGB()
      );
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

   private String getDisplayValue() {
      double value = this.setting.getValue();
      double format = this.setting.getFormat();
      if (format == 0.1) {
         return String.format("%.1f", value);
      } else if (format == 0.01) {
         return String.format("%.2f", value);
      } else if (format == 0.001) {
         return String.format("%.3f", value);
      } else if (format == 1.0E-4) {
         return String.format("%.4f", value);
      } else {
         return format >= 1.0 ? String.format("%.0f", value) : String.valueOf(value);
      }
   }

   @Override
   public void onGuiClose() {
      this.currentColor1 = null;
      this.hoverAnimation = 0.0F;
      super.onGuiClose();
   }

   private void slide(double n) {
      this.setting
         .getValue(
            MathUtil.roundToNearest(
               MathHelper.clamp((n - (this.parentX() + 5)) / (this.parentWidth() - 10), 0.0, 1.0) * (this.setting.getMax() - this.setting.getMin())
                  + this.setting.getMin(),
               this.setting.getFormat()
            )
         );
   }

   @Override
   public void keyPressed(int n, int n2, int n3) {
      if (this.mouseOver && this.parent.extended && n == 259) {
         this.setting.getValue(this.setting.getDefaultValue());
      }

      super.keyPressed(n, n2, n3);
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (this.isHovered(n, n2) && n3 == 0) {
         this.dragging = true;
         this.slide(n);
      }

      super.mouseClicked(n, n2, n3);
   }

   @Override
   public void mouseReleased(double n, double n2, int n3) {
      if (this.dragging && n3 == 0) {
         this.dragging = false;
      }

      super.mouseReleased(n, n2, n3);
   }

   @Override
   public void mouseDragged(double n, double n2, int n3, double n4, double n5) {
      if (this.dragging) {
         this.slide(n);
      }

      super.mouseDragged(n, n2, n3, n4, n5);
   }
}
