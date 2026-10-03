package skid.ykow.module.modules.misc;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;

public final class TridentBoost extends Module {
   public static TridentBoost instance;
   private final NumberSetting SpeedMultiplier = new NumberSetting(EncryptedString.of("Boost"), 0.1, 50.0, 2.0, 0.1);
   private final BooleanSetting allowOutOfWater = new BooleanSetting(EncryptedString.of("Out of water"), true);

   public TridentBoost() {
      super(EncryptedString.of("Trident Boost"), EncryptedString.of("Boosts you when using riptide with a trident."), -1, Category.MISC);
      this.addsettings(new Setting[]{this.allowOutOfWater, this.SpeedMultiplier});
      instance = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   public double getMultiplier() {
      return this.isEnabled() ? this.SpeedMultiplier.getIntValue() : 1.0;
   }

   public boolean allowOutOfWater() {
      return this.isEnabled() ? this.allowOutOfWater.getValue() : false;
   }

   public static TridentBoost getInstance() {
      if (instance == null) {
         instance = new TridentBoost();
      }

      return instance;
   }
}
