package skid.ykow.module.modules.render;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.Render2DEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;

public final class HUD extends Module {
   private static final CharSequence watermarkText = EncryptedString.of("ykow");
   private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
   private final Map<Module, Float> moduleAnimations = new HashMap<>();
   private float watermarkGlow = 0.0F;
   private long lastFrameTime = System.currentTimeMillis();
   private final BooleanSetting showWatermark = new BooleanSetting(EncryptedString.of("Watermark"), true);
   private final BooleanSetting showInfo = new BooleanSetting(EncryptedString.of("Info"), true);
   private final BooleanSetting showModules = new BooleanSetting("Modules", true);
   private final BooleanSetting showTime = new BooleanSetting("Time", true);
   private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
   private final BooleanSetting showCompass = new BooleanSetting("Compass", true);
   private final ModeSetting<HUD.HUDTheme> theme = new ModeSetting<>("Theme", HUD.HUDTheme.MODERN_DARK, HUD.HUDTheme.class);
   private final ModeSetting<HUD.ModuleListSorting> moduleSortingMode = new ModeSetting<>(
      "Sort Mode", HUD.ModuleListSorting.LENGTH, HUD.ModuleListSorting.class
   );
   private final BooleanSetting blur = new BooleanSetting("Blur Effect", true);
   private final BooleanSetting glow = new BooleanSetting("Glow", true);
   private final BooleanSetting animations = new BooleanSetting("Animations", true);
   private final BooleanSetting gradient = new BooleanSetting("Gradient", true);
   private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
   private final Integer rainbowSpeed = 1;
   private final Integer rainbowSaturation = 1;
   private final Integer rainbowBrightness = 1;
   private final Integer cornerRadius = 8;

   public HUD() {
      super(EncryptedString.of("HUD"), EncryptedString.of("Modern glassmorphic HUD"), -1, Category.RENDER);
      this.addsettings(
         new Setting[]{
            this.showWatermark,
            this.showInfo,
            this.showModules,
            this.showTime,
            this.showCoordinates,
            this.showCompass,
            this.theme,
            this.moduleSortingMode,
            this.blur,
            this.glow,
            this.animations,
            this.gradient,
            this.rainbow
         }
      );
   }

   @EventListener
   public void onRender2D(Render2DEvent event) {
      if (this.mc.currentScreen != Ykow.INSTANCE.GUI) {
         this.updateAnimations();
         DrawContext ctx = event.context;
         int width = this.mc.getWindow().getWidth();
         int height = this.mc.getWindow().getHeight();
         RenderUtils.unscaledProjection();
         HUD.HUDTheme currentTheme = (HUD.HUDTheme)this.theme.getValue();
         if (this.showWatermark.getValue()) {
            this.renderModernWatermark(ctx, width, currentTheme);
         }

         if (this.showInfo.getValue() && this.mc.player != null) {
            this.renderModernInfo(ctx, currentTheme);
         }

         if (this.showCoordinates.getValue() && this.mc.player != null) {
            this.renderModernCoords(ctx, height, currentTheme);
         }

         if (this.showCompass.getValue() && this.mc.player != null) {
            this.renderModernCompass(ctx, width, currentTheme);
         }

         if (this.showModules.getValue()) {
            this.renderModernModules(ctx, width, currentTheme);
         }

         RenderUtils.scaledProjection();
      }
   }

   private void updateAnimations() {
      long currentTime = System.currentTimeMillis();
      float delta = Math.min((float)(currentTime - this.lastFrameTime) / 16.67F, 3.0F);
      this.lastFrameTime = currentTime;
      if (this.animations.getValue()) {
         this.watermarkGlow = (float)((Math.sin(currentTime / 500.0) + 1.0) / 2.0);
         List<Module> enabledModules = Ykow.INSTANCE.getModuleManager().elementCodec();
         this.moduleAnimations.keySet().removeIf(m -> !enabledModules.contains(m));

         for (Module module : enabledModules) {
            float current = this.moduleAnimations.getOrDefault(module, 0.0F);
            this.moduleAnimations.put(module, Math.min(current + 0.08F * delta, 1.0F));
         }
      }
   }

