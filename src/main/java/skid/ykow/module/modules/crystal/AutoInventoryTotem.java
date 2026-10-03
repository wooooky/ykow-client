package skid.ykow.module.modules.crystal;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.FakeInvScreen;
import java.util.function.Predicate;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoInventoryTotem extends Module {
   private final NumberSetting delay = new NumberSetting(EncryptedString.of("Delay"), 0.0, 20.0, 0.0, 1.0);
   private final BooleanSetting hotbar = new BooleanSetting(EncryptedString.of("Hotbar"), true)
      .setDescription(EncryptedString.of("Puts a totem in your hotbar as well, if enabled (Setting below will work if this is enabled)"));
   private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0)
      .getValue(EncryptedString.of("Your preferred totem slot"));
   private final BooleanSetting autoSwitch = new BooleanSetting(EncryptedString.of("Auto Switch"), false)
      .setDescription(EncryptedString.of("Switches to totem slot when going inside the inventory"));
   private final BooleanSetting forceTotem = new BooleanSetting(EncryptedString.of("Force Totem"), false)
      .setDescription(EncryptedString.of("Puts the totem in the slot, regardless if its space is taken up by something else"));
   private final BooleanSetting autoOpen = new BooleanSetting(EncryptedString.of("Auto Open"), false)
      .setDescription(EncryptedString.of("Automatically opens and closes the inventory for you"));
   private final NumberSetting stayOpenDuration = new NumberSetting(EncryptedString.of("Stay Open For"), 0.0, 20.0, 0.0, 1.0);
   int delayCounter = -1;
   int stayOpenCounter = -1;

   public AutoInventoryTotem() {
      super(
         EncryptedString.of("Auto Inv Totem"), EncryptedString.of("Automatically equips a totem in your offhand and main hand if empty"), -1, Category.CRYSTAL
      );
      this.addsettings(new Setting[]{this.delay, this.hotbar, this.totemSlot, this.autoSwitch, this.forceTotem, this.autoOpen, this.stayOpenDuration});
   }

   @Override
   public void onEnable() {
      this.delayCounter = -1;
      this.stayOpenCounter = -1;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.shouldOpenInventory() && this.autoOpen.getValue()) {
         this.mc.setScreen(new FakeInvScreen(this.mc.player));
      }

      if (!(this.mc.currentScreen instanceof InventoryScreen) && !(this.mc.currentScreen instanceof FakeInvScreen)) {
         this.delayCounter = -1;
         this.stayOpenCounter = -1;
      } else {
         if (this.delayCounter == -1) {
            this.delayCounter = this.delay.getIntValue();
         }

         if (this.stayOpenCounter == -1) {
            this.stayOpenCounter = this.stayOpenDuration.getIntValue();
         }

         if (this.delayCounter > 0) {
            this.delayCounter--;
         }

         PlayerInventory inventory = this.mc.player.getInventory();
         if (this.autoSwitch.getValue()) {
            inventory.selectedSlot = this.totemSlot.getIntValue() - 1;
         }

         if (this.delayCounter <= 0) {
            if (((ItemStack)inventory.offHand.get(0)).getItem() != Items.TOTEM_OF_UNDYING) {
               int totemSlotIndex = this.findTotemSlot();
               if (totemSlotIndex != -1) {
                  this.mc
                     .interactionManager
                     .clickSlot(
                        ((PlayerScreenHandler)((InventoryScreen)this.mc.currentScreen).getScreenHandler()).syncId,
                        totemSlotIndex,
                        40,
                        SlotActionType.SWAP,
                        this.mc.player
                     );
                  return;
               }
            }

            if (this.hotbar.getValue()) {
               ItemStack mainHandStack = this.mc.player.getMainHandStack();
               if (mainHandStack.isEmpty() || this.forceTotem.getValue() && mainHandStack.getItem() != Items.TOTEM_OF_UNDYING) {
                  int totemSlotIndex = this.findTotemSlot();
                  if (totemSlotIndex != -1) {
                     this.mc
                        .interactionManager
                        .clickSlot(
                           ((PlayerScreenHandler)((InventoryScreen)this.mc.currentScreen).getScreenHandler()).syncId,
                           totemSlotIndex,
                           inventory.selectedSlot,
                           SlotActionType.SWAP,
                           this.mc.player
                        );
                     return;
                  }
               }
            }

            if (this.isTotemEquipped() && this.autoOpen.getValue()) {
               if (this.stayOpenCounter != 0) {
                  this.stayOpenCounter--;
                  return;
               }

               this.mc.currentScreen.close();
               this.stayOpenCounter = this.stayOpenDuration.getIntValue();
            }
         }
      }
   }

   public boolean isTotemEquipped() {
      return this.hotbar.getValue()
         ? this.mc.player.getInventory().getStack(this.totemSlot.getIntValue() - 1).getItem() == Items.TOTEM_OF_UNDYING
            && this.mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
            && this.mc.currentScreen instanceof FakeInvScreen
         : this.mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING && this.mc.currentScreen instanceof FakeInvScreen;
   }

   public boolean shouldOpenInventory() {
      return this.hotbar.getValue()
         ? (
               this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
                  || this.mc.player.getInventory().getStack(this.totemSlot.getIntValue() - 1).getItem() != Items.TOTEM_OF_UNDYING
            )
            && !(this.mc.currentScreen instanceof FakeInvScreen)
            && this.countTotems(item -> item == Items.TOTEM_OF_UNDYING) != 0
         : this.mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING
            && !(this.mc.currentScreen instanceof FakeInvScreen)
            && this.countTotems(item2 -> item2 == Items.TOTEM_OF_UNDYING) != 0;
   }

   private int findTotemSlot() {
      PlayerInventory inventory = this.mc.player.getInventory();

      for (int slotIndex = 0; slotIndex < inventory.main.size(); slotIndex++) {
         if (((ItemStack)inventory.main.get(slotIndex)).getItem() == Items.TOTEM_OF_UNDYING) {
            return slotIndex;
         }
      }

      return -1;
   }

   private int countTotems(Predicate<Item> predicate) {
      int count = 0;
      PlayerInventory inventory = this.mc.player.getInventory();

      for (int slotIndex = 0; slotIndex < inventory.main.size(); slotIndex++) {
         ItemStack stack = (ItemStack)inventory.main.get(slotIndex);
         if (predicate.test(stack.getItem())) {
            count += stack.getCount();
         }
      }

      return count;
   }
}
