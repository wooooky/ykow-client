package skid.ykow.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

public class PingUtil {
   public static Integer getPingString() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.getNetworkHandler() != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         if (entry == null) {
            return 0;
         } else {
            int ping = entry.getLatency();
            return ping;
         }
      } else {
         return 0;
      }
   }
}