   private void renderModernWatermark(DrawContext ctx, int width, HUD.HUDTheme theme) {
      String watermark = watermarkText.toString();
      String time = timeFormatter.format(new Date());
      int y = 20;
      int spacing = 12;
      int radius = this.cornerRadius;
      int watermarkWidth = TextRenderer.getWidth(watermark);
      int watermarkPadding = 20;
      int watermarkBoxWidth = watermarkWidth + watermarkPadding * 2;
      int watermarkX = width / 2 - watermarkBoxWidth / 2;
      Color accentColor = this.rainbow.getValue() ? this.getRainbowColor(0) : theme.accent;
      this.renderGlassBox(ctx, watermarkX, y, watermarkBoxWidth, 32, theme, radius, true, accentColor);
      if (this.gradient.getValue() && !this.rainbow.getValue()) {
         this.renderGradientOverlay(ctx, watermarkX, y, watermarkBoxWidth, 32, theme.accent, theme.secondary, radius);
      }

      Color watermarkColor = this.rainbow.getValue() ? this.getRainbowColor(5) : this.interpolateColor(theme.text, theme.accent, this.watermarkGlow * 0.3F);
      this.drawModernText(ctx, watermark, watermarkX, y, watermarkBoxWidth, 32, watermarkColor, true);
      if (this.showTime.getValue()) {
         int timeWidth = TextRenderer.getWidth(time);
         int timePadding = 16;
         int timeBoxWidth = timeWidth + timePadding * 2;
         int timeX = width / 2 - timeBoxWidth / 2;
         int timeY = y + 32 + spacing;
         Color timeAccent = this.rainbow.getValue() ? this.getRainbowColor(10) : theme.accent;
         this.renderGlassBox(ctx, timeX, timeY, timeBoxWidth, 24, theme, radius, false, timeAccent);
         Color timeColor = this.rainbow.getValue() ? this.getRainbowColor(15) : theme.subtext;
         this.drawModernText(ctx, time, timeX, timeY, timeBoxWidth, 24, timeColor, false);
      }
   }

   private void renderModernInfo(DrawContext ctx, HUD.HUDTheme theme) {
      int x = 20;
      int y = 20;
      int spacing = 10;
      int radius = this.cornerRadius;
      String[] infoItems = new String[]{"FPS " + this.mc.getCurrentFps(), "ykow+"};

      for (int i = 0; i < infoItems.length; i++) {
         String item = infoItems[i];
         int textWidth = TextRenderer.getWidth(item);
         int boxWidth = textWidth + 24;
         int boxHeight = 28;
         int currentY = y + (boxHeight + spacing) * i;
         Color boxAccent = this.rainbow.getValue() ? this.getRainbowColor(20 + i * 5) : theme.accent;
         this.renderGlassBox(ctx, x, currentY, boxWidth, boxHeight, theme, radius, false, boxAccent);
         Color accentColor = this.rainbow.getValue() ? this.getRainbowColor(25 + i * 5) : (i == 0 ? theme.accent : theme.primary);
         RenderUtils.renderRoundedQuad(ctx.getMatrices(), accentColor, x + 4, currentY + 4, x + 6, currentY + boxHeight - 4, 1.0, 1.0);
         Color textColor = this.rainbow.getValue() ? this.getRainbowColor(30 + i * 5) : theme.text;
         this.drawModernText(ctx, item, x + 10, currentY, boxWidth - 10, boxHeight, textColor, false);
      }
   }

