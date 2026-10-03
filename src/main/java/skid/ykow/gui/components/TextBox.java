package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class TextBox extends Component {
   private final StringSetting setting;
   private float hoverAnimation;
   private Color currentColor;
   private final Color TEXT_COLOR;
   private final Color VALUE_COLOR;
   private final Color HOVER_COLOR;
   private final Color INPUT_BG;
   private final Color INPUT_BORDER;
   private final float CORNER_RADIUS = 4.0F;
   private final float HOVER_ANIMATION_SPEED = 0.25F;
   private final int MAX_VISIBLE_CHARS = 7;

   public TextBox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.hoverAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.VALUE_COLOR = new Color(120, 210, 255);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.INPUT_BG = new Color(30, 30, 35);
      this.INPUT_BORDER = new Color(60, 60, 65);
      this.setting = (StringSetting)setting;
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
      int n5 = this.parentY() + this.parentOffset() + this.offset + 9;
      TextRenderer.drawString(String.valueOf(this.setting.getName()), drawContext, n4, n5, this.TEXT_COLOR.getRGB());
      int n6 = n4 + TextRenderer.getWidth(this.setting.getName() + ": ") + 5;
      int n7 = this.parentWidth() - n6 + this.parentX() - 5;
      int n8 = n5 - 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.INPUT_BORDER, n6, n8, n6 + n7, n8 + 18, 4.0, 4.0, 4.0, 4.0, 50.0);
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.INPUT_BG, n6 + 1, n8 + 1, n6 + n7 - 1, n8 + 18 - 1, 3.5, 3.5, 3.5, 3.5, 50.0);
      TextRenderer.drawString(this.formatDisplayValue(this.setting.getValue()), drawContext, n6 + 4, n8 + 3, this.VALUE_COLOR.getRGB());
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

   private String formatDisplayValue(String s) {
      if (s != null && !s.isEmpty()) {
         return s.length() <= 7 ? s : s.substring(0, 4) + "...";
      } else {
         return "...";
      }
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (this.isHovered(n, n2) && n3 == 0) {
         this.mc.setScreen(new StringBox(this, this.setting));
      }

      super.mouseClicked(n, n2, n3);
   }

   @Override
   public void onGuiClose() {
      this.currentColor = null;
      this.hoverAnimation = 0.0F;
      super.onGuiClose();
   }
}
