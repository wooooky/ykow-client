package skid.ykow.gui;

import skid.ykow.gui.components.BlocksBox;
import skid.ykow.gui.components.BlocksFilter;
import skid.ykow.gui.components.FriendsBox;
import skid.ykow.gui.components.FriendsFilter;
import skid.ykow.gui.components.ItemBox;
import skid.ykow.gui.components.ItemFilter;
import skid.ykow.gui.components.MacroBox;
import skid.ykow.gui.components.MacroFilter;
import skid.ykow.gui.components.StringBox;
import skid.ykow.gui.components.TextBox;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.BlocksSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ColorSetting;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.module.setting.MacroSetting;
import skid.ykow.module.setting.MinMaxSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public final class ClickGUI extends Screen {
   public Color currentColor;
   private static final Color BG_DEEPEST = new Color(8, 8, 12, 250);
   private static final Color BG_DEEP = new Color(14, 14, 20, 240);
   private static final Color BG_CARD = new Color(20, 20, 28, 230);
   private static final Color BG_ELEVATED = new Color(28, 28, 38, 220);
   private static final Color BG_HOVER = new Color(35, 35, 48, 200);
   private static final Color ACCENT_PRIMARY = new Color(124, 58, 237);
   private static final Color ACCENT_GLOW = new Color(167, 139, 250);
   private static final Color ACCENT_SECONDARY = new Color(59, 130, 246);
   private static final Color ACCENT_TERTIARY = new Color(236, 72, 153);
   private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
   private static final Color TEXT_SECONDARY = new Color(203, 213, 225);
   private static final Color TEXT_MUTED = new Color(148, 163, 184);
   private static final Color TEXT_DIM = new Color(100, 116, 139);
   private static final Color SUCCESS = new Color(34, 197, 94);
   private static final Color ERROR = new Color(239, 68, 68);
   private static final Color DIVIDER = new Color(51, 65, 85, 100);
   private int guiX = -1;
   private int guiY = -1;
   private final int GUI_WIDTH = 980;
   private final int GUI_HEIGHT = 640;
   private final int SIDEBAR_WIDTH = 240;
   private final int HEADER_HEIGHT = 72;
   private final int PADDING = 20;
   private final int CARD_RADIUS = 14;
   private List<CategoryWindow> reaperWindows;
   private CharSequence tooltipText;
   private int tooltipX;
   private int tooltipY;
   private final Color DESCRIPTION_BG = new Color(40, 40, 40, 200);
   private Category selectedCategory = Category.CRYSTAL;
   private Module selectedModule = null;
   private String searchQuery = "";
   private boolean isSearchFocused = false;
   private int moduleScrollOffset = 0;
   private int settingsScrollOffset = 0;
   private final Map<Category, Float> categoryHoverAnim = new HashMap<>();
   private final Map<Module, Float> moduleHoverAnim = new HashMap<>();
   private float panelTransition = 0.0F;
   private float searchPulse = 0.0F;
   private float globalTime = 0.0F;
   private boolean isDragging = false;
   private int dragOffsetX = 0;
   private int dragOffsetY = 0;
   private boolean isDraggingSlider = false;
   private Setting draggedSetting = null;
   private boolean draggedIsMin = false;

   public ClickGUI() {
      super(Text.empty());

      for (Category cat : Category.values()) {
         this.categoryHoverAnim.put(cat, 0.0F);
      }

      this.reaperWindows = new ArrayList<>();
      int windowX = 50;
      Category[] categories = Category.values();

      for (Category category : categories) {
         this.reaperWindows.add(new CategoryWindow(windowX, 50, 230, 30, category, this));
         windowX += 250;
      }
   }

   private boolean shouldUseReaperGUI() {
      return Ykow.OpenGui();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (this.shouldUseReaperGUI()) {
         if (MinecraftClient.getInstance().currentScreen == this) {
            if (skid.ykow.Ykow.INSTANCE.screen != null) {
               skid.ykow.Ykow.INSTANCE.screen.render(context, 0, 0, delta);
            }

            if (this.currentColor == null) {
               this.currentColor = new Color(0, 0, 0, 0);
            } else {
               this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
            }

            int alpha = this.currentColor.getAlpha();
            int targetAlpha = Ykow.renderBackground.getValue() ? 200 : 0;
            if (alpha != targetAlpha) {
               this.currentColor = ColorUtil.keyCodec(0.05F, targetAlpha, this.currentColor);
            }

            if (MinecraftClient.getInstance().currentScreen instanceof ClickGUI) {
               context.fill(
                  0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), this.currentColor.getRGB()
               );
            }

            RenderUtils.unscaledProjection();
            int scaledMouseX = mouseX * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
            int scaledMouseY = mouseY * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
            super.render(context, scaledMouseX, scaledMouseY, delta);

            for (CategoryWindow window : this.reaperWindows) {
               window.render(context, scaledMouseX, scaledMouseY, delta);
               window.updatePosition(scaledMouseX, scaledMouseY, delta);
            }

            if (this.tooltipText != null) {
               this.renderReaperTooltip(context, this.tooltipText, this.tooltipX, this.tooltipY);
               this.tooltipText = null;
            }

            RenderUtils.scaledProjection();
         }
      } else {
         int scaledMouseX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
         int scaledMouseY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
         RenderUtils.unscaledProjection();
         if (this.guiX == -1 || this.guiY == -1) {
            int screenW = MinecraftClient.getInstance().getWindow().getWidth();
            int screenH = MinecraftClient.getInstance().getWindow().getHeight();
            this.guiX = (screenW - 980) / 2;
            this.guiY = (screenH - 640) / 2;
         }

         if (this.isDragging) {
            this.guiX = scaledMouseX - this.dragOffsetX;
            this.guiY = scaledMouseY - this.dragOffsetY;
         }

         this.globalTime += delta * 0.05F;
         this.panelTransition = Math.min(1.0F, this.panelTransition + delta * 0.08F);
         this.searchPulse = (float)Math.sin(this.globalTime * 2.0F) * 0.5F + 0.5F;
         this.renderBackdrop(context);
         this.renderMainContainer(context, scaledMouseX, scaledMouseY, delta);
         RenderUtils.scaledProjection();
      }
   }

   private void renderReaperTooltip(DrawContext drawContext, CharSequence charSequence, int x, int y) {
      if (charSequence != null && charSequence.length() != 0) {
         int width = TextRenderer.getWidth(charSequence);
         int framebufferWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
         if (x + width + 10 > framebufferWidth) {
            x = framebufferWidth - width - 10;
         }

         RenderUtils.renderRoundedQuad(drawContext.getMatrices(), this.DESCRIPTION_BG, x - 5, y - 5, x + width + 5, y + 15, 6.0, 6.0, 6.0, 6.0, 50.0);
         TextRenderer.drawString(charSequence, drawContext, x, y, Color.WHITE.getRGB());
      }
   }

   public void setTooltip(CharSequence tooltipText, int tooltipX, int tooltipY) {
      this.tooltipText = tooltipText;
      this.tooltipX = tooltipX;
      this.tooltipY = tooltipY;
   }

   private void renderBackdrop(DrawContext context) {
      int screenW = MinecraftClient.getInstance().getWindow().getWidth();
      int screenH = MinecraftClient.getInstance().getWindow().getHeight();
      Color gradTop = new Color(6, 6, 10, 200);
      Color gradBot = new Color(15, 10, 25, 220);
      context.fillGradient(0, 0, screenW, screenH, gradTop.getRGB(), gradBot.getRGB());
      long time = System.currentTimeMillis();
      float orb1X = (float)(screenW * 0.2 + Math.sin(time / 8000.0) * 150.0);
      float orb1Y = (float)(screenH * 0.3 + Math.cos(time / 6000.0) * 100.0);
      this.renderGlowOrb(context, orb1X, orb1Y, 180.0F, ACCENT_PRIMARY, 3);
      float orb2X = (float)(screenW * 0.75 + Math.cos(time / 7000.0) * 120.0);
      float orb2Y = (float)(screenH * 0.6 + Math.sin(time / 9000.0) * 80.0);
      this.renderGlowOrb(context, orb2X, orb2Y, 140.0F, ACCENT_SECONDARY, 2);
      float orb3X = (float)(screenW * 0.5 + Math.sin(time / 5000.0) * 100.0);
      float orb3Y = (float)(screenH * 0.8 + Math.cos(time / 8000.0) * 60.0);
      this.renderGlowOrb(context, orb3X, orb3Y, 100.0F, ACCENT_TERTIARY, 2);
   }

   private void renderGlowOrb(DrawContext context, float x, float y, float size, Color color, int layers) {
      for (int i = layers; i > 0; i--) {
         float layerSize = size * (1.0F - i * 0.2F);
         int alpha = 2 + i * 2;
         Color layerColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
         RenderUtils.renderCircle(context.getMatrices(), layerColor, x, y, layerSize, 32);
      }
   }

   private void renderMainContainer(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderDropShadow(context, this.guiX, this.guiY, 980, 640);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_DEEPEST, this.guiX, this.guiY, 980.0, 640.0, 14.0, 120.0);
      Color borderGlow = new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 40);
      RenderUtils.renderRoundedOutline(context, borderGlow, this.guiX, this.guiY, this.guiX + 980, this.guiY + 640, 14.0, 14.0, 14.0, 14.0, 1.5, 120.0);
      this.renderHeader(context, mouseX, mouseY);
      this.renderSidebar(context, mouseX, mouseY, delta);
      this.renderContent(context, mouseX, mouseY, delta);
      if (this.selectedModule != null) {
         this.renderSettingsPanel(context, mouseX, mouseY, delta);
      }
   }

   private void renderDropShadow(DrawContext context, int x, int y, int width, int height) {
      int shadowSize = 20;

      for (int i = 0; i < shadowSize; i++) {
         int alpha = (shadowSize - i) * 3;
         Color shadowColor = new Color(0, 0, 0, alpha);
         RenderUtils.renderRoundedQuadShader(context.getMatrices(), shadowColor, x - i, y - i, width + i * 2, height + i * 2, 14 + i, 80.0);
      }
   }

   private void renderHeader(DrawContext context, int mouseX, int mouseY) {
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_DEEP, this.guiX, this.guiY, 980.0, 72.0, 14.0, 100.0);
      int lineY = this.guiY + 72 - 1;
      context.fillGradient(
         this.guiX + 20,
         lineY,
         this.guiX + 980 - 20,
         lineY + 2,
         new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 80).getRGB(),
         new Color(ACCENT_TERTIARY.getRed(), ACCENT_TERTIARY.getGreen(), ACCENT_TERTIARY.getBlue(), 80).getRGB()
      );
      int titleX = this.guiX + 20 + 8;
      int titleY = this.guiY + 24;
      RenderUtils.renderCircle(context.getMatrices(), ACCENT_PRIMARY, titleX + 12, titleY + 12, 12.0, 16);
      RenderUtils.renderCircle(context.getMatrices(), ACCENT_GLOW, titleX + 12, titleY + 12, 8.0, 16);
      TextRenderer.drawString("GYPSY", context, titleX + 38, titleY + 3, TEXT_PRIMARY.getRGB());
      TextRenderer.drawString("CLIENT", context, titleX + 38, titleY + 14, TEXT_MUTED.getRGB());
      int versionX = titleX + 130;
      int versionY = titleY + 5;
      RenderUtils.renderRoundedQuadShader(
         context.getMatrices(),
         new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 80),
         versionX,
         versionY,
         50.0,
         18.0,
         9.0,
         60.0
      );
      TextRenderer.drawCenteredString("v1.3", context, versionX + 25, versionY + 5, ACCENT_GLOW.getRGB());
      int btnY = this.guiY + 18;
      int closeX = this.guiX + 980 - 20 - 36;
      boolean closeHovered = this.isPointInRect(mouseX, mouseY, closeX, btnY, 36, 36);
      Color closeBg = closeHovered
         ? new Color(ERROR.getRed(), ERROR.getGreen(), ERROR.getBlue(), 120)
         : new Color(BG_ELEVATED.getRed(), BG_ELEVATED.getGreen(), BG_ELEVATED.getBlue(), 100);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), closeBg, closeX, btnY, 36.0, 36.0, 10.0, 60.0);
      Color textColor = closeHovered ? TEXT_PRIMARY : TEXT_SECONDARY;
      TextRenderer.drawCenteredString("✕", context, closeX + 18, btnY + 11, textColor.getRGB());
   }

   private void renderSidebar(DrawContext context, int mouseX, int mouseY, float delta) {
      int sidebarX = this.guiX;
      int sidebarY = this.guiY + 72;
      int sidebarH = 568;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_DEEP, sidebarX, sidebarY, 240.0, sidebarH, 0.0, 100.0);
      int contentY = sidebarY + 20;
      TextRenderer.drawString("CATEGORIES", context, sidebarX + 20, contentY, TEXT_DIM.getRGB());
      contentY += 32;
      Category[] categories = Category.values();

      for (Category cat : categories) {
         boolean isSelected = cat == this.selectedCategory;
         boolean isHovered = this.isPointInRect(mouseX, mouseY, sidebarX + 12, contentY, 216, 50);
         float targetAnim = isHovered ? 1.0F : (isSelected ? 0.5F : 0.0F);
         float currentAnim = this.categoryHoverAnim.get(cat);
         this.categoryHoverAnim.put(cat, currentAnim + (targetAnim - currentAnim) * delta * 0.15F);
         float anim = this.categoryHoverAnim.get(cat);
         int btnX = sidebarX + 12;
         int btnW = 216;
         Color btnBg;
         if (isSelected) {
            btnBg = new Color(
               (int)(ACCENT_PRIMARY.getRed() + (BG_CARD.getRed() - ACCENT_PRIMARY.getRed()) * (1.0F - anim)),
               (int)(ACCENT_PRIMARY.getGreen() + (BG_CARD.getGreen() - ACCENT_PRIMARY.getGreen()) * (1.0F - anim)),
               (int)(ACCENT_PRIMARY.getBlue() + (BG_CARD.getBlue() - ACCENT_PRIMARY.getBlue()) * (1.0F - anim)),
               140 + (int)(40.0F * anim)
            );
         } else {
            btnBg = new Color(BG_CARD.getRed(), BG_CARD.getGreen(), BG_CARD.getBlue(), 80 + (int)(80.0F * anim));
         }

         RenderUtils.renderRoundedQuadShader(context.getMatrices(), btnBg, btnX, contentY, btnW, 50.0, 12.0, 80.0);
         if (isSelected) {
            int barAlpha = 180 + (int)(75.0 * Math.sin(this.globalTime * 3.0F));
            Color barColor = new Color(ACCENT_GLOW.getRed(), ACCENT_GLOW.getGreen(), ACCENT_GLOW.getBlue(), barAlpha);
            RenderUtils.renderRoundedQuadShader(context.getMatrices(), barColor, btnX + 4, contentY + 10, 3.0, 30.0, 2.0, 50.0);
         }

         int iconX = btnX + 22;
         int iconY = contentY + 25;
         Color iconColor = isSelected ? ACCENT_GLOW : TEXT_MUTED;
         RenderUtils.renderCircle(context.getMatrices(), iconColor, iconX, iconY, 7.0, 16);
         if (isSelected) {
            RenderUtils.renderCircle(context.getMatrices(), new Color(255, 255, 255, 100), iconX, iconY, 4.0, 12);
         }

         int textX = iconX + 20;
         int textY = contentY + 20;
         Color textColor = isSelected ? TEXT_PRIMARY : TEXT_SECONDARY;
         TextRenderer.drawString(cat.name, context, textX, textY, textColor.getRGB());
         int moduleCount = skid.ykow.Ykow.INSTANCE.getModuleManager().keyCodec(cat).size();
         String countStr = String.valueOf(moduleCount);
         int badgeW = 32;
         int badgeX = btnX + btnW - badgeW - 8;
         int badgeY = contentY + 16;
         Color badgeBg = isSelected ? new Color(BG_DEEPEST.getRed(), BG_DEEPEST.getGreen(), BG_DEEPEST.getBlue(), 150) : BG_ELEVATED;
         RenderUtils.renderRoundedQuadShader(context.getMatrices(), badgeBg, badgeX, badgeY, badgeW, 20.0, 10.0, 50.0);
         TextRenderer.drawCenteredString(countStr, context, badgeX + 16, badgeY + 6, TEXT_MUTED.getRGB());
         contentY += 58;
      }
   }

   private void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
      int contentX = this.guiX + 240;
      int contentY = this.guiY + 72;
      int contentW = 740 - (this.selectedModule != null ? 320 : 0);
      int contentH = 568;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_CARD, contentX, contentY, contentW, contentH, 0.0, 100.0);
      this.renderSearchBar(context, contentX, contentY, contentW, mouseX, mouseY);
      int gridY = contentY + 90;
      int gridH = contentH - 90;
      this.renderModulesGrid(context, contentX, gridY, contentW, gridH, mouseX, mouseY, delta);
   }

   private void renderSearchBar(DrawContext context, int x, int y, int width, int mouseX, int mouseY) {
      int searchX = x + 20;
      int searchY = y + 20;
      int searchW = width - 40;
      int searchH = 52;
      boolean hovered = this.isPointInRect(mouseX, mouseY, searchX, searchY, searchW, searchH);
      Color searchBg = this.isSearchFocused ? BG_ELEVATED : (hovered ? BG_HOVER : new Color(BG_DEEP.getRed(), BG_DEEP.getGreen(), BG_DEEP.getBlue(), 150));
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), searchBg, searchX, searchY, searchW, searchH, 12.0, 80.0);
      if (this.isSearchFocused) {
         int glowAlpha = 60 + (int)(40.0F * this.searchPulse);
         Color glowColor = new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), glowAlpha);
         RenderUtils.renderRoundedOutline(context, glowColor, searchX, searchY, searchX + searchW, searchY + searchH, 12.0, 12.0, 12.0, 12.0, 2.0, 80.0);
      }

      int iconX = searchX + 18;
      int iconY = searchY + 20;
      TextRenderer.drawString("\ud83d\udd0d", context, iconX, iconY, TEXT_SECONDARY.getRGB());
      String displayText = this.searchQuery.isEmpty() ? "Search modules..." : this.searchQuery;
      Color textColor = this.searchQuery.isEmpty() ? TEXT_DIM : TEXT_PRIMARY;
      int textX = iconX + 32;
      int textY = searchY + 20;
      TextRenderer.drawString(displayText, context, textX, textY, textColor.getRGB());
      if (this.isSearchFocused && System.currentTimeMillis() / 600L % 2L == 0L) {
         int cursorX = textX + TextRenderer.getWidth(this.searchQuery);
         int cursorH = 16;
         context.fill(cursorX, textY - 2, cursorX + 2, textY + cursorH, ACCENT_PRIMARY.getRGB());
      }

      if (!this.searchQuery.isEmpty()) {
         int clearX = searchX + searchW - 40;
         int clearY = searchY + (searchH - 28) / 2;
         boolean clearHovered = this.isPointInRect(mouseX, mouseY, clearX, clearY, 28, 28);
         Color clearBg = clearHovered ? new Color(ERROR.getRed(), ERROR.getGreen(), ERROR.getBlue(), 100) : BG_ELEVATED;
         RenderUtils.renderRoundedQuadShader(context.getMatrices(), clearBg, clearX, clearY, 28.0, 28.0, 8.0, 50.0);
         TextRenderer.drawCenteredString("✕", context, clearX + 14, clearY + 8, TEXT_SECONDARY.getRGB());
      }
   }

   private void renderModulesGrid(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY, float delta) {
      List<Module> modules = skid.ykow.Ykow.INSTANCE.getModuleManager().keyCodec(this.selectedCategory);
      if (!this.searchQuery.isEmpty()) {
         modules = modules.stream().filter(m -> m.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())).collect(Collectors.toList());
      }

      int cardW = 280;
      int cardH = 110;
      int gap = 16;
      int cols = Math.max(1, (width - 40) / (cardW + gap));
      int startX = x + 20;
      int startY = y + 20;
      double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
      context.enableScissor((int)(x / scale), (int)(y / scale), (int)((x + width) / scale), (int)((y + height) / scale));

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         if (!this.moduleHoverAnim.containsKey(module)) {
            this.moduleHoverAnim.put(module, 0.0F);
         }

         int row = i / cols;
         int col = i % cols;
         int cardX = startX + col * (cardW + gap);
         int cardY = startY + row * (cardH + gap) - this.moduleScrollOffset * 50;
         if (cardY + cardH >= y && cardY <= y + height) {
            boolean hovered = this.isPointInRect(mouseX, mouseY, cardX, cardY, cardW, cardH);
            boolean enabled = module.isEnabled();
            boolean selected = module == this.selectedModule;
            float targetAnim = hovered ? 1.0F : 0.0F;
            float currentAnim = this.moduleHoverAnim.get(module);
            this.moduleHoverAnim.put(module, currentAnim + (targetAnim - currentAnim) * delta * 0.15F);
            float anim = this.moduleHoverAnim.get(module);
            int liftOffset = (int)(anim * -3.0F);
            Color cardBg;
            if (selected) {
               cardBg = new Color(BG_ELEVATED.getRed(), BG_ELEVATED.getGreen(), BG_ELEVATED.getBlue(), 220);
            } else {
               cardBg = new Color(BG_CARD.getRed(), BG_CARD.getGreen(), BG_CARD.getBlue(), 180 + (int)(40.0F * anim));
            }

            RenderUtils.renderRoundedQuadShader(context.getMatrices(), cardBg, cardX, cardY + liftOffset, cardW, cardH, 12.0, 80.0);
            if (enabled || selected) {
               Color borderColor = enabled
                  ? new Color(SUCCESS.getRed(), SUCCESS.getGreen(), SUCCESS.getBlue(), 100 + (int)(60.0F * anim))
                  : new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 80);
               RenderUtils.renderRoundedOutline(
                  context, borderColor, cardX, cardY + liftOffset, cardX + cardW, cardY + liftOffset + cardH, 12.0, 12.0, 12.0, 12.0, 1.5 + anim, 80.0
               );
            }

            int statusX = cardX + 16;
            int statusY = cardY + liftOffset + 20;
            Color statusColor = enabled ? SUCCESS : new Color(71, 85, 105);
            if (enabled) {
               int pulseAlpha = 20 + (int)(15.0 * Math.sin(this.globalTime * 4.0F));
               RenderUtils.renderCircle(
                  context.getMatrices(), new Color(SUCCESS.getRed(), SUCCESS.getGreen(), SUCCESS.getBlue(), pulseAlpha), statusX, statusY, 10.0, 16
               );
            }

            RenderUtils.renderCircle(context.getMatrices(), statusColor, statusX, statusY, 6.0, 16);
            int nameX = statusX + 20;
            int nameY = cardY + liftOffset + 15;
            TextRenderer.drawString(module.getName().toString(), context, nameX, nameY, TEXT_PRIMARY.getRGB());
            String desc = module.getDescription().toString();
            int descMaxW = cardW - 32;
            if (TextRenderer.getWidth(desc) > descMaxW) {
               while (TextRenderer.getWidth(desc + "...") > descMaxW && desc.length() > 1) {
                  desc = desc.substring(0, desc.length() - 1);
               }

               desc = desc + "...";
            }

            int descY = cardY + liftOffset + 52;
            TextRenderer.drawString(desc, context, cardX + 16, descY, TEXT_MUTED.getRGB());
            int footerY = cardY + liftOffset + cardH - 36;
            if (!module.getSettings().isEmpty()) {
               int settingsCount = module.getSettings().size();
               String settingsText = settingsCount + " setting" + (settingsCount != 1 ? "s" : "");
               TextRenderer.drawString(settingsText, context, cardX + 16, footerY + 8, TEXT_DIM.getRGB());
            }

            int switchX = cardX + cardW - 60;
            this.renderEnhancedSwitch(context, switchX, footerY, enabled, hovered || selected);
         }
      }

      context.disableScissor();
      if (modules.isEmpty()) {
         int emptyY = y + height / 2 - 40;
         TextRenderer.drawCenteredString("No modules found", context, x + width / 2, emptyY, TEXT_MUTED.getRGB());
         TextRenderer.drawCenteredString("Try a different search term", context, x + width / 2, emptyY + 20, TEXT_DIM.getRGB());
      }
   }

   private void renderEnhancedSwitch(DrawContext context, int x, int y, boolean enabled, boolean highlighted) {
      int switchW = 48;
      int switchH = 26;
      Color trackColor = enabled ? new Color(SUCCESS.getRed(), SUCCESS.getGreen(), SUCCESS.getBlue(), 180) : new Color(71, 85, 105, 150);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), trackColor, x, y, switchW, switchH, 13.0, 60.0);
      if (!enabled) {
         Color innerShadow = new Color(0, 0, 0, 30);
         RenderUtils.renderRoundedQuadShader(context.getMatrices(), innerShadow, x + 2, y + 2, switchW - 4, switchH - 4, 11.0, 50.0);
      }

      int knobSize = 20;
      float knobX = enabled ? x + switchW - knobSize - 3 : x + 3;
      Color knobShadow = new Color(0, 0, 0, 40);
      RenderUtils.renderCircle(context.getMatrices(), knobShadow, knobX + knobSize / 2.0F + 1.0F, y + switchH / 2.0F + 1.0F, knobSize / 2.0F + 1.0F, 16);
      Color knobColor = enabled ? new Color(248, 250, 252) : new Color(203, 213, 225);
      RenderUtils.renderCircle(context.getMatrices(), knobColor, knobX + knobSize / 2.0F, y + switchH / 2.0F, knobSize / 2.0F, 20);
      if (highlighted || enabled) {
         Color highlight = new Color(255, 255, 255, enabled ? 120 : 60);
         RenderUtils.renderCircle(context.getMatrices(), highlight, knobX + knobSize / 2.0F - 2.0F, y + switchH / 2.0F - 2.0F, 4.0, 12);
      }
   }

   private void renderSettingsPanel(DrawContext context, int mouseX, int mouseY, float delta) {
      int panelW = 320;
      int panelX = this.guiX + 980 - panelW;
      int panelY = this.guiY + 72;
      int panelH = 568;
      int slideOffset = (int)((1.0F - this.panelTransition) * 50.0F);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_DEEP, panelX + slideOffset, panelY, panelW, panelH, 0.0, 100.0);
      context.fill(panelX + slideOffset, panelY, panelX + slideOffset + 1, panelY + panelH, DIVIDER.getRGB());
      int headerY = panelY + 20;
      int iconX = panelX + slideOffset + 20;
      RenderUtils.renderCircle(context.getMatrices(), ACCENT_PRIMARY, iconX + 6, headerY + 8, 6.0, 12);
      String moduleName = this.selectedModule.getName().toString();
      int nameX = iconX + 20;
      TextRenderer.drawString(moduleName, context, nameX, headerY + 3, TEXT_PRIMARY.getRGB());
      boolean enabled = this.selectedModule.isEnabled();
      String statusText = enabled ? "ENABLED" : "DISABLED";
      Color statusColor = enabled ? SUCCESS : TEXT_MUTED;
      int statusY = headerY + 22;
      TextRenderer.drawString(statusText, context, nameX, statusY, statusColor.getRGB());
      int closeX = panelX + slideOffset + panelW - 20 - 32;
      boolean closeHovered = this.isPointInRect(mouseX, mouseY, closeX, headerY, 32, 32);
      Color closeBg = closeHovered ? new Color(ERROR.getRed(), ERROR.getGreen(), ERROR.getBlue(), 100) : BG_ELEVATED;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), closeBg, closeX, headerY, 32.0, 32.0, 10.0, 60.0);
      TextRenderer.drawCenteredString("✕", context, closeX + 16, headerY + 10, TEXT_SECONDARY.getRGB());
      int settingsStartY = headerY + 70;
      int availableHeight = panelH - (settingsStartY - panelY) - 20;
      double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
      context.enableScissor(
         (int)((panelX + slideOffset) / scale),
         (int)(settingsStartY / scale),
         (int)((panelX + slideOffset + panelW) / scale),
         (int)((settingsStartY + availableHeight) / scale)
      );
      int settingY = settingsStartY - this.settingsScrollOffset;
      List<Setting> settings = this.selectedModule.getSettings();
      if (settings.isEmpty()) {
         int emptyY = settingsStartY + availableHeight / 2 - 20;
         TextRenderer.drawCenteredString("No settings available", context, panelX + slideOffset + panelW / 2, emptyY, TEXT_MUTED.getRGB());
      } else {
         for (Setting setting : settings) {
            int itemHeight = this.getSettingHeight(setting);
            if (settingY + itemHeight >= settingsStartY && settingY <= settingsStartY + availableHeight) {
               this.renderSettingItem(context, panelX + slideOffset + 20, settingY, panelW - 40, setting, mouseX, mouseY);
            }

            settingY += itemHeight + 16;
         }
      }

      context.disableScissor();
      int totalHeight = this.getTotalSettingsHeight(settings);
      if (totalHeight > availableHeight) {
         this.renderScrollbar(context, panelX + slideOffset, settingsStartY, panelW, availableHeight, totalHeight, this.settingsScrollOffset);
      }
   }

   private void renderScrollbar(DrawContext context, int panelX, int startY, int panelW, int viewHeight, int contentHeight, int scrollOffset) {
      int scrollbarW = 5;
      int scrollbarX = panelX + panelW - 10 - scrollbarW;
      RenderUtils.renderRoundedQuadShader(
         context.getMatrices(),
         new Color(BG_ELEVATED.getRed(), BG_ELEVATED.getGreen(), BG_ELEVATED.getBlue(), 100),
         scrollbarX,
         startY,
         scrollbarW,
         viewHeight,
         3.0,
         50.0
      );
      float scrollRatio = (float)viewHeight / contentHeight;
      int thumbHeight = Math.max(30, (int)(viewHeight * scrollRatio));
      float scrollProgress = (float)scrollOffset / (contentHeight - viewHeight);
      int thumbY = startY + (int)((viewHeight - thumbHeight) * scrollProgress);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), ACCENT_PRIMARY, scrollbarX, thumbY, scrollbarW, thumbHeight, 3.0, 50.0);
   }

   private int getTotalSettingsHeight(List<Setting> settings) {
      int total = 0;

      for (Setting setting : settings) {
         total += this.getSettingHeight(setting) + 16;
      }

      return total;
   }

   private int getSettingHeight(Setting setting) {
      if (setting instanceof NumberSetting || setting instanceof MinMaxSetting) {
         return 70;
      } else {
         return setting instanceof ColorSetting ? 80 : 64;
      }
   }

   private void renderSettingItem(DrawContext context, int x, int y, int width, Setting setting, int mouseX, int mouseY) {
      int itemHeight = this.getSettingHeight(setting);
      boolean hovered = this.isPointInRect(mouseX, mouseY, x, y, width, itemHeight);
      Color itemBg = hovered ? BG_HOVER : new Color(BG_CARD.getRed(), BG_CARD.getGreen(), BG_CARD.getBlue(), 120);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), itemBg, x, y, width, itemHeight, 10.0, 70.0);
      TextRenderer.drawString(setting.getName().toString(), context, x + 14, y + 14, TEXT_SECONDARY.getRGB());
      if (setting instanceof BooleanSetting bool) {
         this.renderEnhancedSwitch(context, x + width - 60, y + 18, bool.getValue(), hovered);
      } else if (setting instanceof NumberSetting num) {
         this.renderNumberSetting(context, x, y, width, num, mouseX, mouseY, hovered);
      } else if (setting instanceof MinMaxSetting minMax) {
         this.renderMinMaxSetting(context, x, y, width, minMax, mouseX, mouseY, hovered);
      } else if (setting instanceof ModeSetting<?> mode) {
         this.renderModeSetting(context, x, y, width, mode, hovered);
      } else if (setting instanceof BlocksSetting blocks) {
         this.renderCountSetting(context, x, y, width, blocks.size(), "block");
      } else if (setting instanceof BindSetting bind) {
         this.renderBindSetting(context, x, y, width, bind, hovered);
      } else if (setting instanceof StringSetting str) {
         this.renderStringSetting(context, x, y, width, str, hovered);
      } else if (setting instanceof ColorSetting color) {
         this.renderColorSetting(context, x, y, width, color, hovered);
      } else if (setting instanceof ItemSetting item) {
         this.renderItemSetting(context, x, y, width, item);
      } else if (setting instanceof MacroSetting macro) {
         this.renderCountSetting(context, x, y, width, macro.getCommands().size(), "command");
      } else if (setting instanceof FriendsSetting friends) {
         this.renderCountSetting(context, x, y, width, friends.size(), "friend");
      }
   }

   private void renderNumberSetting(DrawContext context, int x, int y, int width, NumberSetting num, int mouseX, int mouseY, boolean hovered) {
      String value = this.formatNumberValue(num);
      int valueX = x + width - TextRenderer.getWidth(value) - 14;
      TextRenderer.drawString(value, context, valueX, y + 14, ACCENT_PRIMARY.getRGB());
      int sliderY = y + 42;
      int sliderX = x + 14;
      int sliderW = width - 28;
      int sliderH = 6;
      RenderUtils.renderRoundedQuadShader(
         context.getMatrices(),
         new Color(BG_ELEVATED.getRed(), BG_ELEVATED.getGreen(), BG_ELEVATED.getBlue(), 150),
         sliderX,
         sliderY,
         sliderW,
         sliderH,
         3.0,
         50.0
      );
      double progress = (num.getValue() - num.getMin()) / (num.getMax() - num.getMin());
      int progressW = (int)(sliderW * progress);
      Color progressColor = new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 200);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), progressColor, sliderX, sliderY, progressW, sliderH, 3.0, 50.0);
      int thumbX = sliderX + progressW;
      int thumbY = sliderY + sliderH / 2;
      boolean thumbHovered = Math.abs(mouseX - thumbX) < 10 && Math.abs(mouseY - thumbY) < 10;
      if (thumbHovered || this.isDraggingSlider) {
         RenderUtils.renderCircle(
            context.getMatrices(), new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), 40), thumbX, thumbY, 12.0, 16
         );
      }

      RenderUtils.renderCircle(context.getMatrices(), ACCENT_PRIMARY, thumbX, thumbY, 8.0, 16);
      RenderUtils.renderCircle(context.getMatrices(), new Color(255, 255, 255, 180), thumbX, thumbY, 4.0, 12);
   }

   private void renderMinMaxSetting(DrawContext context, int x, int y, int width, MinMaxSetting minMax, int mouseX, int mouseY, boolean hovered) {
      String value = this.formatMinMaxValue(minMax);
      int valueX = x + width - TextRenderer.getWidth(value) - 14;
      TextRenderer.drawString(value, context, valueX, y + 14, ACCENT_PRIMARY.getRGB());
      int sliderY = y + 42;
      int sliderX = x + 14;
      int sliderW = width - 28;
      int sliderH = 6;
      RenderUtils.renderRoundedQuadShader(
         context.getMatrices(),
         new Color(BG_ELEVATED.getRed(), BG_ELEVATED.getGreen(), BG_ELEVATED.getBlue(), 150),
         sliderX,
         sliderY,
         sliderW,
         sliderH,
         3.0,
         50.0
      );
      double minProgress = (minMax.getCurrentMin() - minMax.getMinValue()) / (minMax.getMaxValue() - minMax.getMinValue());
      double maxProgress = (minMax.getCurrentMax() - minMax.getMinValue()) / (minMax.getMaxValue() - minMax.getMinValue());
      int minX = sliderX + (int)(sliderW * minProgress);
      int maxX = sliderX + (int)(sliderW * maxProgress);
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), ACCENT_PRIMARY, minX, sliderY, maxX - minX, sliderH, 3.0, 50.0);
      int thumbY = sliderY + sliderH / 2;
      RenderUtils.renderCircle(context.getMatrices(), ACCENT_PRIMARY, minX, thumbY, 8.0, 16);
      RenderUtils.renderCircle(context.getMatrices(), ACCENT_SECONDARY, maxX, thumbY, 8.0, 16);
      RenderUtils.renderCircle(context.getMatrices(), new Color(255, 255, 255, 180), minX, thumbY, 4.0, 12);
      RenderUtils.renderCircle(context.getMatrices(), new Color(255, 255, 255, 180), maxX, thumbY, 4.0, 12);
   }

   private void renderModeSetting(DrawContext context, int x, int y, int width, ModeSetting<?> mode, boolean hovered) {
      String modeName = mode.getValue().name();
      int btnW = Math.min(140, TextRenderer.getWidth(modeName) + 28);
      int btnX = x + width - btnW - 14;
      int btnY = y + 36;
      Color btnBg = hovered ? BG_HOVER : BG_ELEVATED;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), btnBg, btnX, btnY, btnW, 24.0, 8.0, 60.0);
      TextRenderer.drawCenteredString(modeName, context, btnX + btnW / 2, btnY + 8, ACCENT_PRIMARY.getRGB());
      TextRenderer.drawString("‹", context, btnX + 6, btnY + 6, TEXT_MUTED.getRGB());
      TextRenderer.drawString("›", context, btnX + btnW - 14, btnY + 6, TEXT_MUTED.getRGB());
   }

   private void renderBindSetting(DrawContext context, int x, int y, int width, BindSetting bind, boolean hovered) {
      String keyName = bind.isListening() ? "Press a key..." : KeyUtils.getKey(bind.getValue()).toString();
      int btnW = Math.min(140, TextRenderer.getWidth(keyName) + 28);
      int btnX = x + width - btnW - 14;
      int btnY = y + 36;
      Color btnBg;
      if (bind.isListening()) {
         int pulseAlpha = 100 + (int)(60.0 * Math.sin(this.globalTime * 5.0F));
         btnBg = new Color(ACCENT_PRIMARY.getRed(), ACCENT_PRIMARY.getGreen(), ACCENT_PRIMARY.getBlue(), pulseAlpha);
      } else {
         btnBg = hovered ? BG_HOVER : BG_ELEVATED;
      }

      RenderUtils.renderRoundedQuadShader(context.getMatrices(), btnBg, btnX, btnY, btnW, 24.0, 8.0, 60.0);
      Color textColor = bind.isListening() ? TEXT_PRIMARY : ACCENT_PRIMARY;
      TextRenderer.drawCenteredString(keyName, context, btnX + btnW / 2, btnY + 8, textColor.getRGB());
   }

   private void renderStringSetting(DrawContext context, int x, int y, int width, StringSetting str, boolean hovered) {
      String value = str.getValue();
      if (value.length() > 18) {
         value = value.substring(0, 15) + "...";
      }

      int btnW = Math.min(160, TextRenderer.getWidth(value) + 32);
      int btnX = x + width - btnW - 14;
      int btnY = y + 36;
      Color btnBg = hovered ? BG_HOVER : BG_ELEVATED;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), btnBg, btnX, btnY, btnW, 24.0, 8.0, 60.0);
      TextRenderer.drawString(value, context, btnX + 12, btnY + 8, TEXT_SECONDARY.getRGB());
      TextRenderer.drawString("✎", context, btnX + btnW - 20, btnY + 8, TEXT_MUTED.getRGB());
   }

   private void renderColorSetting(DrawContext context, int x, int y, int width, ColorSetting color, boolean hovered) {
      Color colorValue = color.getValue();
      int previewSize = 32;
      int previewX = x + width - previewSize - 14;
      int previewY = y + 32;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), colorValue, previewX, previewY, previewSize, previewSize, 8.0, 60.0);
      RenderUtils.renderRoundedOutline(context, TEXT_MUTED, previewX, previewY, previewX + previewSize, previewY + previewSize, 8.0, 8.0, 8.0, 8.0, 1.5, 60.0);
      String rgb = String.format("RGB(%d,%d,%d)", colorValue.getRed(), colorValue.getGreen(), colorValue.getBlue());
      TextRenderer.drawString(rgb, context, x + 14, y + 42, TEXT_MUTED.getRGB());
   }

   private void renderItemSetting(DrawContext context, int x, int y, int width, ItemSetting item) {
      Item itemValue = item.getItem();
      int iconSize = 28;
      int iconX = x + width - iconSize - 16;
      int iconY = y + 32;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_ELEVATED, iconX - 4, iconY - 4, iconSize + 8, iconSize + 8, 8.0, 60.0);
      if (itemValue != null && itemValue != Items.AIR) {
         context.drawItem(new ItemStack(itemValue), iconX, iconY);
      } else {
         TextRenderer.drawCenteredString("?", context, iconX + iconSize / 2, iconY + 8, TEXT_MUTED.getRGB());
      }
   }

   private void renderCountSetting(DrawContext context, int x, int y, int width, int count, String singular) {
      String countText = count + " " + singular + (count != 1 ? "s" : "");
      int btnW = Math.min(140, TextRenderer.getWidth(countText) + 28);
      int btnX = x + width - btnW - 14;
      int btnY = y + 36;
      RenderUtils.renderRoundedQuadShader(context.getMatrices(), BG_ELEVATED, btnX, btnY, btnW, 24.0, 8.0, 60.0);
      TextRenderer.drawCenteredString(countText, context, btnX + btnW / 2, btnY + 8, ACCENT_PRIMARY.getRGB());
   }

   private String formatNumberValue(NumberSetting num) {
      double format = num.getFormat();
      double value = num.getValue();
      if (format == 0.1) {
         return String.format("%.1f", value);
      } else if (format == 0.01) {
         return String.format("%.2f", value);
      } else if (format == 0.001) {
         return String.format("%.3f", value);
      } else {
         return format >= 1.0 ? String.format("%.0f", value) : String.valueOf(value);
      }
   }

   private String formatMinMaxValue(MinMaxSetting minMax) {
      double step = minMax.getStep();
      String minStr;
      String maxStr;
      if (step == 0.1) {
         minStr = String.format("%.1f", minMax.getCurrentMin());
         maxStr = String.format("%.1f", minMax.getCurrentMax());
      } else if (step == 0.01) {
         minStr = String.format("%.2f", minMax.getCurrentMin());
         maxStr = String.format("%.2f", minMax.getCurrentMax());
      } else if (step >= 1.0) {
         minStr = String.format("%.0f", minMax.getCurrentMin());
         maxStr = String.format("%.0f", minMax.getCurrentMax());
      } else {
         minStr = String.valueOf(minMax.getCurrentMin());
         maxStr = String.valueOf(minMax.getCurrentMax());
      }

      return minStr + " - " + maxStr;
   }

   private boolean isPointInRect(int px, int py, int x, int y, int width, int height) {
      return px >= x && px <= x + width && py >= y && py <= y + height;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.shouldUseReaperGUI()) {
         int scaledX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
         int scaledY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
         int closeX = this.guiX + 980 - 20 - 36;
         int closeY = this.guiY + 18;
         if (this.isPointInRect(scaledX, scaledY, closeX, closeY, 36, 36)) {
            this.close();
            return true;
         } else if (this.selectedModule != null && this.handleSettingsInteraction(scaledX, scaledY, button)) {
            return true;
         } else {
            if (!this.searchQuery.isEmpty()) {
               int contentX = this.guiX + 240;
               int contentW = 740 - (this.selectedModule != null ? 320 : 0);
               int searchX = contentX + 20;
               int searchW = contentW - 40;
               int clearX = searchX + searchW - 40;
               int clearY = this.guiY + 72 + 20 + 12;
               if (this.isPointInRect(scaledX, scaledY, clearX, clearY, 28, 28)) {
                  this.searchQuery = "";
                  return true;
               }
            }

            int contentX = this.guiX + 240;
            int contentW = 740 - (this.selectedModule != null ? 320 : 0);
            int searchX = contentX + 20;
            int searchY = this.guiY + 72 + 20;
            int searchW = contentW - 40;
            if (this.isPointInRect(scaledX, scaledY, searchX, searchY, searchW, 52)) {
               this.isSearchFocused = true;
               return true;
            } else {
               this.isSearchFocused = false;
               if (this.handleCategoryClick(scaledX, scaledY)) {
                  return true;
               } else if (this.handleModuleClick(scaledX, scaledY, button)) {
                  return true;
               } else if (this.isPointInRect(scaledX, scaledY, this.guiX, this.guiY, 980, 72)) {
                  this.isDragging = true;
                  this.dragOffsetX = scaledX - this.guiX;
                  this.dragOffsetY = scaledY - this.guiY;
                  return true;
               } else {
                  return super.mouseClicked(mouseX, mouseY, button);
               }
            }
         }
      } else {
         double scaledX = mouseX * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
         double scaledY = mouseY * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();

         for (CategoryWindow window : this.reaperWindows) {
            window.mouseClicked(scaledX, scaledY, button);
         }

         return super.mouseClicked(scaledX, scaledY, button);
      }
   }

   private boolean handleSettingsInteraction(int mouseX, int mouseY, int button) {
      int panelX = this.guiX + 980 - 320;
      int panelW = 320;
      int panelY = this.guiY + 72;
      int panelH = 568;
      int settingsCloseX = panelX + panelW - 20 - 32;
      int settingsCloseY = panelY + 20;
      if (this.isPointInRect(mouseX, mouseY, settingsCloseX, settingsCloseY, 32, 32)) {
         this.selectedModule = null;
         this.settingsScrollOffset = 0;
         this.panelTransition = 0.0F;
         return true;
      } else {
         int headerY = panelY + 20;
         int settingsStartY = headerY + 70;
         int settingY = settingsStartY - this.settingsScrollOffset;
         int availableHeight = panelH - (settingsStartY - panelY) - 20;
         List<Setting> settings = this.selectedModule.getSettings();
         int settingX = panelX + 20;
         int settingW = panelW - 40;

         for (Setting setting : settings) {
            int itemHeight = this.getSettingHeight(setting);
            if (settingY + itemHeight >= settingsStartY
               && settingY <= settingsStartY + availableHeight
               && this.isPointInRect(mouseX, mouseY, settingX, settingY, settingW, itemHeight)) {
               if (setting instanceof BooleanSetting bool) {
                  bool.setValue(!bool.getValue());
                  return true;
               }

               if (setting instanceof NumberSetting num) {
                  int sliderY = settingY + 42;
                  int sliderX = settingX + 14;
                  int sliderW = settingW - 28;
                  if (mouseY >= sliderY - 8 && mouseY <= sliderY + 14) {
                     this.isDraggingSlider = true;
                     this.draggedSetting = setting;
                     this.updateSliderValue(num, mouseX, sliderX, sliderW);
                     return true;
                  }
               } else if (setting instanceof MinMaxSetting minMax) {
                  int sliderY = settingY + 42;
                  int sliderX = settingX + 14;
                  int sliderW = settingW - 28;
                  if (mouseY >= sliderY - 8 && mouseY <= sliderY + 14) {
                     double minProgress = (minMax.getCurrentMin() - minMax.getMinValue()) / (minMax.getMaxValue() - minMax.getMinValue());
                     double maxProgress = (minMax.getCurrentMax() - minMax.getMinValue()) / (minMax.getMaxValue() - minMax.getMinValue());
                     int minX = sliderX + (int)(sliderW * minProgress);
                     int maxX = sliderX + (int)(sliderW * maxProgress);
                     this.draggedIsMin = Math.abs(mouseX - minX) < Math.abs(mouseX - maxX);
                     this.isDraggingSlider = true;
                     this.draggedSetting = setting;
                     this.updateMinMaxSlider(minMax, mouseX, sliderX, sliderW, this.draggedIsMin);
                     return true;
                  }
               } else {
                  if (setting instanceof ModeSetting<?> mode) {
                     if (button == 0) {
                        mode.cycleUp();
                     } else if (button == 1) {
                        mode.cycleDown();
                     }

                     return true;
                  }

                  if (setting instanceof BindSetting bind) {
                     if (button == 0) {
                        bind.setListening(!bind.isListening());
                     } else if (button == 1 && bind.isListening()) {
                        bind.setValue(-1);
                        bind.setListening(false);
                     }

                     return true;
                  }

                  if (setting instanceof StringSetting) {
                     MinecraftClient.getInstance().setScreen(new StringBox(this.createDummyTextBox(setting), (StringSetting)setting));
                     return true;
                  }

                  if (setting instanceof ItemSetting) {
                     MinecraftClient.getInstance().setScreen(new ItemFilter(this.createDummyItemBox(setting), (ItemSetting)setting));
                     return true;
                  }

                  if (setting instanceof MacroSetting) {
                     MinecraftClient.getInstance().setScreen(new MacroFilter(this.createDummyMacroBox(setting), (MacroSetting)setting));
                     return true;
                  }

                  if (setting instanceof FriendsSetting) {
                     MinecraftClient.getInstance().setScreen(new FriendsFilter(this.createDummyFriendsBox(setting), (FriendsSetting)setting));
                     return true;
                  }

                  if (setting instanceof BlocksSetting) {
                     MinecraftClient.getInstance().setScreen(new BlocksFilter(this.createDummyBlocksBox(setting), (BlocksSetting)setting));
                     return true;
                  }

                  if (setting instanceof ColorSetting color) {
                     Color current = color.getValue();
                     Color newColor;
                     if (current.equals(Color.WHITE)) {
                        newColor = Color.RED;
                     } else if (current.equals(Color.RED)) {
                        newColor = new Color(255, 128, 0);
                     } else if (current.getRed() == 255 && current.getGreen() == 128) {
                        newColor = Color.YELLOW;
                     } else if (current.equals(Color.YELLOW)) {
                        newColor = Color.GREEN;
                     } else if (current.equals(Color.GREEN)) {
                        newColor = Color.CYAN;
                     } else if (current.equals(Color.CYAN)) {
                        newColor = Color.BLUE;
                     } else if (current.equals(Color.BLUE)) {
                        newColor = new Color(128, 0, 255);
                     } else if (current.getRed() == 128 && current.getBlue() == 255) {
                        newColor = Color.MAGENTA;
                     } else if (current.equals(Color.MAGENTA)) {
                        newColor = new Color(255, 0, 128);
                     } else {
                        newColor = Color.WHITE;
                     }

                     color.setValue(newColor);
                     return true;
                  }
               }
            }

            settingY += itemHeight + 16;
         }

         return false;
      }
   }

   private boolean handleCategoryClick(int mouseX, int mouseY) {
      int sidebarX = this.guiX;
      int categoryY = this.guiY + 72 + 20 + 32;
      Category[] categories = Category.values();

      for (Category cat : categories) {
         int btnX = sidebarX + 12;
         int btnW = 216;
         if (this.isPointInRect(mouseX, mouseY, btnX, categoryY, btnW, 50)) {
            if (this.selectedCategory != cat) {
               this.selectedCategory = cat;
               this.moduleScrollOffset = 0;
            }

            return true;
         }

         categoryY += 58;
      }

      return false;
   }

   private boolean handleModuleClick(int mouseX, int mouseY, int button) {
      boolean lala = Ykow.OpenGui();
      List<Module> modules = skid.ykow.Ykow.INSTANCE.getModuleManager().keyCodec(this.selectedCategory);
      if (!this.searchQuery.isEmpty()) {
         modules = modules.stream().filter(m -> m.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())).collect(Collectors.toList());
      }

      int contentX = this.guiX + 240;
      int contentY = this.guiY + 72 + 90;
      int contentW = 740 - (this.selectedModule != null ? 320 : 0);
      int cardW = 280;
      int cardH = 110;
      int gap = 16;
      int cols = Math.max(1, (contentW - 40) / (cardW + gap));
      int startX = contentX + 20;
      int startY = contentY + 20;

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         int row = i / cols;
         int col = i % cols;
         int cardX = startX + col * (cardW + gap);
         int cardY = startY + row * (cardH + gap) - this.moduleScrollOffset * 50;
         if (this.isPointInRect(mouseX, mouseY, cardX, cardY, cardW, cardH)) {
            int switchX = cardX + cardW - 60;
            int switchY = cardY + cardH - 36;
            if (this.isPointInRect(mouseX, mouseY, switchX, switchY, 48, 26)) {
               module.toggle();
               return true;
            }

            if (button == 0) {
               module.toggle();
            } else if (button == 1) {
               this.selectedModule = module;
               this.settingsScrollOffset = 0;
               this.panelTransition = 0.0F;
            }

            return true;
         }
      }

      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.shouldUseReaperGUI()) {
         if (this.isDraggingSlider && this.draggedSetting != null && this.selectedModule != null) {
            int scaledX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
            int panelX = this.guiX + 980 - 320;
            int panelW = 320;
            int settingX = panelX + 20;
            int settingW = panelW - 40;
            int sliderX = settingX + 14;
            int sliderW = settingW - 28;
            if (this.draggedSetting instanceof NumberSetting) {
               this.updateSliderValue((NumberSetting)this.draggedSetting, scaledX, sliderX, sliderW);
               return true;
            }

            if (this.draggedSetting instanceof MinMaxSetting) {
               this.updateMinMaxSlider((MinMaxSetting)this.draggedSetting, scaledX, sliderX, sliderW, this.draggedIsMin);
               return true;
            }
         }

         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      } else {
         double scaledXx = mouseX * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
         double scaledY = mouseY * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();

         for (CategoryWindow window : this.reaperWindows) {
            window.mouseDragged(scaledXx, scaledY, button, deltaX, deltaY);
         }

         return super.mouseDragged(scaledXx, scaledY, button, deltaX, deltaY);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (!this.shouldUseReaperGUI()) {
         this.isDragging = false;
         this.isDraggingSlider = false;
         this.draggedSetting = null;
         return super.mouseReleased(mouseX, mouseY, button);
      } else {
         double scaledX = mouseX * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();
         double scaledY = mouseY * (int)MinecraftClient.getInstance().getWindow().getScaleFactor();

         for (CategoryWindow window : this.reaperWindows) {
            window.mouseReleased(scaledX, scaledY, button);
         }

         return super.mouseReleased(scaledX, scaledY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (!this.shouldUseReaperGUI()) {
         int scaledX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
         int scaledY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
         if (this.selectedModule != null) {
            int panelX = this.guiX + 980 - 320;
            int panelY = this.guiY + 72;
            int panelW = 320;
            int panelH = 568;
            if (this.isPointInRect(scaledX, scaledY, panelX, panelY, panelW, panelH)) {
               List<Setting> settings = this.selectedModule.getSettings();
               int totalHeight = this.getTotalSettingsHeight(settings);
               int headerHeight = 90;
               int availableHeight = panelH - headerHeight - 20;
               int maxScroll = Math.max(0, totalHeight - availableHeight);
               this.settingsScrollOffset = Math.max(0, Math.min(maxScroll, this.settingsScrollOffset - (int)(verticalAmount * 25.0)));
               return true;
            }
         }

         this.moduleScrollOffset = Math.max(0, this.moduleScrollOffset - (int)verticalAmount);
         return true;
      } else {
         double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();

         for (CategoryWindow window : this.reaperWindows) {
            window.mouseScrolled(mouseX, scaledY, horizontalAmount, verticalAmount);
         }

         return super.mouseScrolled(mouseX, scaledY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.shouldUseReaperGUI()) {
         for (CategoryWindow window : this.reaperWindows) {
            window.keyPressed(keyCode, scanCode, modifiers);
         }

         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         if (this.selectedModule != null) {
            for (Setting setting : this.selectedModule.getSettings()) {
               if (setting instanceof BindSetting bind && bind.isListening()) {
                  if (keyCode == 256) {
                     bind.setListening(false);
                  } else if (keyCode == 259) {
                     bind.setValue(-1);
                     bind.setListening(false);
                  } else {
                     bind.setValue(keyCode);
                     bind.setListening(false);
                  }

                  return true;
               }
            }
         }

         if (this.isSearchFocused) {
            if (keyCode == 259 && !this.searchQuery.isEmpty()) {
               this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
               return true;
            }

            if (keyCode == 256) {
               this.isSearchFocused = false;
               return true;
            }

            if (keyCode == 257) {
               this.isSearchFocused = false;
               return true;
            }
         }

         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.isSearchFocused && !Character.isISOControl(chr)) {
         this.searchQuery = this.searchQuery + chr;
         return true;
      } else {
         return super.charTyped(chr, modifiers);
      }
   }

   private void updateSliderValue(NumberSetting setting, int mouseX, int sliderX, int sliderW) {
      double progress = Math.max(0.0, Math.min(1.0, (double)(mouseX - sliderX) / sliderW));
      double newValue = setting.getMin() + progress * (setting.getMax() - setting.getMin());
      double format = setting.getFormat();
      newValue = Math.round(newValue / format) * format;
      setting.setValue(newValue);
   }

   private void updateMinMaxSlider(MinMaxSetting setting, int mouseX, int sliderX, int sliderW, boolean isMin) {
      double progress = Math.max(0.0, Math.min(1.0, (double)(mouseX - sliderX) / sliderW));
      double newValue = setting.getMinValue() + progress * (setting.getMaxValue() - setting.getMinValue());
      double step = setting.getStep();
      newValue = Math.round(newValue / step) * step;
      if (isMin) {
         setting.setCurrentMin(Math.min(newValue, setting.getCurrentMax()));
      } else {
         setting.setCurrentMax(Math.max(newValue, setting.getCurrentMin()));
      }
   }

   private TextBox createDummyTextBox(Setting setting) {
      return new TextBox(null, setting, 0) {
         @Override
         public int parentX() {
            return 0;
         }

         @Override
         public int parentY() {
            return 0;
         }

         @Override
         public int parentWidth() {
            return 0;
         }

         @Override
         public int parentHeight() {
            return 0;
         }

         @Override
         public int parentOffset() {
            return 0;
         }
      };
   }

   private ItemBox createDummyItemBox(Setting setting) {
      return new ItemBox(null, setting, 0) {
         @Override
         public int parentX() {
            return 0;
         }

         @Override
         public int parentY() {
            return 0;
         }

         @Override
         public int parentWidth() {
            return 0;
         }

         @Override
         public int parentHeight() {
            return 0;
         }

         @Override
         public int parentOffset() {
            return 0;
         }
      };
   }

   private MacroBox createDummyMacroBox(Setting setting) {
      return new MacroBox(null, setting, 0) {
         @Override
         public int parentX() {
            return 0;
         }

         @Override
         public int parentY() {
            return 0;
         }

         @Override
         public int parentWidth() {
            return 0;
         }

         @Override
         public int parentHeight() {
            return 0;
         }

         @Override
         public int parentOffset() {
            return 0;
         }
      };
   }

   private FriendsBox createDummyFriendsBox(Setting setting) {
      return new FriendsBox(null, setting, 0) {
         @Override
         public int parentX() {
            return 0;
         }

         @Override
         public int parentY() {
            return 0;
         }

         @Override
         public int parentWidth() {
            return 0;
         }

         @Override
         public int parentHeight() {
            return 0;
         }

         @Override
         public int parentOffset() {
            return 0;
         }
      };
   }

   private BlocksBox createDummyBlocksBox(Setting setting) {
      return new BlocksBox(null, setting, 0) {
         @Override
         public int parentX() {
            return 0;
         }

         @Override
         public int parentY() {
            return 0;
         }

         @Override
         public int parentWidth() {
            return 0;
         }

         @Override
         public int parentHeight() {
            return 0;
         }

         @Override
         public int parentOffset() {
            return 0;
         }
      };
   }

   public void close() {
      if (this.shouldUseReaperGUI()) {
         skid.ykow.Ykow.INSTANCE.getModuleManager().getModuleByClass(Ykow.class).setEnabled(false);
      } else {
         skid.ykow.Ykow.INSTANCE.getModuleManager().getModuleByClass(Ykow.class).setEnabled(false);
      }

      this.onGuiClose();
      super.close();
   }

   public boolean shouldPause() {
      return false;
   }

   public void onGuiClose() {
      this.selectedModule = null;
      this.searchQuery = "";
      this.isSearchFocused = false;
      this.moduleScrollOffset = 0;
      this.settingsScrollOffset = 0;
      this.isDragging = false;
      this.isDraggingSlider = false;
      this.draggedSetting = null;
      this.panelTransition = 0.0F;
      this.globalTime = 0.0F;
      this.categoryHoverAnim.clear();
      this.moduleHoverAnim.clear();

      for (Category cat : Category.values()) {
         this.categoryHoverAnim.put(cat, 0.0F);
      }

      if (this.shouldUseReaperGUI()) {
         MinecraftClient.getInstance().setScreenAndRender(skid.ykow.Ykow.INSTANCE.screen);
         this.currentColor = null;

         for (CategoryWindow window : this.reaperWindows) {
            window.onGuiClose();
         }
      }
   }
}