   private void renderModernCoords(DrawContext ctx, int height, HUD.HUDTheme theme) {
      if (this.mc.player != null) {
         int x = 20;
         int y = height - 60;
         int radius = this.cornerRadius;
         String coords = String.format("%.0f %.0f %.0f", this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
         String dimension = this.getDimensionCoords();
         String secondLine = dimension.isEmpty() ? this.getDimensionName() : dimension;
         int maxWidth = Math.max(TextRenderer.getWidth(coords), TextRenderer.getWidth(secondLine));
         int boxWidth = maxWidth + 28;
         int boxHeight = 44;
         Color coordAccent = this.rainbow.getValue() ? this.getRainbowColor(40) : theme.accent;
         this.renderGlassBox(ctx, x, y, boxWidth, boxHeight, theme, radius, false, coordAccent);
         Color iconColor = this.rainbow.getValue() ? this.getRainbowColor(45) : theme.accent;
         RenderUtils.renderRoundedQuad(ctx.getMatrices(), iconColor, x + 8, y + 8, x + 12, y + 12, 2.0, 2.0);
         Color coordsColor = this.rainbow.getValue() ? this.getRainbowColor(50) : theme.text;
         Color dimensionColor = this.rainbow.getValue() ? this.getRainbowColor(55) : theme.subtext;
         this.drawModernText(ctx, coords, x + 18, y + 4, boxWidth - 18, 20, coordsColor, false);
         this.drawModernText(ctx, secondLine, x + 18, y + 24, boxWidth - 18, 20, dimensionColor, false);
      }
   }

   private void renderModernCompass(DrawContext ctx, int width, HUD.HUDTheme theme) {
      if (this.mc.player != null) {
         int compassWidth = 200;
         int compassHeight = 36;
         int x = width / 2 - compassWidth / 2;
         int y = 100;
         int radius = this.cornerRadius;
         Color compassAccent = this.rainbow.getValue() ? this.getRainbowColor(75) : theme.accent;
         this.renderGlassBox(ctx, x, y, compassWidth, compassHeight, theme, radius, false, compassAccent);
         float yaw = (this.mc.player.getYaw() % 360.0F + 360.0F) % 360.0F;
         String[] directions = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S"};
         float[] angles = new float[]{0.0F, 45.0F, 90.0F, 135.0F, 180.0F, 225.0F, 270.0F, 315.0F, 360.0F};
         int centerX = x + compassWidth / 2;
         int centerY = y + compassHeight / 2;

         for (int i = 0; i < directions.length; i++) {
            float angle = angles[i];
            float offset = angle - yaw;

            while (offset > 180.0F) {
               offset -= 360.0F;
            }

            while (offset < -180.0F) {
               offset += 360.0F;
            }

            int dirX = centerX - (int)(offset * 1.5F);
            if (dirX >= x + 15 && dirX <= x + compassWidth - 15) {
               float distance = Math.abs(offset);
               float alpha = Math.max(0.0F, 1.0F - distance / 90.0F);
               boolean isCardinal = i % 2 == 0;
               String dir = directions[i];
               Color dirColor;
               if (this.rainbow.getValue()) {
                  dirColor = this.getRainbowColor(80 + i * 5);
               } else if (distance < 5.0F) {
                  dirColor = theme.accent;
               } else if (isCardinal) {
                  dirColor = theme.text;
               } else {
                  dirColor = theme.subtext;
               }

               Color finalColor = new Color(dirColor.getRed(), dirColor.getGreen(), dirColor.getBlue(), (int)(dirColor.getAlpha() * alpha));
               int textWidth = TextRenderer.getWidth(dir);
               int textX = dirX - textWidth / 2;
               int textY = centerY - 5;
               TextRenderer.drawString(dir, ctx, textX, textY, finalColor.getRGB());
               if (distance < 2.0F) {
                  Color indicatorColor = this.rainbow.getValue() ? this.getRainbowColor(85) : theme.accent;
                  RenderUtils.renderRoundedQuad(ctx.getMatrices(), indicatorColor, centerX - 1, y + 6, centerX + 1, y + 10, 1.0, 1.0);
                  RenderUtils.renderRoundedQuad(
                     ctx.getMatrices(), indicatorColor, centerX - 1, y + compassHeight - 10, centerX + 1, y + compassHeight - 6, 1.0, 1.0
                  );
               }
            }
         }
      }
   }

   private void renderModernModules(DrawContext ctx, int width, HUD.HUDTheme theme) {
      List modules = this.getSortedModules();
      if (!modules.isEmpty()) {
         int y = 20;
         int rightMargin = 20;
         int spacing = 8;
         int radius = this.cornerRadius;

         for (int i = 0; i < modules.size(); i++) {
            Module module = (Module)modules.get(i);
            String name = module.getName().toString();
            float animation = this.animations.getValue() ? this.moduleAnimations.getOrDefault(module, 0.0F) : 1.0F;
            if (!(animation < 0.01F)) {
               int textWidth = TextRenderer.getWidth(name);
               int padding = 16;
               int boxWidth = (int)((textWidth + padding * 2) * animation);
               int boxHeight = 26;
               int x = width - boxWidth - rightMargin;
               Color moduleAccent = this.rainbow.getValue() ? this.getRainbowColor(60 + i * 3) : theme.accent;
               this.renderGlassBox(ctx, x, y, boxWidth, boxHeight, theme, radius, false, moduleAccent);
               Color moduleColor = this.rainbow.getValue() ? this.getRainbowColor(65 + i * 3) : this.getModuleAccent(theme, i);
               int barWidth = (int)(3.0F * animation);
               RenderUtils.renderRoundedQuad(ctx.getMatrices(), moduleColor, x, y, x + barWidth, y + boxHeight, radius, 0.0);
               Color baseTextColor = this.rainbow.getValue() ? this.getRainbowColor(70 + i * 3) : theme.text;
               Color textColor = new Color(
                  baseTextColor.getRed(), baseTextColor.getGreen(), baseTextColor.getBlue(), (int)(baseTextColor.getAlpha() * animation)
               );
               this.drawModernText(ctx, name, x, y, boxWidth, boxHeight, textColor, false);
               y += (int)((boxHeight + spacing) * animation);
            }
         }
      }
   }

   private void renderGlassBox(DrawContext ctx, int x, int y, int width, int height, HUD.HUDTheme theme, int radius, boolean emphasized, Color accentColor) {
      Color bgColor = emphasized ? theme.backgroundEmphasized : theme.background;
      RenderUtils.renderRoundedQuad(ctx.getMatrices(), bgColor, x, y, x + width, y + height, radius, radius);
      if (this.glow.getValue()) {
         Color glowColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), emphasized ? 40 : 20);
         RenderUtils.renderRoundedQuad(ctx.getMatrices(), glowColor, x - 1, y - 1, x + width + 1, y + height + 1, radius + 1, radius + 1);
      }

