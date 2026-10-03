package skid.ykow.module.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.modules.client.Friends;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.FriendsSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.Timer;

public final class TriggerBot extends Module {
   private final NumberSetting swordThresholdMax = new NumberSetting(EncryptedString.of("Sword Threshold Max"), 0.1, 1.0, 0.95, 0.01);
   private final NumberSetting swordThresholdMin = new NumberSetting(EncryptedString.of("Sword Threshold Min"), 0.1, 1.0, 0.9, 0.01);
   private final NumberSetting axeThresholdMax = new NumberSetting(EncryptedString.of("Axe Threshold Max"), 0.1, 1.0, 0.95, 0.01);
   private final NumberSetting axeThresholdMin = new NumberSetting(EncryptedString.of("Axe Threshold Min"), 0.1, 1.0, 0.9, 0.01);
   private final NumberSetting axePostDelayMax = new NumberSetting(EncryptedString.of("Axe Post Max"), 1.0, 500.0, 120.0, 0.5);
   private final NumberSetting axePostDelayMin = new NumberSetting(EncryptedString.of("Axe Post Min"), 1.0, 500.0, 120.0, 0.5);
   private final NumberSetting reactionTimeMax = new NumberSetting(EncryptedString.of("Reaction Time Max"), 1.0, 350.0, 95.0, 0.5);
   private final NumberSetting reactionTimeMin = new NumberSetting(EncryptedString.of("Reaction Time Min"), 1.0, 350.0, 20.0, 0.5);
   private final BooleanSetting preferCrits = new BooleanSetting(EncryptedString.of("Prefer Crits"), false);
   private final NumberSetting preferCritsMin = new NumberSetting(EncryptedString.of("CritMin Cooldown"), 0.1, 1.0, 0.9, 0.01);
   private final BooleanSetting ignorePassiveMobs = new BooleanSetting(EncryptedString.of("No Passive"), true);
   private final BooleanSetting ignoreInvisible = new BooleanSetting(EncryptedString.of("No Invisible"), true);
   private final BooleanSetting ignoreCrystals = new BooleanSetting(EncryptedString.of("No Crystals"), true);
   private final BooleanSetting respectShields = new BooleanSetting(EncryptedString.of("Ignore Shields"), false);
   private final BooleanSetting useOnlySwordOrAxe = new BooleanSetting(EncryptedString.of("Only Sword/Axe"), true);
   private final BooleanSetting onlyWhenMouseDown = new BooleanSetting(EncryptedString.of("Only Mouse Hold"), false);
   private final BooleanSetting disableOnWorldChange = new BooleanSetting(EncryptedString.of("Disable on Load"), false);
   private final BooleanSetting samePlayer = new BooleanSetting(EncryptedString.of("Same Player"), false);

   private final Timer timer = new Timer();
   private final Timer samePlayerTimer = new Timer();
   private final Timer timerReactionTime = new Timer();
   public boolean waitingForDelay = false;
   private boolean waitingForReaction = false;
   private long currentReactionDelay = 0L;
   private float randomizedPostDelay = 0.0F;
   private float randomizedThreshold = 0.0F;
   private Entity target;
   private String lastTargetUUID = null;
   private Object lastWorld = null;

   public TriggerBot() {
      super(
         EncryptedString.of("Trigger Bot"),
         EncryptedString.of("Automatically attacks once aimed at a target"),
         -1,
         Category.COMBAT
      );
      this.addsettings(
         new Setting[]{
            this.swordThresholdMax,
            this.swordThresholdMin,
            this.axeThresholdMax,
            this.axeThresholdMin,
            this.axePostDelayMax,
            this.axePostDelayMin,
            this.reactionTimeMax,
            this.reactionTimeMin,
            this.ignorePassiveMobs,
            this.ignoreCrystals,
            this.respectShields,
            this.preferCrits,
            this.preferCritsMin,
            this.ignoreInvisible,
            this.onlyWhenMouseDown,
            this.useOnlySwordOrAxe,
            this.disableOnWorldChange,
            this.samePlayer
         }
      );
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.player == null || this.mc.world == null) {
         return;
      }

