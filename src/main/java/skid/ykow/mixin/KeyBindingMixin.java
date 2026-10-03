package skid.ykow.mixin;

import skid.ykow.Ykow;
import skid.ykow.imixin.IKeybinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({KeyBinding.class})
public abstract class KeyBindingMixin implements IKeybinding {
   @Shadow
   private Key boundKey;

   @Shadow
   public abstract void setPressed(boolean var1);

   @Override
   public boolean ykow$isActuallyPressed() {
      return InputUtil.isKeyPressed(Ykow.mc.getWindow().getHandle(), this.boundKey.getCode());
   }

   @Override
   public void ykow$resetPressed() {
      this.setPressed(this.ykow$isActuallyPressed());
   }
}
