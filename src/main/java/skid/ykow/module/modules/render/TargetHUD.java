package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PacketSendEvent;
import skid.ykow.event.events.Render2DEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.TextRenderer;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public final class TargetHUD extends Module {
   private final NumberSetting xPosition = new NumberSetting(EncryptedString.of("X"), 0.0, 1920.0, 500.0, 1.0);
   private final NumberSetting yPosition = new NumberSetting(EncryptedString.of("Y"), 0.0, 1080.0, 500.0, 1.0);
   private final BooleanSetting timeoutEnabled = new BooleanSetting(EncryptedString.of("Timeout"), true)
      .setDescription(EncryptedString.of("Target hud will disappear after 10 seconds"));
   private final NumberSetting fadeSpeed = new NumberSetting(EncryptedString.of("Fade Speed"), 5.0, 30.0, 15.0, 1.0)
      .getValue(EncryptedString.of("Speed of animations"));
   private final Color primaryColor = new Color(255, 50, 100);
   private final Color backgroundColor = new Color(0, 0, 0, 175);
   private long lastAttackTime = 0L;
   public static float fadeProgress = 1.0F;
   private float currentHealth = 0.0F;
   private TargetHUD.TargetHUDHandler hudHandler;

   public TargetHUD() {
      super(EncryptedString.of("Target HUD"), EncryptedString.of("Displays detailed information about your target with style"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.xPosition, this.yPosition, this.timeoutEnabled, this.fadeSpeed});
   }

   @Override
   public void onEnable() {
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onRender2D(Render2DEvent render2DEvent) {
      DrawContext drawContext = render2DEvent.context;
      int xPos = this.xPosition.getIntValue();
      int yPos = this.yPosition.getIntValue();
      float fadeSpeed = this.fadeSpeed.getFloatValue();
      Color primaryColor = this.primaryColor;
      Color backgroundColor = this.backgroundColor;
      RenderUtils.unscaledProjection();
      boolean hasTarget = this.mc.player.getAttacking() != null
         && this.mc.player.getAttacking() instanceof PlayerEntity
         && this.mc.player.getAttacking().isAlive();
      boolean notTimedOut = !this.timeoutEnabled.getValue() || System.currentTimeMillis() - this.lastAttackTime <= 10000L;
      float targetFade;
      if (hasTarget && notTimedOut) {
         targetFade = 0.0F;
      } else {
         targetFade = 1.0F;
      }

      fadeProgress = RenderUtils.fast(fadeProgress, targetFade, fadeSpeed);
      if (fadeProgress < 0.99F && hasTarget) {
         LivingEntity target = this.mc.player.getAttacking();
         PlayerListEntry playerListEntry = this.mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
         MatrixStack matrices = drawContext.getMatrices();
         matrices.push();
         float fadeAmount = 1.0F - fadeProgress;
         float scaleAmount = 0.8F + 0.2F * fadeAmount;
         matrices.translate(xPos, yPos, 0.0F);
         matrices.scale(scaleAmount, scaleAmount, 1.0F);
         matrices.translate(-xPos, -yPos, 0.0F);
         this.currentHealth = RenderUtils.fast(this.currentHealth, target.getHealth() + target.getAbsorptionAmount(), fadeSpeed * 0.5F);
         this.keyCodec(drawContext, xPos, yPos, (PlayerEntity)target, playerListEntry, fadeAmount, primaryColor, backgroundColor);
         matrices.pop();
      }

      RenderUtils.scaledProjection();
   }

   private void keyCodec(
      DrawContext drawContext, int n, int n2, PlayerEntity playerEntity, PlayerListEntry playerListEntry, float n3, Color color, Color color2
   ) {
      MatrixStack matrices = drawContext.getMatrices();
      RenderUtils.renderRoundedQuad(
         matrices,
         new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(50.0F * n3)),
         n - 5,
         n2 - 5,
         n + 300 + 5,
         n2 + 180 + 5,
         15.0,
         15.0,
         15.0,
         15.0,
         30.0
      );
      RenderUtils.renderRoundedQuad(
         matrices,
         new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(color2.getAlpha() * n3)),
         n,
         n2,
         n + 300,
         n2 + 180,
         10.0,
         10.0,
         10.0,
         10.0,
         20.0
      );
      Color color3 = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * n3));
      RenderUtils.renderRoundedQuad(matrices, color3, n + 20, n2, n + 300 - 20, n2 + 3, 0.0, 0.0, 0.0, 0.0, 10.0);
      RenderUtils.renderRoundedQuad(matrices, color3, n + 20, n2 + 180 - 3, n + 300 - 20, n2 + 180, 0.0, 0.0, 0.0, 0.0, 10.0);
      if (playerListEntry != null) {
         RenderUtils.renderRoundedQuad(matrices, new Color(30, 30, 30, (int)(200.0F * n3)), n + 15, n2 + 15, n + 85, n2 + 85, 5.0, 5.0, 5.0, 5.0, 10.0);
         PlayerSkinDrawer.draw(drawContext, playerListEntry.getSkinTextures().texture(), n + 25, n2 + 25, 50);
         TextRenderer.drawString(
            playerEntity.getName().getString(),
            drawContext,
            n + 100,
            n2 + 25,
            ColorUtil.keyCodec((int)((float)(System.currentTimeMillis() % 1000L) / 1000.0F), 1).getRGB()
         );
         TextRenderer.drawString(
            MathUtil.roundToNearest(playerEntity.distanceTo(this.mc.player), 1.0) + " blocks away", drawContext, n + 100, n2 + 45, Color.WHITE.getRGB()
         );
         RenderUtils.renderRoundedQuad(matrices, new Color(60, 60, 60, (int)(200.0F * n3)), n + 15, n2 + 95, n + 300 - 15, n2 + 110, 5.0, 5.0, 5.0, 5.0, 10.0);
         float b = this.currentHealth / playerEntity.getMaxHealth();
         float n4 = 270.0F * Math.min(1.0F, b);
         RenderUtils.renderRoundedQuad(
            matrices,
            this.keyCodec(b * (float)(0.8F + 0.2F * Math.sin(System.currentTimeMillis() / 300.0)), n3),
            n + 15,
            n2 + 95,
            n + 15 + (int)n4,
            n2 + 110,
            5.0,
            5.0,
            5.0,
            5.0,
            10.0
         );
         String s = Math.round(this.currentHealth) + "/" + Math.round(playerEntity.getMaxHealth()) + " HP";
         TextRenderer.drawString(s, drawContext, n + 15 + (int)n4 / 2 - TextRenderer.getWidth(s) / 2, n2 + 95, Color.WHITE.getRGB());
         int n5 = n2 + 120;
         this.keyCodec(
            drawContext, n + 15, n5, 80, 45, "PING", playerListEntry.getLatency() + "ms", this.keyCodec(playerListEntry.getLatency(), n3), color3, n3
         );
         String s2;
         if (playerListEntry != null) {
            s2 = "PLAYER";
         } else {
            s2 = "BOT";
         }

         Color color4;
         if (playerListEntry != null) {
            color4 = new Color(100, 255, 100, (int)(255.0F * n3));
         } else {
            color4 = new Color(255, 100, 100, (int)(255.0F * n3));
         }

         this.keyCodec(drawContext, n + 100 + 5, n5, 80, 45, "TYPE", s2, color4, color3, n3);
         if (playerEntity.hurtTime > 0) {
            this.keyCodec(drawContext, n + 200 + 5, n5, 80, 45, "HURT", playerEntity.hurtTime + "", this.elementCodec(playerEntity.hurtTime, n3), color3, n3);
         } else {
            this.keyCodec(drawContext, n + 200 + 5, n5, 80, 45, "HURT", "No", new Color(150, 150, 150, (int)(255.0F * n3)), color3, n3);
         }
      } else {
         TextRenderer.drawString("BOT DETECTED", drawContext, n + 150 - TextRenderer.getWidth("BOT DETECTED") / 2, n2 + 90, new Color(255, 50, 50).getRGB());
      }
   }

   private void keyCodec(DrawContext drawContext, int n, int n2, int n3, int n4, String s, String s2, Color color, Color color2, float n5) {
      MatrixStack matrices = drawContext.getMatrices();
      RenderUtils.renderRoundedQuad(matrices, color2, n, n2, n + n3, n2 + 3, 3.0, 3.0, 0.0, 0.0, 6.0);
      RenderUtils.renderRoundedQuad(matrices, new Color(30, 30, 30, (int)(200.0F * n5)), n, n2 + 3, n + n3, n2 + n4, 0.0, 0.0, 3.0, 3.0, 6.0);
      TextRenderer.drawString(s, drawContext, n + n3 / 2 - TextRenderer.getWidth(s) / 2, n2 + 5, new Color(200, 200, 200, (int)(255.0F * n5)).getRGB());
      TextRenderer.drawString(s2, drawContext, n + n3 / 2 - TextRenderer.getWidth(s2) / 2, n2 + n4 - 17, color.getRGB());
   }

   private Color keyCodec(float n, float n2) {
      Color color;
      if (n > 0.75F) {
         color = ColorUtil.keyCodec(new Color(100, 255, 100), new Color(255, 255, 100), (1.0F - n) * 4.0F);
      } else if (n > 0.25F) {
         color = ColorUtil.keyCodec(new Color(255, 255, 100), new Color(255, 100, 100), (0.75F - n) * 2.0F);
      } else {
         float n3;
         if (n < 0.1F) {
            n3 = (float)(0.7F + 0.3F * Math.sin(System.currentTimeMillis() / 200.0));
         } else {
            n3 = 1.0F;
         }

         color = new Color((int)(255.0F * n3), (int)(100.0F * n3), (int)(100.0F * n3));
      }

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * n2));
   }

   private Color keyCodec(int n, float n2) {
      Color color;
      if (n < 50) {
         color = new Color(100, 255, 100);
      } else if (n < 100) {
         color = ColorUtil.keyCodec(new Color(100, 255, 100), new Color(255, 255, 100), (n - 50) / 50.0F);
      } else if (n < 200) {
         color = ColorUtil.keyCodec(new Color(255, 255, 100), new Color(255, 150, 50), (n - 100) / 100.0F);
      } else {
         color = ColorUtil.keyCodec(new Color(255, 150, 50), new Color(255, 80, 80), Math.min(1.0F, (n - 200) / 300.0F));
      }

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * n2));
   }

   private Color elementCodec(int n, float n2) {
      double n3 = 0.7F + 0.3F * Math.sin(System.currentTimeMillis() / 150.0);
      float min = Math.min(1.0F, n / 10.0F);
      Color color = new Color(255, (int)(50.0F + 100.0F * (1.0F - min)), (int)(50.0F + 100.0F * (1.0F - min)));
      return new Color((int)(color.getRed() * (float)n3), (int)(color.getGreen() * (float)n3), (int)(color.getBlue() * (float)n3), (int)(255.0F * n2));
   }

   private void renderInfoBox(
      DrawContext drawContext, int xPos, int yPos, int width, int height, String title, String value, Color color, Color borderColor, float fadeAmount
   ) {
      MatrixStack matrices = drawContext.getMatrices();
      RenderUtils.renderRoundedQuad(matrices, borderColor, xPos, yPos, xPos + width, yPos + 3, 3.0, 3.0, 0.0, 0.0, 6.0);
      RenderUtils.renderRoundedQuad(
         matrices, new Color(30, 30, 30, (int)(200.0F * fadeAmount)), xPos, yPos + 3, xPos + width, yPos + height, 0.0, 0.0, 3.0, 3.0, 6.0
      );
      TextRenderer.drawString(
         title, drawContext, xPos + width / 2 - TextRenderer.getWidth(title) / 2, yPos + 5, new Color(200, 200, 200, (int)(255.0F * fadeAmount)).getRGB()
      );
      TextRenderer.drawString(value, drawContext, xPos + width / 2 - TextRenderer.getWidth(value) / 2, yPos + height - 17, color.getRGB());
   }

   private Color getHealthColor(float healthPercent, float fadeAmount) {
      Color color;
      if (healthPercent > 0.75F) {
         color = ColorUtil.keyCodec(new Color(100, 255, 100), new Color(255, 255, 100), (1.0F - healthPercent) * 4.0F);
      } else if (healthPercent > 0.25F) {
         color = ColorUtil.keyCodec(new Color(255, 255, 100), new Color(255, 100, 100), (0.75F - healthPercent) * 2.0F);
      } else {
         float pulseIntensity;
         if (healthPercent < 0.1F) {
            pulseIntensity = (float)(0.7F + 0.3F * Math.sin(System.currentTimeMillis() / 200.0));
         } else {
            pulseIntensity = 1.0F;
         }

         color = new Color((int)(255.0F * pulseIntensity), (int)(100.0F * pulseIntensity), (int)(100.0F * pulseIntensity));
      }

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * fadeAmount));
   }

   private Color getPingColor(int ping, float fadeAmount) {
      Color color;
      if (ping < 50) {
         color = new Color(100, 255, 100);
      } else if (ping < 100) {
         color = ColorUtil.keyCodec(new Color(100, 255, 100), new Color(255, 255, 100), (ping - 50) / 50.0F);
      } else if (ping < 200) {
         color = ColorUtil.keyCodec(new Color(255, 255, 100), new Color(255, 150, 50), (ping - 100) / 100.0F);
      } else {
         color = ColorUtil.keyCodec(new Color(255, 150, 50), new Color(255, 80, 80), Math.min(1.0F, (ping - 200) / 300.0F));
      }

      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * fadeAmount));
   }

   private Color getHurtColor(int hurtTime, float fadeAmount) {
      double pulseIntensity = 0.7F + 0.3F * Math.sin(System.currentTimeMillis() / 150.0);
      float hurtIntensity = Math.min(1.0F, hurtTime / 10.0F);
      Color color = new Color(255, (int)(50.0F + 100.0F * (1.0F - hurtIntensity)), (int)(50.0F + 100.0F * (1.0F - hurtIntensity)));
      return new Color(
         (int)(color.getRed() * (float)pulseIntensity),
         (int)(color.getGreen() * (float)pulseIntensity),
         (int)(color.getBlue() * (float)pulseIntensity),
         (int)(255.0F * fadeAmount)
      );
   }

   @EventListener
   public void onPacketSend(PacketSendEvent packetEvent) {
      if (packetEvent.getPacket() instanceof PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket) {
         if (this.hudHandler == null) {
            this.hudHandler = new TargetHUD.TargetHUDHandler(this);
         }

         if (this.hudHandler.isAttackPacket(playerInteractEntityC2SPacket)) {
            this.lastAttackTime = System.currentTimeMillis();
         }
      }
   }

   public static class TargetHUDHandler {
      public static final MinecraftClient MC = MinecraftClient.getInstance();
      final TargetHUD this$0;

      TargetHUDHandler(TargetHUD this$0) {
         this.this$0 = this$0;
      }

      public boolean isAttackPacket(PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket) {
         String string;
         try {
            string = playerInteractEntityC2SPacket.toString();
            if (string.contains("ATTACK")) {
               return true;
            }
         } catch (Exception var5) {
            return MC.player != null && MC.player.getAttacking() != null && MC.player.getAttacking() instanceof PlayerEntity;
         }

         try {
            if (MC.player == null || MC.player.getAttacking() == null || !(MC.player.getAttacking() instanceof PlayerEntity)) {
               return false;
            }

            boolean contains = string.contains(Hand.MAIN_HAND.toString());
            boolean contains2 = string.contains("INTERACT_AT");
            if (contains && contains2) {
               return true;
            }
         } catch (Exception var7) {
            return MC.player != null && MC.player.getAttacking() != null && MC.player.getAttacking() instanceof PlayerEntity;
         }

         try {
            return MC.player.handSwinging && MC.player.getAttacking() != null;
         } catch (Exception var6) {
            return MC.player != null && MC.player.getAttacking() != null && MC.player.getAttacking() instanceof PlayerEntity;
         }
      }
   }
}
