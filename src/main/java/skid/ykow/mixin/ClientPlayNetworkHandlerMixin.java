package skid.ykow.mixin;

import skid.ykow.event.events.ChunkDataEvent;
import skid.ykow.event.events.EntitySpawnEvent;
import skid.ykow.manager.EventManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"onChunkData"},
      at = {@At("TAIL")}
   )
   private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
      EventManager.elementCodec(new ChunkDataEvent(packet));
   }

   @Inject(
      method = {"onEntitySpawn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
      EntitySpawnEvent event = new EntitySpawnEvent(packet);
      EventManager.elementCodec(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }
}