      Color highlight = new Color(255, 255, 255, 15);
      RenderUtils.renderRoundedQuad(ctx.getMatrices(), highlight, x + 2, y + 1, x + width - 2, y + 2, radius, radius);
   }

   private void renderGradientOverlay(DrawContext ctx, int x, int y, int width, int height, Color c1, Color c2, int radius) {
      Color gradStart = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 30);
      new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 10);
      RenderUtils.renderRoundedQuad(ctx.getMatrices(), gradStart, x, y, x + width, y + height / 2, radius, 0.0);
   }

   private void drawModernText(DrawContext ctx, String text, int boxX, int boxY, int boxWidth, int boxHeight, Color color, boolean large) {
      int textWidth = TextRenderer.getWidth(text);
      int textHeight = 10;
      int textX = boxX + (boxWidth - textWidth) / 2;
      int textY = boxY + (boxHeight - textHeight) / 2;
      if (this.blur.getValue()) {
         TextRenderer.drawString(text, ctx, textX + 1, textY + 1, new Color(0, 0, 0, 60).getRGB());
      }

      TextRenderer.drawString(text, ctx, textX, textY, color.getRGB());
   }

   private String getDimensionName() {
      if (this.mc.world == null) {
         return "";
      } else {
         String dimension = this.mc.world.getRegistryKey().getValue().getPath();
         return dimension.contains("nether") ? "Nether" : (dimension.contains("end") ? "End" : "Overworld");
      }
   }

   private String getDimensionCoords() {
      if (this.mc.world == null) {
         return "";
      } else {
         String dimension = this.mc.world.getRegistryKey().getValue().getPath();
         if (dimension.contains("nether")) {
            return String.format("OW %.0f %.0f", this.mc.player.getX() * 8.0, this.mc.player.getZ() * 8.0);
         } else {
            return dimension.contains("overworld") ? String.format("Nether %.0f %.0f", this.mc.player.getX() / 8.0, this.mc.player.getZ() / 8.0) : "";
         }
      }
   }

   private Color getModuleAccent(HUD.HUDTheme theme, int index) {
      Color[] colors = new Color[]{theme.accent, theme.primary, theme.secondary};
      return colors[index % colors.length];
   }

   private Color getRainbowColor(int offset) {
      long time = System.currentTimeMillis();
      double speed = this.rainbowSpeed.intValue();
      float saturation = this.rainbowSaturation.intValue();
      float brightness = this.rainbowBrightness.intValue();
      float hue = (float)((time * speed + offset * 100) % 3600.0 / 3600.0);
      return Color.getHSBColor(hue, saturation, brightness);
   }

   private Color interpolateColor(Color c1, Color c2, float ratio) {
      ratio = Math.max(0.0F, Math.min(1.0F, ratio));
      int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
      int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
      int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
      int a = (int)(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * ratio);
      return new Color(r, g, b, a);
   }

   private List<Module> getSortedModules() {
      List<Module> modules = Ykow.INSTANCE.getModuleManager().elementCodec();
      HUD.ModuleListSorting sorting = (HUD.ModuleListSorting)this.moduleSortingMode.getValue();

      return switch (sorting) {
         case LENGTH -> modules.stream().sorted((a, b) -> Integer.compare(TextRenderer.getWidth(b.getName()), TextRenderer.getWidth(a.getName()))).toList();
         case ALPHABETICAL -> modules.stream().sorted(Comparator.comparing(module -> module.getName().toString())).toList();
         case CATEGORY -> modules.stream().sorted(Comparator.comparing(Module::getCategory).thenComparing(module -> module.getName().toString())).toList();
      };
   }

   static enum HUDTheme {
      MODERN_DARK(
         "Modern Dark",
         new Color(20, 20, 25, 160),
         new Color(25, 25, 30, 180),
         new Color(240, 240, 245, 230),
         new Color(180, 180, 190, 200),
         new Color(88, 166, 255),
         new Color(138, 180, 248),
         new Color(168, 130, 255)
      ),
      CYBER(
         "Cyber",
         new Color(10, 15, 25, 160),
         new Color(15, 20, 30, 180),
         new Color(0, 255, 255, 240),
         new Color(180, 180, 200, 200),
         new Color(0, 255, 255),
         new Color(255, 0, 128),
         new Color(138, 43, 226)
      ),
      MINIMAL(
         "Minimal",
         new Color(30, 30, 32, 140),
         new Color(35, 35, 37, 160),
         new Color(255, 255, 255, 240),
         new Color(200, 200, 205, 200),
         new Color(120, 120, 128),
         new Color(140, 140, 148),
         new Color(160, 160, 168)
      ),
      OCEAN(
         "Ocean",
         new Color(15, 25, 35, 160),
         new Color(20, 30, 40, 180),
         new Color(220, 240, 255, 240),
         new Color(160, 180, 200, 200),
         new Color(64, 224, 208),
         new Color(72, 209, 204),
         new Color(32, 178, 170)
      ),
      SUNSET(
         "Sunset",
         new Color(30, 20, 25, 160),
         new Color(35, 25, 30, 180),
         new Color(255, 240, 230, 240),
         new Color(200, 180, 170, 200),
         new Color(255, 107, 107),
         new Color(255, 159, 64),
         new Color(255, 193, 7)
      );

      public final String name;
      public final Color background;
      public final Color backgroundEmphasized;
      public final Color text;
      public final Color subtext;
      public final Color accent;
      public final Color primary;
      public final Color secondary;

      private HUDTheme(String name, Color bg, Color bgEmp, Color text, Color subtext, Color accent, Color primary, Color secondary) {
         this.name = name;
         this.background = bg;
         this.backgroundEmphasized = bgEmp;
         this.text = text;
         this.subtext = subtext;
         this.accent = accent;
         this.primary = primary;
         this.secondary = secondary;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }

   static enum ModuleListSorting {
      LENGTH("Length"),
      ALPHABETICAL("Alphabetical"),
      CATEGORY("Category");

      private final String name;

      private ModuleListSorting(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }
   }
}
