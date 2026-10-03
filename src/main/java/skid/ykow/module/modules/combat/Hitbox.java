package skid.ykow.module.modules.combat;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TargetMarginEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import net.minecraft.entity.player.PlayerEntity;

public final class Hitbox extends Module {
   private final NumberSetting expand = new NumberSetting(EncryptedString.of("Expand"), 0.0, 2.0, 0.5, 0.05);
   private final BooleanSetting enableRender = new BooleanSetting("Enable Render", true);

   public Hitbox() {
      super(EncryptedString.of("HitBox"), EncryptedString.of("Expands a player's hitbox."), -1, Category.COMBAT);
      this.addsettings(new Setting[]{this.enableRender, this.expand});
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
   public void onTargetMargin(TargetMarginEvent targetMarginEvent) {
      if (targetMarginEvent.entity instanceof PlayerEntity) {
         targetMarginEvent.cir.setReturnValue((float)this.expand.getValue());
      }
   }

   public double getHitboxExpansion() {
      return !this.enableRender.getValue() ? 0.0 : this.expand.getValue();
   }
}