      // Emula o WorldChangeEvent do Volt (ykow nao tem esse evento)
      if (this.lastWorld != null && this.lastWorld != this.mc.world) {
         if (this.disableOnWorldChange.getValue() && this.isEnabled()) {
            this.toggle();
            this.lastWorld = this.mc.world;
            return;
         }
      }
      this.lastWorld = this.mc.world;

      if (this.mc.player.isUsingItem()) {
         return;
      }
      if (this.mc.currentScreen != null) {
         return;
      }

      if (this.axeThresholdMin.getFloatValue() >= this.axeThresholdMax.getFloatValue()) {
         this.axeThresholdMin.setValue(this.axeThresholdMax.getFloatValue() - 0.05F);
      }
      if (this.swordThresholdMin.getFloatValue() >= this.swordThresholdMax.getFloatValue()) {
         this.swordThresholdMin.setValue(this.swordThresholdMax.getFloatValue() - 0.05F);
      }
      if (this.axePostDelayMin.getValue() >= this.axePostDelayMax.getValue()) {
         this.axePostDelayMin.setValue(this.axePostDelayMax.getValue() - 0.5);
      }
      if (this.reactionTimeMin.getValue() >= this.reactionTimeMax.getValue()) {
         this.reactionTimeMin.setValue(this.reactionTimeMax.getValue() - 0.5);
      }

      Entity current = null;
      if (this.mc.crosshairTarget instanceof EntityHitResult hitResult) {
         current = hitResult.getEntity();
      }
      this.target = current;
      if (this.target == null) {
         return;
      }
      if (!this.isHoldingSwordOrAxe()) {
         return;
      }
      if (this.onlyWhenMouseDown.getValue() && !this.mc.options.attackKey.isPressed()) {
         return;
      }
      if (!this.hasTarget(this.target)) {
         return;
      }
      if (this.respectShields.getValue()) {
         Item item = this.mc.player.getMainHandStack().getItem();
         if (this.target instanceof PlayerEntity playerTarget && playerTarget.isBlocking() && item instanceof SwordItem) {
            return;
         }
      }
      if (this.isPreferCritReady()) {
         this.attack();
         return;
      }

