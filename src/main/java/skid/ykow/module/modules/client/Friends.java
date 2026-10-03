package skid.ykow.module.modules.client;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;

public final class Friends extends Module {
   public static Friends instance;
   private final FriendsSetting list = new FriendsSetting(EncryptedString.of("Friends"));

   public Friends() {
      super(EncryptedString.of("Friends"), EncryptedString.of("To add some friends if u have any"), -1, Category.CLIENT);
      this.addsettings(new Setting[]{this.list});
      instance = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   public static Friends getInstance() {
      if (instance == null) {
         instance = new Friends();
      }

      return instance;
   }
}
