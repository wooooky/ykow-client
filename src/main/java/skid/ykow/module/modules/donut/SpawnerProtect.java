package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.TickEvent;
import skid.ykow.mixin.PlayerInventoryAccessorMixin;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.EncryptedString;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class SpawnerProtect extends Module {
   private final NumberSetting detectionRadius = new NumberSetting(EncryptedString.of("Alpha"), 1.0, 32.0, 11.0, 1.0);
   private final NumberSetting breakDelay = new NumberSetting(EncryptedString.of("Alpha"), 1.0, 20.0, 5.0, 1.0);
   int silkTouchSlot = 0;
   private final String noSilkTouchMessage = "You Need To Have Silk Touch Pickaxe";
   private final String noSpawnersMessage = "No spawners found in detection range!";
   private final BooleanSetting enableWebhook = new BooleanSetting(EncryptedString.of("Discord Notification"), true);
   private final StringSetting webhookUrl = new StringSetting(EncryptedString.of("Webhook"), "");
   private List<BlockPos> detectedSpawners = new ArrayList<>();
   private BlockPos currentTarget = null;
   private int breakingTicks = 0;
   private int silkTouchSlotIndex = -1;
   private int originalSlot = -1;
   private boolean isSneaking = false;
   private boolean isBreaking = false;
   private int delayTicks = 0;
   private boolean hasCheckedInitialRequirements = false;
   private boolean isBreakingBlock = false;
   private int breakProgress = 0;
   private boolean playerDetected = false;
   private String detectedPlayerName = null;

   public SpawnerProtect() {
      super(
         EncryptedString.of("Spawner Protetion"),
         EncryptedString.of("It breakes your spawners and puts them in the enderchest if player is nearby"),
         -1,
         Category.DONUT
      );
      this.addsettings(new Setting[]{this.detectionRadius, this.breakDelay, this.enableWebhook, this.webhookUrl});
   }

   @Override
   public void onEnable() {
      if (this.mc.player != null && this.mc.world != null) {
         this.detectedSpawners.clear();
         this.currentTarget = null;
         this.breakingTicks = 0;
         this.silkTouchSlotIndex = -1;
         this.originalSlot = -1;
         this.isSneaking = false;
         this.isBreaking = false;
         this.delayTicks = 0;
         this.hasCheckedInitialRequirements = false;
         this.isBreakingBlock = false;
         this.breakProgress = 0;
         this.playerDetected = false;
         this.detectedPlayerName = null;
         this.silkTouchSlotIndex = this.findSilkTouchPickaxe();
         if (this.silkTouchSlotIndex == -1) {
            String kickMessage = new String("You Need To Have Silk Touch Pickaxe".getBytes());
            if (this.detectedPlayerName != null) {
               kickMessage = "Player detected: " + this.detectedPlayerName;
            }

            this.kickPlayerAndDisable(kickMessage);
         } else {
            this.hasCheckedInitialRequirements = true;
            this.scanForSpawners();
         }
      } else {
         this.toggle();
      }
   }

   @Override
   public void onDisable() {
      if (this.isSneaking && this.mc.player != null) {
         this.mc.player.setSneaking(false);
         this.isSneaking = false;
      }

      if (this.isBreakingBlock && this.currentTarget != null) {
         this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.currentTarget, Direction.UP));
         this.isBreakingBlock = false;
      }

      if (this.originalSlot >= 0 && this.originalSlot < 9 && this.mc.player != null) {
         PlayerInventoryAccessorMixin inventoryAccessor = (PlayerInventoryAccessorMixin)this.mc.player.getInventory();
         inventoryAccessor.setSelectedSlot(this.originalSlot);
      }
   }

   @EventListener
   private void onTick(TickEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (!this.hasCheckedInitialRequirements) {
            if (!this.checkInitialRequirements()) {
               return;
            }

            this.scanForSpawners();
            this.hasCheckedInitialRequirements = true;
         }

         if (this.delayTicks > 0) {
            this.delayTicks--;
         } else {
            boolean currentPlayerDetected = this.checkForOtherPlayers();
            if (!currentPlayerDetected) {
               this.scanForSpawners();
               this.playerDetected = false;
               this.detectedPlayerName = null;
               if (this.isSneaking) {
                  this.mc.player.setSneaking(false);
                  this.isSneaking = false;
               }

               if (this.isBreaking) {
                  this.isBreaking = false;
                  this.currentTarget = null;
                  if (this.isBreakingBlock) {
                     this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.currentTarget, Direction.UP));
                     this.isBreakingBlock = false;
                  }
               }
            } else {
               if (!this.playerDetected) {
                  this.playerDetected = true;
               }

               this.setSneaking(true);
               if (this.detectedSpawners.isEmpty()) {
                  this.scanForSpawners();
               }

               if (this.detectedSpawners.isEmpty()) {
                  String kickMessage = "No spawners to protect! Player detected but nothing to save.";
                  if (this.detectedPlayerName != null) {
                     kickMessage = kickMessage + " | Player near: " + this.detectedPlayerName;
                  }

                  this.kickPlayerAndDisable(kickMessage);
               } else {
                  PlayerInventoryAccessorMixin inventoryAccessor = (PlayerInventoryAccessorMixin)this.mc.player.getInventory();
                  if (inventoryAccessor.getSelectedSlot() != this.silkTouchSlotIndex) {
                     this.originalSlot = inventoryAccessor.getSelectedSlot();
                     inventoryAccessor.setSelectedSlot(this.silkTouchSlotIndex);
                  }

                  this.processSpawnerBreaking();
                  if (this.detectedSpawners.isEmpty() && this.currentTarget == null) {
                     String kickMessage = "All spawners have been protected! Disconnecting for safety.";
                     if (this.detectedPlayerName != null) {
                        kickMessage = kickMessage + " | Player near: " + this.detectedPlayerName;
                     }

                     this.kickPlayerAndDisable(kickMessage);
                  }
               }
            }
         }
      }
   }

   private void setSneaking(boolean sneak) {
      if (this.mc.player != null) {
         if (sneak) {
            this.mc.player.setSneaking(true);

            try {
               this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.PRESS_SHIFT_KEY));
            } catch (Exception var6) {
            }

            try {
               this.mc.options.sneakKey.setPressed(true);
            } catch (Exception var5) {
            }

            this.isSneaking = true;
         } else if (!sneak && this.isSneaking) {
            this.mc.player.setSneaking(false);

            try {
               this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.RELEASE_SHIFT_KEY));
            } catch (Exception var4) {
            }

            try {
               this.mc.options.sneakKey.setPressed(false);
            } catch (Exception var3) {
            }

            this.isSneaking = false;
         }
      }
   }

   private boolean checkInitialRequirements() {
      this.silkTouchSlotIndex = this.findSilkTouchPickaxe();
      if (this.silkTouchSlotIndex == -1) {
         String kickMessage = new String("You Need To Have Silk Touch Pickaxe".getBytes());
         if (this.detectedPlayerName != null) {
            kickMessage = kickMessage + " | Player near: " + this.detectedPlayerName;
         }

         this.kickPlayerAndDisable(kickMessage);
         return false;
      } else {
         return true;
      }
   }

   private int findSilkTouchPickaxe() {
      int manualSlot = this.silkTouchSlot;
      if (manualSlot >= 1 && manualSlot <= 9) {
         int slotIndex = manualSlot - 1;
         ItemStack stack = this.mc.player.getInventory().getStack(slotIndex);
         return this.isPickaxe(stack) ? slotIndex : -1;
      } else {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (this.isPickaxe(stack) && stack.hasEnchantments()) {
               String stackString = stack.toString().toLowerCase();
               if (!stackString.contains("silk_touch") && !stackString.contains("silktouch")) {
                  try {
                     String displayName = stack.getName().getString().toLowerCase();
                     if (displayName.contains("silk") || displayName.contains("touch")) {
                        return i;
                     }
                  } catch (Exception var6) {
                  }

                  return i;
               }

               return i;
            }
         }

         return -1;
      }
   }

   private boolean isPickaxe(ItemStack stack) {
      return stack.getItem() == Items.WOODEN_PICKAXE
         || stack.getItem() == Items.STONE_PICKAXE
         || stack.getItem() == Items.IRON_PICKAXE
         || stack.getItem() == Items.GOLDEN_PICKAXE
         || stack.getItem() == Items.DIAMOND_PICKAXE
         || stack.getItem() == Items.NETHERITE_PICKAXE;
   }

   private boolean checkForOtherPlayers() {
      for (PlayerEntity player : this.mc.world.getPlayers()) {
         if (player != this.mc.player && !player.isRemoved()) {
            this.detectedPlayerName = player.getName().getString();
            String alertMessage = "Player detected nearby: " + this.detectedPlayerName;
            this.sendWebhookMessage(alertMessage);
            return true;
         }
      }

      return false;
   }

   private void scanForSpawners() {
      this.detectedSpawners.clear();
      BlockPos playerPos = this.mc.player.getBlockPos();
      int radius = this.detectionRadius.getIntValue();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos pos = playerPos.add(x, y, z);
               if (this.mc.world.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
                  this.detectedSpawners.add(pos);
               }
            }
         }
      }
   }

   private void processSpawnerBreaking() {
      this.setSneaking(true);
      if (this.currentTarget == null) {
         if (this.detectedSpawners.isEmpty()) {
            return;
         }

         this.currentTarget = this.detectedSpawners.get(0);
         this.isBreaking = true;
         this.isBreakingBlock = false;
         this.breakingTicks = 0;
         this.breakProgress = 0;
      }

      if (this.mc.world.getBlockState(this.currentTarget).getBlock() != Blocks.SPAWNER) {
         this.detectedSpawners.remove(this.currentTarget);
         this.currentTarget = null;
         this.isBreaking = false;
         this.isBreakingBlock = false;
         this.breakProgress = 0;
         this.delayTicks = this.breakDelay.getIntValue();
      } else {
         if (this.isBreaking) {
            this.breakSpawner(this.currentTarget);
            this.breakingTicks++;
            if (this.breakingTicks > 200) {
               if (this.isBreakingBlock) {
                  this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, this.currentTarget, Direction.UP));
                  this.isBreakingBlock = false;
               }

               this.detectedSpawners.remove(this.currentTarget);
               this.currentTarget = null;
               this.isBreaking = false;
               this.breakProgress = 0;
               this.delayTicks = this.breakDelay.getIntValue();
            }
         }
      }
   }

   private void breakSpawner(BlockPos pos) {
      this.setSneaking(true);
      Vec3d blockCenter = Vec3d.ofCenter(pos);
      Vec3d playerEyes = this.mc.player.getEyePos();
      Vec3d direction = blockCenter.subtract(playerEyes).normalize();
      float yaw = (float)(Math.atan2(-direction.x, direction.z) * 180.0 / Math.PI);
      float pitch = (float)(Math.asin(-direction.y) * 180.0 / Math.PI);
      this.mc.player.setYaw(yaw);
      this.mc.player.setPitch(pitch);
      Direction face = this.getClosestFace(pos, playerEyes);
      if (!this.isBreakingBlock) {
         this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, pos, face));
         this.isBreakingBlock = true;
         this.breakProgress = 0;
      }

      this.breakProgress++;
      this.mc.player.swingHand(Hand.MAIN_HAND);
      this.mc.interactionManager.updateBlockBreakingProgress(pos, face);
      if (this.breakProgress >= 30) {
         this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, pos, face));
         this.mc.interactionManager.attackBlock(pos, face);
         this.isBreakingBlock = false;
         this.breakProgress = 0;
      }
   }

   private Direction getClosestFace(BlockPos blockPos, Vec3d playerEyes) {
      Vec3d blockCenter = Vec3d.ofCenter(blockPos);
      Vec3d diff = playerEyes.subtract(blockCenter);
      double absX = Math.abs(diff.x);
      double absY = Math.abs(diff.y);
      double absZ = Math.abs(diff.z);
      if (absX > absY && absX > absZ) {
         return diff.x > 0.0 ? Direction.EAST : Direction.WEST;
      } else if (absY > absZ) {
         return diff.y > 0.0 ? Direction.UP : Direction.DOWN;
      } else {
         return diff.z > 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   private void kickPlayerAndDisable(String message) {
      this.toggle();
      if (this.enableWebhook.getValue() && !this.webhookUrl.getValue().isEmpty()) {
         this.sendWebhookMessage(message);
      }

      if (this.mc.getNetworkHandler() != null) {
         this.mc.getNetworkHandler().getConnection().disconnect(Text.literal(message));
      } else if (this.mc.world != null) {
         this.mc.world.disconnect();
      }
   }

   private void sendWebhookMessage(String message) {
      if (this.enableWebhook.getValue() && !this.webhookUrl.getValue().isEmpty()) {
         CompletableFuture.runAsync(
            () -> {
               try {
                  HttpClient client = HttpClient.newHttpClient();
                  String jsonPayload = String.format("{\"content\":\"%s\"}", message.replace("\"", "\\\""));
                  HttpRequest request = HttpRequest.newBuilder()
                     .uri(URI.create(this.webhookUrl.getValue()))
                     .header("Content-Type", "application/json")
                     .POST(BodyPublishers.ofString(jsonPayload))
                     .build();
                  client.send(request, BodyHandlers.ofString());
               } catch (InterruptedException | IOException var5) {
               }
            }
         );
      }
   }
}
