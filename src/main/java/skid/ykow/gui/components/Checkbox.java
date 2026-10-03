package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public final class Checkbox extends Component {
   private final BooleanSetting setting;
   private float hoverAnimation;
   private float enabledAnimation;
   private final float CORNER_RADIUS = 3.0F;
   private final Color TEXT_COLOR;
   private final Color HOVER_COLOR;
   private final Color BOX_BORDER;
   private final Color BOX_BG;
   private final int BOX_SIZE = 13;
   private final float HOVER_ANIMATION_SPEED = 0.005F;
   private final float TOGGLE_ANIMATION_SPEED = 0.002F;

   public Checkbox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.hoverAnimation = 0.0F;
      this.enabledAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.BOX_BORDER = new Color(100, 100, 110);
      this.BOX_BG = new Color(40, 40, 45);
      this.setting = (BooleanSetting)setting;
      float enabledAnimation;
      if (this.setting.getValue()) {
         enabledAnimation = 1.0F;
      } else {
         enabledAnimation = 0.0F;
      }

      this.enabledAnimation = enabledAnimation;
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

      TextRenderer.drawString(
         this.setting.getName(),
         drawContext,
         this.parentX() + 27,
         this.parentY() + this.parentOffset() + this.offset + this.parentHeight() / 2 - 6,
         this.TEXT_COLOR.getRGB()
      );
      this.renderModernCheckbox(drawContext);
   }

   private void updateAnimations(int n, int n2, float n3) {
      float n4 = n3 * 0.05F;
      float n5;
      if (this.isHovered(n, n2) && !this.parent.parent.dragging) {
         n5 = 1.0F;
      } else {
         n5 = 0.0F;
      }

      this.hoverAnimation = (float)MathUtil.exponentialInterpolate(this.hoverAnimation, n5, 0.005F, n4);
      float n6;
      if (this.setting.getValue()) {
         n6 = 1.0F;
      } else {
         n6 = 0.0F;
      }

      this.enabledAnimation = (float)MathUtil.exponentialInterpolate(this.enabledAnimation, n6, 0.002F, n4);
      this.enabledAnimation = (float)MathUtil.clampValue(this.enabledAnimation, 0.0, 1.0);
   }

   private void renderModernCheckbox(DrawContext drawContext) {
      int checkboxX = this.parentX() + 8;
      int checkboxY = this.parentY() + this.parentOffset() + this.offset + this.parentHeight() / 2 - 6;
      Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.BOX_BORDER, checkboxX, checkboxY, checkboxX + 13, checkboxY + 13, 3.0, 3.0, 3.0, 3.0, 50.0);
      RenderUtils.renderRoundedQuad(
         drawContext.getMatrices(), this.BOX_BG, checkboxX + 1, checkboxY + 1, checkboxX + 13 - 1, checkboxY + 13 - 1, 2.5, 2.5, 2.5, 2.5, 50.0
      );
      if (this.enabledAnimation > 0.01F) {
         Color color = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), (int)(255.0F * this.enabledAnimation));
         float checkmarkX = checkboxX + 2 + 9.0F * (1.0F - this.enabledAnimation) / 2.0F;
         float checkmarkY = checkboxY + 2 + 9.0F * (1.0F - this.enabledAnimation) / 2.0F;
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(),
            color,
            checkmarkX,
            checkmarkY,
            checkmarkX + 9.0F * this.enabledAnimation,
            checkmarkY + 9.0F * this.enabledAnimation,
            1.5,
            1.5,
            1.5,
            1.5,
            50.0
         );
         if (this.enabledAnimation > 0.7F) {
            RenderUtils.renderRoundedQuad(
               drawContext.getMatrices(),
               new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), (int)(40.0F * ((this.enabledAnimation - 0.7F) * 3.33F))),
               checkboxX - 1,
               checkboxY - 1,
               checkboxX + 13 + 1,
               checkboxY + 13 + 1,
               3.5,
               3.5,
               3.5,
               3.5,
               50.0
            );
         }
      }
   }

   @Override
   public void keyPressed(int n, int n2, int n3) {
      if (this.mouseOver && this.parent.extended && n == 259) {
         this.setting.setValue(this.setting.getDefaultValue());
      }

      super.keyPressed(n, n2, n3);
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (this.isHovered(n, n2) && n3 == 0) {
         this.setting.toggle();
      }

      super.mouseClicked(n, n2, n3);
   }

   @Override
   public void onGuiClose() {
      super.onGuiClose();
      this.hoverAnimation = 0.0F;
      float enabledAnimation;
      if (this.setting.getValue()) {
         enabledAnimation = 1.0F;
      } else {
         enabledAnimation = 0.0F;
      }

      this.enabledAnimation = enabledAnimation;
   }
}
