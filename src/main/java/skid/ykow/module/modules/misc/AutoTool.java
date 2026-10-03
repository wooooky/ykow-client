package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.AttackBlockEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import java.util.function.Predicate;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.tag.BlockTags;

public final class AutoTool extends Module {
   private final BooleanSetting antiBreak = new BooleanSetting(EncryptedString.of("Anti Break"), true);
   private final NumberSetting antiBreakPercentage = new NumberSetting(EncryptedString.of("Anti Break Percentage"), 1.0, 100.0, 5.0, 1.0);
   private boolean isToolSwapping;
   private int keybindCounter;
   private int selectedToolSlot;

   public AutoTool() {
      super(EncryptedString.of("Auto Tool"), EncryptedString.of("Module that automatically switches to best tool"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.antiBreak, this.antiBreakPercentage});
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
      if (this.keybindCounter <= 0 && this.isToolSwapping && this.selectedToolSlot != -1) {
         InventoryUtil.swap(this.selectedToolSlot);
         this.isToolSwapping = false;
      } else {
         this.keybindCounter--;
      }
   }

   @EventListener
   public void handleAttackBlockEvent(AttackBlockEvent attackBlockEvent) {
      BlockState blockState = this.mc.world.getBlockState(attackBlockEvent.pos);
      ItemStack currentItem = this.mc.player.getMainHandStack();
      double bestEfficiency = -1.0;
      this.selectedToolSlot = -1;

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         double efficiency = calculateToolEfficiency(
            this.mc.player.getInventory().getStack(slotIndex), blockState, itemStack -> !this.isToolBreakingSoon(itemStack)
         );
         if (efficiency >= 0.0 && efficiency > bestEfficiency) {
            this.selectedToolSlot = slotIndex;
            bestEfficiency = efficiency;
         }
      }

      if (this.selectedToolSlot != -1 && bestEfficiency > calculateToolEfficiency(currentItem, blockState, itemStack2 -> !this.isToolBreakingSoon(itemStack2))
         || this.isToolBreakingSoon(currentItem)
         || !isToolItemStack(currentItem)) {
         InventoryUtil.swap(this.selectedToolSlot);
      }

      ItemStack mainHandItem = this.mc.player.getMainHandStack();
      if (this.isToolBreakingSoon(mainHandItem) && isToolItemStack(mainHandItem)) {
         this.mc.options.attackKey.setPressed(false);
         attackBlockEvent.cancel();
      }
   }

   public static double calculateToolEfficiency(ItemStack itemStack, BlockState blockState, Predicate<ItemStack> predicate) {
      if (predicate.test(itemStack) && isToolItemStack(itemStack)) {
         return !itemStack.isSuitableFor(blockState)
               && (
                  !(itemStack.getItem() instanceof SwordItem)
                     || !(blockState.getBlock() instanceof BambooBlock) && !(blockState.getBlock() instanceof BambooShootBlock)
               )
               && (!(itemStack.getItem() instanceof ShearsItem) || !(blockState.getBlock() instanceof LeavesBlock))
               && !blockState.isIn(BlockTags.WOOL)
            ? -1.0
            : 0.0 + itemStack.getMiningSpeedMultiplier(blockState) * 1000.0F;
      } else {
         return -1.0;
      }
   }

   public static boolean isToolItemStack(ItemStack itemStack) {
      return isToolItem(itemStack.getItem());
   }

   public static boolean isToolItem(Item item) {
      return item instanceof ToolItem || item instanceof ShearsItem;
   }

   private boolean isToolBreakingSoon(ItemStack itemStack) {
      return this.antiBreak.getValue()
         && itemStack.getMaxDamage() - itemStack.getDamage() < itemStack.getMaxDamage() * this.antiBreakPercentage.getIntValue() / 100;
   }
}
