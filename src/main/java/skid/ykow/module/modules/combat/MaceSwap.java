package skid.ykow.module.modules.combat;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.AttackEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EnchantmentUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;

public final class MaceSwap extends Module {
   private final BooleanSetting enableWindBurst = new BooleanSetting(EncryptedString.of("Wind Burst"), true);
   private final BooleanSetting enableBreach = new BooleanSetting(EncryptedString.of("Breach"), true);
   private final BooleanSetting onlySword = new BooleanSetting(EncryptedString.of("Only Sword"), false);
   private final BooleanSetting onlyAxe = new BooleanSetting(EncryptedString.of("Only Axe"), false);
   private final BooleanSetting switchBack = new BooleanSetting(EncryptedString.of("Switch Back"), true);
   private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
   private final BooleanSetting debug = new BooleanSetting(EncryptedString.of("Debug"), false);
   private boolean isSwitching;
   private int previousSlot;
   private int currentSwitchDelay;

   public MaceSwap() {
      super(EncryptedString.of("Mace Swap"), EncryptedString.of("Switches to a mace when attacking."), -1, Category.COMBAT);
      this.addsettings(new Setting[]{this.enableWindBurst, this.enableBreach, this.onlySword, this.onlyAxe, this.switchBack, this.switchDelay, this.debug});
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
         if (this.mc.player != null) {
            if (this.isSwitching) {
               if (this.switchBack.getValue()) {
                  this.performSwitchBack();
               } else {
                  this.resetState();
               }
            }
         }
      }
   }

   @EventListener
   public void onAttack(AttackEvent attackEvent) {
      if (this.mc.player != null) {
         if (this.debug.getValue()) {
            System.out.println("[MaceSwap] Attack event triggered");
         }

         if (!this.isValidWeapon()) {
            if (this.debug.getValue()) {
               System.out.println("[MaceSwap] Invalid weapon - not a sword or axe");
            }
         } else {
            if (this.previousSlot == -1) {
               this.previousSlot = this.mc.player.getInventory().selectedSlot;
               if (this.debug.getValue()) {
                  System.out.println("[MaceSwap] Stored previous slot: " + this.previousSlot);
               }
            }

            boolean swapped = false;
            if (this.enableWindBurst.getValue() || this.enableBreach.getValue()) {
               swapped = InventoryUtil.swapStack(itemStack -> {
                  if (itemStack.isEmpty()) {
                     return false;
                  } else {
                     boolean hasWindBurst = false;
                     boolean hasBreach = false;
                     if (this.enableWindBurst.getValue()) {
                        hasWindBurst = EnchantmentUtil.hasEnchantment(itemStack, Enchantments.WIND_BURST);
                     }

                     if (this.enableBreach.getValue()) {
                        hasBreach = EnchantmentUtil.hasEnchantment(itemStack, Enchantments.BREACH);
                     }

                     return hasWindBurst || hasBreach;
                  }
               });
               if (this.debug.getValue()) {
                  System.out.println("[MaceSwap] Tried to swap to enchanted weapon: " + swapped);
               }
            }

            if (!swapped) {
               swapped = InventoryUtil.swapItem(item -> item instanceof SwordItem || item instanceof AxeItem);
               if (this.debug.getValue()) {
                  System.out.println("[MaceSwap] Tried to swap to any weapon: " + swapped);
               }
            }

            if (swapped) {
               this.isSwitching = true;
               if (this.debug.getValue()) {
                  System.out.println("[MaceSwap] Successfully swapped to weapon");
               }
            } else if (this.debug.getValue()) {
               System.out.println("[MaceSwap] Failed to find suitable weapon to swap to");
            }
         }
      }
   }

   private boolean isValidWeapon() {
      Item item = this.mc.player.getMainHandStack().getItem();
      if (this.onlySword.getValue() && this.onlyAxe.getValue()) {
         return item instanceof SwordItem || item instanceof AxeItem;
      } else if (this.onlySword.getValue()) {
         return item instanceof SwordItem;
      } else {
         return this.onlyAxe.getValue() ? item instanceof AxeItem : item instanceof SwordItem || item instanceof AxeItem;
      }
   }

   private void performSwitchBack() {
      if (this.currentSwitchDelay < this.switchDelay.getIntValue()) {
         this.currentSwitchDelay++;
      } else {
         if (this.debug.getValue()) {
            System.out.println("[MaceSwap] Switching back to slot: " + this.previousSlot);
         }

         InventoryUtil.swap(this.previousSlot);
         this.resetState();
      }
   }

   private void resetState() {
      this.previousSlot = -1;
      this.currentSwitchDelay = 0;
      this.isSwitching = false;
      if (this.debug.getValue()) {
         System.out.println("[MaceSwap] Reset state");
      }
   }
}
