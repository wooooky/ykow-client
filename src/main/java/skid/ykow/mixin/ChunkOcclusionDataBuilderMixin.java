package skid.ykow.mixin;

import skid.ykow.event.events.ChunkMarkClosedEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChunkOcclusionDataBuilder.class})
public abstract class ChunkOcclusionDataBuilderMixin {
   @Inject(
      method = {"markClosed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMarkClosed(BlockPos pos, CallbackInfo ci) {
      ChunkMarkClosedEvent event = new ChunkMarkClosedEvent();
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
