package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.BlocksSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class BlocksBox extends Component {
   private final BlocksSetting setting;
   private float hoverAnimation;
   private Color currentColor;
   private final Color TEXT_COLOR;
   private final Color HOVER_COLOR;
   private final Color ITEM_BG;
   private final Color ITEM_BORDER;
   private final float CORNER_RADIUS = 4.0F;
   private final float HOVER_ANIMATION_SPEED = 0.25F;

   public BlocksBox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.hoverAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.ITEM_BG = new Color(30, 30, 35);
      this.ITEM_BORDER = new Color(60, 60, 65);
      this.setting = (BlocksSetting)setting;
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
      String displayText = this.setting.getName() + ": " + this.setting.size() + " block" + (this.setting.size() != 1 ? "s" : "");
      TextRenderer.drawString(displayText, drawContext, n4, n5 - 4, this.TEXT_COLOR.getRGB());
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
      if (this.isHovered(n, n2) && n3 == 0) {
         this.mc.setScreen(new BlocksFilter(this, this.setting));
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
