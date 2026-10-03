package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.embed.DiscordWebhook;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.world.World;

public class WeatherNotifier extends Module {
   private final BooleanSetting enableWebhook = new BooleanSetting("Enable Webhook", true)
      .setDescription(EncryptedString.of("Send webhook notifications when weather changes"));
   private final StringSetting webhookUrl = new StringSetting("Webhook URL", "").setDescription(EncryptedString.of("Discord webhook URL"));
   private final BooleanSetting notifyRain = new BooleanSetting("Notify Rain", true)
      .setDescription(EncryptedString.of("Send notification when it starts raining"));
   private final BooleanSetting notifyThunder = new BooleanSetting("Notify Thunder", true)
      .setDescription(EncryptedString.of("Send notification when there's thunder"));
   private final BooleanSetting notifyClear = new BooleanSetting("Notify Clear", false)
      .setDescription(EncryptedString.of("Send notification when weather clears"));
   private final BooleanSetting debugMode = new BooleanSetting("Debug Mode", false).setDescription(EncryptedString.of("Show debug information in chat"));
   private final BooleanSetting testWebhook = new BooleanSetting("Test Webhook", false).setDescription(EncryptedString.of("Send a test webhook message"));
   private boolean wasRaining = false;
   private boolean wasThundering = false;
   private boolean wasClear = true;
   private int tickCounter = 0;
   private boolean testSent = false;

   public WeatherNotifier() {
      super(EncryptedString.of("Weather Notifier"), EncryptedString.of("Sends webhook notifications when weather changes"), -1, Category.MISC);
      this.addsettings(
         new Setting[]{this.enableWebhook, this.webhookUrl, this.notifyRain, this.notifyThunder, this.notifyClear, this.debugMode, this.testWebhook}
      );
   }

   @Override
   public void onEnable() {
      super.onEnable();
      System.out.println("[WeatherNotifier] Module enabled!");
      if (this.mc.world != null) {
         this.wasRaining = this.mc.world.isRaining();
         this.wasThundering = this.mc.world.isThundering();
         this.wasClear = !this.mc.world.isRaining() && !this.mc.world.isThundering();
         System.out
            .println("[WeatherNotifier] Initial weather state - Rain: " + this.wasRaining + ", Thunder: " + this.wasThundering + ", Clear: " + this.wasClear);
         if (this.enableWebhook.getValue() && !this.webhookUrl.getValue().trim().isEmpty()) {
            this.sendSimpleWebhook("\ud83d\udd27 Weather Notifier Test", "Module has been enabled and is ready to detect weather changes!");
         }
      } else {
         System.out.println("[WeatherNotifier] World is null, cannot initialize weather state");
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      System.out.println("[WeatherNotifier] Module disabled!");
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.world != null && this.mc.player != null) {
         this.tickCounter++;
         if (this.testWebhook.getValue() && !this.testSent) {
            this.testSent = true;
            System.out.println("[WeatherNotifier] Sending test webhook...");
            this.sendSimpleWebhook("\ud83e\uddea Manual Test", "This is a manual test webhook from Weather Notifier!");
         } else if (!this.testWebhook.getValue()) {
            this.testSent = false;
         }

         World world = this.mc.world;
         boolean isRaining = world.isRaining();
         boolean isThundering = world.isThundering();
         boolean isClear = !isRaining && !isThundering;
         if (this.debugMode.getValue() && this.tickCounter % 100 == 0) {
            System.out.println("[WeatherNotifier] Debug - Rain: " + isRaining + ", Thunder: " + isThundering + ", Clear: " + isClear);
            System.out
               .println("[WeatherNotifier] Debug - Was Rain: " + this.wasRaining + ", Was Thunder: " + this.wasThundering + ", Was Clear: " + this.wasClear);
            System.out
               .println(
                  "[WeatherNotifier] Debug - Webhook enabled: " + this.enableWebhook.getValue() + ", URL set: " + !this.webhookUrl.getValue().trim().isEmpty()
               );
         }

         if (this.enableWebhook.getValue() && !this.webhookUrl.getValue().trim().isEmpty()) {
            if (this.notifyRain.getValue() && isRaining && !this.wasRaining) {
               System.out.println("[WeatherNotifier] Rain detected! Sending notification...");
               this.sendSimpleWebhook("\ud83c\udf27️ Rain Started", "It's starting to rain!");
            }

            if (this.notifyThunder.getValue() && isThundering && !this.wasThundering) {
               System.out.println("[WeatherNotifier] Thunder detected! Sending notification...");
               this.sendSimpleWebhook("⛈️ Thunderstorm Started", "A thunderstorm has begun!");
            }

            if (this.notifyClear.getValue() && isClear && !this.wasClear) {
               System.out.println("[WeatherNotifier] Clear weather detected! Sending notification...");
               this.sendSimpleWebhook("☀️ Weather Cleared", "The weather has cleared up!");
            }
         } else if (this.enableWebhook.getValue() && this.webhookUrl.getValue().trim().isEmpty() && this.tickCounter % 200 == 0) {
            System.out.println("[WeatherNotifier] Warning: Webhook is enabled but no URL is set!");
         }

         this.wasRaining = isRaining;
         this.wasThundering = isThundering;
         this.wasClear = isClear;
      }
   }

