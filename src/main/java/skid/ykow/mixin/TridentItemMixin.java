package skid.ykow.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import skid.ykow.module.modules.misc.TridentBoost;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({TridentItem.class})
public abstract class TridentItemMixin {
   @Inject(
      method = {"onStoppedUsing"},
      at = {@At("HEAD")}
   )
   private void onStoppedUsingHead(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
   }

   @Inject(
      method = {"onStoppedUsing"},
      at = {@At("TAIL")}
   )
   private void onStoppedUsingTail(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
   }

   @ModifyArgs(
      method = {"onStoppedUsing"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;addVelocity(DDD)V"
      )
   )
   private void modifyVelocity(Args args) {
      TridentBoost module = TridentBoost.getInstance();
      args.set(0, (Double)args.get(0) * module.getMultiplier());
      args.set(1, (Double)args.get(1) * module.getMultiplier());
      args.set(2, (Double)args.get(2) * module.getMultiplier());
   }

   @ModifyExpressionValue(
      method = {"use"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"
      )}
   )
   private boolean isInWaterUse(boolean original) {
      TridentBoost module = TridentBoost.getInstance();
      return module.allowOutOfWater() || original;
   }

   @ModifyExpressionValue(
      method = {"onStoppedUsing"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"
      )}
   )
   private boolean isInWaterPostUse(boolean original) {
      TridentBoost module = TridentBoost.getInstance();
      return module.allowOutOfWater() || original;
   }
}
