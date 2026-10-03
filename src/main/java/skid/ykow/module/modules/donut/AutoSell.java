package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.ItemSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public final class AutoSell extends Module {
   private final ModeSetting<AutoSell.Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), AutoSell.Mode.SELL, AutoSell.Mode.class);
   private final ItemSetting sellItem = new ItemSetting(EncryptedString.of("Sell Item"), Items.SEA_PICKLE);
   private final NumberSetting delay = new NumberSetting(EncryptedString.of("delay"), 0.0, 20.0, 2.0, 1.0)
      .getValue(EncryptedString.of("What should be delay in ticks"));
   private int delayCounter;
   private boolean isSlotProcessed;

   public AutoSell() {
      super(EncryptedString.of("Auto Sell"), EncryptedString.of("Automatically sells items"), -1, Category.DONUT);
      this.addsettings(new Setting[]{this.mode, this.sellItem, this.delay});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.delayCounter = 20;
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player != null && this.mc.interactionManager != null) {
         if (this.delayCounter > 0) {
            this.delayCounter--;
         } else {
            AutoSell.Mode currentMode = (AutoSell.Mode)this.mode.getValue();
            System.out.println("AutoSell: Current mode: " + currentMode.name());
            if (currentMode.equals(AutoSell.Mode.SELL)) {
               ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
               if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler
                  && ((GenericContainerScreenHandler)currentScreenHandler).getRows() == 5) {
                  boolean hasItemsToSell = false;
                  Item targetItem = this.sellItem.getItem();
                  System.out.println("AutoSell: Looking for item: " + targetItem.getName().getString());

                  for (int i = 45; i < 54 && i < this.mc.player.currentScreenHandler.getStacks().size(); i++) {
                     Item item = ((ItemStack)this.mc.player.currentScreenHandler.getStacks().get(i)).getItem();
                     if (item != Items.AIR) {
                        System.out.println("AutoSell: Found item at slot " + i + ": " + item.getName().getString());
                        if (item.equals(targetItem)) {
                           System.out.println("AutoSell: Selling item at slot " + i);
                           this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                           this.delayCounter = this.delay.getIntValue();
                           return;
                        }
                     }
                  }

                  System.out.println("AutoSell: No target items found, closing screen");
                  this.mc.player.closeHandledScreen();
                  this.delayCounter = 20;
               } else {
                  if (this.mc.getNetworkHandler() != null) {
                     this.mc.getNetworkHandler().sendChatCommand("sell");
                  }

                  this.delayCounter = 20;
               }
            } else {
               ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
               if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                  if (this.mc.getNetworkHandler() != null) {
                     this.mc.getNetworkHandler().sendChatCommand("order " + this.getOrderCommand());
                  }

                  this.delayCounter = 20;
               } else if (((GenericContainerScreenHandler)currentScreenHandler).getRows() != 6) {
                  if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 4) {
                     int emptySlotCount = InventoryUtil.getSlot(Items.AIR);
                     if (emptySlotCount <= 0) {
                        this.mc.player.closeHandledScreen();
                        this.delayCounter = 10;
                     } else if (this.isSlotProcessed && emptySlotCount == 36) {
                        this.isSlotProcessed = false;
                        this.mc.player.closeHandledScreen();
                     } else {
                        Item targetItem = this.sellItem.getItem();
                        if (36 < this.mc.player.currentScreenHandler.getStacks().size()) {
                           Item item = ((ItemStack)this.mc.player.currentScreenHandler.getStacks().get(36)).getItem();
                           if (item != Items.AIR && item == targetItem) {
                              this.mc
                                 .interactionManager
                                 .clickSlot(this.mc.player.currentScreenHandler.syncId, 36, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                              this.delayCounter = this.delay.getIntValue();
                              return;
                           }
                        }

                        this.mc.player.closeHandledScreen();
                        this.delayCounter = 20;
                     }
                  } else if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 3) {
                     if (15 < currentScreenHandler.slots.size()) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 15, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                     }

                     this.delayCounter = 10;
                  } else {
                     this.delayCounter = 20;
                  }
               } else {
                  ItemStack stack = null;
                  if (47 < currentScreenHandler.slots.size()) {
                     stack = currentScreenHandler.getSlot(47).getStack();
                     if (stack.isOf(Items.AIR)) {
                        this.delayCounter = 2;
                     } else {
                        if (stack != null) {
                           for (Object next : stack.getTooltip(TooltipContext.create(this.mc.world), this.mc.player, TooltipType.BASIC)) {
                              String string = next.toString();
                              if (string.contains("Most Money Per Item") && (((Text)next).getStyle().toString().contains("white") || string.contains("white"))) {
                                 if (47 < currentScreenHandler.slots.size()) {
                                    this.mc
                                       .interactionManager
                                       .clickSlot(this.mc.player.currentScreenHandler.syncId, 47, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                                 }

                                 this.delayCounter = 5;
                                 return;
                              }
                           }
                        }

                        Item targetItem = this.sellItem.getItem();
                        System.out.println("AutoSell ORDER: Looking for item: " + targetItem.getName().getString());

                        for (int ix = 0; ix < 44 && ix < currentScreenHandler.slots.size(); ix++) {
                           ItemStack currentStack = currentScreenHandler.getSlot(ix).getStack();
                           if (!currentStack.isOf(Items.AIR)) {
                              System.out.println("AutoSell ORDER: Found item at slot " + ix + ": " + currentStack.getItem().getName().getString());
                              if (currentStack.isOf(targetItem)) {
                                 System.out.println("AutoSell ORDER: Ordering item at slot " + ix);
                                 this.mc
                                    .interactionManager
                                    .clickSlot(this.mc.player.currentScreenHandler.syncId, ix, 1, SlotActionType.QUICK_MOVE, this.mc.player);
                                 this.delayCounter = 10;
                                 return;
                              }
                           }
                        }

                        this.delayCounter = 40;
                        this.mc.player.closeHandledScreen();
                     }
                  } else {
                     this.delayCounter = 2;
                  }
               }
            }
         }
      }
   }

   private String getOrderCommand() {
      Item targetItem = this.sellItem.getItem();
      String command;
      if (targetItem.equals(Items.BONE)) {
         command = "Bones";
      } else {
         command = targetItem.getName().getString();
      }

      System.out.println("AutoSell: Sending order command: " + command + " for item: " + targetItem.getName().getString());
      return command;
   }

   public static enum Mode {
      SELL("Sell", 0),
      ORDER("Order", 1);

      private Mode(final String name, final int ordinal) {
      }
   }
}
