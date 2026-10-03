package skid.ykow.module.modules.render;

import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class Animations extends Module {
   private final BooleanSetting enabled = new BooleanSetting(EncryptedString.of("Enabled"), true)
      .setDescription(EncryptedString.of("Enable custom item animations"));
   private final NumberSetting swingSpeed = new NumberSetting(EncryptedString.of("Swing Speed"), 1.0, 10.0, 1.0, 0.1)
      .setDescription(EncryptedString.of("Speed of item swinging animation"));
   private final NumberSetting scale = new NumberSetting(EncryptedString.of("Scale"), 0.5, 2.0, 1.0, 0.1)
      .setDescription(EncryptedString.of("Scale of held items"));
   private final NumberSetting xOffset = new NumberSetting(EncryptedString.of("X Offset"), -1.0, 1.0, 0.0, 0.1)
      .setDescription(EncryptedString.of("Horizontal offset of held items"));
   private final NumberSetting yOffset = new NumberSetting(EncryptedString.of("Y Offset"), -1.0, 1.0, 0.0, 0.1)
      .setDescription(EncryptedString.of("Vertical offset of held items"));
   private final NumberSetting zOffset = new NumberSetting(EncryptedString.of("Z Offset"), -1.0, 1.0, 0.0, 0.1)
      .setDescription(EncryptedString.of("Depth offset of held items"));

   public Animations() {
      super(EncryptedString.of("Animations"), EncryptedString.of("Custom item animations and transformations"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.enabled, this.swingSpeed, this.scale, this.xOffset, this.yOffset, this.zOffset});
   }

   public boolean shouldAnimate() {
      return this.isEnabled() && this.enabled.getValue();
   }

   public void applyTransformations(MatrixStack matrices, float swingProgress) {
      matrices.push();
      float scaleValue = this.scale.getFloatValue();
      matrices.scale(scaleValue, scaleValue, scaleValue);
      matrices.translate(this.xOffset.getFloatValue(), this.yOffset.getFloatValue(), this.zOffset.getFloatValue());
      float swingSpeedValue = this.swingSpeed.getFloatValue();
      float swingAngle = swingProgress * swingSpeedValue * 90.0F;
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingAngle));
   }
}
