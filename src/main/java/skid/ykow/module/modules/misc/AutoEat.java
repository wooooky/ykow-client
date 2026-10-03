package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.mixin.MinecraftClientAccessor;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;

public final class AutoEat extends Module {
   private final NumberSetting healthThreshold = new NumberSetting(EncryptedString.of("Health Threshold"), 0.0, 19.0, 17.0, 1.0);
   private final NumberSetting hungerThreshold = new NumberSetting(EncryptedString.of("Hunger Threshold"), 0.0, 19.0, 19.0, 1.0);
   public boolean isEa;
   private int selectedFoodSlot;
   private int previousSelectedSlot;

   public AutoEat() {
      super(
         EncryptedString.of("Auto Eat"),
         EncryptedString.of(" It detects whenever the hungerbar/health falls a certain threshold, selects food in your hotbar, and starts eating."),
         -1,
         Category.MISC
      );
      this.addsettings(new Setting[]{this.healthThreshold, this.hungerThreshold});
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
   public void onTick(TickEvent tickEvent) {
      if (this.isEa) {
         if (this.shouldEat()) {
            if (this.mc.player.getInventory().getStack(this.selectedFoodSlot).get(DataComponentTypes.FOOD) != null) {
               int bestSlot = this.findBestFoodSlot();
               if (bestSlot == -1) {
                  this.stopEating();
                  return;
               }

               this.selectSlot(bestSlot);
            }

            this.startEating();
         } else {
            this.stopEating();
         }
      } else if (this.shouldEat()) {
         this.selectedFoodSlot = this.findBestFoodSlot();
         if (this.selectedFoodSlot != -1) {
            this.saveCurrentSlot();
         }
      }
   }

   public boolean shouldEat() {
      boolean healthLow = this.mc.player.getHealth() <= this.healthThreshold.getIntValue();
      boolean hungerLow = this.mc.player.getHungerManager().getFoodLevel() <= this.hungerThreshold.getIntValue();
      return this.findBestFoodSlot() != -1 && (healthLow || hungerLow);
   }

   private int findBestFoodSlot() {
      int bestSlot = -1;
      int bestNutrition = -1;

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         Object value = this.mc.player.getInventory().getStack(slotIndex).getItem().getComponents().get(DataComponentTypes.FOOD);
         if (value != null) {
            int nutrition = ((FoodComponent)value).nutrition();
            if (nutrition > bestNutrition) {
               bestSlot = slotIndex;
               bestNutrition = nutrition;
            }
         }
      }

      return bestSlot;
   }

   private void saveCurrentSlot() {
      this.previousSelectedSlot = this.mc.player.getInventory().selectedSlot;
      this.startEating();
   }

   private void startEating() {
      this.selectSlot(this.selectedFoodSlot);
      this.setUseKeyPressed(true);
      if (!this.mc.player.isUsingItem()) {
         ((MinecraftClientAccessor)this.mc).invokeDoItemUse();
      }

      this.isEa = true;
   }

   private void stopEating() {
      this.selectSlot(this.previousSelectedSlot);
      this.setUseKeyPressed(false);
      this.isEa = false;
   }

   private void setUseKeyPressed(boolean pressed) {
      this.mc.options.useKey.setPressed(pressed);
   }

   private void selectSlot(int slotIndex) {
      InventoryUtil.swap(slotIndex);
      this.selectedFoodSlot = slotIndex;
   }
}
