package skid.ykow.module.modules.misc;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;

public final class PearlBoost extends Module {
   public static PearlBoost instance;
   private final NumberSetting SpeedMultiplier = new NumberSetting(EncryptedString.of("Boost"), 0.1, 50.0, 2.0, 0.1);

   public PearlBoost() {
      super(EncryptedString.of("Pearl Boost"), EncryptedString.of("Boosts your pearl so it goes further"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.SpeedMultiplier});
      instance = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   public double getMultiplier() {
      return this.isEnabled() ? this.SpeedMultiplier.getIntValue() : 1.0;
   }

   public static PearlBoost getInstance() {
      if (instance == null) {
         instance = new PearlBoost();
      }

      return instance;
   }
}
