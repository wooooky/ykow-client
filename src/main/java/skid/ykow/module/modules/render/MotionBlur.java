package skid.ykow.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import skid.ykow.Ykow;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.joml.Matrix4f;

public final class MotionBlur extends Module {
   private static SimpleFramebuffer backup;
   private final NumberSetting strength = new NumberSetting(EncryptedString.of("Strength"), 1.0, 90.0, 40.0, 1.0)
      .setDescription(EncryptedString.of("How much of the previous frame stays visible"));

   public MotionBlur() {
      super(EncryptedString.of("Motion Blur"), EncryptedString.of("Blends previous frames for a smooth motion blur"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.strength});
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (backup != null) {
         backup.delete();
         backup = null;
      }
   }

   private static MotionBlur getEnabled() {
      Module module = Ykow.INSTANCE.getModuleManager().getModuleByClass(MotionBlur.class);
      return module instanceof MotionBlur blur && blur.isEnabled() ? blur : null;
   }

   public static void onRenderStart() {
      MotionBlur module = getEnabled();
      if (module == null) {
         return;
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null) {
         return;
      }

      int width = mc.getWindow().getFramebufferWidth();
      int height = mc.getWindow().getFramebufferHeight();
      if (width <= 0 || height <= 0) {
         return;
      }

      if (backup == null || backup.textureWidth != width || backup.textureHeight != height) {
         if (backup != null) {
            backup.delete();
         }

         backup = new SimpleFramebuffer(width, height, false, MinecraftClient.IS_SYSTEM_MAC);
         return;
      }

      float alpha = (float) (module.strength.getValue() / 100.0);
      if (alpha <= 0.0F) {
         return;
      }

      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 21000.0F), VertexSorter.BY_Z);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      backup.draw(width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      RenderSystem.restoreProjectionMatrix();
   }

   public static void onRenderEnd() {
      if (getEnabled() == null) {
         return;
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world == null || mc.player == null) {
         return;
      }

      int width = mc.getWindow().getFramebufferWidth();
      int height = mc.getWindow().getFramebufferHeight();
      if (width <= 0 || height <= 0) {
         return;
      }

      if (backup == null || backup.textureWidth != width || backup.textureHeight != height) {
         return;
      }

      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 21000.0F), VertexSorter.BY_Z);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      backup.beginWrite(true);
      mc.getFramebuffer().draw(width, height);
      backup.endWrite();
      mc.getFramebuffer().beginWrite(true);
      RenderSystem.restoreProjectionMatrix();
   }
}
