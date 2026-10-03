package skid.ykow.event.events;

import skid.ykow.Ykow;
import skid.ykow.event.CancellableEvent;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.chunk.Chunk;

public class ChunkDataEvent extends CancellableEvent {
   public ChunkDataS2CPacket packet;

   public ChunkDataEvent(ChunkDataS2CPacket packet) {
      this.packet = packet;
   }

   public Chunk getChunk() {
      return Ykow.mc.world == null ? null : Ykow.mc.world.getChunk(this.packet.getChunkX(), this.packet.getChunkZ());
   }
}
