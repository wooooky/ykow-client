package skid.ykow.module.modules.client;

import com.sun.jna.Memory;
import skid.ykow.gui.ClickGUI;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import net.minecraft.text.Text;

public final class SelfDestruct extends Module {
   public static boolean isActive = false;
   public static boolean hasSelfDestructed = false;
   private final BooleanSetting replaceMod = new BooleanSetting(EncryptedString.of("Replace Mod"), true)
      .setDescription(EncryptedString.of("Replaces the mod with the specified JAR file"));
   private final BooleanSetting saveLastModified = new BooleanSetting(EncryptedString.of("Save Last Modified"), true)
      .setDescription(EncryptedString.of("Saves the last modified date after self destruct"));
   private final StringSetting replaceUrl = new StringSetting(
      EncryptedString.of("Replace URL"), "https://cdn.modrinth.com/data/8shC1gFX/versions/sXO3idkS/BetterF3-11.0.1-Fabric-1.21.jar"
   );

   public SelfDestruct() {
      super(EncryptedString.of("Self Destruct"), EncryptedString.of("Removes the client from your game |Credits to Argon for deletion|"), -1, Category.CLIENT);
      this.addsettings(new Setting[]{this.replaceMod, this.saveLastModified, this.replaceUrl});
   }

   @Override
   public void onEnable() {
      isActive = true;
      hasSelfDestructed = true;

      try {
         Thread.sleep(100L);
      } catch (InterruptedException var5) {
      }

      skid.ykow.Ykow.INSTANCE.getModuleManager().getModuleByClass(Ykow.class).toggle(false);
      this.toggle(false);
      skid.ykow.Ykow.INSTANCE.getConfigManager().shutdown();
      if (this.mc.currentScreen instanceof ClickGUI) {
         skid.ykow.Ykow.INSTANCE.shouldPreventClose = false;
         this.mc.currentScreen.close();
      }

      if (this.replaceMod.getValue()) {
         this.scheduleModReplacement();
      }

      for (Module module : skid.ykow.Ykow.INSTANCE.getModuleManager().c()) {
         module.toggle(false);
         module.setName(null);
         module.setDescription(null);

         for (Setting setting : module.getSettings()) {
            setting.getDescription(null);
            setting.setDescription(null);
            if (setting instanceof StringSetting) {
               ((StringSetting)setting).setValue(null);
            }
         }

         module.getSettings().clear();
      }

      if (this.saveLastModified.getValue()) {
         skid.ykow.Ykow.INSTANCE.resetModifiedDate();
      }

      Thread memoryCleanupThread = new Thread(() -> {
         Runtime runtime = Runtime.getRuntime();

         for (int i = 0; i <= 10; i++) {
            runtime.gc();

            try {
               Thread.sleep(100 * i);
               Memory.purge();
               Memory.disposeAll();
            } catch (InterruptedException var3) {
            } catch (Exception var4x) {
            }
         }
      }, "MemoryCleanup");
      memoryCleanupThread.setDaemon(true);
      memoryCleanupThread.start();
      if (this.mc.player != null) {
         this.mc.player.sendMessage(Text.literal("§c§l[SelfDestruct] §rClient has been cleared. Game will continue running."));
      }
   }

   private void scheduleModReplacement() {
      Thread replacementThread = new Thread(() -> {
         try {
            String downloadUrl = this.replaceUrl.getValue();
            File currentJar = Utils.getCurrentJarPath();
            if (currentJar == null || !currentJar.exists() || !currentJar.isFile()) {
               return;
            }

            File tempFile = new File(currentJar.getParentFile(), currentJar.getName() + ".tmp");
            if (this.downloadModFile(downloadUrl, tempFile)) {
               Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                  try {
                     Thread.sleep(500L);
                     Files.move(tempFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  } catch (Exception var3x) {
                  }
               }, "ModReplacementHook"));
            }
         } catch (Exception var4) {
         }
      }, "ModReplacementDownload");
      replacementThread.setDaemon(true);
      replacementThread.start();
   }

   private boolean downloadModFile(String downloadUrl, File targetFile) {
      try {
         URL url = new URL(downloadUrl);
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(30000);
         connection.setInstanceFollowRedirects(true);
         int responseCode = connection.getResponseCode();
         if (responseCode != 200) {
            connection.disconnect();
            return false;
         } else {
            try (
               InputStream inputStream = connection.getInputStream();
               FileOutputStream outputStream = new FileOutputStream(targetFile);
            ) {
               byte[] buffer = new byte[8192];

               int bytesRead;
               while ((bytesRead = inputStream.read(buffer)) != -1) {
                  outputStream.write(buffer, 0, bytesRead);
               }

               outputStream.flush();
            }

            connection.disconnect();
            return true;
         }
      } catch (Exception var14) {
         return false;
      }
   }
}
