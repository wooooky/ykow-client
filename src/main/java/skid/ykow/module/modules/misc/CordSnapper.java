package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.embed.DiscordWebhook;
import java.util.concurrent.CompletableFuture;

public final class CordSnapper extends Module {
   private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
   private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Webhook"), "");
   private int cooldownCounter = 0;

   public CordSnapper() {
      super(EncryptedString.of("Cord Snapper"), EncryptedString.of("Sends base coordinates to discord webhook"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.activateKey, this.webhookUrl});
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player != null) {
         if (this.cooldownCounter > 0) {
            this.cooldownCounter--;
         } else {
            if (KeyUtils.isKeyPressed(this.activateKey.getValue())) {
               DiscordWebhook embedSender = new DiscordWebhook(this.webhookUrl.value);
               embedSender.setContent("Coordinates: x: " + this.mc.player.getX() + " y: " + this.mc.player.getY() + " z: " + this.mc.player.getZ());
               CompletableFuture.runAsync(() -> {
                  try {
                     embedSender.execute();
                  } catch (Throwable var2x) {
                     var2x.printStackTrace(System.err);
                  }
               });
               this.cooldownCounter = 40;
            }
         }
      }
   }
}
