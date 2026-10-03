package skid.ykow.module.modules.combat;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TargetPoseEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.utils.EncryptedString;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;

public final class StaticHitboxes extends Module {
   public StaticHitboxes() {
      super(EncryptedString.of("Static HitBoxes"), EncryptedString.of("Expands a Player's Hitbox"), -1, Category.COMBAT);
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
   public void onTargetPose(TargetPoseEvent targetPoseEvent) {
      if (this.isEnabled() && targetPoseEvent.entity instanceof PlayerEntity && !((PlayerEntity)targetPoseEvent.entity).isMainPlayer()) {
         targetPoseEvent.cir.setReturnValue(EntityPose.STANDING);
      }
   }
}
