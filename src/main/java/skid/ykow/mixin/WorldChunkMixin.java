package skid.ykow.mixin;

import skid.ykow.event.events.SetBlockStateEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WorldChunk.class})
public class WorldChunkMixin {
   @Shadow
   @Final
   World world;

   @Inject(
      method = {"setBlockState"},
      at = {@At("TAIL")}
   )
   private void onSetBlockState(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
      if (this.world.isClient) {
         EventManager.elementCodec(new SetBlockStateEvent(pos, (BlockState)cir.getReturnValue(), state));
      }
   }
}
