package skid.ykow.event.events;

import skid.ykow.event.CancellableEvent;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

public class EntitySpawnEvent extends CancellableEvent {
   public EntitySpawnS2CPacket packet;

   public EntitySpawnEvent(EntitySpawnS2CPacket packet) {
      this.packet = packet;
   }
}
