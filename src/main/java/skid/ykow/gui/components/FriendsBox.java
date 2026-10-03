package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;

public class FriendsBox extends Component {
   private final FriendsSetting setting;
   private float hoverAnimation;
   private Color currentColor;
   private final Color TEXT_COLOR;
   private final Color HOVER_COLOR;
   private final Color COUNT_BG;
   private final float CORNER_RADIUS = 4.0F;
   private final float HOVER_ANIMATION_SPEED = 0.25F;

   public FriendsBox(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.hoverAnimation = 0.0F;
      this.TEXT_COLOR = new Color(230, 230, 230);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.COUNT_BG = new Color(50, 50, 60, 100);
      this.setting = (FriendsSetting)setting;
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

      int textX = this.parentX() + 5;
      int textY = this.parentY() + this.parentOffset() + this.offset + this.parentHeight() / 2 - 8;
      TextRenderer.drawString(String.valueOf(this.setting.getName()), drawContext, textX, textY, this.TEXT_COLOR.getRGB());
      int friendCount = this.setting.size();
      String countText = String.valueOf(friendCount);
      int badgeWidth = Math.max(24, TextRenderer.getWidth(countText) + 12);
      int badgeX = this.parentX() + this.parentWidth() - badgeWidth - 8;
      int badgeY = this.parentY() + this.parentOffset() + this.offset + (this.parentHeight() - 18) / 2;
      RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.COUNT_BG, badgeX, badgeY, badgeX + badgeWidth, badgeY + 18, 9.0, 9.0, 9.0, 9.0, 50.0);
      TextRenderer.drawCenteredString(
         countText, drawContext, badgeX + badgeWidth / 2, badgeY + 5, Utils.getMainColor(255, this.parent.settings.indexOf(this)).getRGB()
      );
   }

   private void updateAnimations(int n, int n2, float n3) {
      float target = this.isHovered(n, n2) && !this.parent.parent.dragging ? 1.0F : 0.0F;
      this.hoverAnimation = (float)MathUtil.exponentialInterpolate(this.hoverAnimation, target, 0.25, n3 * 0.05F);
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      if (this.isHovered(n, n2) && n3 == 0) {
         this.mc.setScreen(new FriendsFilter(this, this.setting));
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
