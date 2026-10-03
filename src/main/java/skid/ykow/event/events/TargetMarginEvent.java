package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class TargetMarginEvent extends CancellableEvent {
   public Entity entity;
   public CallbackInfoReturnable<Float> cir;

   public TargetMarginEvent(Entity entity, CallbackInfoReturnable<Float> cir) {
      this.entity = entity;
      this.cir = cir;
   }
}