      if (!this.waitingForReaction) {
         this.waitingForReaction = true;
         this.timerReactionTime.reset();
         this.currentReactionDelay = (long) randomBetween(this.reactionTimeMin.getValue(), this.reactionTimeMax.getValue());
      }
      if (this.waitingForReaction && this.timerReactionTime.passedMs(this.currentReactionDelay)) {
         this.timerReactionTime.reset();
         if (this.hasElapsedDelay()) {
            if (this.hasTarget(this.target) && this.samePlayerCheck(this.target)) {
               this.attack();
               this.waitingForReaction = false;
            }
         }
      }
   }

   public boolean hasTarget(Entity en) {
      if (en == null || this.mc.player == null) {
         return false;
      }
      if (en == this.mc.player || en == this.mc.cameraEntity || !en.isAlive()) {
         return false;
      }
      if (en instanceof PlayerEntity player) {
         if (isFriend(player)) {
            return false;
         }
         // Substitui o Teams.isTeammate do Volt pelo check vanilla de scoreboard
         try {
            if (this.mc.player.isTeammate(en)) {
               return false;
            }
         } catch (Throwable ignored) {
         }
      }
      if (en instanceof EndCrystalEntity && this.ignoreCrystals.getValue()) {
         return false;
      }
      if (en instanceof TameableEntity) {
         return false;
      }
      if (en instanceof PassiveEntity && this.ignorePassiveMobs.getValue()) {
         return false;
      }
      return !this.ignoreInvisible.getValue() || !en.isInvisible();
   }

   private boolean isPreferCritReady() {
      if (!this.preferCrits.getValue()) {
         return false;
      }
      if (this.mc.player == null) {
         return false;
      }
      boolean isFalling = this.mc.player.getVelocity().y < -0.08F;
      boolean isSneaking = this.mc.player.isSneaking();
      boolean isOnGround = this.mc.player.isOnGround();
      boolean isUsingItem = this.mc.player.isUsingItem();
      boolean hasBlindness = this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
      boolean isRiding = this.mc.player.getVehicle() != null;
      boolean isInWater = this.mc.player.isTouchingWater();
      boolean isInLava = this.mc.player.isInLava();
      boolean isCooldownCharged = this.mc.player.getAttackCooldownProgress(0.0F) >= this.preferCritsMin.getFloatValue();
      return isFalling && !isSneaking && !isOnGround && !isUsingItem && !hasBlindness && !isRiding && !isInWater && !isInLava && isCooldownCharged;
   }

   private boolean samePlayerCheck(Entity entity) {
      if (!this.samePlayer.getValue()) {
         return true;
      }
      if (entity == null) {
         return false;
      }
      if (this.samePlayerTimer.passedMs(3000L)) {
         this.lastTargetUUID = null;
         return true;
      }
      if (this.lastTargetUUID == null) {
         return true;
      }
      return entity.getUuidAsString().equals(this.lastTargetUUID);
   }

   private boolean hasElapsedDelay() {
      if (this.isPreferCritReady()) {
         return false;
      }
      if (this.mc.player == null) {
         return false;
      }
      Item heldItem = this.mc.player.getMainHandStack().getItem();
      float cooldown = this.mc.player.getAttackCooldownProgress(0.0F);

      if (heldItem instanceof AxeItem) {
         if (!this.waitingForDelay) {
            this.randomizedThreshold = (float) randomBetween(this.axeThresholdMin.getFloatValue(), this.axeThresholdMax.getFloatValue());
            this.randomizedPostDelay = (float) randomBetween(this.axePostDelayMin.getValue(), this.axePostDelayMax.getValue());
            this.waitingForDelay = true;
         }
         if (cooldown >= this.randomizedThreshold) {
            if (this.timer.passedMs((long) this.randomizedPostDelay)) {
               this.timer.reset();
               this.waitingForDelay = false;
               return true;
            }
         } else {
            this.timer.reset();
         }
         return false;
      } else {
         float swordDelay = (float) randomBetween(this.swordThresholdMin.getFloatValue(), this.swordThresholdMax.getFloatValue());
         return cooldown >= swordDelay;
      }
   }

   private boolean isHoldingSwordOrAxe() {
      if (!this.useOnlySwordOrAxe.getValue()) {
         return true;
      }
      if (this.mc.player == null) {
         return false;
      }
      Item item = this.mc.player.getMainHandStack().getItem();
      return item instanceof AxeItem || item instanceof SwordItem;
   }

   public void attack() {
      if (this.mc.player == null || this.mc.interactionManager == null || this.target == null) {
         return;
      }
      try {
         this.mc.interactionManager.attackEntity(this.mc.player, this.target);
         this.mc.player.swingHand(Hand.MAIN_HAND);
      } catch (Throwable ignored) {
      }
      if (this.samePlayer.getValue() && this.target != null) {
         this.lastTargetUUID = this.target.getUuidAsString();
         this.samePlayerTimer.reset();
      }
      this.waitingForDelay = false;
   }

   private static double randomBetween(double min, double max) {
      if (max <= min) {
         return Math.max(0.0, min);
      }
      return min + Math.random() * (max - min);
   }

   private static boolean isFriend(PlayerEntity player) {
      try {
         if (Ykow.INSTANCE == null || Ykow.INSTANCE.getModuleManager() == null) {
            return false;
         }
         Module friendsModule = Ykow.INSTANCE.getModuleManager().getModuleByClass(Friends.class);
         if (friendsModule == null) {
            return false;
         }
         String name = player.getGameProfile().getName();
         for (Setting s : friendsModule.getSettings()) {
            if (s instanceof FriendsSetting fs && fs.isFriend(name)) {
               return true;
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }

   @Override
   public void onEnable() {
      this.timer.reset();
      this.timerReactionTime.reset();
      this.samePlayerTimer.reset();
      this.waitingForReaction = false;
      this.waitingForDelay = false;
      this.lastTargetUUID = null;
      this.lastWorld = this.mc != null ? this.mc.world : null;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.timer.reset();
      this.timerReactionTime.reset();
      this.waitingForReaction = false;
      this.waitingForDelay = false;
      super.onDisable();
   }
}
