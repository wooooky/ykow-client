package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import skid.ykow.utils.KeyUtils;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class KeyPearl extends Module {
   private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), -1, false);
   private final NumberSetting throwDelay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0);
   private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
   private final NumberSetting switchBackDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0)
      .getValue(EncryptedString.of("Delay after throwing pearl before switching back"));
   private boolean isActivated;
   private boolean hasThrown;
   private int currentThrowDelay;
   private int previousSlot;
   private int currentSwitchBackDelay;

   public KeyPearl() {
      super(EncryptedString.of("Key Pearl"), EncryptedString.of("Switches to an ender pearl and throws it when you press a bind"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.activateKey, this.throwDelay, this.switchBack, this.switchBackDelay});
   }

   @Override
   public void onEnable() {
      this.resetState();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.currentScreen == null) {
         if (KeyUtils.isKeyPressed(this.activateKey.getValue())) {
            this.isActivated = true;
         }

         if (this.isActivated) {
            if (this.previousSlot == -1) {
               this.previousSlot = this.mc.player.getInventory().selectedSlot;
            }

            InventoryUtil.swap(Items.ENDER_PEARL);
            if (this.currentThrowDelay < this.throwDelay.getIntValue()) {
               this.currentThrowDelay++;
               return;
            }

            if (!this.hasThrown) {
               ActionResult interactItem = this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
               if (interactItem.isAccepted() && interactItem.shouldSwingHand()) {
                  this.mc.player.swingHand(Hand.MAIN_HAND);
               }

               this.hasThrown = true;
            }

            if (this.switchBack.getValue()) {
               this.handleSwitchBack();
            } else {
               this.resetState();
            }
         }
      }
   }

   private void handleSwitchBack() {
      if (this.currentSwitchBackDelay < this.switchBackDelay.getIntValue()) {
         this.currentSwitchBackDelay++;
      } else {
         InventoryUtil.swap(this.previousSlot);
         this.resetState();
      }
   }

   private void resetState() {
      this.previousSlot = -1;
      this.currentThrowDelay = 0;
      this.currentSwitchBackDelay = 0;
      this.isActivated = false;
      this.hasThrown = false;
   }
}
