package skid.ykow.mixin;

import skid.ykow.event.events.AttackBlockEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientPlayerInteractionManager.class})
public class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"attackBlock"},
      at = {@At("HEAD")}
   )
   private void onAttackBlock(BlockPos pos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
      EventManager.elementCodec(new AttackBlockEvent(pos, dir));
   }
}
