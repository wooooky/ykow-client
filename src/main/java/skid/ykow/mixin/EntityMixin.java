package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.event.events.TargetMarginEvent;
import skid.ykow.event.events.TargetPoseEvent;
import skid.ykow.manager.EventManager;
import skid.ykow.module.modules.misc.Freecam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityMixin {
   @Inject(
      method = {"getTargetingMargin"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendMovementPackets(CallbackInfoReturnable<Float> cir) {
      EventManager.elementCodec(new TargetMarginEvent(Entity.class.cast(this), cir));
   }

   @Inject(
      method = {"getPose"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetPose(CallbackInfoReturnable<EntityPose> cir) {
      EventManager.elementCodec(new TargetPoseEvent(Entity.class.cast(this), cir));
   }

   @Inject(
      method = {"changeLookDirection"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
      if (Entity.class.cast(this) == Ykow.mc.player) {
         Freecam freecam = (Freecam)Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(Freecam.class);
         if (freecam.isEnabled()) {
            freecam.updateRotation(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
         }
      }
   }
}
