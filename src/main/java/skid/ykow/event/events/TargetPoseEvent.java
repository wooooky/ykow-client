package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class TargetPoseEvent extends CancellableEvent {
   public Entity entity;
   public CallbackInfoReturnable<EntityPose> cir;

   public TargetPoseEvent(Entity entity, CallbackInfoReturnable<EntityPose> cir) {
      this.entity = entity;
      this.cir = cir;
   }
}
