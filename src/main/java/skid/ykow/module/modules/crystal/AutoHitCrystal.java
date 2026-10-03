package skid.ykow.module.modules.crystal;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.AttackEvent;
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
import skid.ykow.utils.InventoryUtil;
import skid.ykow.utils.KeyUtils;
import skid.ykow.utils.MathUtil;
import skid.ykow.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import org.lwjgl.glfw.GLFW;

public final class AutoHitCrystal extends Module {
   private final BindSetting activateKey = new BindSetting(EncryptedString.of("Activate Key"), 1, false)
      .setDescription(EncryptedString.of("Key that does hit crystalling"));
   private final BooleanSetting checkPlace = new BooleanSetting(EncryptedString.of("Check Place"), false)
      .setDescription(EncryptedString.of("Checks if you can place the obsidian on that block"));
   private final NumberSetting switchDelay = new NumberSetting(EncryptedString.of("Switch Delay"), 0.0, 20.0, 0.0, 1.0);
   private final NumberSetting switchChance = new NumberSetting(EncryptedString.of("Switch Chance"), 0.0, 100.0, 100.0, 1.0);
   private final NumberSetting placeDelay = new NumberSetting(EncryptedString.of("Place Delay"), 0.0, 20.0, 0.0, 1.0);
   private final NumberSetting placeChance = new NumberSetting(EncryptedString.of("Place Chance"), 0.0, 100.0, 100.0, 1.0);
   private final BooleanSetting workWithTotem = new BooleanSetting(EncryptedString.of("Work With Totem"), false);
   private final BooleanSetting workWithCrystal = new BooleanSetting(EncryptedString.of("Work With Crystal"), false);
   private final BooleanSetting swordSwap = new BooleanSetting(EncryptedString.of("Sword Swap"), true);
   private int placeClock = 0;
   private int switchClock = 0;
   private boolean active;
   private boolean crystalling;
   private boolean crystalSelected;

   public AutoHitCrystal() {
      super(EncryptedString.of("Auto Hit Crystal"), EncryptedString.of("Automatically hit-crystals for you"), -1, Category.CRYSTAL);
      this.addsettings(
         new Setting[]{
            this.activateKey,
            this.checkPlace,
            this.switchDelay,
            this.switchChance,
            this.placeDelay,
            this.placeChance,
            this.workWithTotem,
            this.workWithCrystal,
            this.swordSwap
         }
      );
   }

   @Override
   public void onEnable() {
      this.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      super.onDisable();
   }

   @EventListener
   public void onTick(TickEvent tickEvent) {
      int randomChance = MathUtil.randomInt(1, 100);
      if (this.mc.currentScreen == null) {
         if (KeyUtils.isKeyPressed(this.activateKey.getValue())) {
            if (this.mc.crosshairTarget instanceof BlockHitResult hitResult
               && this.mc.crosshairTarget.getType() == Type.BLOCK
               && !this.active
               && !BlockUtil.canPlaceBlockClient(hitResult.getBlockPos())
               && this.checkPlace.getValue()) {
               return;
            }

            ItemStack mainHandStack = this.mc.player.getMainHandStack();
            if (!(mainHandStack.getItem() instanceof SwordItem)
               && (!this.workWithTotem.getValue() || !mainHandStack.isOf(Items.TOTEM_OF_UNDYING))
               && (!this.workWithCrystal.getValue() || !mainHandStack.isOf(Items.END_CRYSTAL))
               && !this.active) {
               return;
            }

            if (this.mc.crosshairTarget instanceof BlockHitResult hitResult
               && !this.active
               && this.swordSwap.getValue()
               && this.mc.crosshairTarget.getType() == Type.BLOCK) {
               Block targetBlock = this.mc.world.getBlockState(hitResult.getBlockPos()).getBlock();
               this.crystalling = targetBlock == Blocks.OBSIDIAN || targetBlock == Blocks.BEDROCK;
            }

            this.active = true;
            if (!this.crystalling && this.mc.crosshairTarget instanceof BlockHitResult hitResult) {
               if (hitResult.getType() == Type.MISS) {
                  return;
               }

               if (!BlockUtil.isBlock(hitResult.getBlockPos(), Blocks.OBSIDIAN)) {
                  if (BlockUtil.isBlock(hitResult.getBlockPos(), Blocks.RESPAWN_ANCHOR) && BlockUtil.isAnchorCharged(hitResult.getBlockPos())) {
                     return;
                  }

                  this.mc.options.useKey.setPressed(false);
                  if (!this.mc.player.isHolding(Items.OBSIDIAN)) {
                     if (this.switchClock > 0) {
                        this.switchClock--;
                        return;
                     }

                     if (randomChance <= this.switchChance.getIntValue()) {
                        this.switchClock = this.switchDelay.getIntValue();
                        InventoryUtil.selectItemFromHotbar(Items.OBSIDIAN);
                     }
                  }

                  if (this.mc.player.isHolding(Items.OBSIDIAN)) {
                     if (this.placeClock > 0) {
                        this.placeClock--;
                        return;
                     }

                     randomChance = MathUtil.randomInt(1, 100);
                     if (randomChance <= this.placeChance.getIntValue()) {
                        WorldUtils.placeBlock(hitResult, true);
                        this.placeClock = this.placeDelay.getIntValue();
                        this.crystalling = true;
                     }
                  }
               }
            }

            if (this.crystalling) {
               if (!this.mc.player.isHolding(Items.END_CRYSTAL) && !this.crystalSelected) {
                  if (this.switchClock > 0) {
                     this.switchClock--;
                     return;
                  }

                  randomChance = MathUtil.randomInt(1, 100);
                  if (randomChance <= this.switchChance.getIntValue()) {
                     this.crystalSelected = InventoryUtil.selectItemFromHotbar(Items.END_CRYSTAL);
                     this.switchClock = this.switchDelay.getIntValue();
                  }
               }

               if (this.mc.player.isHolding(Items.END_CRYSTAL)) {
                  Module autoCrystalModule = Ykow.INSTANCE.getModuleManager().getModuleByClass(AutoCrystal.class);
                  if (autoCrystalModule != null && !autoCrystalModule.isEnabled()) {
                     autoCrystalModule.toggle(true);
                  }
               }
            }
         } else {
            this.reset();
         }
      }
   }

   @EventListener
   public void onItemUse(PreItemUseEvent event) {
      ItemStack mainHandStack = this.mc.player.getMainHandStack();
      if ((mainHandStack.isOf(Items.END_CRYSTAL) || mainHandStack.isOf(Items.OBSIDIAN)) && GLFW.glfwGetMouseButton(this.mc.getWindow().getHandle(), 1) != 1) {
         event.cancel();
      }
   }

   public void reset() {
      this.placeClock = this.placeDelay.getIntValue();
      this.switchClock = this.switchDelay.getIntValue();
      this.active = false;
      this.crystalling = false;
      this.crystalSelected = false;
   }

   @EventListener
   public void onAttack(AttackEvent event) {
      if (this.mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) && GLFW.glfwGetMouseButton(this.mc.getWindow().getHandle(), 0) != 1) {
         event.cancel();
      }
   }
}
