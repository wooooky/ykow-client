package skid.ykow.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.modules.client.Ykow;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.ColorUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.Utils;
import java.awt.Color;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class PlayerESP extends Module {
   private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 0.0, 255.0, 100.0, 1.0);
   private final NumberSetting lineWidth = new NumberSetting(EncryptedString.of("Line width"), 1.0, 10.0, 1.0, 1.0);
   private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false)
      .setDescription(EncryptedString.of("Draws a line from your player to the other"));

   public PlayerESP() {
      super(EncryptedString.of("Player ESP"), EncryptedString.of("Renders players through walls"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.alpha, this.lineWidth, this.tracers});
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
   public void onRender3D(Render3DEvent render3DEvent) {
      for (Object next : this.mc.world.getPlayers()) {
         if (next != this.mc.player) {
            Camera camera = RenderUtils.getCamera();
            if (camera != null) {
               MatrixStack matrixStack = render3DEvent.matrixStack;
               render3DEvent.matrixStack.push();
               Vec3d pos = RenderUtils.getCameraPos();
               matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
               matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
               matrixStack.translate(-pos.x, -pos.y, -pos.z);
            }

            double lerpX = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), ((PlayerEntity)next).prevX, ((PlayerEntity)next).getX());
            double lerpY = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), ((PlayerEntity)next).prevY, ((PlayerEntity)next).getY());
            double lerpZ = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), ((PlayerEntity)next).prevZ, ((PlayerEntity)next).getZ());
            RenderUtils.renderFilledBox(
               render3DEvent.matrixStack,
               (float)lerpX - ((PlayerEntity)next).getWidth() / 2.0F,
               (float)lerpY,
               (float)lerpZ - ((PlayerEntity)next).getWidth() / 2.0F,
               (float)lerpX + ((PlayerEntity)next).getWidth() / 2.0F,
               (float)lerpY + ((PlayerEntity)next).getHeight(),
               (float)lerpZ + ((PlayerEntity)next).getWidth() / 2.0F,
               Utils.getMainColor(this.alpha.getIntValue(), 1).brighter()
            );
            if (this.tracers.getValue()) {
               RenderUtils.renderLine(
                  render3DEvent.matrixStack,
                  Utils.getMainColor(255, 1),
                  this.mc.crosshairTarget.getPos(),
                  ((PlayerEntity)next).getLerpedPos(RenderTickCounter.ONE.getTickDelta(true))
               );
            }

            render3DEvent.matrixStack.pop();
         }
      }
   }

   private void renderPlayerOutline(PlayerEntity playerEntity, Color color, MatrixStack matrixStack) {
      float red = color.brighter().getRed() / 255.0F;
      float green = color.brighter().getGreen() / 255.0F;
      float blue = color.brighter().getBlue() / 255.0F;
      float alpha = color.brighter().getAlpha() / 255.0F;
      Camera camera = this.mc.gameRenderer.getCamera();
      Vec3d subtract = playerEntity.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true)).subtract(camera.getPos());
      float offsetX = (float)subtract.x;
      float offsetY = (float)subtract.y;
      float offsetZ = (float)subtract.z;
      double radians = Math.toRadians(camera.getYaw() + 90.0F);
      double sinOffset = Math.sin(radians) * (playerEntity.getWidth() / 1.7);
      double cosOffset = Math.cos(radians) * (playerEntity.getWidth() / 1.7);
      matrixStack.push();
      Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      if (Ykow.enableMSAA.getValue()) {
         GL11.glEnable(32925);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
      }

      GL11.glDepthFunc(519);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableBlend();
      GL11.glLineWidth(this.lineWidth.getIntValue());
      BufferBuilder begin = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      begin.vertex(positionMatrix, offsetX + (float)sinOffset, offsetY, offsetZ + (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX - (float)sinOffset, offsetY, offsetZ - (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX - (float)sinOffset, offsetY, offsetZ - (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX - (float)sinOffset, offsetY + playerEntity.getHeight(), offsetZ - (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX - (float)sinOffset, offsetY + playerEntity.getHeight(), offsetZ - (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX + (float)sinOffset, offsetY + playerEntity.getHeight(), offsetZ + (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX + (float)sinOffset, offsetY + playerEntity.getHeight(), offsetZ + (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX + (float)sinOffset, offsetY, offsetZ + (float)cosOffset).color(red, green, blue, alpha);
      begin.vertex(positionMatrix, offsetX + (float)sinOffset, offsetY, offsetZ + (float)cosOffset).color(red, green, blue, alpha);
      BufferRenderer.drawWithGlobalProgram(begin.end());
      GL11.glDepthFunc(515);
      GL11.glLineWidth(1.0F);
      RenderSystem.disableBlend();
      if (Ykow.enableMSAA.getValue()) {
         GL11.glDisable(2848);
         GL11.glDisable(32925);
      }

      matrixStack.pop();
   }

   private Color getColorWithAlpha(int a) {
      int f = Ykow.redColor.getIntValue();
      int f2 = Ykow.greenColor.getIntValue();
      int f3 = Ykow.blueColor.getIntValue();
      return Ykow.enableRainbowEffect.getValue() ? ColorUtil.keyCodec(1, a) : new Color(f, f2, f3, a);
   }
}
