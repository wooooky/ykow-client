package skid.ykow.module.modules.misc;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;

public class NameProtect extends Module {
   private final StringSetting fakeName = new StringSetting("Fake Name", "Player");

   public NameProtect() {
      super(EncryptedString.of("Name Protect"), EncryptedString.of("Replaces your name with given one."), -1, Category.MISC);
      this.addsettings(new Setting[]{this.fakeName});
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   public String getFakeName() {
      return this.fakeName.getValue();
   }
}
