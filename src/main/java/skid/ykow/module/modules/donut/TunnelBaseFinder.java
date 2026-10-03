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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public final class TunnelBaseFinder extends Module {
   private final NumberSetting minimumStorage = new NumberSetting(EncryptedString.of("Minimum Storage"), 1.0, 500.0, 100.0, 1.0);
   private final BooleanSetting spawners = new BooleanSetting(EncryptedString.of("Spawners"), true);
   private final BooleanSetting autoTotemBuy = new BooleanSetting(EncryptedString.of("Auto Totem Buy"), true);
   private final NumberSetting totemSlot = new NumberSetting(EncryptedString.of("Totem Slot"), 1.0, 9.0, 8.0, 1.0);
   private final BooleanSetting autoMend = new BooleanSetting(EncryptedString.of("Auto Mend"), true)
      .setDescription(EncryptedString.of("Automatically repairs pickaxe."));
   private final NumberSetting xpBottleSlot = new NumberSetting(EncryptedString.of("XP Bottle Slot"), 1.0, 9.0, 9.0, 1.0);
   private final BooleanSetting discordNotification = new BooleanSetting(EncryptedString.of("Discord Notification"), false);
   private final StringSetting webhook = new StringSetting(EncryptedString.of("Webhook"), "");
   private final BooleanSetting totemCheck = new BooleanSetting(EncryptedString.of("Totem Check"), true);
   private final NumberSetting totemCheckTime = new NumberSetting(EncryptedString.of("Totem Check Time"), 1.0, 120.0, 20.0, 1.0);
   private TunnelBaseFinder.Direction currentDirection;
   private int blocksMined;
   private int spawnerCount;
   private int idleTicks;
   private Vec3d lastPosition;
   private boolean isDigging = false;
   private boolean shouldDig = false;
   private int totemCheckCounter = 0;
   private int totemBuyCounter = 0;
   private double actionDelay = 0.0;
   private int miningTicks = 0;
   private BlockPos currentMiningBlock = null;
   private int postBreakDelay = 0;

   public TunnelBaseFinder() {
      super(EncryptedString.of("Tunnel Base Finder"), EncryptedString.of("Finds bases digging tunnel"), -1, Category.DONUT);
      this.addsettings(
         new Setting[]{
            this.minimumStorage,
            this.spawners,
            this.autoTotemBuy,
            this.totemSlot,
            this.autoMend,
            this.xpBottleSlot,
            this.discordNotification,
            this.webhook,
            this.totemCheck,
            this.totemCheckTime
         }
      );
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.currentDirection = this.getInitialDirection();
      this.blocksMined = 0;
      this.idleTicks = 0;
      this.spawnerCount = 0;
      this.lastPosition = null;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.mc.options.leftKey.setPressed(false);
      this.mc.options.rightKey.setPressed(false);
      this.mc.options.forwardKey.setPressed(false);
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.currentDirection != null) {
         Module moduleByClass = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoEat.class);
         if (!moduleByClass.isEnabled() || !((AutoEat)moduleByClass).shouldEat()) {
            int n = (this.calculateDirection(this.currentDirection) + 90 * this.idleTicks) % 360;
            if (this.mc.player.getYaw() != n) {
               this.mc.player.setYaw(n);
            }

            if (this.mc.player.getPitch() != 2.0F) {
               this.mc.player.setPitch(2.0F);
            }

            this.updateDirection(this.getInitialDirection());
            if (this.blocksMined > 0) {
               this.mc.options.forwardKey.setPressed(false);
               this.blocksMined--;
            } else {
               this.notifyFound();
               if (this.autoTotemBuy.getValue()) {
                  int n2 = this.totemSlot.getIntValue() - 1;
                  if (!this.mc.player.getInventory().getStack(n2).isOf(Items.TOTEM_OF_UNDYING)) {
                     if (this.totemBuyCounter < 30 && !this.shouldDig) {
                        this.totemBuyCounter++;
                        return;
                     }

                     this.totemBuyCounter = 0;
                     this.shouldDig = true;
                     if (this.mc.player.getInventory().selectedSlot != n2) {
                        InventoryUtil.swap(n2);
                     }

                     ScreenHandler currentScreenHandler = this.mc.player.currentScreenHandler;
                     if (this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler
                        && ((GenericContainerScreenHandler)currentScreenHandler).getRows() == 3) {
                        if (currentScreenHandler.getSlot(11).getStack().isOf(Items.END_STONE)) {
                           this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                           this.blocksMined = 10;
                           return;
                        }

                        if (currentScreenHandler.getSlot(16).getStack().isOf(Items.EXPERIENCE_BOTTLE)) {
                           this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                           this.blocksMined = 10;
                           return;
                        }

                        this.mc
                           .player
                           .networkHandler
                           .sendPacket(new PlayerActionC2SPacket(Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, net.minecraft.util.math.Direction.DOWN));
                        if (currentScreenHandler.getSlot(23).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                           this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 23, 0, SlotActionType.PICKUP, this.mc.player);
                           this.blocksMined = 10;
                           return;
                        }

                        this.mc.getNetworkHandler().sendChatCommand("shop");
                        this.blocksMined = 10;
                        return;
                     }

                     this.mc.getNetworkHandler().sendChatCommand("shop");
                     this.blocksMined = 10;
                     return;
                  }

                  if (this.shouldDig) {
                     if (this.mc.currentScreen != null) {
                        this.mc.player.closeHandledScreen();
                        this.blocksMined = 20;
                     }

                     this.shouldDig = false;
                     this.totemBuyCounter = 0;
                  }
               }

               if (this.isDigging) {
                  int n3 = this.xpBottleSlot.getIntValue() - 1;
                  ItemStack getStack = this.mc.player.getInventory().getStack(n3);
                  if (this.mc.player.getInventory().selectedSlot != n3) {
                     InventoryUtil.swap(n3);
                  }

                  if (!getStack.isOf(Items.EXPERIENCE_BOTTLE)) {
                     ScreenHandler fishHook = this.mc.player.currentScreenHandler;
                     if (!(this.mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)
                        || ((GenericContainerScreenHandler)fishHook).getRows() != 3) {
                        this.mc.getNetworkHandler().sendChatCommand("shop");
                        this.blocksMined = 10;
                        return;
                     }

                     if (fishHook.getSlot(11).getStack().isOf(Items.END_STONE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 13, 0, SlotActionType.PICKUP, this.mc.player);
                        this.blocksMined = 10;
                        return;
                     }

                     if (fishHook.getSlot(16).getStack().isOf(Items.EXPERIENCE_BOTTLE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 16, 0, SlotActionType.PICKUP, this.mc.player);
                        this.blocksMined = 10;
                        return;
                     }

                     if (fishHook.getSlot(17).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 17, 0, SlotActionType.PICKUP, this.mc.player);
                        this.blocksMined = 10;
                        return;
                     }

                     this.mc
                        .player
                        .networkHandler
                        .sendPacket(new PlayerActionC2SPacket(Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, net.minecraft.util.math.Direction.DOWN));
                     if (fishHook.getSlot(23).getStack().isOf(Items.LIME_STAINED_GLASS_PANE)) {
                        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 23, 0, SlotActionType.PICKUP, this.mc.player);
                        this.blocksMined = 10;
                        return;
                     }

                     this.mc.getNetworkHandler().sendChatCommand("shop");
                     this.blocksMined = 10;
                  } else {
                     if (this.mc.currentScreen != null) {
                        this.mc.player.closeHandledScreen();
                        this.blocksMined = 20;
                        return;
                     }

                     if (!EnchantmentUtil.hasEnchantment(this.mc.player.getOffHandStack(), Enchantments.MENDING)) {
                        this.mc
                           .interactionManager
                           .clickSlot(this.mc.player.currentScreenHandler.syncId, 36 + this.totemCheckCounter, 40, SlotActionType.SWAP, this.mc.player);
                        this.blocksMined = 20;
                        return;
                     }

                     if (this.mc.player.getOffHandStack().getDamage() > 0) {
                        ActionResult interactItem = this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                        if (interactItem.isAccepted() && interactItem.shouldSwingHand()) {
                           this.mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        this.blocksMined = 1;
                        return;
                     }

                     this.mc
                        .interactionManager
                        .clickSlot(this.mc.player.currentScreenHandler.syncId, 36 + this.totemCheckCounter, 40, SlotActionType.SWAP, this.mc.player);
                     this.isDigging = false;
                  }
               } else {
                  if (this.autoMend.getValue()) {
                     ItemStack size = this.mc.player.getMainHandStack();
                     if (EnchantmentUtil.hasEnchantment(size, Enchantments.MENDING) && size.getMaxDamage() - size.getDamage() < 100) {
                        this.isDigging = true;
                        this.totemCheckCounter = this.mc.player.getInventory().selectedSlot;
                     }
                  }

                  if (this.totemCheck.getValue()) {
                     boolean equals = this.mc.player.getOffHandStack().getItem().equals(Items.TOTEM_OF_UNDYING);
                     Module moduleByClass2 = Ykow.INSTANCE.MODULE_MANAGER.getModuleByClass(AutoTotem.class);
                     if (equals) {
                        this.actionDelay = 0.0;
                     } else if (moduleByClass2.isEnabled() && ((AutoTotem)moduleByClass2).findItemSlot(Items.TOTEM_OF_UNDYING) != -1) {
                        this.actionDelay = 0.0;
                     } else {
                        this.actionDelay++;
                     }

                     if (this.actionDelay > this.totemCheckTime.getValue()) {
                        this.notifyTotemExploded("Your totem exploded", (int)this.mc.player.getX(), (int)this.mc.player.getY(), (int)this.mc.player.getZ());
                        return;
                     }
                  }

                  boolean isBlockValid = false;
                  HitResult crosshairTarget = this.mc.crosshairTarget;
                  if (this.mc.crosshairTarget instanceof BlockHitResult) {
                     BlockPos blockPos = ((BlockHitResult)crosshairTarget).getBlockPos();
                     if (!BlockUtil.isBlockAtPosition(blockPos, Blocks.AIR)) {
                        isBlockValid = this.isBlockPositionValid(blockPos, this.mc.player.getHorizontalFacing());
                     }
                  }

                  if (isBlockValid) {
                     this.handleBlockBreaking(true);
                  }

                  boolean isBlockInDirection = this.isBlockInDirection(this.mc.player.getHorizontalFacing(), 3);
                  boolean isBlockTooFar = false;
                  HitResult crosshairTarget2 = this.mc.crosshairTarget;
                  if (this.mc.crosshairTarget instanceof BlockHitResult) {
                     isBlockTooFar = this.mc.player.getCameraPosVec(1.0F).distanceTo(Vec3d.ofCenter(((BlockHitResult)crosshairTarget2).getBlockPos())) > 3.0;
                  }

                  if (!isBlockValid && (!isBlockTooFar || !isBlockInDirection)) {
                     this.idleTicks++;
                     this.lastPosition = this.mc.player.getPos();
                     this.blocksMined = 5;
                     return;
                  }

                  this.mc.options.forwardKey.setPressed(isBlockInDirection && isBlockTooFar);
                  if (this.idleTicks > 0 && this.lastPosition != null && this.mc.player.getPos().distanceTo(this.lastPosition) > 1.0) {
                     this.lastPosition = this.mc.player.getPos();
                     net.minecraft.util.math.Direction rotateYCounterclockwise = this.mc.player.getHorizontalFacing().rotateYCounterclockwise();
                     BlockPos blockPos2 = this.mc.player.getBlockPos().up().offset(rotateYCounterclockwise);

                     for (int i = 0; i < 5; i++) {
                        blockPos2 = blockPos2.offset(rotateYCounterclockwise);
                        if (!this.mc.world.getBlockState(blockPos2).getBlock().equals(Blocks.AIR)) {
                           if (this.isBlockPositionValid(blockPos2, rotateYCounterclockwise)
                              && this.isBlockPositionValid(blockPos2.offset(rotateYCounterclockwise), rotateYCounterclockwise)) {
                              this.idleTicks--;
                              this.blocksMined = 5;
                           }

                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private int calculateDirection(TunnelBaseFinder.Direction enum4) {
      if (enum4 == TunnelBaseFinder.Direction.NORTH) {
         return 180;
      } else if (enum4 == TunnelBaseFinder.Direction.SOUTH) {
         return 0;
      } else if (enum4 == TunnelBaseFinder.Direction.EAST) {
         return 270;
      } else {
         return enum4 == TunnelBaseFinder.Direction.WEST ? 90 : Math.round(this.mc.player.getYaw());
      }
   }

   private boolean isBlockInDirection(net.minecraft.util.math.Direction direction, int n) {
      BlockPos down = this.mc.player.getBlockPos().down();
      BlockPos getBlockPos = this.mc.player.getBlockPos();

      for (int i = 0; i < n; i++) {
         BlockPos offset = down.offset(direction, i);
         BlockPos offset2 = getBlockPos.offset(direction, i);
         if (this.mc.world.getBlockState(offset).isAir() || !this.isBlockPositionSafe(offset)) {
            return false;
         }

         if (!this.mc.world.getBlockState(offset2).isAir()) {
            return false;
         }
      }

      return true;
   }

   private boolean isBlockPositionValid(BlockPos blockPos, net.minecraft.util.math.Direction direction) {
      BlockPos offset = blockPos.offset(direction);
      net.minecraft.util.math.Direction rotateYClockwise = direction.rotateYClockwise();
      net.minecraft.util.math.Direction up = net.minecraft.util.math.Direction.UP;
      BlockPos offset2 = blockPos.offset(net.minecraft.util.math.Direction.UP, 2);
      BlockPos offset3 = blockPos.offset(net.minecraft.util.math.Direction.DOWN, -2);
      BlockPos offset4 = offset2.offset(rotateYClockwise, -1);
      if (!this.isBlockPositionSafe(offset4) || this.mc.world.getBlockState(offset4).getBlock() == Blocks.GRAVEL) {
         return false;
      } else if (!this.isBlockPositionSafe(offset3.offset(rotateYClockwise, -1))) {
         return false;
      } else {
         BlockPos offset5 = blockPos.offset(rotateYClockwise, 2);
         BlockPos offset6 = blockPos.offset(rotateYClockwise, -2);
         if (!this.isBlockPositionSafe(offset5.offset(up, -1))) {
            return false;
         } else {
            return !this.isBlockPositionSafe(offset6.offset(up, -1)) ? false : this.isBlockPositionSafe(offset.offset(rotateYClockwise, -1).offset(up, -1));
         }
      }
   }

   private boolean isBlockPositionSafe(BlockPos blockPos) {
      return this.isBlockValid(this.mc.world.getBlockState(blockPos).getBlock());
   }

   private boolean isBlockValid(Block block) {
      return block != Blocks.LAVA && block != Blocks.WATER;
   }

   private void updateDirection(TunnelBaseFinder.Direction enum4) {
      double getX = this.mc.player.getX();
      double getY = this.mc.player.getZ();
      double floor = Math.floor(getY);
      double n = Math.floor(getX) + 0.5 - getX;
      double n2 = floor + 0.5 - getY;
      this.mc.options.leftKey.setPressed(false);
      this.mc.options.rightKey.setPressed(false);
      boolean b = false;
      boolean b2 = false;
      if (enum4 == TunnelBaseFinder.Direction.SOUTH) {
         if (n > 0.1) {
            b2 = true;
         } else if (n < -0.1) {
            b = true;
         }
      }

      if (enum4 == TunnelBaseFinder.Direction.NORTH) {
         if (n > 0.1) {
            b = true;
         } else if (n < -0.1) {
            b2 = true;
         }
      }

      if (enum4 == TunnelBaseFinder.Direction.WEST) {
         if (n2 > 0.1) {
            b2 = true;
         } else if (n2 < -0.1) {
            b = true;
         }
      }

      if (enum4 == TunnelBaseFinder.Direction.EAST) {
         if (n2 > 0.1) {
            b = true;
         } else if (n2 < -0.1) {
            b2 = true;
         }
      }

      if (b) {
         this.mc.options.rightKey.setPressed(true);
      }

      if (b2) {
         this.mc.options.leftKey.setPressed(true);
      }
   }

   private void handleBlockBreaking(boolean b) {
      if (!this.mc.player.isUsingItem()) {
         if (b && this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.crosshairTarget;
            BlockPos blockPos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos();
            if (this.postBreakDelay > 0) {
               this.postBreakDelay--;
               return;
            }

            if (!this.mc.world.getBlockState(blockPos).isAir()) {
               if (this.currentMiningBlock == null || !this.currentMiningBlock.equals(blockPos)) {
                  this.currentMiningBlock = blockPos;
                  this.miningTicks = 0;
               }

               this.miningTicks++;
               if (this.miningTicks % 2 == 0) {
                  net.minecraft.util.math.Direction side = blockHitResult.getSide();
                  if (this.mc.interactionManager.updateBlockBreakingProgress(blockPos, side)) {
                     this.mc.particleManager.addBlockBreakingParticles(blockPos, side);
                     this.mc.player.swingHand(Hand.MAIN_HAND);
                  }
               }
            } else {
               if (this.currentMiningBlock != null) {
                  this.currentMiningBlock = null;
                  this.miningTicks = 0;
                  this.postBreakDelay = 4;
               }

               this.mc.interactionManager.cancelBlockBreaking();
            }
         } else {
            this.mc.interactionManager.cancelBlockBreaking();
            this.currentMiningBlock = null;
            this.miningTicks = 0;
         }
      }
   }

   private TunnelBaseFinder.Direction getInitialDirection() {
      float n = this.mc.player.getYaw() % 360.0F;
      if (n < 0.0F) {
         n += 360.0F;
      }

      if (n >= 45.0F && n < 135.0F) {
         return TunnelBaseFinder.Direction.WEST;
      } else if (n >= 135.0F && n < 225.0F) {
         return TunnelBaseFinder.Direction.NORTH;
      } else {
         return n >= 225.0F && n < 315.0F ? TunnelBaseFinder.Direction.EAST : TunnelBaseFinder.Direction.SOUTH;
      }
   }

   private void notifyFound() {
      int n = 0;
      int n2 = 0;
      BlockPos blockPos = null;
      Iterator iterator = BlockUtil.getLoadedChunks().iterator();

      while (iterator.hasNext()) {
         for (Object next : ((WorldChunk)iterator.next()).getBlockEntityPositions()) {
            BlockEntity getBlockEntity = this.mc.world.getBlockEntity((BlockPos)next);
            if (this.spawners.getValue() && getBlockEntity instanceof MobSpawnerBlockEntity) {
               String string = ((MobSpawnerLogicAccessor)((MobSpawnerBlockEntity)getBlockEntity).getLogic()).getSpawnEntry().getNbt().getString("id");
               if (string != "minecraft:cave_spider" && string != "minecraft:spider") {
                  n2++;
                  blockPos = (BlockPos)next;
               }
            }

            if (!(getBlockEntity instanceof ChestBlockEntity) && !(getBlockEntity instanceof EnderChestBlockEntity)) {
               if (getBlockEntity instanceof ShulkerBoxBlockEntity) {
                  throw new RuntimeException();
               }

               if (!(getBlockEntity instanceof FurnaceBlockEntity)
                  && !(getBlockEntity instanceof BarrelBlockEntity)
                  && !(getBlockEntity instanceof EnchantingTableBlockEntity)) {
                  continue;
               }
            }

            n++;
         }
      }

      if (n2 > 0) {
         this.spawnerCount++;
      } else {
         this.spawnerCount = 0;
      }

      if (this.spawnerCount > 10) {
         this.notifyFound("YOU FOUND SPAWNER", blockPos.getX(), blockPos.getY(), blockPos.getZ(), false);
         this.spawnerCount = 0;
      }

      if (n > this.minimumStorage.getIntValue()) {
         this.notifyFound("YOU FOUND BASE", (int)this.mc.player.getPos().x, (int)this.mc.player.getPos().y, (int)this.mc.player.getPos().z, true);
      }
   }

   private void notifyTotemExploded(String s, int n, int n2, int n3) {
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

   private void notifyFound(String s, int n, int n2, int n3, boolean b) {
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

   private void disconnectWithMessage(Text text) {
      MutableText literal = Text.literal("[TunnelBaseFinder] ");
      literal.append(text);
      this.toggle();
      this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(literal));
   }

   public boolean isDigging() {
      return this.isDigging;
   }

   static enum Direction {
      NORTH("north", 0),
      SOUTH("south", 1),
      EAST("east", 2),
      WEST("west", 3);

      private Direction(final String name, final int ordinal) {
      }
   }
}
