package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PreItemUseEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;

public final class ElytraGlide extends Module {
   private boolean shouldStartFlying;
   private int jumpTimer;
   private int originalSlot = -1;
   private boolean hasRightClicked;
   private boolean hasJumped;
   private boolean shouldStartElytraFlight;

   public ElytraGlide() {
      super(
         EncryptedString.of("Elytra Glide"),
         EncryptedString.of("Automatically starts flying when right-clicking firework rocket while standing still"),
         -1,
         Category.MISC
      );
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      if (this.mc.player != null) {
         KeyBinding.setKeyPressed(this.mc.options.jumpKey.getDefaultKey(), false);
      }

      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.currentScreen == null) {
         if (this.mc.player != null) {
            if (!this.mc.player.getInventory().getArmorStack(EquipmentSlot.CHEST.getEntitySlotId()).isOf(Items.ELYTRA)) {
               this.resetState();
            } else {
               if (this.jumpTimer > 0) {
                  this.jumpTimer--;
                  if (this.jumpTimer == 0) {
                     KeyBinding.setKeyPressed(this.mc.options.jumpKey.getDefaultKey(), false);
                  }
               }

               if (this.mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) && this.mc.options.useKey.isPressed() && !this.hasRightClicked) {
                  this.hasRightClicked = true;
                  if (this.mc.player.isOnGround() && !this.mc.player.isFallFlying()) {
                     this.shouldStartFlying = true;
                  }
               }

               if (!this.mc.options.useKey.isPressed()) {
                  this.hasRightClicked = false;
               }

               if (this.shouldStartFlying && this.mc.player.isOnGround()) {
                  KeyBinding.setKeyPressed(this.mc.options.jumpKey.getDefaultKey(), true);
                  this.jumpTimer = 3;
                  this.hasJumped = true;
                  this.shouldStartFlying = false;
                  this.shouldStartElytraFlight = true;
               }

               if (this.shouldStartElytraFlight && this.hasJumped && !this.mc.player.isOnGround() && !this.mc.player.isFallFlying()) {
                  KeyBinding.setKeyPressed(this.mc.options.jumpKey.getDefaultKey(), true);
                  this.jumpTimer = 2;
                  this.shouldStartElytraFlight = false;
                  this.hasJumped = false;
               }

               if (this.mc.player.isFallFlying()) {
                  this.resetState();
               }
            }
         }
      }
   }

   @EventListener
   public void onPreItemUse(PreItemUseEvent event) {
      if (this.mc.player != null) {
         if (this.mc.player.getInventory().getArmorStack(EquipmentSlot.CHEST.getEntitySlotId()).isOf(Items.ELYTRA)
            && this.mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {
            if (this.originalSlot == -1) {
               this.originalSlot = this.mc.player.getInventory().selectedSlot;
            }

            if (!this.mc.player.isFallFlying()) {
               this.shouldStartFlying = true;
            }
         }
      }
   }

   private void resetState() {
      this.shouldStartFlying = false;
      this.jumpTimer = 0;
      this.originalSlot = -1;
      this.hasRightClicked = false;
      this.hasJumped = false;
      this.shouldStartElytraFlight = false;
   }
}
