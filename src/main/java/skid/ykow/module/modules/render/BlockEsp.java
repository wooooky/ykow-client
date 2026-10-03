package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.ChunkDataEvent;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BlocksSetting;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.BlockUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import java.awt.Color;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class BlockEsp extends Module {
   public static BlockEsp instance;
   private final BlocksSetting blocks = new BlocksSetting(EncryptedString.of("Blocks"));
   private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), true);
   private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 1.0, 255.0, 125.0, 1.0);
   private final NumberSetting red = new NumberSetting(EncryptedString.of("Red"), 0.0, 255.0, 255.0, 1.0);
   private final NumberSetting green = new NumberSetting(EncryptedString.of("Green"), 0.0, 255.0, 0.0, 1.0);
   private final NumberSetting blue = new NumberSetting(EncryptedString.of("Blue"), 0.0, 255.0, 0.0, 1.0);
   private final ConcurrentHashMap<Long, Set<BlockPos>> cachedBlocks = new ConcurrentHashMap<>();
   private final ExecutorService executor = Executors.newFixedThreadPool(3);
   private final Set<Long> scanningChunks = ConcurrentHashMap.newKeySet();
   private volatile boolean needsRescan = false;
   private int tickCounter = 0;

   public BlockEsp() {
      super(EncryptedString.of("Block Esp"), EncryptedString.of("Highlights selected blocks through walls"), -1, Category.RENDER);
      this.addsettings(new Setting[]{this.blocks, this.tracers, this.alpha, this.red, this.green, this.blue});
      instance = this;
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.cachedBlocks.clear();
      this.scanningChunks.clear();
      this.needsRescan = true;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.cachedBlocks.clear();
      this.scanningChunks.clear();
   }

   public static BlockEsp getInstance() {
      if (instance == null) {
         instance = new BlockEsp();
      }

      return instance;
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.world != null && this.blocks.size() != 0) {
         this.tickCounter++;
         if (this.needsRescan || this.tickCounter % 100 == 0) {
            this.needsRescan = false;
            this.scanAllChunks();
         }
      }
   }

   @EventListener
   public void onChunkData(ChunkDataEvent event) {
      if (event.getChunk() instanceof WorldChunk) {
         this.scanChunk((WorldChunk)event.getChunk());
      }
   }

   private void scanAllChunks() {
      if (this.mc.world != null) {
         for (WorldChunk chunk : BlockUtil.getLoadedChunks().toList()) {
            this.scanChunk(chunk);
         }
      }
   }

   private void scanChunk(WorldChunk chunk) {
      if (chunk != null && this.blocks.size() != 0) {
         long chunkKey = chunk.getPos().toLong();
         if (this.scanningChunks.add(chunkKey)) {
            this.executor.submit(() -> {
               try {
                  Set<BlockPos> foundBlocks = ConcurrentHashMap.newKeySet();
                  ChunkPos chunkPos = chunk.getPos();
                  int startX = chunkPos.getStartX();
                  int startZ = chunkPos.getStartZ();
                  ChunkSection[] sections = chunk.getSectionArray();
                  int minSection = this.mc.world.getBottomSectionCoord();

                  for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                     ChunkSection section = sections[sectionIndex];
                     if (section != null && !section.isEmpty()) {
                        int sectionY = (minSection + sectionIndex) * 16;

                        for (int x = 0; x < 16; x++) {
                           for (int z = 0; z < 16; z++) {
                              for (int y = 0; y < 16; y++) {
                                 BlockState state = section.getBlockState(x, y, z);
                                 if (this.blocks.contains(state.getBlock())) {
                                    BlockPos pos = new BlockPos(startX + x, sectionY + y, startZ + z);
                                    foundBlocks.add(pos);
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (foundBlocks.isEmpty()) {
                     this.cachedBlocks.remove(chunkKey);
                  } else {
                     this.cachedBlocks.put(chunkKey, foundBlocks);
                  }
               } catch (Exception var21) {
               } finally {
                  this.scanningChunks.remove(chunkKey);
               }
            });
         }
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent event) {
      if (this.blocks.size() != 0 && !this.cachedBlocks.isEmpty()) {
         Camera cam = RenderUtils.getCamera();
         if (cam != null) {
            Vec3d camPos = RenderUtils.getCameraPos();
            MatrixStack matrices = event.matrixStack;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Color blockColor = new Color(this.red.getIntValue(), this.green.getIntValue(), this.blue.getIntValue(), this.alpha.getIntValue());
            Color tracerColor = new Color(this.red.getIntValue(), this.green.getIntValue(), this.blue.getIntValue(), 255);

            for (Set<BlockPos> blockSet : this.cachedBlocks.values()) {
               if (blockSet != null) {
                  for (BlockPos blockPos : blockSet) {
                     if (blockPos != null) {
                        double distSq = this.mc.player.squaredDistanceTo(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                        if (!(distSq > 10000.0)) {
                           RenderUtils.renderFilledBox(
                              matrices,
                              blockPos.getX() + 0.1F,
                              blockPos.getY() + 0.05F,
                              blockPos.getZ() + 0.1F,
                              blockPos.getX() + 0.9F,
                              blockPos.getY() + 0.85F,
                              blockPos.getZ() + 0.9F,
                              blockColor
                           );
                           if (this.tracers.getValue() && this.mc.crosshairTarget != null) {
                              RenderUtils.renderLine(
                                 matrices,
                                 tracerColor,
                                 this.mc.crosshairTarget.getPos(),
                                 new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5)
                              );
                           }
                        }
                     }
                  }
               }
            }

            matrices.pop();
         }
      }
   }

   public BlocksSetting getBlocksSetting() {
      return this.blocks;
   }
}
