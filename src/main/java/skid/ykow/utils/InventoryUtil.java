package skid.ykow.utils;

import skid.ykow.Ykow;
import skid.ykow.mixin.ClientPlayerInteractionManagerAccessor;
import java.util.function.Predicate;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;

public final class InventoryUtil {
   public static void swap(int selectedSlot) {
      if (selectedSlot >= 0 && selectedSlot <= 8) {
         Ykow.mc.player.getInventory().selectedSlot = selectedSlot;
         ((ClientPlayerInteractionManagerAccessor)Ykow.mc.interactionManager).syncSlot();
      }
   }

   public static boolean swapStack(Predicate<ItemStack> predicate) {
      PlayerInventory playerInventory = Ykow.mc.player.getInventory();

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         if (predicate.test(playerInventory.getStack(slotIndex))) {
            playerInventory.selectedSlot = slotIndex;
            ((ClientPlayerInteractionManagerAccessor)Ykow.mc.interactionManager).syncSlot();
            return true;
         }
      }

      return false;
   }

   public static boolean swapItem(Predicate<Item> predicate) {
      PlayerInventory playerInventory = Ykow.mc.player.getInventory();

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         if (predicate.test(playerInventory.getStack(slotIndex).getItem())) {
            playerInventory.selectedSlot = slotIndex;
            ((ClientPlayerInteractionManagerAccessor)Ykow.mc.interactionManager).syncSlot();
            return true;
         }
      }

      return false;
   }

   public static boolean swap(Item targetItem) {
      return swapItem(item -> item == targetItem);
   }

   public static int getSlot(Item targetItem) {
      ScreenHandler currentScreenHandler = Ykow.mc.player.currentScreenHandler;
      if (Ykow.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
         int itemCount = 0;

         for (int slotIndex = 0; slotIndex < ((GenericContainerScreenHandler)Ykow.mc.player.currentScreenHandler).getRows() * 9; slotIndex++) {
            if (currentScreenHandler.getSlot(slotIndex).getStack().getItem().equals(targetItem)) {
               itemCount++;
            }
         }

         return itemCount;
      } else {
         return 0;
      }
   }

   public static boolean selectItemFromHotbar(Item targetItem) {
      PlayerInventory playerInventory = Ykow.mc.player.getInventory();

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         if (playerInventory.getStack(slotIndex).getItem() == targetItem) {
            playerInventory.selectedSlot = slotIndex;
            ((ClientPlayerInteractionManagerAccessor)Ykow.mc.interactionManager).syncSlot();
            return true;
         }
      }

      return false;
   }
}
