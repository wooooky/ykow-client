package skid.ykow.mixin;

import skid.ykow.event.events.KeyEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Keyboard.class})
public class KeyboardMixin {
   @Inject(
      method = {"onKey"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (key != -1) {
         KeyEvent event = new KeyEvent(key, window, action);
         EventManager.elementCodec(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
