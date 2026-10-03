package skid.ykow.module.modules.movement;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.PacketSendEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ColorSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.Utils;
import skid.ykow.utils.packets.PacketUtils;
import skid.ykow.utils.packets.TimedPacket;
import skid.ykow.utils.packets.TimerUtils;
import java.awt.Color;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class FakeLag extends Module {
   private final TimerUtils timer;
   private Vec3d vec3d;
   private boolean isLagging;
   private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
   private final ModeSetting<FakeLag.Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), FakeLag.Mode.FakeLag, FakeLag.Mode.class);
   private final NumberSetting delay2 = new NumberSetting(EncryptedString.of("Delay"), 50.0, 3000.0, 400.0, 600.0)
      .getValue(EncryptedString.of("Delay of holding packets"));
   private final BooleanSetting weaponOnly = new BooleanSetting(EncryptedString.of("Weapon Only"), false);
   private final BooleanSetting realPos = new BooleanSetting(EncryptedString.of("Server Pos"), false);
   private final ColorSetting color = new ColorSetting(EncryptedString.of("Color"), new Color(255, 0, 0, 100))
      .setDescription(EncryptedString.of("Color Of the server pos"));
   private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 12.0, 6.0, 1.0)
      .getValue(EncryptedString.of("Range for lag to enable"));

   public FakeLag() {
      super(EncryptedString.of("Fakelag"), EncryptedString.of("Holds packets and simulates lag"), -1, Category.MOVEMENT);
      this.addsettings(new Setting[]{this.delay2, this.range, this.weaponOnly, this.realPos, this.mode, this.color});
      this.timer = new TimerUtils();
      this.vec3d = Vec3d.ZERO;
      this.isLagging = false;
   }

   @Override
   public void onEnable() {
      this.packetQueue.clear();
   }

   @EventListener
   public void onPacketSend(PacketSendEvent event) {
      if (this.mc.world != null && this.mc.player != null) {
         Packet<?> packet = event.getPacket();
         if (this.shouldSendPacket(packet)) {
            this.sendPacket(false);
            PacketUtils.sendPacketSilently(packet);
            event.cancel();
         } else if (!event.isCancelled()) {
            this.packetQueue.add(new TimedPacket(packet, System.currentTimeMillis()));
            event.cancel();
         }
      }
   }

   public void onTick() {
      int var10001 = (int)this.delay2.getMin();
      this.setDescription(var10001 + "-" + (int)this.delay2.getMax());
      if (this.mc.world != null && this.mc.player != null) {
         if (this.mode.isMode(FakeLag.Mode.FakeLag)) {
            this.handleFakeLag();
         } else if (this.mode.isMode(FakeLag.Mode.LagRange)) {
            this.handleLagRange();
         }

         this.sendPacket(true);
      } else {
         this.sendPacket(false);
      }
   }

   public void onWorldRender(MatrixStack matrices) {
      if (this.realPos.getValue() && this.mc.player != null) {
         if (this.mc.options.getPerspective() != Perspective.FIRST_PERSON && this.isLagging && this.vec3d != null) {
         }

         if (this.mode.isMode(FakeLag.Mode.LagRange)) {
            this.renderLagRange(matrices);
         }
      }
   }

   private void handleFakeLag() {
      this.isLagging = true;
      if (this.timer.delay(this.delay2.getIntValue()) && this.mc.player != null) {
         if (this.weaponOnly.getValue() && this.isHoldingWeapon()) {
            this.sendPacket(true);
         } else {
            this.sendPacket(false);
         }
      }
   }

   private void handleLagRange() {
      if (this.mc.world != null && this.mc.player != null) {
         boolean nearbyPlayer = this.mc
            .world
            .getPlayers()
            .stream()
            .anyMatch(player -> player != this.mc.player && player.squaredDistanceTo(this.mc.player) <= this.range.getIntValue() * this.range.getIntValue());
         this.isLagging = nearbyPlayer;
         if (!nearbyPlayer || this.weaponOnly.getValue() && !this.isHoldingWeapon()) {
            this.sendPacket(false);
         }
      } else {
         this.sendPacket(false);
      }
   }

   private boolean isHoldingWeapon() {
      if (this.mc.player == null) {
         return false;
      } else {
         Item item = this.mc.player.getMainHandStack().getItem();
         ItemStack stack = this.mc.player.getMainHandStack();
         return item instanceof SwordItem;
      }
   }

   private void renderLagRange(MatrixStack matrices) {
      int segments = 64;
      Vec3d playerPos = this.mc.player.getLerpedPos(Utils.getTick());
      double yOffset = 0.5;
      Vec3d previousPoint = null;

      for (int i = 0; i <= segments; i++) {
         double angle = (Math.PI * 2) * i / segments;
         double x = playerPos.x + this.range.getFloatValue() * Math.cos(angle);
         double z = playerPos.z + this.range.getFloatValue() * Math.sin(angle);
         Vec3d currentPoint = new Vec3d(x, playerPos.y + yOffset, z);
         if (previousPoint != null) {
         }

         previousPoint = currentPoint;
      }
   }

   private void sendPacket(boolean delay) {
      try {
         while (!this.packetQueue.isEmpty()) {
            TimedPacket timedPacket = this.packetQueue.peek();
            if (timedPacket == null || delay && !timedPacket.getTimer().hasCooldownElapsed(this.delay2.getIntValue())) {
               break;
            }

            Packet<?> packet = this.packetQueue.remove().getPacket();
            if (packet != null) {
               getPos(packet).ifPresent(pos -> this.vec3d = pos);
               PacketUtils.sendPacketSilently(packet);
            }
         }
      } catch (Exception var4) {
      }
   }

   private boolean shouldSendPacket(Packet<?> packet) {
      return packet instanceof PlayerInteractEntityC2SPacket
         || packet instanceof HandSwingC2SPacket
         || packet instanceof PlayerInteractBlockC2SPacket
         || packet instanceof PlayerInteractItemC2SPacket
         || packet instanceof ClickSlotC2SPacket;
   }

   @NotNull
   public static Optional<Vec3d> getPos(Packet<?> packet) {
      if (packet instanceof PlayerPositionLookS2CPacket p) {
         return Optional.of(new Vec3d(p.getX(), p.getY(), p.getZ()));
      } else {
         return packet instanceof PlayerMoveC2SPacket p
            ? Optional.of(new Vec3d(p.getX(Ykow.mc.player.getX()), p.getY(Ykow.mc.player.getY()), p.getX(Ykow.mc.player.getX())))
            : Optional.empty();
      }
   }

   static enum Mode {
      LagRange("Lag range", 0),
      FakeLag("Fakelag", 1);

      private Mode(final String name, final int ordinal) {
      }
   }
}
