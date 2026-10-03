package skid.ykow.module.modules.client;

import skid.ykow.Ykow;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;

public final class ConfigDebug extends Module {
   private final BooleanSetting testBoolean = new BooleanSetting(EncryptedString.of("Test Boolean"), false)
      .setDescription(EncryptedString.of("Test boolean setting"));
   private final NumberSetting testNumber = new NumberSetting(EncryptedString.of("Test Number"), 1.0, 100.0, 50.0, 1.0)
      .setDescription(EncryptedString.of("Test number setting"));
   private final StringSetting testString = new StringSetting(EncryptedString.of("Test String"), "Default Value")
      .setDescription(EncryptedString.of("Test string setting"));

   public ConfigDebug() {
      super(EncryptedString.of("Config Debug"), EncryptedString.of("Debug module for testing config saving"), -1, Category.CLIENT);
      this.addsettings(new Setting[]{this.testBoolean, this.testNumber, this.testString});
   }

   @Override
   public void onEnable() {
      System.out.println("[ConfigDebug] Module enabled - testing config saving...");
      this.testBoolean.setValue(true);
      this.testNumber.getValue(75.0);
      this.testString.setValue("Test Value Changed");
      Ykow.INSTANCE.getConfigManager().manualSave();
      System.out.println("[ConfigDebug] Config save test completed. Check console for results.");
      this.toggle();
   }

   @Override
   public void onDisable() {
   }
}
