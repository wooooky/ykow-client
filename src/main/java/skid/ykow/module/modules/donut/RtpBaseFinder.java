package skid.ykow.module.modules.donut;

import skid.ykow.Ykow;
import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.mixin.MobSpawnerLogicAccessor;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.modules.crystal.AutoTotem;
import skid.ykow.module.modules.misc.AutoEat;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.ModeSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.BlockUtil;
import skid.ykow.utils.EnchantmentUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.InventoryUtil;
import skid.ykow.utils.embed.DiscordWebhook;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class RtpBaseFinder extends Module {
   public final ModeSetting<RtpBaseFinder.Mode> mode = new ModeSetting<>(EncryptedString.of("Mode"), RtpBaseFinder.Mode.RANDOM, RtpBaseFinder.Mode.class);
   private final BooleanSetting spawn = new BooleanSetting(EncryptedString.of("Spawners"), true);
   private final NumberSetting minStorage = new NumberSetting(EncryptedString.of("Minimum Storage"), 1.0, 500.0, 100.0, 1.0);
   private final BooleanSetting autoTotemBuy = new BooleanSetting(EncryptedString.of("Auto Totem Buy"), true);
   private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 8.0, 1.0);
   private final BooleanSetting autoMend = new BooleanSetting(EncryptedString.of("Auto Mend"), true)
      .setDescription(EncryptedString.of("Automatically repairs pickaxe."));
   private final NumberSetting xpBottleSlot = new NumberSetting(EncryptedString.of("XP Bottle Slot"), 1.0, 9.0, 9.0, 1.0);
   private final BooleanSetting discordNotification = new BooleanSetting(EncryptedString.of("Discord Notification"), false);
   private final StringSetting webhook = new StringSetting(EncryptedString.of("Webhook"), "");
   private final BooleanSetting totemCheck = new BooleanSetting(EncryptedString.of("Totem Check"), true);
   private final NumberSetting totemCheckTime = new NumberSetting(EncryptedString.of("Totem Check Time"), 1.0, 120.0, 20.0, 1.0);
   private final NumberSetting digToY = new NumberSetting(EncryptedString.of("Dig To Y"), -59.0, 30.0, -20.0, 1.0);
   private Vec3d currentPosition;
   private Vec3d previousPosition;
   private double idleTime;
   private double totemCheckCounter = 0.0;
   private boolean isDigging = false;
   private boolean shouldDig = false;
   private boolean isRepairing = false;
   private boolean isBuyingTotem = false;
   private int selectedSlot = 0;
   private int rtpCooldown = 0;
   private int actionDelay = 0;
   private int totemBuyCounter = 0;
   private int spawnerCounter = 0;

   public RtpBaseFinder() {
      super(EncryptedString.of("Rtp Base Finder"), EncryptedString.of("Automatically searches for bases on DonutSMP"), -1, Category.DONUT);
      this.addsettings(
         new Setting[]{
            this.mode,
            this.spawn,
            this.minStorage,
            this.autoTotemBuy,
            this.totemSlot,
            this.autoMend,
            this.xpBottleSlot,
            this.discordNotification,
            this.webhook,
            this.totemCheck,
            this.totemCheckTime,
            this.digToY
         }
      );
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
   public void onTick(TickEvent event) {
      if (this.mc.player != null) {
         if (this.actionDelay > 0) {
            this.actionDelay--;
         } else {
            this.scanForEntities();
            if (this.autoTotemBuy.getValue()) {
               int totemSlotIndex = this.totemSlot.getIntValue() - 1;
               if (!this.mc.player.getInventory().getStack(totemSlotIndex).isOf(Items.TOTEM_OF_UNDYING)) {
                  if (this.totemBuyCounter < 30 && !this.isBuyingTotem) {
                     this.totemBuyCounter++;
                     return;
                  }

                  this.totemBuyCounter = 0;
                  this.isBuyingTotem = true;
                  if (this.mc.player.getInventory().selectedSlot != totemSlotIndex) {
                     InventoryUtil.swap(totemSlotIndex);
                  }

                  ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
                  if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler
                     && ((GenericContainerScreenHandler)currentScreenHandler).getRows() == 3) {
                     if (currentScreenHandler.getSlot(11).getStack().isOf(Items.END_STONE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                        this.actionDelay = 10;
                        return;
                     }

                     if (currentScreenHandler.getSlot(16).getStack().isOf(Items.EXPERIENCE_BOTTLE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                        this.actionDelay = 10;
                        return;
                     }

                     this.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, Direction.DOWN));
                     if (currentScreenHandler.getSlot(23).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 23, 0, SlotActionType.PICKUP, this.mc.player);
                        this.actionDelay = 10;
                        return;
                     }

                     this.mc.getNetworkHandler().sendChatCommand("shop");
                     this.actionDelay = 10;
                     return;
                  }

                  this.mc.getNetworkHandler().sendChatCommand("shop");
                  this.actionDelay = 10;
                  return;
               }

               if (this.isBuyingTotem) {
                  if (this.mc.currentScreen != null) {
                     this.mc.player.closeHandledScreen();
                     this.actionDelay = 20;
                  }

                  this.isBuyingTotem = false;
                  this.totemBuyCounter = 0;
               }
            }

            if (this.isRepairing) {
               int xpBottleSlotIndex = this.xpBottleSlot.getIntValue() - 1;
               ItemStack xpBottleStack = this.mc.player.getInventory().getStack(xpBottleSlotIndex);
               if (this.mc.player.getInventory().selectedSlot != xpBottleSlotIndex) {
                  InventoryUtil.swap(xpBottleSlotIndex);
               }

               if (!xpBottleStack.isOf(Items.EXPERIENCE_BOTTLE)) {
                  ScreenHandler screenHandler = this.mc.player.currentScreenHandler;
                  if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)
                     || ((GenericContainerScreenHandler)screenHandler).getRows() != 3) {
                     this.mc.getNetworkHandler().sendChatCommand("shop");
                     this.actionDelay = 10;
                     return;
                  }

                  if (screenHandler.getSlot(11).getStack().isOf(Items.END_STONE)) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                     this.actionDelay = 10;
                     return;
                  }

                  if (screenHandler.getSlot(16).getStack().isOf(Items.EXPERIENCE_BOTTLE)) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 16, 0, SlotActionType.PICKUP, this.mc.player);
                     this.actionDelay = 10;
                     return;
                  }

                  if (screenHandler.getSlot(17).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 17, 0, SlotActionType.PICKUP, this.mc.player);
                     this.actionDelay = 10;
                     return;
                  }

                  this.mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, Direction.DOWN));
                  if (screenHandler.getSlot(23).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                     this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 23, 0, SlotActionType.PICKUP, this.mc.player);
                     this.actionDelay = 10;
                     return;
                  }

                  this.mc.getNetworkHandler().sendChatCommand("shop");
                  this.actionDelay = 10;
               } else {
                  if (this.mc.currentScreen != null) {
                     this.mc.player.closeHandledScreen();
                     this.actionDelay = 20;
                     return;
                  }

                  if (!EnchantmentUtil.hasEnchantment(this.mc.player.getOffHandStack(), Enchantments.MENDING)) {
                     this.mc
                        .interactionManager
                        .clickSlot(this.mc.player.currentScreenHandler.syncId, 36 + this.selectedSlot, 40, SlotActionType.SWAP, this.mc.player);
                     this.actionDelay = 20;
                     return;
                  }

                  if (this.mc.player.getOffHandStack().getDamage() > 0) {
                     ActionResult interactItem = this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                     if (interactItem.isAccepted() && interactItem.shouldSwingHand()) {
                        this.mc.player.swingHand(Hand.MAIN_HAND);
                     }

                     this.actionDelay = 1;
                     return;
                  }

                  this.mc
                     .interactionManager
                     .clickSlot(this.mc.player.currentScreenHandler.syncId, 36 + this.selectedSlot, 40, SlotActionType.SWAP, this.mc.player);
                  this.isRepairing = false;
               }
            } else {
               if (this.shouldDig) {
                  this.handleAutoEat();
               }

               if (this.totemCheck.getValue()) {
                  boolean equals = this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING);
                  Module moduleByClass = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoTotem.class);
                  if (equals) {
                     this.totemCheckCounter = 0.0;
                  } else if (moduleByClass.isEnabled() && ((AutoTotem)moduleByClass).findItemSlot(Items.TOTEM_OF_UNDYING) != -1) {
                     this.totemCheckCounter = 0.0;
                  } else {
                     this.totemCheckCounter++;
                  }

                  if (this.totemCheckCounter > this.totemCheckTime.getValue()) {
                     this.notifyTotemExplosion("Your totem exploded", (int)this.mc.player.getX(), (int)this.mc.player.getY(), (int)this.mc.player.getZ());
                     return;
                  }
               }

               if (this.rtpCooldown > 0) {
                  this.rtpCooldown--;
                  if (this.rtpCooldown < 1) {
                     if (this.previousPosition != null && this.previousPosition.distanceTo(this.mc.player.getPos()) < 100.0) {
                        this.sendRtpCommand();
                        return;
                     }

                     this.mc.player.setPitch(89.9F);
                     if (this.autoMend.getValue()) {
                        ItemStack size = this.mc.player.getMainHandStack();
                        if (EnchantmentUtil.hasEnchantment(size, Enchantments.MENDING) && size.getMaxDamage() - size.getDamage() < 100) {
                           this.isRepairing = true;
                           this.selectedSlot = this.mc.player.getInventory().selectedSlot;
                        }
                     }

                     this.shouldDig = true;
                  }

                  return;
               }

               if (this.currentPosition != null && this.currentPosition.distanceTo(this.mc.player.getPos()) < 2.0) {
                  this.idleTime++;
               } else {
                  this.currentPosition = this.mc.player.getPos();
                  this.idleTime = 0.0;
               }

               if (this.idleTime > 20.0 && this.isDigging) {
                  this.sendRtpCommand();
                  this.isDigging = false;
                  return;
               }

               if (this.idleTime > 200.0) {
                  this.sendRtpCommand();
                  this.idleTime = 0.0;
                  return;
               }

               if (this.mc.player.getY() < this.digToY.getIntValue() && !this.isDigging) {
                  this.isDigging = true;
                  this.shouldDig = false;
               }
            }
         }
      }
   }

   private void sendRtpCommand() {
      this.shouldDig = false;
      ClientPlayNetworkHandler networkHandler = this.mc.getNetworkHandler();
      RtpBaseFinder.Mode l;
      if (this.mode.getValue() == RtpBaseFinder.Mode.RANDOM) {
         l = this.getRandomMode();
      } else {
         l = (RtpBaseFinder.Mode)this.mode.getValue();
      }

      networkHandler.sendChatCommand("rtp " + this.getModeName(l));
      this.rtpCooldown = 150;
      this.idleTime = 0.0;
      this.previousPosition = new Vec3d(this.mc.player.getPos().toVector3f());
   }

   private void disconnectWithMessage(Text text) {
      MutableText literal = Text.literal("[RTPBaseFinder] ");
      literal.append(text);
      this.toggle();
      this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(literal));
   }

   private RtpBaseFinder.Mode getRandomMode() {
      RtpBaseFinder.Mode[] array = new RtpBaseFinder.Mode[]{
         RtpBaseFinder.Mode.EUCENTRAL,
         RtpBaseFinder.Mode.EUWEST,
         RtpBaseFinder.Mode.EAST,
         RtpBaseFinder.Mode.WEST,
         RtpBaseFinder.Mode.ASIA,
         RtpBaseFinder.Mode.OCEANIA
      };
      return array[new Random().nextInt(array.length)];
   }

   private String getModeName(RtpBaseFinder.Mode mode) {
      int n = mode.ordinal() ^ 1886013532;
      int n2;
      if (n != 0) {
         n2 = (n * 31 >>> 4) % n ^ n >>> 16;
      } else {
         n2 = 0;
      }

      String name = null;

      return switch (n2) {
         case 164469848 -> "eu central";
         case 164469854 -> "eu west";
         default -> mode.name();
      };
   }

   private void handleAutoEat() {
      Module moduleByClass = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEat.class);
      if (!moduleByClass.isEnabled()) {
         this.handleBlockBreaking(true);
      } else if (!((AutoEat)moduleByClass).shouldEat()) {
         this.handleBlockBreaking(true);
      }
   }

   private void handleBlockBreaking(boolean b) {
      if (this.mc.player.getPitch() != 89.9F) {
         this.mc.player.setPitch(89.9F);
      }

      if (!this.mc.player.isUsingItem()) {
         if (b && this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.crosshairTarget;
            BlockPos blockPos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos();
            if (!this.mc.world.getBlockState(blockPos).isAir()) {
               Direction side = blockHitResult.getSide();
               if (this.mc.interactionManager.updateBlockBreakingProgress(blockPos, side)) {
                  this.mc.particleManager.addBlockBreakingParticles(blockPos, side);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
               }
            }
         } else {
            this.mc.interactionManager.cancelBlockBreaking();
         }
      }
   }

   private void scanForEntities() {
      int n = 0;
      int n2 = 0;
      BlockPos blockPos = null;
      Iterator iterator = BlockUtil.getLoadedChunks().iterator();

      while (iterator.hasNext()) {
         for (Object next : ((WorldChunk)iterator.next()).getBlockEntityPositions()) {
            BlockEntity getBlockEntity = this.mc.world.getBlockEntity((BlockPos)next);
            if (this.spawn.getValue() && getBlockEntity instanceof MobSpawnerBlockEntity) {
               String string = ((MobSpawnerLogicAccessor)((MobSpawnerBlockEntity)getBlockEntity).getLogic()).getSpawnEntry().getNbt().getString("id");
               if (string != "minecraft:cave_spider" && string != "minecraft:spider") {
                  n2++;
                  blockPos = (BlockPos)next;
               }
            }

            if (getBlockEntity instanceof ChestBlockEntity
               || getBlockEntity instanceof EnderChestBlockEntity
               || getBlockEntity instanceof ShulkerBoxBlockEntity
               || getBlockEntity instanceof FurnaceBlockEntity
               || getBlockEntity instanceof BarrelBlockEntity
               || getBlockEntity instanceof EnchantingTableBlockEntity) {
               n++;
            }
         }
      }

      if (n2 > 0) {
         this.spawnerCounter++;
      } else {
         this.spawnerCounter = 0;
      }

      if (this.spawnerCounter > 10) {
         this.notifyBaseOrSpawner("YOU FOUND SPAWNER", blockPos.getX(), blockPos.getY(), blockPos.getZ(), false);
         this.spawnerCounter = 0;
      }

      if (n > this.minStorage.getIntValue()) {
         this.notifyBaseOrSpawner("YOU FOUND BASE", (int)this.mc.player.getPos().x, (int)this.mc.player.getPos().y, (int)this.mc.player.getPos().z, true);
      }
   }

   private void notifyBaseOrSpawner(String s, int n, int n2, int n3, boolean b) {
      String s2;
      if (b) {
         s2 = "Base";
      } else {
         s2 = "Spawner";
      }

      if (this.discordNotification.getValue()) {
         DiscordWebhook embedSender = new DiscordWebhook(this.webhook.value);
         DiscordWebhook.EmbedObject bn = new DiscordWebhook.EmbedObject();
         bn.setTitle(s2);
         bn.setThumbnail("https://render.crafty.gg/3d/bust/" + MinecraftClient.getInstance().getSession().getUuidOrNull() + "?format=webp");
         bn.setDescription(s2 + " Found - " + MinecraftClient.getInstance().getSession().getUsername());
         bn.setColor(Color.GRAY);
         bn.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
         bn.addField(s2 + "Found at", "x: " + n + " y: " + n2 + " z: " + n3, true);
         embedSender.addEmbed(bn);

         try {
            embedSender.execute();
         } catch (Throwable var10) {
         }
      }

      this.toggle();
      this.disconnectWithMessage(Text.of(s));
   }

   private void notifyTotemExplosion(String s, int n, int n2, int n3) {
      if (this.discordNotification.getValue()) {
         DiscordWebhook embedSender = new DiscordWebhook(this.webhook.value);
         DiscordWebhook.EmbedObject bn = new DiscordWebhook.EmbedObject();
         bn.setTitle("Totem Exploded");
         bn.setThumbnail("https://render.crafty.gg/3d/bust/" + MinecraftClient.getInstance().getSession().getUuidOrNull() + "?format=webp");
         bn.setDescription("Your Totem Exploded - " + MinecraftClient.getInstance().getSession().getUsername());
         bn.setColor(Color.RED);
         bn.setFooter(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), null);
         bn.addField("Location", "x: " + n + " y: " + n2 + " z: " + n3, true);
         embedSender.addEmbed(bn);

         try {
            embedSender.execute();
         } catch (Throwable var8) {
         }
      }

      this.disconnectWithMessage(Text.of(s));
   }

   public boolean isRepairingActive() {
      return this.isRepairing;
   }

   static enum Mode {
      EUCENTRAL("eucentral", 0),
      EUWEST("euwest", 1),
      EAST("east", 2),
      WEST("west", 3),
      ASIA("asia", 4),
      OCEANIA("oceania", 5),
      RANDOM("random", 6);

      private Mode(final String name, final int ordinal) {
      }
   }
}