   private void sendSimpleWebhook(String title, String description) {
      try {
         String url = this.webhookUrl.getValue().trim();
         if (url.isEmpty()) {
            System.out.println("[WeatherNotifier] Webhook URL not configured!");
            return;
         }

         System.out.println("[WeatherNotifier] Sending simple webhook to: " + url);
         String jsonPayload = String.format(
            "{\"username\":\"Weather Notifier\",\"avatar_url\":\"https://i.imgur.com/OL2y1cr.png\",\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":3447003,\"footer\":{\"text\":\"Ykow Weather Notifier\"},\"fields\":[{\"name\":\"Time\",\"value\":\"%s\",\"inline\":true},{\"name\":\"Location\",\"value\":\"X: %.1f, Y: %.1f, Z: %.1f\",\"inline\":true}]}]}",
            title,
            description,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            this.mc.player.getX(),
            this.mc.player.getY(),
            this.mc.player.getZ()
         );
         URL webhookUrl = new URL(url);
         HttpURLConnection connection = (HttpURLConnection)webhookUrl.openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("User-Agent", "WeatherNotifier/1.0");
         connection.setDoOutput(true);

         try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
         }

         int responseCode = connection.getResponseCode();
         System.out.println("[WeatherNotifier] Webhook response code: " + responseCode);
         if (responseCode == 204) {
            System.out.println("[WeatherNotifier] Webhook sent successfully!");
         } else {
            System.out.println("[WeatherNotifier] Webhook failed with response code: " + responseCode);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
               StringBuilder response = new StringBuilder();

               String line;
               while ((line = br.readLine()) != null) {
                  response.append(line);
               }

               System.out.println("[WeatherNotifier] Error response: " + response.toString());
            }
         }

         connection.disconnect();
      } catch (Exception var15) {
         System.err.println("[WeatherNotifier] Failed to send webhook: " + var15.getMessage());
         var15.printStackTrace();
      }
   }

   private void sendWeatherNotification(String title, String description, Color color) {
      try {
         String url = this.webhookUrl.getValue().trim();
         if (url.isEmpty()) {
            System.out.println("[WeatherNotifier] Webhook URL not configured!");
            return;
         }

         System.out.println("[WeatherNotifier] Sending webhook to: " + url);
         DiscordWebhook webhook = new DiscordWebhook(url);
         webhook.setUsername("Weather Notifier");
         webhook.setAvatarUrl("https://i.imgur.com/OL2y1cr.png");
         DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
         embed.setTitle(title);
         embed.setDescription(description);
         embed.setColor(color);
         embed.setFooter("Ykow Weather Notifier", "https://i.imgur.com/OL2y1cr.png");
         embed.addField("Time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), true);
         if (this.mc.player != null) {
            embed.addField("Location", String.format("X: %.1f, Y: %.1f, Z: %.1f", this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ()), true);
         }

         if (this.mc.getCurrentServerEntry() != null) {
            embed.addField("Server", this.mc.getCurrentServerEntry().address, true);
         }

         webhook.addEmbed(embed);
         webhook.execute();
         System.out.println("[WeatherNotifier] Weather notification sent successfully: " + title);
      } catch (Throwable var7) {
         System.err.println("[WeatherNotifier] Failed to send webhook: " + var7.getMessage());
         var7.printStackTrace();
      }
   }
}
