package skid.ykow.module.modules.crystal;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.PreItemUseEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BindSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.BlockUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class AutoCrystal extends Module {
   private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 1, false)
      .setDescription(EncryptedString.of("Key that does the crystalling"));
   private final NumberSetting placeDelay = new NumberSetting(EncryptedString.of("Place Delay"), 0.0, 20.0, 0.0, 1.0);
   private final NumberSetting breakDelay = new NumberSetting(EncryptedString.of("Break Delay"), 0.0, 20.0, 0.0, 1.0);
   private final BooleanSetting stopOnKill = new BooleanSetting(EncryptedString.of("Stop on Kill"), false)
      .setDescription(EncryptedString.of("Stops crystalling when a player dies nearby"));
   private int placeDelayCounter;
   private int breakDelayCounter;
   public boolean isActive;

   public AutoCrystal() {
      super(EncryptedString.of("Auto Crystal"), EncryptedString.of("Automatically crystals fast for you"), -1, Category.CRYSTAL);
      this.addsettings(new Setting[]{this.activateKey, this.placeDelay, this.breakDelay, this.stopOnKill});
   }

   @Override
   public void onEnable() {
      this.resetCounters();
      this.isActive = false;
      super.onEnable();
   }

   private void resetCounters() {
      this.placeDelayCounter = 0;
      this.breakDelayCounter = 0;
   }

   @EventListener
   public void onTick(TickEvent tickEvent) {
      if (this.mc.currentScreen == null) {
         this.updateCounters();
         if (!this.mc.player.isUsingItem()) {
            if (this.isKeyActive()) {
               if (this.mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                  if (!this.stopOnKill.getValue() || !WorldUtils.isDeadBodyNearby()) {
                     this.handleInteraction();
                  }
               }
            }
         }
      }
   }

   private void updateCounters() {
      if (this.placeDelayCounter > 0) {
         this.placeDelayCounter--;
      }

      if (this.breakDelayCounter > 0) {
         this.breakDelayCounter--;
      }
   }

   private boolean isKeyActive() {
      int activateKeyCode = this.activateKey.getValue();
      if (activateKeyCode != -1 && !KeyUtils.isKeyPressed(activateKeyCode)) {
         this.resetCounters();
         return this.isActive = false;
      } else {
         return this.isActive = true;
      }
   }

   private void handleInteraction() {
      HitResult crosshairTarget = this.mc.crosshairTarget;
      if (this.mc.crosshairTarget instanceof BlockHitResult) {
         this.handleBlockInteraction((BlockHitResult)crosshairTarget);
      } else if (this.mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
         this.handleEntityInteraction(entityHitResult);
      }
   }

   private void handleBlockInteraction(BlockHitResult blockHitResult) {
      if (blockHitResult.getType() == Type.BLOCK) {
         if (this.placeDelayCounter <= 0) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            if ((BlockUtil.isBlockAtPosition(blockPos, Blocks.OBSIDIAN) || BlockUtil.isBlockAtPosition(blockPos, Blocks.BEDROCK))
               && this.isValidCrystalPlacement(blockPos)) {
               BlockUtil.interactWithBlock(blockHitResult, true);
               this.placeDelayCounter = this.placeDelay.getIntValue();
            }
         }
      }
   }

   private void handleEntityInteraction(EntityHitResult entityHitResult) {
      if (this.breakDelayCounter <= 0) {
         Entity entity = entityHitResult.getEntity();
         if (entity instanceof EndCrystalEntity || entity instanceof SlimeEntity) {
            this.mc.interactionManager.attackEntity(this.mc.player, entity);
            this.mc.player.swingHand(Hand.MAIN_HAND);
            this.breakDelayCounter = this.breakDelay.getIntValue();
         }
      }
   }

   @EventListener
   public void onPreItemUse(PreItemUseEvent preItemUseEvent) {
      if (this.mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
         if (this.mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
            if (this.mc.crosshairTarget.getType() == Type.BLOCK) {
               BlockPos blockPos = blockHitResult.getBlockPos();
               if (BlockUtil.isBlockAtPosition(blockPos, Blocks.OBSIDIAN) || BlockUtil.isBlockAtPosition(blockPos, Blocks.BEDROCK)) {
                  preItemUseEvent.cancel();
               }
            }
         }
      }
   }

   private boolean isValidCrystalPlacement(BlockPos blockPos) {
      BlockPos up = blockPos.up();
      if (!this.mc.world.isAir(up)) {
         return false;
      } else {
         int crystalX = up.getX();
         int crystalY = up.getY();
         int crystalZ = up.getZ();
         return this.mc.world.getOtherEntities(null, new Box(crystalX, crystalY, crystalZ, crystalX + 1.0, crystalY + 2.0, crystalZ + 1.0)).isEmpty();
      }
   }
}
