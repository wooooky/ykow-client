package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.PingUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class ScoreboardModule extends Module {
   private static final String OBJECTIVE_NAME = "ykow_scoreboard";
   private ScoreboardObjective customObjective;
   private ScoreboardObjective originalObjective;
   private boolean wasEnabled = false;
   private final StringSetting Money = new StringSetting(EncryptedString.of("Money"), "67M");
   private final StringSetting Shards = new StringSetting(EncryptedString.of("Shards"), "67");
   private final StringSetting Kills = new StringSetting(EncryptedString.of("Kills"), "67");
   private final StringSetting Deaths = new StringSetting(EncryptedString.of("Deaths"), "67");
   private final StringSetting Keyall = new StringSetting(EncryptedString.of("Key All"), "6m 7s");
   private final StringSetting Playtime = new StringSetting(EncryptedString.of("Playtime"), "6h 7m");
   private final StringSetting Team = new StringSetting(EncryptedString.of("Team"), "6 7 team ✌");
   private final StringSetting Title = new StringSetting(EncryptedString.of("Title"), "Protected by KC");
   private final StringSetting Region = new StringSetting(EncryptedString.of("Region"), "Protected by KC");
   private final Boolean enabled = true;

   public ScoreboardModule() {
      super(EncryptedString.of("Fake Stats"), EncryptedString.of("Custom scoreboard overlay"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.Money, this.Shards, this.Kills, this.Deaths, this.Keyall, this.Playtime, this.Team, this.Title, this.Region});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.wasEnabled = true;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.restoreOriginalScoreboard();
      this.wasEnabled = false;
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.enabled && this.isEnabled()) {
            this.updateCustomScoreboard();
         } else if (this.wasEnabled) {
            this.restoreOriginalScoreboard();
            this.wasEnabled = false;
         }
      }
   }

   private void updateCustomScoreboard() {
      Scoreboard scoreboard = this.mc.world.getScoreboard();
      if (this.originalObjective == null) {
         this.originalObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      }

      if (this.customObjective == null) {
         ScoreboardObjective existing = scoreboard.getNullableObjective("ykow_scoreboard");
         if (existing != null) {
            scoreboard.removeObjective(existing);
         }

         this.customObjective = scoreboard.addObjective(
            "ykow_scoreboard",
            ScoreboardCriterion.DUMMY,
            this.parseGradientText("  Protected by ykow  ", "#007CF9", "#00C6F9"),
            RenderType.INTEGER,
            true,
            null
         );
      }

      scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.customObjective);
      scoreboard.removeObjective(this.customObjective);
      this.customObjective = scoreboard.addObjective(
         "ykow_scoreboard",
         ScoreboardCriterion.DUMMY,
         this.parseGradientText("  Protected by ykow  ", "#007CF9", "#00C6F9"),
         RenderType.INTEGER,
         true,
         null
      );
      scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.customObjective);
      List lines = this.getScoreboardLines();

      for (int i = 0; i < lines.size(); i++) {
         Text lineText = (Text)lines.get(i);
         String invisibleId = "§" + i + "§r";
         ScoreHolder holder = ScoreHolder.fromName(invisibleId);
         ScoreAccess score = scoreboard.getOrCreateScore(holder, this.customObjective);
         score.setScore(lines.size() - i);
         score.setDisplayText(lineText);
      }

      String Formatedtitle = "   " + this.Title.getValue() + "   ";
      this.customObjective.setDisplayName(this.parseGradientText(Formatedtitle, "#007CF9", "#00C6F9"));
   }

   private List<Text> getScoreboardLines() {
      List<Text> lines = new ArrayList<>();
      lines.add(Text.literal(" "));
      lines.add(
         Text.literal("")
            .append(Text.literal("$ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(64512)).withBold(true)))
            .append(Text.literal("Money ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Money.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(64512))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("★ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(10683385))))
            .append(Text.literal("Shards ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Shards.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(10683385))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("\ud83d\udde1 ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16515072))))
            .append(Text.literal("Kills ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Kills.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16515072))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("☠ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16348675))))
            .append(Text.literal("Deaths ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Deaths.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16348675))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("⌛ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(42236))))
            .append(Text.literal("Keyall ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Keyall.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(42236))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("⌚ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16573184))))
            .append(Text.literal("Playtime ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Playtime.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16573184))))
      );
      lines.add(
         Text.literal("")
            .append(Text.literal("\ud83e\ude93 ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(42236))))
            .append(Text.literal("Team ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16777215))))
            .append(Text.literal(this.Team.getValue()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(42236))))
      );
      lines.add(Text.literal(" "));
      lines.add(
         Text.literal("")
            .append(Text.literal(this.Region.getValue() + " (").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(11053224))))
            .append(Text.literal(PingUtil.getPingString().toString()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(42236))))
            .append(Text.literal(")").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(11053224))))
      );
      return lines;
   }

   private void restoreOriginalScoreboard() {
      if (this.mc.world != null) {
         Scoreboard scoreboard = this.mc.world.getScoreboard();
         if (this.customObjective != null) {
            scoreboard.removeObjective(this.customObjective);
            this.customObjective = null;
         }

         if (this.originalObjective != null) {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.originalObjective);
            this.originalObjective = null;
         }
      }
   }

   private Text parseGradientText(String text, String startHex, String endHex) {
      if (text.isEmpty()) {
         return Text.literal("");
      } else {
         Color startColor = Color.decode(startHex);
         Color endColor = Color.decode(endHex);
         MutableText result = Text.literal("");

         for (int i = 0; i < text.length(); i++) {
            float progress = text.length() > 1 ? (float)i / (text.length() - 1) : 0.0F;
            Color interpolated = ColorUtil.keyCodec(startColor, endColor, progress);
            int rgb = interpolated.getRed() << 16 | interpolated.getGreen() << 8 | interpolated.getBlue();
            result.append(Text.literal(String.valueOf(text.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true)));
         }

         return result;
      }
   }

   private String formatColoredText(String text, String hex) {
      Color color = Color.decode(hex);
      StringBuilder result = new StringBuilder();

      for (int i = 0; i < text.length(); i++) {
         result.append(this.formatMinecraftColor(color));
         result.append(text.charAt(i));
      }

      return result.toString();
   }

   private String formatMinecraftColor(Color color) {
      String hex = String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
      StringBuilder result = new StringBuilder("§x");

      for (char c : hex.toCharArray()) {
         result.append("§").append(c);
      }

      return result.toString();
   }
}
