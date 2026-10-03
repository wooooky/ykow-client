package skid.ykow.module.modules.misc;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class AutoMine extends Module {
   private final BooleanSetting lockView = new BooleanSetting(EncryptedString.of("Lock View"), true);
   private final NumberSetting pitch = new NumberSetting(EncryptedString.of("Pitch"), -180.0, 180.0, 0.0, 0.1);
   private final NumberSetting yaw = new NumberSetting(EncryptedString.of("Yaw"), -180.0, 180.0, 0.0, 0.1);

   public AutoMine() {
      super(EncryptedString.of("Auto Mine"), EncryptedString.of("Module that allows players to automatically mine"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.lockView, this.pitch, this.yaw});
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
         Module moduleByClass = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEat.class);
         if (!moduleByClass.isEnabled() || !((AutoEat)moduleByClass).shouldEat()) {
            this.processMiningAction(true);
            if (this.lockView.getValue()) {
               float currentYaw = this.mc.player.getYaw();
               float currentPitch = this.mc.player.getPitch();
               float targetYaw = this.yaw.getFloatValue();
               float targetPitch = this.pitch.getFloatValue();
               if (currentYaw != targetYaw || currentPitch != targetPitch) {
                  this.mc.player.setYaw(targetYaw);
                  this.mc.player.setPitch(targetPitch);
               }
            }
         }
      }
   }

   private void processMiningAction(boolean shouldMine) {
      if (!this.mc.player.isUsingItem()) {
         if (shouldMine && this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.crosshairTarget;
            BlockPos blockPos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos();
            if (!this.mc.world.getBlockState(blockPos).isAir()) {
               Direction side = blockHitResult.getSide();
               if (this.mc.interactionManager.updateBlockBreakingProgress(blockPos, side)) {
                  this.mc.particleManager.addBlockBreakingParticles(blockPos, side);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
               }
            }
         } else {
            this.mc.interactionManager.cancelBlockBreaking();
         }
      }
   }
}
