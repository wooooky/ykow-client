package skid.ykow.module.modules.misc;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.KeyEvent;
import skid.ykow.event.events.MouseScrolledEvent;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.Utils;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

public final class Freecam extends Module {
   private final NumberSetting speed = new NumberSetting(EncryptedString.of("Speed"), 1.0, 10.0, 1.0, 0.1);
   public final Vector3d currentPosition = new Vector3d();
   public final Vector3d previousPosition = new Vector3d();
   private final Vector3d velocity = new Vector3d();
   private Perspective currentPerspective;
   private double movementSpeed;
   public float yaw;
   public float pitch;
   public float previousYaw;
   public float previousPitch;
   private boolean isMovingForward;
   private boolean isMovingBackward;
   private boolean isMovingRight;
   private boolean isMovingLeft;
   private boolean isMovingUp;
   private boolean isMovingDown;
   public static Freecam instance;

   public Freecam() {
      super(EncryptedString.of("Freecam"), EncryptedString.of("Move freely with smooth camera"), -1, Category.MISC);
      this.addsettings(new Setting[]{this.speed});
      instance = this;
   }

   @Override
   public void onEnable() {
      if (this.mc.player == null) {
         this.toggle();
      } else {
         this.mc.options.getFovEffectScale().setValue(0.0);
         this.mc.options.getBobView().setValue(false);
         this.yaw = this.mc.player.getYaw();
         this.pitch = this.mc.player.getPitch();
         this.currentPerspective = this.mc.options.getPerspective();
         this.movementSpeed = this.speed.getValue();
         Utils.copyVector(this.currentPosition, this.mc.gameRenderer.getCamera().getPos());
         Utils.copyVector(this.previousPosition, this.mc.gameRenderer.getCamera().getPos());
         if (this.mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            this.yaw += 180.0F;
            this.pitch *= -1.0F;
         }

         this.previousYaw = this.yaw;
         this.previousPitch = this.pitch;
         this.isMovingForward = this.mc.options.forwardKey.isPressed();
         this.isMovingBackward = this.mc.options.backKey.isPressed();
         this.isMovingRight = this.mc.options.rightKey.isPressed();
         this.isMovingLeft = this.mc.options.leftKey.isPressed();
         this.isMovingUp = this.mc.options.jumpKey.isPressed();
         this.isMovingDown = this.mc.options.sneakKey.isPressed();
         this.resetMovementKeys();
         if (this.mc.player != null) {
            this.mc.player.noClip = true;
         }

         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      this.resetMovementKeys();
      this.previousPosition.set(this.currentPosition);
      this.previousYaw = this.yaw;
      this.previousPitch = this.pitch;
      if (this.mc.player != null) {
         this.mc.player.noClip = false;
      }

      if (this.currentPerspective != null) {
         this.mc.options.setPerspective(this.currentPerspective);
      }

      super.onDisable();
   }

   private void resetMovementKeys() {
      this.mc.options.forwardKey.setPressed(false);
      this.mc.options.backKey.setPressed(false);
      this.mc.options.rightKey.setPressed(false);
      this.mc.options.leftKey.setPressed(false);
      this.mc.options.jumpKey.setPressed(false);
      this.mc.options.sneakKey.setPressed(false);
   }

   @EventListener
   private void handleTickEvent(TickEvent tickEvent) {
      if (this.mc.cameraEntity != null && this.mc.cameraEntity.isInsideWall()) {
         this.mc.getCameraEntity().noClip = true;
      }

      if (this.mc.player != null) {
         this.mc.player.noClip = true;
      }

      if (!this.currentPerspective.isFirstPerson()) {
         this.mc.options.setPerspective(Perspective.FIRST_PERSON);
      }
   }

   @EventListener
   private void handleRenderTick(Render3DEvent event) {
      float partialTicks = 0.016666668F;
      Vec3d forward = Vec3d.fromPolar(0.0F, this.yaw);
      Vec3d right = Vec3d.fromPolar(0.0F, this.yaw + 90.0F);
      double targetX = 0.0;
      double targetY = 0.0;
      double targetZ = 0.0;
      double speed = this.movementSpeed * (this.mc.options.sprintKey.isPressed() ? 1.0 : 0.5);
      if (this.isMovingForward) {
         targetX += forward.x * speed;
         targetZ += forward.z * speed;
      }

      if (this.isMovingBackward) {
         targetX -= forward.x * speed;
         targetZ -= forward.z * speed;
      }

      if (this.isMovingRight) {
         targetX += right.x * speed;
         targetZ += right.z * speed;
      }

      if (this.isMovingLeft) {
         targetX -= right.x * speed;
         targetZ -= right.z * speed;
      }

      if (this.isMovingUp) {
         targetY += speed;
      }

      if (this.isMovingDown) {
         targetY -= speed;
      }

      double accel = 0.2;
      this.velocity.x = this.velocity.x + (targetX - this.velocity.x) * accel;
      this.velocity.y = this.velocity.y + (targetY - this.velocity.y) * accel;
      this.velocity.z = this.velocity.z + (targetZ - this.velocity.z) * accel;
      this.previousPosition.set(this.currentPosition);
      this.currentPosition.add(this.velocity.x * partialTicks, this.velocity.y * partialTicks, this.velocity.z * partialTicks);
   }

   @EventListener
   public void onKey(KeyEvent keyEvent) {
      if (!KeyUtils.isKeyPressed(292)) {
         boolean handled = true;
         if (this.mc.options.forwardKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingForward = keyEvent.mode != 0;
            this.mc.options.forwardKey.setPressed(false);
         } else if (this.mc.options.backKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingBackward = keyEvent.mode != 0;
            this.mc.options.backKey.setPressed(false);
         } else if (this.mc.options.rightKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingRight = keyEvent.mode != 0;
            this.mc.options.rightKey.setPressed(false);
         } else if (this.mc.options.leftKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingLeft = keyEvent.mode != 0;
            this.mc.options.leftKey.setPressed(false);
         } else if (this.mc.options.jumpKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingUp = keyEvent.mode != 0;
            this.mc.options.jumpKey.setPressed(false);
         } else if (this.mc.options.sneakKey.matchesKey(keyEvent.key, 0)) {
            this.isMovingDown = keyEvent.mode != 0;
            this.mc.options.sneakKey.setPressed(false);
         } else {
            handled = false;
         }

         if (handled) {
            keyEvent.cancel();
         }
      }
   }

   @EventListener
   private void handleMouseScrolledEvent(MouseScrolledEvent mouseScrolledEvent) {
      if (this.mc.currentScreen == null) {
         this.movementSpeed = this.movementSpeed + mouseScrolledEvent.amount * 0.25 * this.movementSpeed;
         if (this.movementSpeed < 0.1) {
            this.movementSpeed = 0.1;
         }

         mouseScrolledEvent.cancel();
      }
   }

   public static Freecam getInstance() {
      if (instance == null) {
         instance = new Freecam();
      }

      return instance;
   }

   public void updateRotation(double deltaYaw, double deltaPitch) {
      this.previousYaw = this.yaw;
      this.previousPitch = this.pitch;
      this.yaw = (float)(this.yaw + deltaYaw);
      this.pitch = (float)(this.pitch + deltaPitch);
      this.pitch = MathHelper.clamp(this.pitch, -90.0F, 90.0F);
   }

   public double getInterpolatedX(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousPosition.x, this.currentPosition.x);
   }

   public double getInterpolatedY(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousPosition.y, this.currentPosition.y);
   }

   public double getInterpolatedZ(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousPosition.z, this.currentPosition.z);
   }

   public double getInterpolatedYaw(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousYaw, this.yaw);
   }

   public double getInterpolatedPitch(float partialTicks) {
      return MathHelper.lerp(partialTicks, this.previousPitch, this.pitch);
   }
}
