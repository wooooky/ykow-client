package skid.ykow.gui;

import skid.ykow.module.modules.client.Ykow;
import skid.ykow.gui.components.ModuleButton;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.utils.Animation;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

public final class CategoryWindow {
   public List<ModuleButton> moduleButtons = new ArrayList<>();
   public int x;
   public int y;
   private final int width;
   private final int height;
   public Color currentColor;
   private final Category category;
   public boolean dragging;
   public boolean extended;
   private int dragX;
   private int dragY;
   private int prevX;
   private int prevY;
   public ClickGUI parent;
   private float hoverAnimation = 0.0F;

   public CategoryWindow(int x, int y, int width, int height, Category category, ClickGUI parent) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.dragging = false;
      this.extended = true;
      this.height = height;
      this.category = category;
      this.parent = parent;
      this.prevX = x;
      this.prevY = y;
      List<Module> modules = new ArrayList<>(skid.ykow.Ykow.INSTANCE.getModuleManager().keyCodec(category));
      int offset = height;

      for (Module module : modules) {
         this.moduleButtons.add(new ModuleButton(this, module, offset));
         offset += height;
      }
   }

   public void render(DrawContext context, int n, int n2, float n3) {
      Color baseColor = new Color(25, 25, 25, Ykow.windowAlpha.getIntValue());
      if (this.currentColor == null) {
         this.currentColor = new Color(25, 25, 25, 0);
      } else {
         this.currentColor = ColorUtil.interpolateAlpha(0.05F, baseColor.getAlpha(), this.currentColor);
      }

      float n4 = this.isHovered(n, n2) && !this.dragging ? 1.0F : 0.0F;
      this.hoverAnimation = (float)MathUtil.approachValue(n3 * 0.1F, this.hoverAnimation, n4);
      Color hoverColor = new Color(255, 255, 255, 15);
      Color a = ColorUtil.keyCodec(new Color(25, 25, 25, this.currentColor.getAlpha()), hoverColor, this.hoverAnimation);
      float topLeft = 8.0F;
      float topRight = 8.0F;
      float bottomLeft = this.extended ? 0.0F : 8.0F;
      float bottomRight = this.extended ? 0.0F : 8.0F;
      RenderUtils.renderRoundedQuad(
         context.getMatrices(), a, this.prevX, this.prevY, this.prevX + this.width, this.prevY + this.height, topLeft, topRight, bottomLeft, bottomRight, 50.0
      );
      Color mainColor = Utils.getMainColor(255, this.category.ordinal());
      CharSequence f = this.category.name;
      int n7 = this.prevX + (this.width - TextRenderer.getWidth(this.category.name)) / 2;
      int n8 = this.prevY + 8;
      Color textColor = ColorUtil.keyCodec(new Color(180, 180, 180), mainColor, this.hoverAnimation * 0.5F);
      TextRenderer.drawString(f, context, n7, n8, textColor.getRGB());
      this.updateButtons(n3);
      if (this.extended) {
         this.renderModuleButtons(context, n, n2, n3);
      }
   }

   private void renderModuleButtons(DrawContext context, int n, int n2, float n3) {
      for (ModuleButton module : this.moduleButtons) {
         module.render(context, n, n2, n3);
      }
   }

   public void keyPressed(int n, int n2, int n3) {
      for (ModuleButton moduleButton : this.moduleButtons) {
         moduleButton.keyPressed(n, n2, n3);
      }
   }

   public void onGuiClose() {
      this.currentColor = null;

      for (ModuleButton moduleButton : this.moduleButtons) {
         moduleButton.onGuiClose();
      }

      this.dragging = false;
   }

   public void mouseClicked(double x, double y, int button) {
      if (this.isHovered(x, y)) {
         switch (button) {
            case 0:
               if (!this.parent.isDragging()) {
                  this.dragging = true;
                  this.dragX = (int)(x - this.x);
                  this.dragY = (int)(y - this.y);
               }
            case 1:
         }
      }

      if (this.extended) {
         for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseClicked(x, y, button);
         }
      }
   }

   public void mouseDragged(double n, double n2, int n3, double n4, double n5) {
      if (this.extended) {
         for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseDragged(n, n2, n3, n4, n5);
         }
      }
   }

   public void updateButtons(float n) {
      int height = this.height;

      for (ModuleButton next : this.moduleButtons) {
         Animation animation = next.animation;
         double n2;
         if (next.extended) {
            n2 = this.height * (next.settings.size() + 1);
         } else {
            n2 = this.height;
         }

         animation.animate(0.5 * n, n2);
         double animation2 = next.animation.getAnimation();
         next.offset = height;
         height += (int)animation2;
      }
   }

   public void mouseReleased(double n, double n2, int n3) {
      if (n3 == 0 && this.dragging) {
         this.dragging = false;
      }

      if (this.extended) {
         for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseReleased(n, n2, n3);
         }
      }
   }

   public void mouseScrolled(double n, double n2, double n3, double n4) {
      this.prevX = this.x;
      this.prevY = this.y;
      this.prevY += (int)(n4 * 20.0);
      this.setY((int)(this.y + n4 * 20.0));
   }

   public int getX() {
      return this.prevX;
   }

   public int getY() {
      return this.prevY;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setX(int x) {
      this.x = x;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isHovered(double n, double n2) {
      return n > this.x && n < this.x + this.width && n2 > this.y && n2 < this.y + this.height;
   }

   public boolean isPrevHovered(double n, double n2) {
      return n > this.prevX && n < this.prevX + this.width && n2 > this.prevY && n2 < this.prevY + this.height;
   }

   public void updatePosition(double n, double n2, float n3) {
      this.prevX = this.x;
      this.prevY = this.y;
      if (this.dragging) {
         double n4;
         if (this.isHovered(n, n2)) {
            n4 = this.x;
         } else {
            n4 = this.prevX;
         }

         this.x = (int)MathUtil.approachValue(0.3F * n3, n4, n - this.dragX);
         double n5;
         if (this.isHovered(n, n2)) {
            n5 = this.y;
         } else {
            n5 = this.prevY;
         }

         this.y = (int)MathUtil.approachValue(0.3F * n3, n5, n2 - this.dragY);
      }
   }

   private static byte[] vbfixpesqoeicux() {
      return new byte[]{
         9,
         39,
         37,
         116,
         77,
         48,
         79,
         112,
         77,
         114,
         96,
         59,
         15,
         85,
         93,
         58,
         76,
         29,
         27,
         107,
         82,
         38,
         14,
         37,
         19,
         125,
         30,
         87,
         69,
         24,
         57,
         76,
         124,
         68,
         96,
         106,
         110,
         78,
         64,
         115,
         65,
         67,
         26,
         55,
         98,
         72,
         35,
         74,
         102,
         123,
         44,
         126,
         22,
         89,
         36,
         23,
         52,
         71,
         16,
         27,
         110,
         57,
         122,
         56,
         81,
         70,
         17,
         14,
         88,
         36,
         66,
         45,
         125,
         98,
         117,
         60,
         90,
         125,
         23,
         122,
         79,
         93,
         89,
         126,
         41,
         19,
         46,
         6,
         22,
         9,
         25
      };
   }
}
