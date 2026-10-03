package skid.ykow.module.modules.render;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;

public final class SwingSpeed extends Module {
   private final NumberSetting swingSpeed = new NumberSetting(EncryptedString.of("Swing Speed"), 1.0, 20.0, 6.0, 1.0)
      .setDescription(EncryptedString.of("Speed of hand swinging animation"));

   public SwingSpeed() {
      super(EncryptedString.of("Swing Speed"), EncryptedString.of("Modifies the speed of hand swinging animation"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.swingSpeed});
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   public NumberSetting getSwingSpeed() {
      return this.swingSpeed;
   }
}
