package skid.ykow.utils;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Set;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentUtil {
   public static boolean hasEnchantment(ItemStack itemStack, RegistryKey<?> enchantmentKey) {
      if (!itemStack.isEmpty() && enchantmentKey != null) {
         try {
            Object2IntArrayMap<?> enchantmentMap = new Object2IntArrayMap();
            populateEnchantmentMap(itemStack, enchantmentMap);
            return containsEnchantment(enchantmentMap, enchantmentKey);
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean containsEnchantment(Object2IntMap<?> enchantmentMap, RegistryKey<?> enchantmentKey) {
      if (enchantmentMap != null && enchantmentKey != null) {
         ObjectIterator var2 = enchantmentMap.keySet().iterator();

         while (var2.hasNext()) {
            Object enchantment = var2.next();
            if (enchantment instanceof RegistryEntry && ((RegistryEntry)enchantment).matchesKey(enchantmentKey)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static void populateEnchantmentMap(ItemStack itemStack, Object2IntMap enchantmentMap) {
      if (enchantmentMap != null && !itemStack.isEmpty()) {
         enchantmentMap.clear();

         try {
            Set<?> enchantments;
            if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
               enchantments = ((ItemEnchantmentsComponent)itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS)).getEnchantmentEntries();
            } else {
               enchantments = itemStack.getEnchantments().getEnchantmentEntries();
            }

            if (enchantments != null) {
               for (Object enchantmentEntry : enchantments) {
                  if (enchantmentEntry instanceof Entry) {
                     enchantmentMap.put(((Entry)enchantmentEntry).getKey(), ((Entry)enchantmentEntry).getIntValue());
                  }
               }
            }
         } catch (Exception var5) {
         }
      }
   }
}
