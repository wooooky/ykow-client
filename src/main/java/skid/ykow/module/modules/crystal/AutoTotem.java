package skid.ykow.module.modules.crystal;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.modules.donut.RtpBaseFinder;
import skid.ykow.module.modules.donut.TunnelBaseFinder;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoTotem extends Module {
   private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 5.0, 1.0, 1.0);
   private int delayCounter;

   public AutoTotem() {
      super(EncryptedString.of("Auto Totem"), EncryptedString.of("Automatically holds totem in your off hand"), -1, Category.CRYSTAL);
      this.addsettings(new Setting[]{this.delay});
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
      if (this.mc.player != null) {
         Module rtpBaseFinder = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(RtpBaseFinder.class);
         if (!rtpBaseFinder.isEnabled() || !((RtpBaseFinder)rtpBaseFinder).isRepairingActive()) {
            Module tunnelBaseFinder = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(TunnelBaseFinder.class);
            if (!tunnelBaseFinder.isEnabled() || !((TunnelBaseFinder)tunnelBaseFinder).isDigging()) {
               if (this.mc.player.getInventory().getStack(40).getItem() == Items.TOTEM_OF_UNDYING) {
                  this.delayCounter = this.delay.getIntValue();
               } else if (this.delayCounter > 0) {
                  this.delayCounter--;
               } else {
                  int slot = this.findItemSlot(Items.TOTEM_OF_UNDYING);
                  if (slot != -1) {
                     this.mc
                        .interactionManager
                        .clickSlot(this.mc.player.currentScreenHandler.syncId, convertSlotIndex(slot), 40, SlotActionType.SWAP, this.mc.player);
                     this.delayCounter = this.delay.getIntValue();
                  }
               }
            }
         }
      }
   }

   public int findItemSlot(Item item) {
      if (this.mc.player == null) {
         return -1;
      } else {
         for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
            if (this.mc.player.getInventory().getStack(slotIndex).isOf(item)) {
               return slotIndex;
            }
         }

         return -1;
      }
   }

   private static int convertSlotIndex(int slotIndex) {
      return slotIndex < 9 ? 36 + slotIndex : slotIndex;
   }
}
