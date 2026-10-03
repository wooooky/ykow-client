package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public final class AutoSpawnerSell extends Module {
   private final NumberSetting dropDelay = new NumberSetting(EncryptedString.of("Drop Delay"), 0.0, 120.0, 30.0, 1.0)
      .getValue(EncryptedString.of("How often it should start dropping bones in minutes"));
   private final NumberSetting pageAmount = new NumberSetting(EncryptedString.of("Page Amount"), 1.0, 10.0, 2.0, 1.0)
      .getValue(EncryptedString.of("How many pages should it drop before selling"));
   private final NumberSetting pageSwitchDelay = new NumberSetting(EncryptedString.of("Page Switch Delay"), 0.0, 720.0, 4.0, 1.0)
      .getValue(EncryptedString.of("How often it should switch pages in seconds"));
   private final NumberSetting delay = new NumberSetting(EncryptedString.of("delay"), 0.0, 20.0, 1.0, 1.0)
      .getValue(EncryptedString.of("What should be delay in ticks"));
   private int delayCounter;
   private int pageCounter;
   private boolean isProcessing;
   private boolean isSelling;
   private boolean isPageSwitching;

   public AutoSpawnerSell() {
      super(EncryptedString.of("Auto Spawner Sell"), EncryptedString.of("Automatically drops bones from spawner and sells them"), -1, Category.DONUT);
      this.addsettings(new Setting[]{this.dropDelay, this.pageAmount, this.pageSwitchDelay, this.delay});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.delayCounter = 20;
      this.isProcessing = false;
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.delayCounter > 0) {
         this.delayCounter--;
      } else if (this.mc.player != null) {
         if (this.pageCounter >= this.pageAmount.getIntValue()) {
            this.isSelling = true;
            this.pageCounter = 0;
            this.delayCounter = 40;
         } else {
            if (!this.isSelling) {
               ScreenHandler fishHook = this.mc.player.currentScreenHandler;
               if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                  KeyBinding.onKeyPressed(Type.MOUSE.createFromCode(1));
                  this.delayCounter = 20;
                  return;
               }

               if (fishHook.getSlot(15).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 15, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                  this.delayCounter = 10;
                  return;
               }

               if (this.mc.player.currentScreenHandler.getSlot(13).getStack().isOf(Items.SKELETON_SKULL)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 11, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 20;
                  return;
               }

               if (!this.mc.player.currentScreenHandler.getSlot(53).getStack().isOf(Items.GOLD_INGOT)) {
                  this.mc.player.closeHandledScreen();
                  this.delayCounter = 20;
                  return;
               }

               if (this.mc.player.currentScreenHandler.getSlot(48).getStack().isOf(Items.ARROW)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 48, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 20;
                  return;
               }

               boolean b2 = true;

               for (int k = 0; k < 45; k++) {
                  if (!this.mc.player.currentScreenHandler.getSlot(k).getStack().isOf(Items.BONE)) {
                     b2 = false;
                     break;
                  }
               }

               if (b2) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 52, 1, SlotActionType.THROW, this.mc.player);
                  this.isProcessing = true;
                  this.delayCounter = this.pageSwitchDelay.getIntValue() * 20;
                  this.pageCounter++;
               } else if (this.isProcessing) {
                  this.isProcessing = false;
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 50, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 20;
               } else {
                  this.isProcessing = false;
                  if (this.pageCounter != 0) {
                     this.pageCounter = 0;
                     this.isSelling = true;
                     this.delayCounter = 40;
                     return;
                  }

                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 45, 1, SlotActionType.THROW, this.mc.player);
                  this.delayCounter = 1200 * this.dropDelay.getIntValue();
               }
            } else {
               ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
               if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                  this.mc.getNetworkHandler().sendChatCommand("order " + this.getOrderCommand());
                  this.delayCounter = 20;
                  return;
               }

               if (((GenericContainerScreenHandler)currentScreenHandler).getRows() != 6) {
                  if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 4) {
                     int emptySlotCount = InventoryUtil.getSlot(Items.AIR);
                     if (emptySlotCount <= 0) {
                        this.mc.player.closeHandledScreen();
                        this.delayCounter = 10;
                        return;
                     }

                     if (this.isPageSwitching && emptySlotCount == 36) {
                        this.isPageSwitching = false;
                        this.mc.player.closeHandledScreen();
                        return;
                     }

                     Item targetItem = this.getInventoryItem();

                     while (true) {
                        int slotIndex = 36;
                        Item item = ((ItemStack)this.mc.player.currentScreenHandler.getStacks().get(36)).getItem();
                        if (item != Items.AIR && item == targetItem) {
                           this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 36, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                           this.delayCounter = this.delay.getIntValue();
                           if (this.delay.getIntValue() != 0) {
                              break;
                           }
                        }
                     }
                  } else if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 3) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 15, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                     this.isPageSwitching = true;
                     this.delayCounter = 10;
                  }
               } else {
                  ItemStack stack = currentScreenHandler.getSlot(47).getStack();
                  if (stack.isOf(Items.AIR)) {
                     this.delayCounter = 2;
                     this.mc.player.closeHandledScreen();
                     return;
                  }

                  for (Object next : stack.getTooltip(TooltipContext.create(this.mc.world), this.mc.player, TooltipType.BASIC)) {
                     String string = next.toString();
                     if (string.contains("Most Money Per Item") && (((Text)next).getStyle().toString().contains("white") || string.contains("white"))) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 47, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                        this.delayCounter = 5;
                        return;
                     }
                  }

                  for (int i = 0; i < 44; i++) {
                     if (currentScreenHandler.getSlot(i).getStack().isOf(this.getInventoryItem())) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                        this.delayCounter = 10;
                        return;
                     }
                  }

                  this.delayCounter = 40;
                  this.mc.player.closeHandledScreen();
               }
            }
         }
      }
   }

   private Item getInventoryItem() {
      for (int i = 0; i < 35; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (!stack.isOf(Items.AIR)) {
            return stack.getItem();
         }
      }

      return Items.AIR;
   }

   private String getOrderCommand() {
      Item j = this.getInventoryItem();
      return j.equals(Items.BONE) ? "Bones" : j.getName().getString();
   }
}
