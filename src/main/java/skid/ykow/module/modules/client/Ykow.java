package skid.ykow.module.modules.client;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PacketReceiveEvent;
import skid.ykow.gui.ClickGUI;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;

public final class Ykow extends Module {
   public static Ykow instance;
   public static final NumberSetting redColor = new NumberSetting(EncryptedString.of("Red"), 0.0, 255.0, 166.0, 1.0);
   public static final NumberSetting greenColor = new NumberSetting(EncryptedString.of("Green"), 0.0, 255.0, 63.0, 1.0);
   public static final NumberSetting blueColor = new NumberSetting(EncryptedString.of("Blue"), 0.0, 255.0, 255.0, 1.0);
   public static final NumberSetting windowAlpha = new NumberSetting(EncryptedString.of("Window Alpha"), 0.0, 255.0, 93.0, 1.0);
   public static final BooleanSetting enableBreathingEffect = new BooleanSetting(EncryptedString.of("Breathing"), false)
      .setDescription(EncryptedString.of("Color breathing effect (only with rainbow off)"));
   public static final BooleanSetting enableRainbowEffect = new BooleanSetting(EncryptedString.of("Rainbow"), false)
      .setDescription(EncryptedString.of("Enables LGBTQ mode"));
   public static final BooleanSetting renderBackground = new BooleanSetting(EncryptedString.of("Background"), true)
      .setDescription(EncryptedString.of("Renders the background of the Click Gui"));
   public static final BooleanSetting useCustomFont = new BooleanSetting(EncryptedString.of("Custom Font"), true);
   private final BooleanSetting preventClose = new BooleanSetting(EncryptedString.of("Prevent Close"), true)
      .setDescription(EncryptedString.of("For servers with freeze plugins that don't let you open the GUI"));
   public static final NumberSetting cornerRoundness = new NumberSetting(EncryptedString.of("Roundness"), 1.0, 10.0, 8.0, 1.0);
   public static final ModeSetting<Ykow.AnimationMode> animationMode = new ModeSetting<>(
      EncryptedString.of("Animations"), Ykow.AnimationMode.NORMAL, Ykow.AnimationMode.class
   );
   public static final BooleanSetting enableMSAA = new BooleanSetting(EncryptedString.of("MSAA"), true)
      .setDescription(EncryptedString.of("Anti Aliasing | This can impact performance if you're using tracers but gives them a smoother look |"));
   public static final BooleanSetting Gui = new BooleanSetting(EncryptedString.of("Open GUI"), false).setDescription(EncryptedString.of("Open GUI"));
   public static final NumberSetting guiScale = new NumberSetting(EncryptedString.of("GUI Scale"), 0.5, 2.0, 1.0, 0.1)
      .setDescription(EncryptedString.of("Scale factor for the GUI size"));
   public boolean shouldPreventClose;

   public Ykow() {
      super(EncryptedString.of("ykow"), EncryptedString.of("Settings for the client"), 344, Category.CLIENT);
      this.addsettings(
         new Setting[]{
            redColor, greenColor, blueColor, windowAlpha, renderBackground, this.preventClose, cornerRoundness, animationMode, enableMSAA, Gui, guiScale
         }
      );
      instance = this;
   }

   @Override
   public void onEnable() {
      if (!SelfDestruct.hasSelfDestructed && skid.ykow.Ykow.isInWorld()) {
         skid.ykow.Ykow.INSTANCE.screen = this.mc.currentScreen;
         if (skid.ykow.Ykow.INSTANCE.GUI != null) {
            this.mc.setScreenAndRender(skid.ykow.Ykow.INSTANCE.GUI);
         } else if (this.mc.currentScreen instanceof InventoryScreen) {
            this.shouldPreventClose = true;
         }

         if (new Random().nextInt(3) == 1) {
            CompletableFuture.runAsync(() -> {});
         }

         super.onEnable();
      } else {
         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      if (this.mc != null && this.mc.currentScreen instanceof ClickGUI) {
         skid.ykow.Ykow.INSTANCE.GUI.close();
         this.mc.setScreenAndRender(skid.ykow.Ykow.INSTANCE.screen);
         skid.ykow.Ykow.INSTANCE.GUI.onGuiClose();
      } else if (this.mc != null && this.mc.currentScreen instanceof InventoryScreen) {
         this.shouldPreventClose = false;
      }

      super.onDisable();
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
      if (this.shouldPreventClose && packetReceiveEvent.packet instanceof OpenScreenS2CPacket && this.preventClose.getValue()) {
         packetReceiveEvent.cancel();
      }
   }

   public static boolean OpenGui() {
      return Gui.getValue();
   }

   public static enum AnimationMode {
      NORMAL("Normal", 0),
      POSITIVE("Positive", 1),
      OFF("Off", 2);

      private AnimationMode(final String name, final int ordinal) {
      }

      public static Ykow getInstance() {
         if (Ykow.instance == null) {
            Ykow.instance = new Ykow();
         }

         return Ykow.instance;
      }
   }
}
