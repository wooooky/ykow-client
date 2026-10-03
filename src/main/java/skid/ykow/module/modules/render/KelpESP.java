package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.KelpBlock;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class KelpESP extends Module {
   private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 10.0, 100.0, 50.0, 1.0);
   private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 50.0, 255.0, 150.0, 1.0);
   private final BooleanSetting showHeight = new BooleanSetting(EncryptedString.of("Show Height"), true)
      .setDescription(EncryptedString.of("Shows the height of the kelp"));
   private final BooleanSetting onlyTallest = new BooleanSetting(EncryptedString.of("Only Tallest"), true)
      .setDescription(EncryptedString.of("Only highlights the tallest kelp in each cluster"));
   private final BooleanSetting onlySurfaceKelp = new BooleanSetting(EncryptedString.of("Only Surface Kelp"), true)
      .setDescription(EncryptedString.of("Only highlights kelp that reaches the water surface"));
   private final NumberSetting minSurfaceHeight = new NumberSetting(EncryptedString.of("Min Surface Height"), 1.0, 20.0, 3.0, 1.0)
      .setDescription(EncryptedString.of("Minimum height for kelp to be considered surface-reaching"));
   private final Map<BlockPos, Integer> kelpHeights = new HashMap<>();
   private final Map<BlockPos, Integer> waterSurfaceLevels = new HashMap<>();
   private final Set<BlockPos> processedClusters = new HashSet<>();

   public KelpESP() {
      super(EncryptedString.of("Kelp ESP"), EncryptedString.of("Highlights kelp that reaches the water surface for base finding"), -1, Category.RENDER);
      this.range.setDescription(EncryptedString.of("Range to search for kelp"));
      this.alpha.setDescription(EncryptedString.of("Transparency of the ESP"));
      this.addsettings(new Setting[]{this.range, this.alpha, this.showHeight, this.onlyTallest, this.onlySurfaceKelp, this.minSurfaceHeight});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.kelpHeights.clear();
      this.waterSurfaceLevels.clear();
      this.processedClusters.clear();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.kelpHeights.clear();
      this.waterSurfaceLevels.clear();
      this.processedClusters.clear();
   }

   @EventListener
   public void onRender3D(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         this.scanForKelp();
         Camera cam = RenderUtils.getCamera();
         if (cam != null) {
            Vec3d camPos = RenderUtils.getCameraPos();
            MatrixStack matrices = event.matrixStack;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
         }

         for (Entry<BlockPos, Integer> entry : this.kelpHeights.entrySet()) {
            BlockPos pos = entry.getKey();
            int height = entry.getValue();
            if ((!this.onlySurfaceKelp.getValue() || this.reachesWaterSurface(pos, height))
               && (!this.onlyTallest.getValue() || this.isTallestInCluster(pos, height))) {
               Color color = this.getKelpColor(height);
               RenderUtils.renderFilledBox(event.matrixStack, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, color);
               RenderUtils.renderLine(
                  event.matrixStack, color, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), new Vec3d(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
               );
               if (this.showHeight.getValue()) {
                  String heightText = String.valueOf(height);
                  new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
               }
            }
         }

         event.matrixStack.pop();
      }
   }

   private void scanForKelp() {
      if (this.mc.player != null && this.mc.world != null) {
         this.kelpHeights.clear();
         this.waterSurfaceLevels.clear();
         this.processedClusters.clear();
         BlockPos playerPos = this.mc.player.getBlockPos();
         int range = this.range.getIntValue();

         for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
               for (int y = -20; y <= 20; y++) {
                  BlockPos pos = playerPos.add(x, y, z);
                  BlockState state = this.mc.world.getBlockState(pos);
                  if (state.getBlock() instanceof KelpBlock) {
                     int height = this.getKelpHeight(pos);
                     if (height > 0) {
                        this.kelpHeights.put(pos, height);
                        int surfaceLevel = this.findWaterSurfaceLevel(pos);
                        this.waterSurfaceLevels.put(pos, surfaceLevel);
                     }
                  }
               }
            }
         }
      }
   }

   private int getKelpHeight(BlockPos pos) {
      if (this.mc.world == null) {
         return 0;
      } else {
         int height = 0;
         BlockPos currentPos = pos;

         while (this.mc.world.getBlockState(currentPos).getBlock() instanceof KelpBlock) {
            height++;
            currentPos = currentPos.up();
            if (height > 25) {
               break;
            }
         }

         return height;
      }
   }

   private int findWaterSurfaceLevel(BlockPos kelpBase) {
      if (this.mc.world == null) {
         return kelpBase.getY();
      } else {
         for (BlockPos currentPos = kelpBase; currentPos.getY() < 320; currentPos = currentPos.up()) {
            BlockState state = this.mc.world.getBlockState(currentPos);
            if (state.isAir()) {
               return currentPos.getY();
            }

            if (!state.getFluidState().isEmpty()
               && !(state.getBlock() instanceof KelpBlock)
               && state.getBlock() != Blocks.SEAGRASS
               && state.getBlock() != Blocks.TALL_SEAGRASS) {
               return currentPos.getY();
            }
         }

         return kelpBase.getY();
      }
   }

   private boolean reachesWaterSurface(BlockPos kelpBase, int kelpHeight) {
      Integer surfaceLevel = this.waterSurfaceLevels.get(kelpBase);
      if (surfaceLevel == null) {
         return false;
      } else {
         int kelpTop = kelpBase.getY() + kelpHeight - 1;
         boolean reachesSurface = kelpTop >= surfaceLevel - 1;
         boolean meetsMinHeight = kelpHeight >= this.minSurfaceHeight.getIntValue();
         return reachesSurface && meetsMinHeight;
      }
   }

   private boolean isTallestInCluster(BlockPos pos, int height) {
      for (int x = -3; x <= 3; x++) {
         for (int z = -3; z <= 3; z++) {
            BlockPos checkPos = pos.add(x, 0, z);
            Integer checkHeight = this.kelpHeights.get(checkPos);
            if (checkHeight != null && checkHeight > height) {
               return false;
            }
         }
      }

      return true;
   }

   private Color getKelpColor(int height) {
      int red;
      int green;
      int blue;
      if (height <= 5) {
         red = 0;
         green = 255;
         blue = 0;
      } else if (height <= 10) {
         red = 255;
         green = 255;
         blue = 0;
      } else if (height <= 15) {
         red = 255;
         green = 165;
         blue = 0;
      } else {
         red = 255;
         green = 0;
         blue = 0;
      }

      return new Color(red, green, blue, this.alpha.getIntValue());
   }
}
