package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PostItemUseEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;

public class FastPlace extends Module {
   private final BooleanSetting onlyXP = new BooleanSetting("Only XP", false);
   private final BooleanSetting allowBlocks = new BooleanSetting("Blocks", true);
   private final BooleanSetting allowItems = new BooleanSetting("Items", true);
   private final NumberSetting useDelay = new NumberSetting("Delay", 0.0, 10.0, 0.0, 1.0);

   public FastPlace() {
      super(EncryptedString.of("Fast Place"), EncryptedString.of("Spams use action."), -1, Category.MISC);
      this.addsettings(new Setting[]{this.onlyXP, this.allowBlocks, this.allowItems, this.useDelay});
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
   public void onPostItemUse(PostItemUseEvent postItemUseEvent) {
      ItemStack mainHandStack = this.mc.player.getMainHandStack();
      ItemStack offHandStack = this.mc.player.getOffHandStack();
      Item mainHandItem = mainHandStack.getItem();
      Item offHandItem = this.mc.player.getOffHandStack().getItem();
      if (mainHandStack.isOf(Items.EXPERIENCE_BOTTLE) || offHandStack.isOf(Items.EXPERIENCE_BOTTLE) || !this.onlyXP.getValue()) {
         if (!this.onlyXP.getValue()) {
            if (!(mainHandItem instanceof BlockItem) && !(offHandItem instanceof BlockItem)) {
               if (!this.allowItems.getValue()) {
                  return;
               }
            } else if (!this.allowBlocks.getValue()) {
               return;
            }
         }

         if (mainHandItem.getComponents().get(DataComponentTypes.FOOD) == null) {
            if (offHandItem.getComponents().get(DataComponentTypes.FOOD) == null) {
               if (!mainHandStack.isOf(Items.RESPAWN_ANCHOR)
                  && !mainHandStack.isOf(Items.GLOWSTONE)
                  && !offHandStack.isOf(Items.RESPAWN_ANCHOR)
                  && !offHandStack.isOf(Items.GLOWSTONE)) {
                  if (!(mainHandItem instanceof RangedWeaponItem) && !(offHandItem instanceof RangedWeaponItem)) {
                     postItemUseEvent.cooldown = this.useDelay.getIntValue();
                  }
               }
            }
         }
      }
   }
}
