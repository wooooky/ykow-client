package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PacketReceiveEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public final class TimeChanger extends Module {
   private final NumberSetting time = new NumberSetting(EncryptedString.of("Time"), 0.0, 24000.0, 6000.0, 1.0)
      .setDescription(EncryptedString.of("Time of day in ticks (0 sunrise, 6000 noon, 12000 sunset, 18000 midnight)"));

   public TimeChanger() {
      super(EncryptedString.of("Time Changer"), EncryptedString.of("Overrides the client-side time of day"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.time});
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.world != null) {
         long timeOfDay = (long) this.time.getValue();
         this.mc.world.setTime(timeOfDay);
         this.mc.world.setTimeOfDay(timeOfDay);
      }
   }

   @EventListener
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.packet instanceof WorldTimeUpdateS2CPacket) {
         event.cancel();
      }
   }
}
