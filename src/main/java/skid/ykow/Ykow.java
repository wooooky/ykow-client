package skid.ykow;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.DiscordBuild;
import com.jagrosh.discordipc.entities.RichPresence.Builder;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import skid.ykow.gui.ClickGUI;
import skid.ykow.manager.ConfigManager;
import skid.ykow.manager.EventManager;
import skid.ykow.module.ModuleManager;
import java.io.File;
import java.time.OffsetDateTime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public final class Ykow {
   public ConfigManager configManager;
   public ModuleManager MODULE_MANAGER;
   public EventManager EVENT_BUS;
   public static MinecraftClient mc;
   public String version;
   public static Ykow INSTANCE;
   public boolean shouldPreventClose;
   public ClickGUI GUI;
   public Screen screen;
   public long modified;
   public File jar;
   private static final long DISCORD_APP_ID = 1423261533575581710L;
   private IPCClient client;
   private OffsetDateTime startTimestamp;

   public Ykow() {
      try {
         INSTANCE = this;
         this.version = " b1.3";
         this.screen = null;
         this.EVENT_BUS = new EventManager();
         this.MODULE_MANAGER = new ModuleManager();
         this.GUI = new ClickGUI();
         this.configManager = new ConfigManager();
         this.getConfigManager().loadProfile();
         this.jar = new File(Ykow.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         this.modified = this.jar.lastModified();
         this.shouldPreventClose = false;
         mc = MinecraftClient.getInstance();
         this.initializeDiscordRPC();
         this.sendlogs();
      } catch (Throwable var2) {
         var2.printStackTrace(System.err);
      }
   }

   private void sendlogs() {
   }

   public static boolean isInWorld() {
      return mc != null && mc.player != null && mc.world != null;
   }

   public static ClientPlayerEntity getPlayerSafe() {
      return isInWorld() ? mc.player : null;
   }

   public static ClientWorld getWorldSafe() {
      return isInWorld() ? mc.world : null;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public ModuleManager getModuleManager() {
      return this.MODULE_MANAGER;
   }

   public EventManager getEventBus() {
      return this.EVENT_BUS;
   }

   public void resetModifiedDate() {
      this.jar.setLastModified(this.modified);
   }

   private void initializeDiscordRPC() {
      new Thread(() -> {
         try {
            System.out.println("[Discord RPC] Creating IPC client with ID: 1423261533575581710");
            this.client = new IPCClient(1423261533575581710L);
            this.startTimestamp = OffsetDateTime.now();
            System.out.println("[Discord RPC] Setting up listener...");
            this.client.setListener(new IPCListener() {
               public void onReady(IPCClient client) {
                  System.out.println("[Discord RPC] Connected successfully");

                  try {
                     Ykow.this.updateDiscordPresence("In Menu");
                  } catch (Exception var3x) {
                     System.err.println("[Discord RPC] Failed to set initial presence: " + var3x.getMessage());
                  }
               }

               public void onClose(IPCClient client, String message) {
                  System.out.println("[Discord RPC] Connection closed: " + message);
               }
            });
            System.out.println("[Discord RPC] Attempting to connect...");
            this.client.connect(new DiscordBuild[0]);
            System.out.println("[Discord RPC] Connected successfully!");
         } catch (NoDiscordClientException var2) {
            System.err.println("[Discord RPC] Discord not running - Rich Presence disabled");
            this.client = null;
         } catch (Exception var3) {
            System.err.println("[Discord RPC] Failed to initialize (this is non-fatal)");
            System.err.println("[Discord RPC] Error: " + var3.getClass().getName() + ": " + var3.getMessage());
            this.client = null;
         }
      }, "Discord-RPC-Init").start();
   }

   public void updateDiscordPresence(String details) {
      if (this.client != null) {
         try {
            Builder builder = new Builder();
            builder.setDetails(details)
               .setStartTimestamp(this.startTimestamp)
               .setLargeImage("ykow_logo", "Ykow" + this.version)
               .setSmallImage("minecraft", "Minecraft");
            this.client.sendRichPresence(builder.build());
         } catch (Exception var3) {
            System.err.println("[Discord RPC] Failed to update presence: " + var3.getMessage());
         }
      }
   }

   public void updatePresenceFromGameState() {
      if (isInWorld()) {
         if (mc.isInSingleplayer()) {
            this.updateDiscordPresence("Playing Singleplayer");
         } else if (mc.getCurrentServerEntry() != null) {
            String serverIP = mc.getCurrentServerEntry().address;
            this.updateDiscordPresence("Playing on " + serverIP);
         } else {
            this.updateDiscordPresence("Playing Multiplayer");
         }
      } else {
         this.updateDiscordPresence("In Menu");
      }
   }

   public void shutdownDiscordRPC() {
      try {
         if (this.client != null) {
            this.client.close();
         }

         System.out.println("[Discord RPC] Shutdown successfully");
      } catch (Exception var2) {
         System.err.println("[Discord RPC] Error during shutdown: " + var2.getMessage());
      }
   }
}
