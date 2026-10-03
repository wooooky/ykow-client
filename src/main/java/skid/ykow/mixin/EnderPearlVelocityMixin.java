package skid.ykow.mixin;

import skid.ykow.module.modules.misc.PearlBoost;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EnderPearlEntity.class})
public class EnderPearlVelocityMixin {
   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void boostVelocityOnFirstTick(CallbackInfo ci) {
      EnderPearlEntity pearl = (EnderPearlEntity)(Object)this;
      if (pearl.age <= 1) {
         System.out.println("BOOSTING PEARL!");
         PearlBoost module = PearlBoost.getInstance();
         System.out.println("Module enabled: " + module.isEnabled());
         double multiplier = module.getMultiplier();
         System.out.println("Multiplier: " + multiplier);
         Vec3d oldVelocity = pearl.getVelocity();
         System.out.println("Old velocity: " + oldVelocity);
         pearl.setVelocity(oldVelocity.multiply(multiplier));
         Vec3d newVelocity = pearl.getVelocity();
         System.out.println("New velocity: " + newVelocity);
         System.out.println("---");
      }
   }
}
