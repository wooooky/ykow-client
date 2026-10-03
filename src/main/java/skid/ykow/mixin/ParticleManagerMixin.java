package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.module.modules.render.NoRender;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ParticleManager.class})
public class ParticleManagerMixin {
   @Inject(
      method = {"addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onAddParticle(
      ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir
   ) {
      if (Ykow.INSTANCE != null) {
         NoRender noRender = (NoRender)Ykow.INSTANCE.getModuleManager().getModuleByClass(NoRender.class);
         if (noRender != null && noRender.isEnabled()) {
            if (!noRender.shouldRenderParticles()) {
               cir.setReturnValue(null);
               return;
            }

            if (!noRender.shouldRenderExplosions() && this.isExplosionParticle(parameters)) {
               cir.setReturnValue(null);
               return;
            }

            if (!noRender.shouldRenderSmoke() && this.isSmokeParticle(parameters)) {
               cir.setReturnValue(null);
               return;
            }
         }
      }
   }

   private boolean isExplosionParticle(ParticleEffect effect) {
      String particleType = effect.getType().toString().toLowerCase();
      return particleType.contains("explosion") || particleType.contains("explode") || particleType.contains("blast") || particleType.contains("boom");
   }

   private boolean isSmokeParticle(ParticleEffect effect) {
      String particleType = effect.getType().toString().toLowerCase();
      return particleType.contains("smoke") || particleType.contains("cloud") || particleType.contains("fume");
   }
}
