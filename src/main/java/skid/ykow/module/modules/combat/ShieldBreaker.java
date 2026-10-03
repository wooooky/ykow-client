package skid.ykow.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import skid.ykow.utils.Timer;

public final class ShieldBreaker extends Module {
   private final NumberSetting maxMs = new NumberSetting(EncryptedString.of("Reaction Max MS"), 0.0, 2000.0, 200.0, 1.0);
   private final NumberSetting minMS = new NumberSetting(EncryptedString.of("Reaction Min MS"), 0.0, 2000.0, 120.0, 1.0);
   private final NumberSetting switchDelayMax = new NumberSetting(EncryptedString.of("Switch Max MS"), 0.0, 2000.0, 150.0, 1.0);
   private final NumberSetting switchDelayMin = new NumberSetting(EncryptedString.of("Switch Min MS"), 0.0, 2000.0, 80.0, 1.0);
   private final NumberSetting attackDelayMax = new NumberSetting(EncryptedString.of("Attack Max MS"), 0.0, 2000.0, 150.0, 1.0);
   private final NumberSetting attackDelayMin = new NumberSetting(EncryptedString.of("Attack Min MS"), 0.0, 2000.0, 80.0, 1.0);
   private final NumberSetting switchBackMax = new NumberSetting(EncryptedString.of("Switch Back Max MS"), 0.0, 2000.0, 180.0, 1.0);
   private final NumberSetting switchBackMin = new NumberSetting(EncryptedString.of("Switch Back Min MS"), 0.0, 2000.0, 100.0, 1.0);
   private final NumberSetting cooldownMs = new NumberSetting(EncryptedString.of("Cooldown MS"), 0.0, 2000.0, 350.0, 1.0);
   private final BooleanSetting swapBack = new BooleanSetting(EncryptedString.of("Swap Back"), true);
   private final BooleanSetting requireAxe = new BooleanSetting(EncryptedString.of("Require Axe"), true);
   private final BooleanSetting waitCooldown = new BooleanSetting(EncryptedString.of("Wait Vanilla Cooldown"), true);

   private enum Stage {
      IDLE,
      REACTION,
      SWITCH,
      ATTACK,
      SWITCH_BACK,
      COOLDOWN
   }

   private final Timer stageTimer = new Timer();
   private Stage stage = Stage.IDLE;
   private long pendingDelay = 0L;
   private int previousSlot = -1;
   private int axeSlot = -1;

   public ShieldBreaker() {
      super(
         EncryptedString.of("Shield Breaker"),
         EncryptedString.of("Uses your axe to break their shield"),
         -1,
         Category.COMBAT
      );
      this.addsettings(
         new Setting[]{
            this.maxMs,
            this.minMS,
            this.switchDelayMax,
            this.switchDelayMin,
            this.attackDelayMax,
            this.attackDelayMin,
            this.switchBackMax,
            this.switchBackMin,
            this.cooldownMs,
            this.swapBack,
            this.requireAxe,
            this.waitCooldown
         }
      );
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player == null || this.mc.world == null || this.mc.interactionManager == null) {
         return;
      }
      if (this.mc.currentScreen != null) {
         return;
      }

      this.sanitizeSettings();

      boolean shouldBreak = this.shouldBreakShield();
      if (!shouldBreak) {
         this.abort();
         return;
      }

