package skid.ykow.utils;

import skid.ykow.module.modules.client.Ykow;

public final class Animation {
   private double currentValue;
   private final double targetValue;

   public Animation(double targetValue) {
      this.currentValue = targetValue;
      this.targetValue = targetValue;
   }

   public void animate(double speed, double target) {
      if (Ykow.animationMode.isMode(Ykow.AnimationMode.NORMAL)) {
         this.currentValue = MathUtil.approachValue((float)speed, this.currentValue, target);
      } else if (Ykow.animationMode.isMode(Ykow.AnimationMode.POSITIVE)) {
         this.currentValue = MathUtil.smoothStep(speed, this.currentValue, target);
      } else if (Ykow.animationMode.isMode(Ykow.AnimationMode.OFF)) {
         this.currentValue = target;
      }
   }

   public double getAnimation() {
      return this.currentValue;
   }

   public void setAnimation(double factor) {
      this.currentValue = MathUtil.smoothStep(factor, this.currentValue, this.targetValue);
   }
}
