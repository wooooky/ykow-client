package skid.ykow.utils.packets;

import skid.ykow.Ykow;
import skid.ykow.mixin.IClientWorldMixin;
import java.util.Objects;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.Vec3d;

public class PacketUtils {
   public static void sendPacket(Packet packet) {
      if (Ykow.mc.player != null) {
         Ykow.mc.getNetworkHandler().sendPacket(packet);
      }
   }

   public static void sendPosition(Vec3d pos) {
      if (Ykow.mc.player != null) {
         Ykow.mc.player.networkHandler.sendPacket(new PositionAndOnGround(pos.getX(), pos.getY(), pos.getZ(), Ykow.mc.player.isOnGround()));
      }
   }

   public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
      if (Ykow.mc.getNetworkHandler() != null && Ykow.mc.world != null) {
         PendingUpdateManager pendingUpdateManager = ((IClientWorldMixin)Ykow.mc.world).getPendingUpdateManager().incrementSequence();

         try {
            int i = pendingUpdateManager.getSequence();
            Ykow.mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
         } catch (Throwable var51) {
            if (pendingUpdateManager != null) {
               try {
                  pendingUpdateManager.close();
               } catch (Throwable var4) {
                  var51.addSuppressed(var4);
               }
            }

            throw var51;
         }

         if (pendingUpdateManager != null) {
            pendingUpdateManager.close();
         }
      }
   }

   public static void sendPacketSilently(Packet packet) {
      Ykow.mc.getNetworkHandler().getConnection().send(packet, (PacketCallbacks)null);
   }

   public void sendPacket2(Packet<?> packetIn) {
      if (packetIn != null) {
         Objects.requireNonNull(Ykow.mc.getNetworkHandler()).getConnection().send(packetIn);
      }
   }
}
