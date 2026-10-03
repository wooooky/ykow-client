package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.MinMaxSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public final class Slider extends Component {
   private boolean draggingMin;
   private boolean draggingMax;
   private double offsetMinX;
   private double offsetMaxX;
   public double lerpedOffsetMinX;
   public double lerpedOffsetMaxX;
   private float hoverAnimation = 0.0F;
   private final MinMaxSetting setting;
   public Color accentColor1;
   public Color accentColor2;
   private static final Color TEXT_COLOR = new Color(230, 230, 230);
   private static final Color HOVER_COLOR = new Color(255, 255, 255, 20);
   private static final Color TRACK_BG_COLOR = new Color(60, 60, 65);
   private static final Color THUMB_COLOR = new Color(240, 240, 240);
   private static final float TRACK_HEIGHT = 4.0F;
   private static final float TRACK_RADIUS = 2.0F;
   private static final float THUMB_SIZE = 8.0F;
   private static final float ANIMATION_SPEED = 0.25F;

   public Slider(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.setting = (MinMaxSetting)setting;
      this.lerpedOffsetMinX = this.parentX();
      this.lerpedOffsetMaxX = this.parentX() + this.parentWidth();
   }

   @Override
   public void render(DrawContext drawContext, int n, int n2, float n3) {
      super.render(drawContext, n, n2, n3);
      MatrixStack matrices = drawContext.getMatrices();
      this.updateAnimations(n, n2, n3);
      this.offsetMinX = (this.setting.getCurrentMin() - this.setting.getMinValue())
            / (this.setting.getMaxValue() - this.setting.getMinValue())
            * (this.parentWidth() - 10)
         + 5.0;
      this.offsetMaxX = (this.setting.getCurrentMax() - this.setting.getMinValue())
            / (this.setting.getMaxValue() - this.setting.getMinValue())
            * (this.parentWidth() - 10)
         + 5.0;
      this.lerpedOffsetMinX = MathUtil.approachValue((float)(0.5 * n3), this.lerpedOffsetMinX, this.offsetMinX);
      this.lerpedOffsetMaxX = MathUtil.approachValue((float)(0.5 * n3), this.lerpedOffsetMaxX, this.offsetMaxX);
      if (!this.parent.parent.dragging) {
         drawContext.fill(
            this.parentX(),
            this.parentY() + this.parentOffset() + this.offset,
            this.parentX() + this.parentWidth(),
            this.parentY() + this.parentOffset() + this.offset + this.parentHeight(),
            new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), (int)(HOVER_COLOR.getAlpha() * this.hoverAnimation)).getRGB()
         );
      }

      int n4 = this.parentY() + this.offset + this.parentOffset() + 25;
      int n5 = this.parentX() + 5;
      RenderUtils.renderRoundedQuad(matrices, TRACK_BG_COLOR, n5, n4, n5 + (this.parentWidth() - 10), n4 + 4.0F, 2.0, 2.0, 2.0, 2.0, 50.0);
      if (this.lerpedOffsetMaxX > this.lerpedOffsetMinX) {
         RenderUtils.renderRoundedQuad(
            matrices, this.accentColor1, n5 + this.lerpedOffsetMinX - 5.0, n4, n5 + this.lerpedOffsetMaxX - 5.0, n4 + 4.0F, 2.0, 2.0, 2.0, 2.0, 50.0
         );
      }

      String displayText = this.getDisplayText();
      TextRenderer.drawString(
         this.setting.getName(), drawContext, this.parentX() + 5, this.parentY() + this.parentOffset() + this.offset + 9, TEXT_COLOR.getRGB()
      );
      TextRenderer.drawString(
         displayText,
         drawContext,
         this.parentX() + this.parentWidth() - TextRenderer.getWidth(displayText) - 5,
         this.parentY() + this.parentOffset() + this.offset + 9,
         this.accentColor1.getRGB()
      );
      float n6 = n4 + 2.0F - 4.0F;
      RenderUtils.renderRoundedQuad(
         matrices,
         THUMB_COLOR,
         (float)(n5 + this.lerpedOffsetMinX - 5.0 - 4.0),
         n6,
         (float)(n5 + this.lerpedOffsetMinX - 5.0 + 4.0),
         n6 + 8.0F,
         4.0,
         4.0,
         4.0,
         4.0,
         50.0
      );
      RenderUtils.renderRoundedQuad(
         matrices,
         THUMB_COLOR,
         (float)(n5 + this.lerpedOffsetMaxX - 5.0 - 4.0),
         n6,
         (float)(n5 + this.lerpedOffsetMaxX - 5.0 + 4.0),
         n6 + 8.0F,
         4.0,
         4.0,
         4.0,
         4.0,
         50.0
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

   private String getDisplayText() {
      return this.setting.getCurrentMin() == this.setting.getCurrentMax()
         ? this.formatValue(this.setting.getCurrentMin())
         : this.formatValue(this.setting.getCurrentMin()) + " - " + this.formatValue(this.setting.getCurrentMax());
   }

   private String formatValue(double value) {
      double step = this.setting.getStep();
      if (step == 0.1) {
         return String.format("%.1f", value);
      } else if (step == 0.01) {
         return String.format("%.2f", value);
      } else if (step == 0.001) {
         return String.format("%.3f", value);
      } else {
         return step >= 1.0 ? String.format("%.0f", value) : String.valueOf(value);
      }
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (n3 == 0 && this.isHovered(n, n2)) {
         if (this.isHoveredMin(n, n2)) {
            this.draggingMin = true;
            this.slideMin(n);
         } else if (this.isHoveredMax(n, n2)) {
            this.draggingMax = true;
            this.slideMax(n);
         } else if (n < this.parentX() + this.offsetMinX) {
            this.draggingMin = true;
            this.slideMin(n);
         } else if (n > this.parentX() + this.offsetMaxX) {
            this.draggingMax = true;
            this.slideMax(n);
         } else if (n - (this.parentX() + this.offsetMinX) < this.parentX() + this.offsetMaxX - n) {
            this.draggingMin = true;
            this.slideMin(n);
         } else {
            this.draggingMax = true;
            this.slideMax(n);
         }
      }

      super.mouseClicked(n, n2, n3);
   }

   @Override
   public void keyPressed(int n, int n2, int n3) {
      if (this.mouseOver && n == 259) {
         this.setting.setCurrentMax(this.setting.getDefaultMax());
         this.setting.setCurrentMin(this.setting.getDefaultMin());
      }

      super.keyPressed(n, n2, n3);
   }

   public boolean isHoveredMin(double n, double n2) {
      return this.isHovered(n, n2) && n > this.parentX() + this.offsetMinX - 8.0 && n < this.parentX() + this.offsetMinX + 8.0;
   }

   public boolean isHoveredMax(double n, double n2) {
      return this.isHovered(n, n2) && n > this.parentX() + this.offsetMaxX - 8.0 && n < this.parentX() + this.offsetMaxX + 8.0;
   }

   @Override
   public void mouseReleased(double n, double n2, int n3) {
      if (n3 == 0) {
         this.draggingMin = false;
         this.draggingMax = false;
      }

      super.mouseReleased(n, n2, n3);
   }

   @Override
   public void mouseDragged(double n, double n2, int n3, double n4, double n5) {
      if (this.draggingMin) {
         this.slideMin(n);
      }

      if (this.draggingMax) {
         this.slideMax(n);
      }

      super.mouseDragged(n, n2, n3, n4, n5);
   }

   @Override
   public void onGuiClose() {
      this.accentColor1 = null;
      this.accentColor2 = null;
      this.hoverAnimation = 0.0F;
      super.onGuiClose();
   }

   private void slideMin(double n) {
      this.setting
         .setCurrentMin(
            Math.min(
               MathUtil.roundToNearest(
                  MathHelper.clamp((n - (this.parentX() + 5)) / (this.parentWidth() - 10), 0.0, 1.0)
                        * (this.setting.getMaxValue() - this.setting.getMinValue())
                     + this.setting.getMinValue(),
                  this.setting.getStep()
               ),
               this.setting.getCurrentMax()
            )
         );
   }

   private void slideMax(double n) {
      this.setting
         .setCurrentMax(
            Math.max(
               MathUtil.roundToNearest(
                  MathHelper.clamp((n - (this.parentX() + 5)) / (this.parentWidth() - 10), 0.0, 1.0)
                        * (this.setting.getMaxValue() - this.setting.getMinValue())
                     + this.setting.getMinValue(),
                  this.setting.getStep()
               ),
               this.setting.getCurrentMin()
            )
         );
   }

   @Override
   public void onUpdate() {
      Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
      Color mainColor2 = Utils.getMainColor(255, this.parent.settings.indexOf(this) + 1);
      if (this.accentColor1 == null) {
         this.accentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 0);
      } else {
         this.accentColor1 = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), this.accentColor1.getAlpha());
      }

      if (this.accentColor2 == null) {
         this.accentColor2 = new Color(mainColor2.getRed(), mainColor2.getGreen(), mainColor2.getBlue(), 0);
      } else {
         this.accentColor2 = new Color(mainColor2.getRed(), mainColor2.getGreen(), mainColor2.getBlue(), this.accentColor2.getAlpha());
      }

      if (this.accentColor1.getAlpha() != 255) {
         this.accentColor1 = ColorUtil.keyCodec(0.05F, 255, this.accentColor1);
      }

      if (this.accentColor2.getAlpha() != 255) {
         this.accentColor2 = ColorUtil.keyCodec(0.05F, 255, this.accentColor2);
      }

      super.onUpdate();
   }
}
