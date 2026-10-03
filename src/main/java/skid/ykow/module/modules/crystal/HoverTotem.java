package skid.ykow.module.modules.crystal;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.mixin.HandledScreenMixin;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class HoverTotem extends Module {
   private final NumberSetting tickDelay = new NumberSetting(EncryptedString.of("Tick Delay"), 0.0, 20.0, 0.0, 1.0)
      .getValue(EncryptedString.of("Ticks to wait between operations"));
   private final BooleanSetting hotbarTotem = new BooleanSetting(EncryptedString.of("Hotbar Totem"), true)
      .setDescription(EncryptedString.of("Also places a totem in your preferred hotbar slot"));
   private final NumberSetting hotbarSlot = new NumberSetting(EncryptedString.of("Hotbar Slot"), 1.0, 9.0, 1.0, 1.0)
      .getValue(EncryptedString.of("Your preferred hotbar slot for totem (1-9)"));
   private final BooleanSetting autoSwitchToTotem = new BooleanSetting(EncryptedString.of("Auto Switch To Totem"), false)
      .setDescription(EncryptedString.of("Automatically switches to totem slot when inventory is opened"));
   private int remainingDelay;

   public HoverTotem() {
      super(
         EncryptedString.of("Hover Totem"),
         EncryptedString.of("Equips a totem in offhand and optionally hotbar when hovering over one in inventory"),
         -1,
         Category.CRYSTAL
      );
      this.addsettings(new Setting[]{this.tickDelay, this.hotbarTotem, this.hotbarSlot, this.autoSwitchToTotem});
   }

   @Override
   public void onEnable() {
      this.resetDelay();
      super.onEnable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player != null) {
         Screen currentScreen = this.mc.currentScreen;
         if (!(this.mc.currentScreen instanceof InventoryScreen)) {
            this.resetDelay();
         } else {
            Slot focusedSlot = ((HandledScreenMixin)currentScreen).getFocusedSlot();
            if (focusedSlot != null && focusedSlot.getIndex() <= 35) {
               if (this.autoSwitchToTotem.getValue()) {
                  this.mc.player.getInventory().selectedSlot = this.hotbarSlot.getIntValue() - 1;
               }

               if (focusedSlot.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                  if (this.remainingDelay > 0) {
                     this.remainingDelay--;
                  } else {
                     int index = focusedSlot.getIndex();
                     int syncId = ((PlayerScreenHandler)((InventoryScreen)currentScreen).getScreenHandler()).syncId;
                     if (!this.mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                        this.equipOffhandTotem(syncId, index);
                     } else {
                        if (this.hotbarTotem.getValue()) {
                           int n = this.hotbarSlot.getIntValue() - 1;
                           if (!this.mc.player.getInventory().getStack(n).isOf(Items.TOTEM_OF_UNDYING)) {
                              this.equipHotbarTotem(syncId, index, n);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void equipOffhandTotem(int syncId, int slotIndex) {
      this.mc.interactionManager.clickSlot(syncId, slotIndex, 40, SlotActionType.SWAP, this.mc.player);
      this.resetDelay();
   }

   private void equipHotbarTotem(int syncId, int slotIndex, int hotbarSlotIndex) {
      this.mc.interactionManager.clickSlot(syncId, slotIndex, hotbarSlotIndex, SlotActionType.SWAP, this.mc.player);
      this.resetDelay();
   }

   private void resetDelay() {
      this.remainingDelay = this.tickDelay.getIntValue();
   }
}
