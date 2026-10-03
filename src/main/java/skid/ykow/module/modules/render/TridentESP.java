package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import java.awt.Color;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class TridentESP extends Module {
   private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 10.0, 200.0, 100.0, 1.0);
   private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 50.0, 255.0, 200.0, 1.0);
   private final BooleanSetting showDistance = new BooleanSetting(EncryptedString.of("Show Distance"), true)
      .setDescription(EncryptedString.of("Shows distance to the drowned"));
   private final BooleanSetting showHealth = new BooleanSetting(EncryptedString.of("Show Health"), true)
      .setDescription(EncryptedString.of("Shows health of the drowned"));
   private final BooleanSetting onlyTrident = new BooleanSetting(EncryptedString.of("Only Trident"), true)
      .setDescription(EncryptedString.of("Only highlights drowned with tridents"));

   public TridentESP() {
      super(EncryptedString.of("Trident ESP"), EncryptedString.of("Highlights drowned with tridents"), -1, Category.RENDER);
      this.range.setDescription(EncryptedString.of("Range to search for drowned"));
      this.alpha.setDescription(EncryptedString.of("Transparency of the ESP"));
      this.addsettings(new Setting[]{this.range, this.alpha, this.showDistance, this.showHealth, this.onlyTrident});
   }

   @EventListener
   public void onRender3D(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         Camera cam = RenderUtils.getCamera();
         if (cam != null) {
            Vec3d camPos = RenderUtils.getCameraPos();
            MatrixStack matrices = event.matrixStack;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
         }

         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof DrownedEntity drowned && (!this.onlyTrident.getValue() || this.hasTrident(drowned))) {
               double distance = this.mc.player.distanceTo(drowned);
               if (!(distance > this.range.getValue())) {
                  Color color = this.getDrownedColor(drowned, distance);
                  float width = drowned.getWidth();
                  float height = drowned.getHeight();
                  double x = drowned.getX();
                  double y = drowned.getY();
                  double z = drowned.getZ();
                  RenderUtils.renderFilledBox(
                     event.matrixStack,
                     (float)(x - width / 2.0F),
                     (float)y,
                     (float)(z - width / 2.0F),
                     (float)(x + width / 2.0F),
                     (float)(y + height),
                     (float)(z + width / 2.0F),
                     color
                  );
                  RenderUtils.renderLine(
                     event.matrixStack, color, new Vec3d(x - width / 2.0F, y, z - width / 2.0F), new Vec3d(x + width / 2.0F, y + height, z + width / 2.0F)
                  );
                  this.renderEntityOutline(event.matrixStack, x, y, z, width, height, color);
               }
            }
         }

         event.matrixStack.pop();
      }
   }

   private boolean hasTrident(DrownedEntity drowned) {
      ItemStack mainHand = drowned.getMainHandStack();
      if (mainHand.getItem() == Items.TRIDENT) {
         return true;
      } else {
         ItemStack offHand = drowned.getOffHandStack();
         return offHand.getItem() == Items.TRIDENT;
      }
   }

   private Color getDrownedColor(DrownedEntity drowned, double distance) {
      int red = 255;
      int green = 0;
      int blue = 0;
      double distanceFactor = Math.max(0.0, 1.0 - distance / this.range.getValue());
      red = (int)(255.0 * distanceFactor);
      green = (int)(50.0 * distanceFactor);
      blue = (int)(50.0 * distanceFactor);
      float healthPercent = drowned.getHealth() / drowned.getMaxHealth();
      if (healthPercent < 0.5F) {
         red = (int)(255.0 * distanceFactor);
         green = (int)(165.0 * distanceFactor);
         blue = 0;
      }

      return new Color(red, green, blue, this.alpha.getIntValue());
   }

   private void renderEntityOutline(MatrixStack matrices, double x, double y, double z, float width, float height, Color color) {
      RenderUtils.renderLine(matrices, color, new Vec3d(x - width / 2.0F, y, z - width / 2.0F), new Vec3d(x - width / 2.0F, y + height, z - width / 2.0F));
      RenderUtils.renderLine(matrices, color, new Vec3d(x + width / 2.0F, y, z - width / 2.0F), new Vec3d(x + width / 2.0F, y + height, z - width / 2.0F));
      RenderUtils.renderLine(matrices, color, new Vec3d(x - width / 2.0F, y, z + width / 2.0F), new Vec3d(x - width / 2.0F, y + height, z + width / 2.0F));
      RenderUtils.renderLine(matrices, color, new Vec3d(x + width / 2.0F, y, z + width / 2.0F), new Vec3d(x + width / 2.0F, y + height, z + width / 2.0F));
      RenderUtils.renderLine(
         matrices, color, new Vec3d(x - width / 2.0F, y + height, z - width / 2.0F), new Vec3d(x + width / 2.0F, y + height, z - width / 2.0F)
      );
      RenderUtils.renderLine(
         matrices, color, new Vec3d(x + width / 2.0F, y + height, z - width / 2.0F), new Vec3d(x + width / 2.0F, y + height, z + width / 2.0F)
      );
      RenderUtils.renderLine(
         matrices, color, new Vec3d(x + width / 2.0F, y + height, z + width / 2.0F), new Vec3d(x - width / 2.0F, y + height, z + width / 2.0F)
      );
      RenderUtils.renderLine(
         matrices, color, new Vec3d(x - width / 2.0F, y + height, z + width / 2.0F), new Vec3d(x - width / 2.0F, y + height, z - width / 2.0F)
      );
   }
}