      switch (this.stage) {
         case IDLE -> {
            this.previousSlot = this.mc.player.getInventory().selectedSlot;
            this.axeSlot = findHotbarAxeSlot();
            if (this.axeSlot == -1) {
               if (this.requireAxe.getValue()) {
                  this.enterStage(Stage.COOLDOWN, this.cooldownMs.getIntValue());
                  return;
               }
               this.axeSlot = this.previousSlot;
            }
            this.enterStage(Stage.REACTION, randomBetween(this.minMS.getIntValue(), this.maxMs.getIntValue()));
         }
         case REACTION -> {
            if (!this.stageTimer.passedMs(this.pendingDelay)) {
               return;
            }
            int fresh = findHotbarAxeSlot();
            if (fresh != -1) {
               this.axeSlot = fresh;
            }
            if (this.axeSlot == -1 && this.requireAxe.getValue()) {
               this.enterStage(Stage.COOLDOWN, this.cooldownMs.getIntValue());
               return;
            }
            if (this.axeSlot != -1 && this.mc.player.getInventory().selectedSlot != this.axeSlot) {
               InventoryUtil.swap(this.axeSlot);
            }
            this.enterStage(Stage.SWITCH, randomBetween(this.switchDelayMin.getIntValue(), this.switchDelayMax.getIntValue()));
         }
         case SWITCH -> {
            if (!this.stageTimer.passedMs(this.pendingDelay)) {
               return;
            }
            if (this.axeSlot != -1 && this.mc.player.getInventory().selectedSlot != this.axeSlot) {
               InventoryUtil.swap(this.axeSlot);
               this.enterStage(Stage.SWITCH, randomBetween(this.switchDelayMin.getIntValue(), this.switchDelayMax.getIntValue()));
               return;
            }
            this.enterStage(Stage.ATTACK, randomBetween(this.attackDelayMin.getIntValue(), this.attackDelayMax.getIntValue()));
         }
         case ATTACK -> {
            if (!this.stageTimer.passedMs(this.pendingDelay)) {
               return;
            }
            if (this.waitCooldown.getValue() && this.mc.player.getAttackCooldownProgress(0.5F) < 1.0F) {
               return;
            }
            if (this.mc.crosshairTarget instanceof EntityHitResult hitResult) {
               Entity target = hitResult.getEntity();
               try {
                  this.mc.interactionManager.attackEntity(this.mc.player, target);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
               } catch (Throwable ignored) {
               }
            }
            this.enterStage(Stage.SWITCH_BACK, randomBetween(this.switchBackMin.getIntValue(), this.switchBackMax.getIntValue()));
         }
         case SWITCH_BACK -> {
            if (!this.stageTimer.passedMs(this.pendingDelay)) {
               return;
            }
            this.restoreSlot();
            this.enterStage(Stage.COOLDOWN, this.cooldownMs.getIntValue());
         }
         case COOLDOWN -> {
            if (!this.stageTimer.passedMs(this.pendingDelay)) {
               return;
            }
            this.stage = Stage.IDLE;
         }
      }
   }

   private boolean shouldBreakShield() {
      if (!(this.mc.crosshairTarget instanceof EntityHitResult hitResult)) {
         return false;
      }
      Entity target = hitResult.getEntity();
      if (!(target instanceof PlayerEntity playerTarget)) {
         return false;
      }
      if (!playerTarget.isBlocking()) {
         return false;
      }
      if (this.mc.player.isBlocking()) {
         return false;
      }
      return !this.mc.player.isUsingItem();
   }

   private void enterStage(Stage next, long delay) {
      this.stage = next;
      this.pendingDelay = Math.max(0L, delay);
      this.stageTimer.reset();
   }

   private void abort() {
      if (this.stage == Stage.SWITCH_BACK || this.stage == Stage.ATTACK || this.stage == Stage.SWITCH) {
         this.restoreSlot();
      }
      if (this.stage != Stage.COOLDOWN) {
         this.stage = Stage.IDLE;
         this.pendingDelay = 0L;
      }
   }

   private void restoreSlot() {
      if (!this.swapBack.getValue()) {
         this.previousSlot = -1;
         return;
      }
      if (this.previousSlot != -1 && this.mc.player != null) {
         int clamped = Math.max(0, Math.min(8, this.previousSlot));
         InventoryUtil.swap(clamped);
      }
      this.previousSlot = -1;
   }

   private void sanitizeSettings() {
      if (this.minMS.getIntValue() >= this.maxMs.getIntValue()) {
         this.maxMs.setValue(this.minMS.getIntValue() + 1);
      }
      if (this.switchDelayMin.getIntValue() >= this.switchDelayMax.getIntValue()) {
         this.switchDelayMax.setValue(this.switchDelayMin.getIntValue() + 1);
      }
      if (this.attackDelayMin.getIntValue() >= this.attackDelayMax.getIntValue()) {
         this.attackDelayMax.setValue(this.attackDelayMin.getIntValue() + 1);
      }
      if (this.switchBackMin.getIntValue() >= this.switchBackMax.getIntValue()) {
         this.switchBackMax.setValue(this.switchBackMin.getIntValue() + 1);
      }
   }

   private static long randomBetween(int min, int max) {
      if (max <= min) {
         return Math.max(0, min);
      }
      return (long) (min + Math.random() * (max - min));
   }

   private int findHotbarAxeSlot() {
      if (this.mc.player == null) {
         return -1;
      }
      for (int i = 0; i < 9; i++) {
         if (this.mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
            return i;
         }
      }
      return -1;
   }

   @Override
   public void onEnable() {
      this.stage = Stage.IDLE;
      this.pendingDelay = 0L;
      this.previousSlot = -1;
      this.axeSlot = -1;
      this.stageTimer.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      try {
         this.restoreSlot();
      } catch (Throwable ignored) {
      }
      this.stage = Stage.IDLE;
      this.pendingDelay = 0L;
      this.axeSlot = -1;
      this.stageTimer.reset();
      super.onDisable();
   }
}
