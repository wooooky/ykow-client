package skid.ykow.gui.components;

import skid.ykow.gui.Component;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public final class Keybind extends Component {
   private final BindSetting keybind;
   private Color accentColor;
   private Color currentAlpha;
   private float hoverAnimation = 0.0F;
   private float listenAnimation = 0.0F;
   private static final Color TEXT_COLOR = new Color(230, 230, 230);
   private static final Color LISTENING_TEXT_COLOR = new Color(255, 255, 255);
   private static final Color HOVER_COLOR = new Color(255, 255, 255, 20);
   private static final Color BUTTON_BG_COLOR = new Color(60, 60, 65);
   private static final Color BUTTON_ACTIVE_BG_COLOR = new Color(80, 80, 85);
   private static final float BUTTON_RADIUS = 4.0F;
   private static final float ANIMATION_SPEED = 0.25F;
   private static final float LISTEN_ANIMATION_SPEED = 0.35F;
   private static final int BUTTON_MIN_WIDTH = 80;
   private static final int BUTTON_PADDING = 16;

   public Keybind(ModuleButton moduleButton, Setting setting, int n) {
      super(moduleButton, setting, n);
      this.keybind = (BindSetting)setting;
   }

   @Override
   public void render(DrawContext drawContext, int n, int n2, float n3) {
      super.render(drawContext, n, n2, n3);
      MatrixStack matrices = drawContext.getMatrices();
      this.updateAnimations(n, n2, n3);
      if (!this.parent.parent.dragging) {
         drawContext.fill(
            this.parentX(),
            this.parentY() + this.parentOffset() + this.offset,
            this.parentX() + this.parentWidth(),
            this.parentY() + this.parentOffset() + this.offset + this.parentHeight(),
            new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), (int)(HOVER_COLOR.getAlpha() * this.hoverAnimation)).getRGB()
         );
      }

      TextRenderer.drawString(
         this.setting.getName(), drawContext, this.parentX() + 5, this.parentY() + this.parentOffset() + this.offset + 9, TEXT_COLOR.getRGB()
      );
      String string;
      if (this.keybind.isListening()) {
         string = "Listening...";
      } else {
         string = KeyUtils.getKey(this.keybind.getValue()).toString();
      }

      int textWidth = TextRenderer.getWidth(string);
      int max = Math.max(80, textWidth + 16);
      int n4 = this.parentX() + this.parentWidth() - max - 5;
      int n5 = this.parentY() + this.parentOffset() + this.offset + (this.parentHeight() - 20) / 2;
      RenderUtils.renderRoundedQuad(
         matrices, ColorUtil.keyCodec(BUTTON_BG_COLOR, BUTTON_ACTIVE_BG_COLOR, this.listenAnimation), n4, n5, n4 + max, n5 + 20, 4.0, 4.0, 4.0, 4.0, 50.0
      );
      float listenAlpha = this.listenAnimation * 0.7F;
      float hoverAlpha;
      if (this.isButtonHovered(n, n2, n4, n5, max, 20)) {
         hoverAlpha = 0.2F;
      } else {
         hoverAlpha = 0.0F;
      }

      float maxAlpha = Math.max(listenAlpha, hoverAlpha);
      if (maxAlpha > 0.0F) {
         RenderUtils.renderRoundedQuad(
            matrices,
            new Color(this.accentColor.getRed(), this.accentColor.getGreen(), this.accentColor.getBlue(), (int)(this.accentColor.getAlpha() * maxAlpha)),
            n4,
            n5,
            n4 + max,
            n5 + 20,
            4.0,
            4.0,
            4.0,
            4.0,
            50.0
         );
      }

      TextRenderer.drawString(
         string, drawContext, n4 + (max - textWidth) / 2, n5 + 6 - 3, ColorUtil.keyCodec(TEXT_COLOR, LISTENING_TEXT_COLOR, this.listenAnimation).getRGB()
      );
      if (this.keybind.isListening()) {
         RenderUtils.renderRoundedQuad(
            matrices,
            new Color(
               this.accentColor.getRed(),
               this.accentColor.getGreen(),
               this.accentColor.getBlue(),
               (int)(this.accentColor.getAlpha() * ((float)Math.abs(Math.sin(System.currentTimeMillis() / 500.0)) * 0.3F))
            ),
            n4,
            n5,
            n4 + max,
            n5 + 20,
            4.0,
            4.0,
            4.0,
            4.0,
            50.0
         );
      }
   }

   private void updateAnimations(int n, int n2, float n3) {
      float n4 = n3 * 0.05F;
      float n5;
      if (this.isHovered(n, n2) && !this.parent.parent.dragging) {
         n5 = 1.0F;
      } else {
         n5 = 0.0F;
      }

      this.hoverAnimation = (float)MathUtil.exponentialInterpolate(this.hoverAnimation, n5, 0.25, n4);
      float n6;
      if (this.keybind.isListening()) {
         n6 = 1.0F;
      } else {
         n6 = 0.0F;
      }

      this.listenAnimation = (float)MathUtil.exponentialInterpolate(this.listenAnimation, n6, 0.35F, n4);
   }

   private boolean isButtonHovered(double n, double n2, int n3, int n4, int n5, int n6) {
      return n >= n3 && n <= n3 + n5 && n2 >= n4 && n2 <= n4 + n6;
   }

   @Override
   public void mouseClicked(double n, double n2, int n3) {
      String string;
      if (this.keybind.isListening()) {
         string = "Listening...";
      } else {
         string = KeyUtils.getKey(this.keybind.getValue()).toString();
      }

      int max = Math.max(80, TextRenderer.getWidth(string) + 16);
      if (this.isButtonHovered(
         n, n2, this.parentX() + this.parentWidth() - max - 5, this.parentY() + this.parentOffset() + this.offset + (this.parentHeight() - 20) / 2, max, 20
      )) {
         if (!this.keybind.isListening()) {
            if (n3 == 0) {
               this.keybind.setListening(true);
            }
         } else {
            this.keybind.setValue(n3);
            if (this.keybind.isModuleKey() && this.parent != null && this.parent.module != null) {
               this.parent.module.setKeybind(n3);
            }

            this.keybind.setListening(false);
         }
      }

      super.mouseClicked(n, n2, n3);
   }

   @Override
   public void keyPressed(int n, int n2, int n3) {
      if (this.keybind.isListening()) {
         if (n == 256) {
            this.keybind.setListening(false);
         } else if (n == 259) {
            this.keybind.setValue(-1);
            if (this.keybind.isModuleKey() && this.parent != null && this.parent.module != null) {
               this.parent.module.setKeybind(-1);
            }

            this.keybind.setListening(false);
         } else {
            this.keybind.setValue(n);
            if (this.keybind.isModuleKey() && this.parent != null && this.parent.module != null) {
               this.parent.module.setKeybind(n);
            }

            this.keybind.setListening(false);
         }
      }

      super.keyPressed(n, n2, n3);
   }

   @Override
   public void onUpdate() {
      Color mainColor = Utils.getMainColor(255, this.parent.settings.indexOf(this));
      if (this.accentColor == null) {
         this.accentColor = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 0);
      } else {
         this.accentColor = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), this.accentColor.getAlpha());
      }

      if (this.accentColor.getAlpha() != 255) {
         this.accentColor = ColorUtil.keyCodec(0.05F, 255, this.accentColor);
      }

      super.onUpdate();
   }

   @Override
   public void onGuiClose() {
      if (this.keybind.isListening()) {
         this.keybind.setListening(false);
      }

      this.accentColor = null;
      this.hoverAnimation = 0.0F;
      this.listenAnimation = 0.0F;
      super.onGuiClose();
   }
}
