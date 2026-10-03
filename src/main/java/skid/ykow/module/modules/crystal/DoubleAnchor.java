package skid.ykow.module.modules.crystal;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.BlockUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import skid.ykow.utils.KeyUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class DoubleAnchor extends Module {
   private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 71, false)
      .setDescription(EncryptedString.of("Key that starts double anchoring"));
   private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
   private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 1.0, 1.0);
   private int delayCounter = 0;
   private int step = 0;
   private boolean isAnchoring = false;

   public DoubleAnchor() {
      super(EncryptedString.of("Double Anchor"), EncryptedString.of("Automatically Places 2 anchors"), -1, Category.CRYSTAL);
      this.addsettings(new Setting[]{this.switchDelay, this.totemSlot, this.activateKey});
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
      if (this.mc.currentScreen == null) {
         if (this.mc.player != null) {
            if (this.hasRequiredItems()) {
               if (this.isAnchoring || this.checkActivationKey()) {
                  HitResult crosshairTarget = this.mc.crosshairTarget;
                  if (!(this.mc.crosshairTarget instanceof BlockHitResult)
                     || BlockUtil.isBlockAtPosition(((BlockHitResult)crosshairTarget).getBlockPos(), Blocks.AIR)) {
                     this.isAnchoring = false;
                     this.resetState();
                  } else if (this.delayCounter < this.switchDelay.getIntValue()) {
                     this.delayCounter++;
                  } else {
                     if (this.step == 0) {
                        InventoryUtil.swap(Items.RESPAWN_ANCHOR);
                     } else if (this.step == 1) {
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                     } else if (this.step == 2) {
                        InventoryUtil.swap(Items.GLOWSTONE);
                     } else if (this.step == 3) {
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                     } else if (this.step == 4) {
                        InventoryUtil.swap(Items.RESPAWN_ANCHOR);
                     } else if (this.step == 5) {
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                     } else if (this.step == 6) {
                        InventoryUtil.swap(Items.GLOWSTONE);
                     } else if (this.step == 7) {
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                     } else if (this.step == 8) {
                        InventoryUtil.swap(this.totemSlot.getIntValue() - 1);
                     } else if (this.step == 9) {
                        BlockUtil.interactWithBlock((BlockHitResult)crosshairTarget, true);
                     } else if (this.step == 10) {
                        this.isAnchoring = false;
                        this.step = 0;
                        this.resetState();
                        return;
                     }

                     this.step++;
                  }
               }
            }
         }
      }
   }

   private boolean hasRequiredItems() {
      boolean hasAnchor = false;
      boolean hasGlowstone = false;

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         ItemStack itemStack = this.mc.player.getInventory().getStack(slotIndex);
         if (itemStack.getItem().equals(Items.RESPAWN_ANCHOR)) {
            hasAnchor = true;
         }

         if (itemStack.getItem().equals(Items.GLOWSTONE)) {
            hasGlowstone = true;
         }
      }

      return hasAnchor && hasGlowstone;
   }

   private boolean checkActivationKey() {
      int keyCode = this.activateKey.getValue();
      if (keyCode != -1 && KeyUtils.isKeyPressed(keyCode)) {
         return this.isAnchoring = true;
      } else {
         this.resetState();
         return false;
      }
   }

   private void resetState() {
      this.delayCounter = 0;
   }

   public boolean isAnchoringActive() {
      return this.isAnchoring;
   }
}
