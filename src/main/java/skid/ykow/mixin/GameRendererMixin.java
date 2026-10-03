package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.manager.EventManager;
import skid.ykow.module.modules.misc.Freecam;
import skid.ykow.module.modules.render.MotionBlur;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GameRenderer.class})
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private Camera camera;

   @Shadow
   protected abstract double getFov(Camera var1, float var2, boolean var3);

   @Shadow
   public abstract Matrix4f getBasicProjectionMatrix(double var1);

   @Inject(
      method = {"renderWorld"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
         ordinal = 1
      )}
   )
   private void onWorldRender(RenderTickCounter rtc, CallbackInfo ci) {
      EventManager.elementCodec(
         new Render3DEvent(new MatrixStack(), this.getBasicProjectionMatrix(this.getFov(this.camera, rtc.getTickDelta(true), true)), rtc.getTickDelta(true))
      );
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At("HEAD")}
   )
   private void onRenderWorldStart(RenderTickCounter rtc, CallbackInfo ci) {
      MotionBlur.onRenderStart();
   }

   @Inject(
      method = {"renderWorld"},
      at = {@At("TAIL")}
   )
   private void onRenderWorldEnd(RenderTickCounter rtc, CallbackInfo ci) {
      MotionBlur.onRenderEnd();
   }

   @Inject(
      method = {"shouldRenderBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
      if (Ykow.INSTANCE.getModuleManager().getModuleByClass(Freecam.class).isEnabled()) {
         cir.setReturnValue(false);
      }
   }
}
