package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.module.Module;
import skid.ykow.module.modules.combat.Hitbox;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
   @ModifyVariable(
      method = {"renderHitbox"},
      ordinal = 0,
      at = @At(
         value = "STORE",
         ordinal = 0
      )
   )
   private static Box onRenderHitboxEditBox(Box box) {
      Module hitboxes = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(Hitbox.class);
      return hitboxes.isEnabled() ? box.expand(((Hitbox)hitboxes).getHitboxExpansion()) : box;
   }
}
