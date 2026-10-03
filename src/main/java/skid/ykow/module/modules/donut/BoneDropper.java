package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public final class BoneDropper extends Module {
   private final ModeSetting<BoneDropper.Mode> dropMode = new ModeSetting<>(EncryptedString.of("Mode"), BoneDropper.Mode.SPAWNER, BoneDropper.Mode.class);
   private final NumberSetting dropDelay = new NumberSetting(EncryptedString.of("Drop Delay"), 0.0, 120.0, 30.0, 1.0)
      .getValue(EncryptedString.of("How often it should start dropping bones in minutes"));
   private final NumberSetting pageSwitchDelay = new NumberSetting(EncryptedString.of("Page Switch Delay"), 0.0, 720.0, 4.0, 1.0)
      .getValue(EncryptedString.of("How often it should switch pages in seconds"));
   private int delayCounter;
   private boolean isPageSwitching;
   private BoneDropper.SpawnerState spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
   private int currentBoneSlot = 0;
   private int limePaneClickCount = 0;
   private int spamClickCount = 0;

   public BoneDropper() {
      super(EncryptedString.of("Bone Dropper"), EncryptedString.of("Automatically drops bones from spawner"), -1, Category.DONUT);
      this.addsettings(new Setting[]{this.dropMode, this.dropDelay, this.pageSwitchDelay});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.delayCounter = 20;
      this.isPageSwitching = false;
      this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
      this.currentBoneSlot = 0;
      this.limePaneClickCount = 0;
      this.spamClickCount = 0;
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
         if (this.dropMode.isMode(BoneDropper.Mode.SPAWNER)) {
            this.handleSpawnerMode();
         } else {
            this.handleOrderMode();
         }
      }
   }

   private void handleSpawnerMode() {
      switch (this.spawnerState) {
         case OPENING_INVENTORY:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               KeyBinding.onKeyPressed(Type.MOUSE.createFromCode(1));
               this.delayCounter = 20;
            } else {
               this.spawnerState = BoneDropper.SpawnerState.CHECKING_SLOTS;
               this.delayCounter = 5;
            }
            break;
         case CHECKING_SLOTS:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            boolean allBones = true;

            for (int i = 0; i < 45; i++) {
               if (!this.mc.player.currentScreenHandler.getSlot(i).getStack().isOf(Items.BONE)) {
                  allBones = false;
                  break;
               }
            }

            if (allBones) {
               this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 50, 0, SlotActionType.PICKUP, this.mc.player);
               this.spawnerState = BoneDropper.SpawnerState.CHECKING_ARROW;
               this.delayCounter = 10;
            } else {
               this.spawnerState = BoneDropper.SpawnerState.DROPPING_BONES;
               this.currentBoneSlot = 0;
               this.delayCounter = 5;
            }
            break;
         case CHECKING_ARROW:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            if (this.mc.player.currentScreenHandler.getSlot(53).getStack().isOf(Items.ARROW)) {
               this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 53, 0, SlotActionType.PICKUP, this.mc.player);
               this.spawnerState = BoneDropper.SpawnerState.CHECKING_SLOTS;
               this.delayCounter = 10;
            } else {
               this.spawnerState = BoneDropper.SpawnerState.CHECKING_SLOTS;
               this.delayCounter = 5;
            }
            break;
         case DROPPING_BONES:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            boolean foundBone = false;

            for (int ix = this.currentBoneSlot; ix < 45; ix++) {
               if (this.mc.player.currentScreenHandler.getSlot(ix).getStack().isOf(Items.BONE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ix, 1, SlotActionType.THROW, this.mc.player);
                  this.currentBoneSlot = ix + 1;
                  this.delayCounter = 3;
                  foundBone = true;
                  break;
               }
            }

            if (!foundBone) {
               this.spawnerState = BoneDropper.SpawnerState.CLICKING_SLOT_49_FIRST;
               this.currentBoneSlot = 0;
               this.delayCounter = 10;
            }
            break;
         case CLICKING_SLOT_49_FIRST:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 48, 0, SlotActionType.PICKUP, this.mc.player);
            this.spawnerState = BoneDropper.SpawnerState.CLICKING_LIME_PANE;
            this.limePaneClickCount = 0;
            this.delayCounter = 10;
            break;
         case CLICKING_LIME_PANE:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            for (int ixxxxx = 0; ixxxxx < this.mc.player.currentScreenHandler.slots.size(); ixxxxx++) {
               if (this.mc.player.currentScreenHandler.getSlot(ixxxxx).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxxxx, 0, SlotActionType.PICKUP, this.mc.player);
                  this.limePaneClickCount++;
                  this.delayCounter = 10;
                  if (this.limePaneClickCount >= 2) {
                     this.spawnerState = BoneDropper.SpawnerState.CLICKING_BLOCK_AGAIN;
                     this.limePaneClickCount = 0;
                  }

                  return;
               }
            }

            this.spawnerState = BoneDropper.SpawnerState.CLICKING_BLOCK_AGAIN;
            this.delayCounter = 10;
            break;
         case CLICKING_BLOCK_AGAIN:
            this.mc.player.closeHandledScreen();
            this.spawnerState = BoneDropper.SpawnerState.SPAMMING_SLOT_49;
            this.spamClickCount = 0;
            this.delayCounter = 20;
            break;
         case SPAMMING_SLOT_49:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               KeyBinding.onKeyPressed(Type.MOUSE.createFromCode(1));
               this.delayCounter = 10;
            } else {
               boolean foundLimePane = false;

               for (int ixx = 0; ixx < this.mc.player.currentScreenHandler.slots.size(); ixx++) {
                  if (this.mc.player.currentScreenHandler.getSlot(ixx).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                     foundLimePane = true;
                     break;
                  }
               }

               if (foundLimePane) {
                  this.spawnerState = BoneDropper.SpawnerState.CLOSING_INVENTORY;
                  this.spamClickCount = 0;
                  this.delayCounter = 10;
               } else {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 48, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 3;
               }
            }
            break;
         case CLOSING_INVENTORY:
            this.mc.player.closeHandledScreen();
            this.spawnerState = BoneDropper.SpawnerState.CLICKING_BLOCK_FINAL;
            this.delayCounter = 20;
            break;
         case CLICKING_BLOCK_FINAL:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               KeyBinding.onKeyPressed(Type.MOUSE.createFromCode(1));
               this.delayCounter = 20;
            } else {
               this.spawnerState = BoneDropper.SpawnerState.DROPPING_ALL_BONES;
               this.currentBoneSlot = 0;
               this.delayCounter = 5;
            }
            break;
         case DROPPING_ALL_BONES:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            boolean foundItem = false;

            for (int ixxx = this.currentBoneSlot; ixxx < 45; ixxx++) {
               if (this.mc.player.currentScreenHandler.getSlot(ixxx).getStack().isOf(Items.BONE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxx, 1, SlotActionType.THROW, this.mc.player);
                  this.currentBoneSlot = ixxx + 1;
                  this.delayCounter = 3;
                  foundItem = true;
                  break;
               }
            }

            if (!foundItem) {
               this.spawnerState = BoneDropper.SpawnerState.CLICKING_SLOT_49_SECOND;
               this.currentBoneSlot = 0;
               this.delayCounter = 10;
            }
            break;
         case CLICKING_SLOT_49_SECOND:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 48, 0, SlotActionType.PICKUP, this.mc.player);
            this.spawnerState = BoneDropper.SpawnerState.CLICKING_LIME_PANE_SECOND;
            this.limePaneClickCount = 0;
            this.delayCounter = 10;
            break;
         case CLICKING_LIME_PANE_SECOND:
            if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
               this.spawnerState = BoneDropper.SpawnerState.OPENING_INVENTORY;
               this.delayCounter = 20;
               return;
            }

            for (int ixxxx = 0; ixxxx < this.mc.player.currentScreenHandler.slots.size(); ixxxx++) {
               if (this.mc.player.currentScreenHandler.getSlot(ixxxx).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, ixxxx, 0, SlotActionType.PICKUP, this.mc.player);
                  this.limePaneClickCount++;
                  this.delayCounter = 10;
                  if (this.limePaneClickCount >= 2) {
                     this.spawnerState = BoneDropper.SpawnerState.CHECKING_SLOTS;
                     this.limePaneClickCount = 0;
                     this.delayCounter = 20;
                  }

                  return;
               }
            }

            this.spawnerState = BoneDropper.SpawnerState.CHECKING_SLOTS;
            this.delayCounter = 20;
      }
   }

   private void handleOrderMode() {
      ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
      if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
         this.mc.getNetworkHandler().sendChatCommand("order");
         this.delayCounter = 20;
      } else {
         if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 6) {
            if (currentScreenHandler.getSlot(49).getStack().isOf(Items.MAP)) {
               this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 51, 0, SlotActionType.PICKUP, this.mc.player);
               this.delayCounter = 20;
               return;
            }

            for (int slotIndex = 0; slotIndex < 45; slotIndex++) {
               if (currentScreenHandler.getSlot(slotIndex).getStack().isOf(Items.BONE)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slotIndex, 1, SlotActionType.THROW, this.mc.player);
                  this.delayCounter = this.dropDelay.getIntValue();
                  return;
               }
            }

            int targetSlot;
            if (this.isPageSwitching) {
               targetSlot = 45;
            } else {
               targetSlot = 53;
            }

            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, targetSlot, 0, SlotActionType.PICKUP, this.mc.player);
            this.isPageSwitching = !this.isPageSwitching;
            this.delayCounter = this.pageSwitchDelay.getIntValue();
         } else if (((GenericContainerScreenHandler)currentScreenHandler).getRows() == 3) {
            if (this.mc.currentScreen == null) {
               return;
            }

            if (this.mc.currentScreen.getTitle().getString().contains("Your Orders")) {
               for (int slotIndexx = 0; slotIndexx < 26; slotIndexx++) {
                  if (currentScreenHandler.getSlot(slotIndexx).getStack().isOf(Items.BONE)) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, slotIndexx, 0, SlotActionType.PICKUP, this.mc.player);
                     this.delayCounter = 20;
                     return;
                  }
               }

               this.delayCounter = 200;
               return;
            }

            if (this.mc.currentScreen.getTitle().getString().contains("Edit Order")) {
               if (currentScreenHandler.getSlot(13).getStack().isOf(Items.CHEST)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 20;
                  return;
               }

               if (currentScreenHandler.getSlot(15).getStack().isOf(Items.CHEST)) {
                  this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 15, 0, SlotActionType.PICKUP, this.mc.player);
                  this.delayCounter = 20;
                  return;
               }
            }

            this.delayCounter = 200;
         }
      }
   }

   static enum Mode {
      SPAWNER("Spawner", 0),
      ORDER("Order", 1);

      private Mode(final String name, final int ordinal) {
      }
   }

   static enum SpawnerState {
      OPENING_INVENTORY,
      CHECKING_SLOTS,
      CHECKING_ARROW,
      DROPPING_BONES,
      CLICKING_SLOT_49_FIRST,
      CLICKING_LIME_PANE,
      CLICKING_BLOCK_AGAIN,
      SPAMMING_SLOT_49,
      CLOSING_INVENTORY,
      CLICKING_BLOCK_FINAL,
      DROPPING_ALL_BONES,
      CLICKING_SLOT_49_SECOND,
      CLICKING_LIME_PANE_SECOND;
   }
}
