package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.MinMaxSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public final class AutoTPA extends Module {
   private final MinMaxSetting delayRange = new MinMaxSetting(EncryptedString.of("Delay"), 1.0, 80.0, 1.0, 10.0, 30.0);
   private final ModeSetting<AutoTPA.Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), AutoTPA.Mode.TPAHERE, AutoTPA.Mode.class);
   private final StringSetting playerName = new StringSetting(EncryptedString.of("Player"), "DrDonutt");
   private int delayCounter;

   public AutoTPA() {
      super(EncryptedString.of("Auto Tpa"), EncryptedString.of("Module that helps you teleport streamers to you"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.mode, this.delayRange, this.playerName});
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
      if (this.delayCounter > 0) {
         this.delayCounter--;
      } else {
         ClientPlayNetworkHandler networkHandler = this.mc.getNetworkHandler();
         String commandPrefix;
         if (this.mode.getValue().equals(AutoTPA.Mode.TPA)) {
            commandPrefix = "tpa ";
         } else {
            commandPrefix = "tpahere ";
         }

         networkHandler.sendCommand(commandPrefix + this.playerName.getValue());
         this.delayCounter = this.delayRange.getRandomIntInRange();
      }
   }

   static enum Mode {
      TPA("Tpa", 0),
      TPAHERE("Tpahere", 1);

      private Mode(final String name, final int ordinal) {
      }
   }
}
