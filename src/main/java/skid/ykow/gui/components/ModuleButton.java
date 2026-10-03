package skid.ykow.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import skid.ykow.Ykow;
import skid.ykow.gui.CategoryWindow;
import skid.ykow.gui.Component;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.BlocksSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.module.setting.MacroSetting;
import skid.ykow.module.setting.MinMaxSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.Animation;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import skid.ykow.utils.Utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ModuleButton {
   public List<Component> settings;
   public CategoryWindow parent;
   public Module module;
   public int offset;
   public boolean extended;
   public int settingOffset;
   public Color currentColor;
   public Color currentAlpha;
   public Animation animation;
   private final float CORNER_RADIUS = 6.0F;
   private final Color ACCENT_COLOR;
   private final Color HOVER_COLOR;
   private final Color ENABLED_COLOR;
   private final Color DISABLED_COLOR;
   private final Color DESCRIPTION_BG;
   private float hoverAnimation;
   private float enabledAnimation;
   private final float expandAnimation;

   public ModuleButton(CategoryWindow parent, Module module, int offset) {
      this.settings = new ArrayList<>();
      this.animation = new Animation(0.0);
      this.ACCENT_COLOR = new Color(255, 0, 102);
      this.HOVER_COLOR = new Color(255, 255, 255, 20);
      this.ENABLED_COLOR = new Color(255, 0, 102);
      this.DISABLED_COLOR = new Color(180, 180, 180);
      this.DESCRIPTION_BG = new Color(40, 40, 40, 200);
      this.hoverAnimation = 0.0F;
      this.enabledAnimation = 0.0F;
      this.expandAnimation = 0.0F;
      this.parent = parent;
      this.module = module;
      this.offset = offset;
      this.extended = false;
      this.settingOffset = parent.getHeight();

      for (Object next : module.getSettings()) {
         if (next instanceof BooleanSetting) {
            this.settings.add(new Checkbox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof NumberSetting) {
            this.settings.add(new NumberBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof ModeSetting) {
            this.settings.add(new ModeBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof BindSetting) {
            this.settings.add(new Keybind(this, (Setting)next, this.settingOffset));
         } else if (next instanceof StringSetting) {
            this.settings.add(new TextBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof MinMaxSetting) {
            this.settings.add(new Slider(this, (Setting)next, this.settingOffset));
         } else if (next instanceof ItemSetting) {
            this.settings.add(new ItemBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof BlocksSetting) {
            this.settings.add(new BlocksBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof FriendsSetting) {
            this.settings.add(new FriendsBox(this, (Setting)next, this.settingOffset));
         } else if (next instanceof MacroSetting) {
            this.settings.add(new MacroBox(this, (Setting)next, this.settingOffset));
         }

         this.settingOffset = this.settingOffset + parent.getHeight();
      }
   }

   public void render(DrawContext drawContext, int n, int n2, float n3) {
      if (this.parent.getY() + this.offset <= MinecraftClient.getInstance().getWindow().getHeight()) {
         Iterator<Component> iterator = this.settings.iterator();

         while (iterator.hasNext()) {
            iterator.next().onUpdate();
         }

         this.updateAnimations(n, n2, n3);
         int x = this.parent.getX();
         int n4 = this.parent.getY() + this.offset;
         int width = this.parent.getWidth();
         int height = this.parent.getHeight();
         this.renderButtonBackground(drawContext, x, n4, width, height);
         this.renderIndicator(drawContext, x, n4, height);
         this.renderModuleInfo(drawContext, x, n4, width, height);
         if (this.extended) {
            this.renderSettings(drawContext, n, n2, n3);
         }

         if (this.isHovered(n, n2) && !this.parent.dragging) {
            Ykow.INSTANCE.GUI.setTooltip(this.module.getDescription(), n + 10, n2 + 10);
         }
      }
   }

   private void updateAnimations(int n, int n2, float n3) {
      float n4 = n3 * 0.05F;
      float n5;
      if (this.isHovered(n, n2) && !this.parent.dragging) {
         n5 = 1.0F;
      } else {
         n5 = 0.0F;
      }

      this.hoverAnimation = (float)MathUtil.exponentialInterpolate(this.hoverAnimation, n5, 0.05F, n4);
      float n6;
      if (this.module.isEnabled()) {
         n6 = 1.0F;
      } else {
         n6 = 0.0F;
      }

      this.enabledAnimation = (float)MathUtil.exponentialInterpolate(this.enabledAnimation, n6, 0.005F, n4);
      this.enabledAnimation = (float)MathUtil.clampValue(this.enabledAnimation, 0.0, 1.0);
   }

   private void renderButtonBackground(DrawContext drawContext, int n, int n2, int n3, int n4) {
      Color a = ColorUtil.keyCodec(new Color(25, 25, 30, 230), this.HOVER_COLOR, this.hoverAnimation);
      boolean b = this.parent.moduleButtons.get(this.parent.moduleButtons.size() - 1) == this;
      if (b && !this.extended) {
         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), a, n, n2, n + n3, n2 + n4, 0.0, 0.0, 6.0, 6.0, 50.0);
      } else if (b && this.extended) {
         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), a, n, n2, n + n3, n2 + n4, 0.0, 0.0, 0.0, 0.0, 50.0);
      } else {
         drawContext.fill(n, n2, n + n3, n2 + n4, a.getRGB());
      }

      if (this.parent.moduleButtons.indexOf(this) > 0) {
         drawContext.fill(n + 4, n2, n + n3 - 4, n2 + 1, new Color(60, 60, 65, 100).getRGB());
      }
   }

   private void renderIndicator(DrawContext drawContext, int n, int n2, int n3) {
      Color color;
      if (this.module.isEnabled()) {
         color = Utils.getMainColor(255, Ykow.INSTANCE.getModuleManager().keyCodec(this.module.getCategory()).indexOf(this.module));
      } else {
         color = this.ACCENT_COLOR;
      }

      float n4 = 5.0F * this.enabledAnimation;
      if (n4 > 0.1F) {
         RenderUtils.renderRoundedQuad(
            drawContext.getMatrices(),
            ColorUtil.keyCodec(this.DISABLED_COLOR, color, this.enabledAnimation),
            n,
            n2 + 2,
            n + n4,
            n2 + n3 - 2,
            1.5,
            1.5,
            1.5,
            1.5,
            60.0
         );
      }
   }

   private void renderModuleInfo(DrawContext drawContext, int n, int n2, int n3, int n4) {
      TextRenderer.drawString(
         this.module.getName(),
         drawContext,
         n + 10,
         n2 + n4 / 2 - 6,
         ColorUtil.keyCodec(this.DISABLED_COLOR, this.ENABLED_COLOR, this.enabledAnimation).getRGB()
      );
      int n5 = n + n3 - 40;
      int n6 = n2 + n4 / 2 - 6;
      RenderUtils.renderRoundedQuad(
         drawContext.getMatrices(),
         ColorUtil.keyCodec(new Color(60, 60, 65, 200), new Color(236, 153, 181), this.enabledAnimation),
         n5,
         n6,
         n5 + 24.0F,
         n6 + 12.0F,
         6.0,
         6.0,
         6.0,
         6.0,
         50.0
      );
      float n7 = n5 + 6.0F + 12.0F * this.enabledAnimation;
      RenderUtils.renderCircle(
         drawContext.getMatrices(), ColorUtil.keyCodec(new Color(180, 180, 180), this.ENABLED_COLOR, this.enabledAnimation), n7, n6 + 6.0F, 5.0, 12
      );
      if (this.module.isEnabled()) {
         RenderUtils.renderCircle(
            drawContext.getMatrices(),
            new Color(this.ENABLED_COLOR.getRed(), this.ENABLED_COLOR.getGreen(), this.ENABLED_COLOR.getBlue(), 30),
            n7,
            n6 + 6.0F,
            8.0,
            16
         );
      }
   }

   private void renderSettings(DrawContext drawContext, int n, int n2, float n3) {
      int n4 = this.parent.getY() + this.offset + this.parent.getHeight();
      double animation = this.animation.getAnimation();
      RenderSystem.enableScissor(this.parent.getX(), Ykow.mc.getWindow().getHeight() - (n4 + (int)animation), this.parent.getWidth(), (int)animation);
      Iterator<Component> iterator = this.settings.iterator();

      while (iterator.hasNext()) {
         iterator.next().render(drawContext, n, n2 - n4, n3);
      }

      this.renderSliderControls(drawContext);
      RenderSystem.disableScissor();
   }

   private void renderSliderControls(DrawContext drawContext) {
      for (Component next : this.settings) {
         if (next instanceof NumberBox numberBox) {
            this.renderModernSliderKnob(
               drawContext,
               next.parentX() + Math.max(numberBox.lerpedOffsetX, 2.5),
               next.parentY() + numberBox.offset + next.parentOffset() + 27.5,
               numberBox.currentColor1
            );
         } else if (next instanceof Slider) {
            this.renderModernSliderKnob(
               drawContext,
               next.parentX() + Math.max(((Slider)next).lerpedOffsetMinX, 2.5),
               next.parentY() + next.offset + next.parentOffset() + 27.5,
               ((Slider)next).accentColor1
            );
            this.renderModernSliderKnob(
               drawContext,
               next.parentX() + Math.max(((Slider)next).lerpedOffsetMaxX, 2.5),
               next.parentY() + next.offset + next.parentOffset() + 27.5,
               ((Slider)next).accentColor1
            );
         }
      }
   }

   private void renderModernSliderKnob(DrawContext drawContext, double n, double n2, Color color) {
      RenderUtils.renderCircle(drawContext.getMatrices(), new Color(0, 0, 0, 100), n, n2, 7.0, 18);
      RenderUtils.renderCircle(drawContext.getMatrices(), color, n, n2, 5.5, 16);
      RenderUtils.renderCircle(drawContext.getMatrices(), new Color(255, 255, 255, 70), n, n2 - 1.0, 3.0, 12);
   }

   public void onExtend() {
      Iterator<ModuleButton> iterator = this.parent.moduleButtons.iterator();

      while (iterator.hasNext()) {
         iterator.next().extended = false;
      }
   }

   public void keyPressed(int n, int n2, int n3) {
      Iterator<Component> iterator = this.settings.iterator();

      while (iterator.hasNext()) {
         iterator.next().keyPressed(n, n2, n3);
      }
   }

   public void mouseDragged(double n, double n2, int n3, double n4, double n5) {
      if (this.extended) {
         Iterator<Component> iterator = this.settings.iterator();

         while (iterator.hasNext()) {
            iterator.next().mouseDragged(n, n2, n3, n4, n5);
         }
      }
   }

   public void mouseClicked(double n, double n2, int button) {
      if (this.isHovered(n, n2)) {
         if (button == 0) {
            int n4 = this.parent.getX() + this.parent.getWidth() - 30;
            int n5 = this.parent.getY() + this.offset + this.parent.getHeight() / 2 - 3;
            if (n >= n4 && n <= n4 + 12 && n2 >= n5 && n2 <= n5 + 6) {
               this.module.toggle();
            } else if (!this.module.getSettings().isEmpty() && n > this.parent.getX() + this.parent.getWidth() - 25) {
               if (!this.extended) {
                  this.onExtend();
               }

               this.extended = !this.extended;
            } else {
               this.module.toggle();
            }
         } else if (button == 1) {
            if (this.module.getSettings().isEmpty()) {
               return;
            }

            if (!this.extended) {
               this.onExtend();
            }

            this.extended = !this.extended;
         }
      }

      if (this.extended) {
         for (Component setting : this.settings) {
            setting.mouseClicked(n, n2, button);
         }
      }
   }

   public void onGuiClose() {
      this.currentAlpha = null;
      this.currentColor = null;
      this.hoverAnimation = 0.0F;
      float enabledAnimation;
      if (this.module.isEnabled()) {
         enabledAnimation = 1.0F;
      } else {
         enabledAnimation = 0.0F;
      }

      this.enabledAnimation = enabledAnimation;
      Iterator<Component> iterator = this.settings.iterator();

      while (iterator.hasNext()) {
         iterator.next().onGuiClose();
      }
   }

   public void mouseReleased(double n, double n2, int n3) {
      Iterator<Component> iterator = this.settings.iterator();

      while (iterator.hasNext()) {
         iterator.next().mouseReleased(n, n2, n3);
      }
   }

   public boolean isHovered(double n, double n2) {
      return n > this.parent.getX()
         && n < this.parent.getX() + this.parent.getWidth()
         && n2 > this.parent.getY() + this.offset
         && n2 < this.parent.getY() + this.offset + this.parent.getHeight();
   }
}
