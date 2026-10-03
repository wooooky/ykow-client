package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.module.modules.render.SwingSpeed;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class LivingEntityMixin {
   @Inject(
      method = {"getHandSwingDuration"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getHandSwingDurationInject(CallbackInfoReturnable<Integer> cir) {
      if (Ykow.INSTANCE != null && Ykow.mc != null) {
         SwingSpeed swingSpeedModule = (SwingSpeed)Ykow.INSTANCE.getModuleManager().getModuleByClass(SwingSpeed.class);
         if (swingSpeedModule != null && swingSpeedModule.isEnabled()) {
            cir.setReturnValue(swingSpeedModule.getSwingSpeed().getIntValue());
         }
      }
   }
}
