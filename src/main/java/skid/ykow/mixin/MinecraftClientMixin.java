package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.event.events.AttackEvent;
import skid.ykow.event.events.BlockBreakingEvent;
import skid.ykow.event.events.PostItemUseEvent;
import skid.ykow.event.events.PreItemUseEvent;
import skid.ykow.event.events.ResolutionChangedEvent;
import skid.ykow.event.events.SetScreenEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {
   @Shadow
   @Nullable
   public ClientWorld world;
   @Shadow
   @Final
   private Window window;
   @Shadow
   private int itemUseCooldown;

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      if (this.world != null) {
         EventManager.elementCodec(new TickEvent());
      }
   }

   @Inject(
      method = {"onResolutionChanged"},
      at = {@At("HEAD")}
   )
   private void onResolutionChanged(CallbackInfo ci) {
      EventManager.elementCodec(new ResolutionChangedEvent(this.window));
   }

   @Inject(
      method = {"doItemUse"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onItemUseReturn(CallbackInfo ci) {
      PostItemUseEvent event = new PostItemUseEvent(this.itemUseCooldown);
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

      this.itemUseCooldown = event.cooldown;
   }

   @Inject(
      method = {"doItemUse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onItemUseHead(CallbackInfo ci) {
      PreItemUseEvent event = new PreItemUseEvent(this.itemUseCooldown);
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

      this.itemUseCooldown = event.cooldown;
   }

   @Inject(
      method = {"doAttack"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAttack(CallbackInfoReturnable<Boolean> cir) {
      AttackEvent event = new AttackEvent();
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"handleBlockBreaking"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onBlockBreaking(boolean breaking, CallbackInfo ci) {
      BlockBreakingEvent event = new BlockBreakingEvent();
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"setScreen"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetScreen(Screen screen, CallbackInfo ci) {
      SetScreenEvent event = new SetScreenEvent(screen);
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"stop"},
      at = {@At("HEAD")}
   )
   private void onClose(CallbackInfo callbackInfo) {
      Ykow.INSTANCE.getConfigManager().shutdown();
   }
}
