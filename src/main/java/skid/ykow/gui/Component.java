package skid.ykow.gui;

import skid.ykow.gui.components.ModuleButton;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Component {
   public MinecraftClient mc = MinecraftClient.getInstance();
   public ModuleButton parent;
   public Setting setting;
   public int offset;
   public Color currentColor;
   public boolean mouseOver;
   int x;
   int y;
   int width;
   int height;

   public Component(ModuleButton parent, Setting setting, int offset) {
      this.parent = parent;
      this.setting = setting;
      this.offset = offset;
      this.x = this.parentX();
      this.y = this.parentY() + this.parentOffset() + offset;
      this.width = this.parentX() + this.parentWidth();
      this.height = this.parentY() + this.parentOffset() + offset + this.parentHeight();
   }

   public int parentX() {
      return this.parent.parent.getX();
   }

   public int parentY() {
      return this.parent.parent.getY();
   }

   public int parentWidth() {
      return this.parent.parent.getWidth();
   }

   public int parentHeight() {
      return this.parent.parent.getHeight();
   }

   public int parentOffset() {
      return this.parent.offset;
   }

   public void render(DrawContext drawContext, int n, int n2, float n3) {
      this.updateMouseOver(n, n2);
      this.x = this.parentX();
      this.y = this.parentY() + this.parentOffset() + this.offset;
      this.width = this.parentX() + this.parentWidth();
      this.height = this.parentY() + this.parentOffset() + this.offset + this.parentHeight();
      if (this.currentColor == null) {
         this.currentColor = new Color(0, 0, 0, 0);
      }

      drawContext.fill(this.x, this.y, this.width, this.height, this.currentColor.getRGB());
   }

   private void updateMouseOver(double n, double n2) {
      this.mouseOver = this.isHovered(n, n2);
   }

   public void renderDescription(DrawContext drawContext, int n, int n2, float n3) {
      if (this.isHovered(n, n2) && this.setting.getDescription() != null && !this.parent.parent.dragging) {
         CharSequence description = this.setting.getDescription();
         int descriptionWidth = TextRenderer.getWidth(description);
         int n4 = this.mc.getWindow().getWidth() / 2 - descriptionWidth / 2;
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(),
            new Color(100, 100, 100, 100),
            n4 - 5,
            this.mc.getWindow().getHeight() / 2 + 294,
            n4 + descriptionWidth + 5,
            this.mc.getWindow().getHeight() / 2 + 318,
            3.0,
            10.0
         );
         TextRenderer.drawString(description, drawContext, n4, this.mc.getWindow().getHeight() / 2 + 300, Color.WHITE.getRGB());
      }
   }

   public void onGuiClose() {
      this.currentColor = null;
   }

   public void keyPressed(int n, int n2, int n3) {
   }

   public boolean isHovered(double n, double n2) {
      return n > this.parentX()
         && n < this.parentX() + this.parentWidth()
         && n2 > this.offset + this.parentOffset() + this.parentY()
         && n2 < this.offset + this.parentOffset() + this.parentY() + this.parentHeight();
   }

   public void onUpdate() {
      if (this.currentColor == null) {
         this.currentColor = new Color(0, 0, 0, 0);
      } else {
         this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
      }

      if (this.currentColor.getAlpha() != 120) {
         this.currentColor = ColorUtil.keyCodec(0.05F, 120, this.currentColor);
      }
   }

   public void mouseClicked(double n, double n2, int n3) {
   }

   public void mouseReleased(double n, double n2, int n3) {
   }

   public void mouseDragged(double n, double n2, int n3, double n4, double n5) {
   }
}
