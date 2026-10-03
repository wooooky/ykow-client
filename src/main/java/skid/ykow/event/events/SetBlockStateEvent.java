package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SetBlockStateEvent extends CancellableEvent {
   public BlockPos pos;
   public BlockState newState;
   public BlockState oldState;

   public SetBlockStateEvent(BlockPos pos, BlockState newState, BlockState oldState) {
      this.pos = pos;
      this.newState = oldState;
      this.oldState = newState;
   }
}
