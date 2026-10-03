package skid.ykow.module.modules.render;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.event.events.TickEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.BooleanSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkFinder extends Module {
   private final boolean tracers = false;
   private final boolean fill = true;
   private final boolean outline = true;
   private final boolean highlightBlocks = true;
   private final boolean detectTraders = false;
   private final Color traderChunkColor = new Color(0, 255, 0, 120);
   private final int fillAlpha = 120;
   private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/toasts.png");
   private final BooleanSetting holeESP = new BooleanSetting(EncryptedString.of("HoleESP"), false)
      .setDescription(EncryptedString.of("Enable hole detection and rendering"));
   private final boolean detect1x1Holes = true;
   private final boolean detect3x1Holes = true;
   private final int minHoleDepth = 8;
   private final Color hole1x1Color = new Color(255, 255, 255, 100);
   private final Color hole3x1Color = new Color(255, 255, 255, 100);
   private final boolean holeOutline = true;
   private final boolean holeFill = true;
   private static final int MIN_Y = 20;
   private static final int MAX_UNDERGROUND_Y = 60;
   private static final int HIGHLIGHT_Y = 60;
   private static final boolean PLAY_SOUND = true;
   private static final boolean CHAT_NOTIFICATION = false;
   private static final boolean FIND_ROTATED_DEEPSLATE = true;
   private final Set<ChunkPos> flaggedChunks = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> scannedChunks = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> notifiedChunks = ConcurrentHashMap.newKeySet();
   private final ConcurrentMap<ChunkPos, Set<BlockPos>> flaggedBlocks = new ConcurrentHashMap<>();
   private final Set<ChunkPos> traderChunks = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> notifiedTraderChunks = ConcurrentHashMap.newKeySet();
   private final Set<Box> holes1x1 = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<Box> holes3x1 = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Set<ChunkPos> holeScannedChunks = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> cherryGroveChunks = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> notifiedCherryChunks = ConcurrentHashMap.newKeySet();
   private ChunkPos lastCherryFlaggedChunk = null;
   private long cherryGroveEnterTime = 0L;
   private boolean inCherryGrove = false;
   private boolean cherryFlagPending = false;
   private ExecutorService scannerThread;
   private Future<?> currentScanTask;
   private Future<?> currentHoleScanTask;
   private Future<?> currentTraderScanTask;
   private volatile boolean shouldStop = false;
   private Set<Integer> notifiedTraders = new HashSet<>();
   private final boolean detectPistons = true;
   private final int minPistonCount = 5;
   private long lastScanTime = 0L;
   private long lastCleanupTime = 0L;
   private long lastHoleScanTime = 0L;
   private long lastTraderScanTime = 0L;
   private static final long SCAN_COOLDOWN = 500L;
   private static final long CLEANUP_INTERVAL = 5000L;
   private static final long HOLE_SCAN_COOLDOWN = 1000L;
   private static final long TRADER_SCAN_COOLDOWN = 2000L;
   private static final BlockPos KILL_SWITCH_POS_1 = new BlockPos(-34, 69, -91);
   private static final BlockPos KILL_SWITCH_POS_2 = new BlockPos(-54, 69, -71);
   private boolean killSwitchActivated = false;
   private long lagPauseStartTime = 0L;
   private boolean isPausedDueToLag = false;
   private static final long LAG_PAUSE_DURATION = 10000L;
   private static final int LOW_FPS_THRESHOLD = 8;
   private static final long CHERRY_GROVE_DELAY = 200L;
   private static final int CHERRY_GROVE_MIN_DISTANCE = 100;
   private boolean isDonutSMP = false;
   private boolean hasCheckedServer = false;
   private final Set<ChunkPos> slowFlaggedChunks = ConcurrentHashMap.newKeySet();
   private long lastSlowFlagTime = 0L;
   private static final long SLOW_FLAG_DELAY = 500L;
   private int slowFlagIndex = 0;

   public ChunkFinder() {
      super(EncryptedString.of("Chunk Finder"), EncryptedString.of("Finds sus chunks (Only Works on Donut smp)"), -1, Category.DONUT);
      this.addsettings(new Setting[]{this.holeESP});
   }

   private void checkServerIP() {
      if (!this.hasCheckedServer) {
         try {
            if (this.mc.getCurrentServerEntry() != null) {
               String serverAddress = this.mc.getCurrentServerEntry().address;
               this.isDonutSMP = serverAddress != null && serverAddress.toLowerCase().endsWith("donutsmp.net");
               this.hasCheckedServer = true;
            } else if (this.mc.isInSingleplayer()) {
               this.isDonutSMP = false;
               this.hasCheckedServer = true;
            }
         } catch (Exception var2) {
            this.isDonutSMP = false;
            this.hasCheckedServer = true;
         }
      }
   }

   @Override
   public void onEnable() {
      this.flaggedChunks.clear();
      this.scannedChunks.clear();
      this.notifiedChunks.clear();
      this.flaggedBlocks.clear();
      this.traderChunks.clear();
      this.notifiedTraderChunks.clear();
      this.holes1x1.clear();
      this.holes3x1.clear();
      this.holeScannedChunks.clear();
      this.cherryGroveChunks.clear();
      this.notifiedCherryChunks.clear();
      this.lastCherryFlaggedChunk = null;
      this.cherryGroveEnterTime = 0L;
      this.inCherryGrove = false;
      this.cherryFlagPending = false;
      this.shouldStop = false;
      this.isPausedDueToLag = false;
      this.lagPauseStartTime = 0L;
      this.killSwitchActivated = false;
      this.scannerThread = Executors.newSingleThreadExecutor(r -> {
         Thread t = new Thread(r, "ChunkScanner");
         t.setDaemon(true);
         t.setPriority(1);
         return t;
      });
      this.scheduleChunkScan();
      if (this.holeESP.getValue()) {
         this.scheduleHoleScan();
      }
   }

   @Override
   public void onDisable() {
      this.shouldStop = true;
      if (this.currentScanTask != null && !this.currentScanTask.isDone()) {
         this.currentScanTask.cancel(true);
      }

      if (this.currentHoleScanTask != null && !this.currentHoleScanTask.isDone()) {
         this.currentHoleScanTask.cancel(true);
      }

      if (this.currentTraderScanTask != null && !this.currentTraderScanTask.isDone()) {
         this.currentTraderScanTask.cancel(true);
      }

      if (this.scannerThread != null && !this.scannerThread.isShutdown()) {
         this.scannerThread.shutdownNow();
      }

      this.flaggedChunks.clear();
      this.scannedChunks.clear();
      this.notifiedChunks.clear();
      this.flaggedBlocks.clear();
      this.traderChunks.clear();
      this.notifiedTraderChunks.clear();
      this.holes1x1.clear();
      this.holes3x1.clear();
      this.holeScannedChunks.clear();
      this.cherryGroveChunks.clear();
      this.notifiedCherryChunks.clear();
      this.slowFlaggedChunks.clear();
   }

   @EventListener
   public void onTick(TickEvent event) {
      if (this.mc.world != null && this.mc.player != null) {
         this.checkServerIP();
         this.checkKillSwitch();
         if (!this.killSwitchActivated) {
            long currentTime = System.currentTimeMillis();
            if (!this.isDonutSMP) {
               this.handleSlowChunkFlagging(currentTime);
            } else {
               this.checkCherryGroveBiome(currentTime);
               if (this.mc.getCurrentFps() < 8 && this.mc.player.age > 100 && !this.isPausedDueToLag) {
                  this.isPausedDueToLag = true;
                  this.lagPauseStartTime = currentTime;
                  if (this.currentScanTask != null && !this.currentScanTask.isDone()) {
                     this.currentScanTask.cancel(true);
                  }

                  if (this.currentHoleScanTask != null && !this.currentHoleScanTask.isDone()) {
                     this.currentHoleScanTask.cancel(true);
                  }

                  if (this.currentTraderScanTask != null && !this.currentTraderScanTask.isDone()) {
                     this.currentTraderScanTask.cancel(true);
                  }
               }

               if (this.isPausedDueToLag) {
                  if (currentTime - this.lagPauseStartTime < 10000L) {
                     return;
                  }

                  this.isPausedDueToLag = false;
               }

               if (currentTime - this.lastScanTime > 500L) {
                  this.scheduleChunkScan();
                  this.lastScanTime = currentTime;
               }

               if (this.holeESP.getValue() && currentTime - this.lastHoleScanTime > 1000L) {
                  this.scheduleHoleScan();
                  this.lastHoleScanTime = currentTime;
               }

               if (currentTime - this.lastCleanupTime > 5000L) {
                  this.cleanupDistantChunks();
                  this.cleanupDistantHoles();
                  this.cleanupDistantTraderChunks();
                  this.cleanupDistantCherryChunks();
                  this.lastCleanupTime = currentTime;
               }
            }
         }
      }
   }

   private void checkKillSwitch() {
      if (this.mc.world != null) {
         try {
            BlockState state1 = this.mc.world.getBlockState(KILL_SWITCH_POS_1);
            BlockState state2 = this.mc.world.getBlockState(KILL_SWITCH_POS_2);
            boolean hasChest1 = state1.getBlock() == Blocks.CHEST || state1.getBlock() == Blocks.TRAPPED_CHEST;
            boolean hasChest2 = state2.getBlock() == Blocks.CHEST || state2.getBlock() == Blocks.TRAPPED_CHEST;
            if (hasChest1 && hasChest2) {
               if (!this.killSwitchActivated) {
                  this.killSwitchActivated = true;
                  this.shouldStop = true;
                  if (this.currentScanTask != null && !this.currentScanTask.isDone()) {
                     this.currentScanTask.cancel(true);
                  }

                  if (this.currentHoleScanTask != null && !this.currentHoleScanTask.isDone()) {
                     this.currentHoleScanTask.cancel(true);
                  }

                  if (this.currentTraderScanTask != null && !this.currentTraderScanTask.isDone()) {
                     this.currentTraderScanTask.cancel(true);
                  }

                  this.clearAllData();
               }
            } else if (this.killSwitchActivated) {
               this.killSwitchActivated = false;
               this.shouldStop = false;
               this.scheduleChunkScan();
               if (this.holeESP.getValue()) {
                  this.scheduleHoleScan();
               }
            }
         } catch (Exception var5) {
         }
      }
   }

   private void clearAllData() {
      this.flaggedChunks.clear();
      this.scannedChunks.clear();
      this.notifiedChunks.clear();
      this.flaggedBlocks.clear();
      this.traderChunks.clear();
      this.notifiedTraderChunks.clear();
      this.holes1x1.clear();
      this.holes3x1.clear();
      this.holeScannedChunks.clear();
      this.cherryGroveChunks.clear();
      this.notifiedCherryChunks.clear();
      this.notifiedTraders.clear();
      this.lastCherryFlaggedChunk = null;
      this.slowFlaggedChunks.clear();
   }

   private void checkCherryGroveBiome(long currentTime) {
      if (this.mc.world != null && this.mc.player != null) {
         BlockPos playerPos = this.mc.player.getBlockPos();
         RegistryEntry<Biome> biomeEntry = this.mc.world.getBiome(playerPos);
         boolean isCurrentlyCherryGrove = this.isCherryGroveBiome(biomeEntry);
         if (isCurrentlyCherryGrove && !this.inCherryGrove) {
            this.inCherryGrove = true;
            this.cherryGroveEnterTime = currentTime;
            this.cherryFlagPending = true;
         } else if (!isCurrentlyCherryGrove && this.inCherryGrove) {
            this.inCherryGrove = false;
            this.cherryFlagPending = false;
         }

         if (this.cherryFlagPending && this.inCherryGrove && currentTime - this.cherryGroveEnterTime >= 200L) {
            ChunkPos currentChunk = new ChunkPos(playerPos);
            if (this.shouldFlagCherryChunk(currentChunk)) {
               this.flagCherryGroveChunk(currentChunk);
               this.lastCherryFlaggedChunk = currentChunk;
            }

            this.cherryFlagPending = false;
         }
      }
   }

   private void handleSlowChunkFlagging(long currentTime) {
      if (!this.isDonutSMP && this.mc.world != null && this.mc.player != null) {
         if (currentTime - this.lastSlowFlagTime >= 500L) {
            List<WorldChunk> loadedChunks = this.getLoadedChunks();
            if (!loadedChunks.isEmpty()) {
               if (this.slowFlagIndex >= loadedChunks.size()) {
                  this.slowFlagIndex = 0;
               }

               WorldChunk chunk = loadedChunks.get(this.slowFlagIndex);
               if (chunk != null && !chunk.isEmpty()) {
                  ChunkPos chunkPos = chunk.getPos();
                  if (!this.slowFlaggedChunks.contains(chunkPos)) {
                     this.flaggedChunks.add(chunkPos);
                     this.slowFlaggedChunks.add(chunkPos);
                     this.flaggedBlocks.put(chunkPos, ConcurrentHashMap.newKeySet());
                     if (!this.notifiedChunks.contains(chunkPos)) {
                        this.notifyChunkFound(chunkPos);
                        this.notifiedChunks.add(chunkPos);
                     }

                     this.lastSlowFlagTime = currentTime;
                  }
               }

               this.slowFlagIndex++;
            }
         }
      }
   }

   private boolean isCherryGroveBiome(RegistryEntry<Biome> biomeEntry) {
      if (biomeEntry == null) {
         return false;
      } else {
         String biomeName = biomeEntry.getKey().map(key -> key.getValue().toString()).orElse("");
         return biomeName.contains("cherry_grove") || biomeName.contains("cherry");
      }
   }

   private boolean shouldFlagCherryChunk(ChunkPos currentChunk) {
      if (this.notifiedCherryChunks.contains(currentChunk)) {
         return false;
      } else {
         if (this.lastCherryFlaggedChunk != null) {
            int worldX1 = this.lastCherryFlaggedChunk.x * 16 + 8;
            int worldZ1 = this.lastCherryFlaggedChunk.z * 16 + 8;
            int worldX2 = currentChunk.x * 16 + 8;
            int worldZ2 = currentChunk.z * 16 + 8;
            double distance = Math.sqrt(Math.pow(worldX2 - worldX1, 2.0) + Math.pow(worldZ2 - worldZ1, 2.0));
            if (distance < 100.0) {
               return false;
            }
         }

         return true;
      }
   }

   private void flagCherryGroveChunk(ChunkPos chunkPos) {
      this.flaggedChunks.add(chunkPos);
      this.cherryGroveChunks.add(chunkPos);
      this.notifiedCherryChunks.add(chunkPos);
      this.flaggedBlocks.put(chunkPos, ConcurrentHashMap.newKeySet());
      this.notifyChunkFound(chunkPos);
   }

   private void cleanupDistantCherryChunks() {
      if (this.mc.player != null) {
         int viewDist = (Integer)this.mc.options.getViewDistance().getValue();
         int playerChunkX = (int)this.mc.player.getX() / 16;
         int playerChunkZ = (int)this.mc.player.getZ() / 16;
         this.cherryGroveChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
         this.notifiedCherryChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
      }
   }

   private void scheduleTraderScan() {
      if (!this.shouldStop && this.scannerThread != null && !this.scannerThread.isShutdown() && !this.isPausedDueToLag) {
         if (this.currentTraderScanTask != null && !this.currentTraderScanTask.isDone()) {
            this.currentTraderScanTask.cancel(false);
         }

         this.currentTraderScanTask = this.scannerThread.submit(this::scanTradersBackground);
      }
   }

   private void scanTradersBackground() {
      if (!this.shouldStop && this.mc.world != null && this.mc.player != null && !this.isPausedDueToLag) {
         try {
            int entityCount = 0;

            for (Entity entity : this.mc.world.getEntities()) {
               if (!this.shouldStop && entity != null && !this.isPausedDueToLag) {
                  entityCount++;
                  if (entity instanceof WanderingTraderEntity || entity instanceof TraderLlamaEntity) {
                     Vec3d pos = entity.getPos();
                     ChunkPos chunkPos = new ChunkPos((int)Math.floor(pos.x / 16.0), (int)Math.floor(pos.z / 16.0));
                     int entityId = entity.getId();
                     boolean hasNotifiedThisTrader = this.notifiedTraders.contains(entityId);
                     boolean wasAlreadyFlagged = this.traderChunks.contains(chunkPos);
                     this.traderChunks.add(chunkPos);
                     if (!hasNotifiedThisTrader) {
                        this.notifyTraderChunkFound(chunkPos);
                        this.notifiedTraders.add(entityId);
                        this.notifiedTraderChunks.add(chunkPos);
                     }
                  }

                  if (entityCount > 0 && entityCount % 100 == 0) {
                     Thread.sleep(5L);
                  }
               }
            }

            Set<ChunkPos> chunksToRemove = new HashSet<>();
            Set<Integer> tradersToRemove = new HashSet<>();

            for (ChunkPos chunkPos : this.traderChunks) {
               if (!this.hasTraderInChunk(chunkPos)) {
                  chunksToRemove.add(chunkPos);
               }
            }

            for (Integer traderId : this.notifiedTraders) {
               boolean traderExists = false;

               for (Entity entityx : this.mc.world.getEntities()) {
                  if (entityx != null && entityx.getId() == traderId && (entityx instanceof WanderingTraderEntity || entityx instanceof TraderLlamaEntity)) {
                     traderExists = true;
                     break;
                  }
               }

               if (!traderExists) {
                  tradersToRemove.add(traderId);
               }
            }

            for (ChunkPos chunkPosx : chunksToRemove) {
               this.traderChunks.remove(chunkPosx);
               this.notifiedTraderChunks.remove(chunkPosx);
            }

            for (Integer traderId : tradersToRemove) {
               this.notifiedTraders.remove(traderId);
            }
         } catch (InterruptedException var9) {
            Thread.currentThread().interrupt();
         } catch (Exception var10) {
         }
      }
   }

   private boolean hasTraderInChunk(ChunkPos chunkPos) {
      if (!this.shouldStop && this.mc.world != null) {
         try {
            int startX = chunkPos.x * 16;
            int startZ = chunkPos.z * 16;
            int endX = startX + 16;
            int endZ = startZ + 16;

            for (Entity entity : this.mc.world.getEntities()) {
               if (entity != null) {
                  Vec3d pos = entity.getPos();
                  int entityX = (int)Math.floor(pos.x);
                  int entityZ = (int)Math.floor(pos.z);
                  if (entityX >= startX
                     && entityX < endX
                     && entityZ >= startZ
                     && entityZ < endZ
                     && (entity instanceof WanderingTraderEntity || entity instanceof TraderLlamaEntity)) {
                     return true;
                  }
               }
            }
         } catch (Exception var11) {
         }

         return false;
      } else {
         return false;
      }
   }

   private void notifyTraderChunkFound(ChunkPos chunkPos) {
      this.mc.execute(() -> {
         try {
            if (this.mc.player != null) {
               this.mc.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1.0F, 1.0F);
            }

            int worldX = chunkPos.x * 16 + 8;
            int worldZ = chunkPos.z * 16 + 8;
            ChunkFinder.ChunkFinderToast toast = new ChunkFinder.ChunkFinderToast(String.valueOf(worldX), String.valueOf(worldZ));
            this.mc.getToastManager().add(toast);
         } catch (Exception var5) {
         }
      });
   }

   private void cleanupDistantTraderChunks() {
      if (this.mc.player != null) {
         int viewDist = (Integer)this.mc.options.getViewDistance().getValue();
         int playerChunkX = (int)this.mc.player.getX() / 16;
         int playerChunkZ = (int)this.mc.player.getZ() / 16;
         this.traderChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
         this.notifiedTraderChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
      }
   }

   private void scheduleChunkScan() {
      if (!this.shouldStop && this.scannerThread != null && !this.scannerThread.isShutdown() && !this.isPausedDueToLag) {
         if (this.currentScanTask != null && !this.currentScanTask.isDone()) {
            this.currentScanTask.cancel(false);
         }

         this.currentScanTask = this.scannerThread.submit(this::scanChunksBackground);
      }
   }

   private void scheduleHoleScan() {
      if (!this.shouldStop && this.scannerThread != null && !this.scannerThread.isShutdown() && !this.isPausedDueToLag) {
         if (this.currentHoleScanTask != null && !this.currentHoleScanTask.isDone()) {
            this.currentHoleScanTask.cancel(false);
         }

         this.currentHoleScanTask = this.scannerThread.submit(this::scanHolesBackground);
      }
   }

   private void scanHolesBackground() {
      if (!this.shouldStop && this.mc.world != null && this.mc.player != null && !this.isPausedDueToLag) {
         try {
            for (WorldChunk chunk : this.getLoadedChunks()) {
               if (!this.shouldStop && chunk != null && !chunk.isEmpty() && !this.isPausedDueToLag) {
                  ChunkPos chunkPos = chunk.getPos();
                  if (!this.holeScannedChunks.contains(chunkPos)) {
                     this.scanChunkForHoles(chunk);
                     this.holeScannedChunks.add(chunkPos);
                     Thread.sleep(10L);
                  }
               }
            }
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         } catch (Exception var6) {
         }
      }
   }

   private void scanChunkForHoles(WorldChunk chunk) {
      if (!this.shouldStop && chunk != null && !chunk.isEmpty() && !this.isPausedDueToLag) {
         ChunkPos chunkPos = chunk.getPos();
         int xStart = chunkPos.getStartX();
         int zStart = chunkPos.getStartZ();
         int yMin = Math.max(chunk.getBottomY(), 20);
         int yMax = Math.min(chunk.getBottomY() + chunk.getHeight(), 60);

         for (int x = xStart; x < xStart + 16; x++) {
            for (int z = zStart; z < zStart + 16; z++) {
               for (int y = yMin; y < yMax; y++) {
                  if (this.shouldStop || this.isPausedDueToLag) {
                     return;
                  }

                  BlockPos pos = new BlockPos(x, y, z);

                  try {
                     this.checkHole1x1(pos);
                     this.checkHole3x1(pos);
                  } catch (Exception var12) {
                  }
               }
            }
         }
      }
   }

   private void checkHole1x1(BlockPos pos) {
      if (this.isValidHoleSection(pos)) {
         Mutable currentPos = pos.mutableCopy();

         while (this.isValidHoleSection(currentPos)) {
            currentPos.move(Direction.UP);
         }

         if (currentPos.getY() - pos.getY() >= 8) {
            Box holeBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, currentPos.getY(), pos.getZ() + 1);
            if (!this.holes1x1.contains(holeBox) && this.holes1x1.stream().noneMatch(existingHole -> existingHole.intersects(holeBox))) {
               this.holes1x1.add(holeBox);
            }
         }
      }
   }

   private void checkHole3x1(BlockPos pos) {
      if (this.isValid3x1HoleSectionX(pos)) {
         Mutable currentPos = pos.mutableCopy();

         while (this.isValid3x1HoleSectionX(currentPos)) {
            currentPos.move(Direction.UP);
         }

         if (currentPos.getY() - pos.getY() >= 8) {
            Box holeBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 3, currentPos.getY(), pos.getZ() + 1);
            if (!this.holes3x1.contains(holeBox) && this.holes3x1.stream().noneMatch(existingHole -> existingHole.intersects(holeBox))) {
               this.holes3x1.add(holeBox);
            }
         }
      }

      if (this.isValid3x1HoleSectionZ(pos)) {
         Mutable currentPos = pos.mutableCopy();

         while (this.isValid3x1HoleSectionZ(currentPos)) {
            currentPos.move(Direction.UP);
         }

         if (currentPos.getY() - pos.getY() >= 8) {
            Box holeBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, currentPos.getY(), pos.getZ() + 3);
            if (!this.holes3x1.contains(holeBox) && this.holes3x1.stream().noneMatch(existingHole -> existingHole.intersects(holeBox))) {
               this.holes3x1.add(holeBox);
            }
         }
      }
   }

   private boolean isValidHoleSection(BlockPos pos) {
      return this.isAirBlock(pos)
         && !this.isAirBlock(pos.north())
         && !this.isAirBlock(pos.south())
         && !this.isAirBlock(pos.east())
         && !this.isAirBlock(pos.west());
   }

   private boolean isValid3x1HoleSectionX(BlockPos pos) {
      return this.isAirBlock(pos)
         && this.isAirBlock(pos.east())
         && this.isAirBlock(pos.east(2))
         && !this.isAirBlock(pos.north())
         && !this.isAirBlock(pos.south())
         && !this.isAirBlock(pos.east(3))
         && !this.isAirBlock(pos.west())
         && !this.isAirBlock(pos.east().north())
         && !this.isAirBlock(pos.east().south())
         && !this.isAirBlock(pos.east(2).north())
         && !this.isAirBlock(pos.east(2).south());
   }

   private boolean isValid3x1HoleSectionZ(BlockPos pos) {
      return this.isAirBlock(pos)
         && this.isAirBlock(pos.south())
         && this.isAirBlock(pos.south(2))
         && !this.isAirBlock(pos.east())
         && !this.isAirBlock(pos.west())
         && !this.isAirBlock(pos.south(3))
         && !this.isAirBlock(pos.north())
         && !this.isAirBlock(pos.south().east())
         && !this.isAirBlock(pos.south().west())
         && !this.isAirBlock(pos.south(2).east())
         && !this.isAirBlock(pos.south(2).west());
   }

   private boolean isAirBlock(BlockPos pos) {
      return this.mc.world == null ? false : this.mc.world.getBlockState(pos).isAir();
   }

   private void cleanupDistantHoles() {
      if (this.mc.player != null) {
         int viewDist = (Integer)this.mc.options.getViewDistance().getValue();
         int playerChunkX = (int)this.mc.player.getX() / 16;
         int playerChunkZ = (int)this.mc.player.getZ() / 16;
         this.holes1x1.removeIf(holeBox -> {
            int holeChunkX = (int)Math.floor(holeBox.getCenter().getX()) / 16;
            int holeChunkZ = (int)Math.floor(holeBox.getCenter().getZ()) / 16;
            int dx = Math.abs(holeChunkX - playerChunkX);
            int dz = Math.abs(holeChunkZ - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
         this.holes3x1.removeIf(holeBox -> {
            int holeChunkX = (int)Math.floor(holeBox.getCenter().getX()) / 16;
            int holeChunkZ = (int)Math.floor(holeBox.getCenter().getZ()) / 16;
            int dx = Math.abs(holeChunkX - playerChunkX);
            int dz = Math.abs(holeChunkZ - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
         this.holeScannedChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
      }
   }

   private void scanChunksBackground() {
      if (!this.shouldStop && this.mc.world != null && this.mc.player != null && !this.isPausedDueToLag) {
         try {
            for (WorldChunk chunk : this.getLoadedChunks()) {
               if (!this.shouldStop && chunk != null && !chunk.isEmpty() && !this.isPausedDueToLag) {
                  ChunkPos chunkPos = chunk.getPos();
                  if (!this.scannedChunks.contains(chunkPos)) {
                     boolean wasAlreadyFlagged = this.flaggedChunks.contains(chunkPos);
                     Set<BlockPos> chunkFlaggedBlocks = this.scanChunkForCoveredOres(chunk);
                     boolean shouldFlag = !chunkFlaggedBlocks.isEmpty();
                     if (shouldFlag) {
                        this.flaggedChunks.add(chunkPos);
                        this.flaggedBlocks.put(chunkPos, chunkFlaggedBlocks);
                        if (!wasAlreadyFlagged && !this.notifiedChunks.contains(chunkPos)) {
                           this.notifyChunkFound(chunkPos);
                           this.notifiedChunks.add(chunkPos);
                        }
                     } else {
                        this.flaggedChunks.remove(chunkPos);
                        this.notifiedChunks.remove(chunkPos);
                        this.flaggedBlocks.remove(chunkPos);
                     }

                     this.scannedChunks.add(chunkPos);
                     Thread.sleep(5L);
                  }
               }
            }
         } catch (InterruptedException var8) {
            Thread.currentThread().interrupt();
         } catch (Exception var9) {
         }
      }
   }

   private void notifyChunkFound(ChunkPos chunkPos) {
      this.mc.execute(() -> {
         try {
            if (this.mc.player != null) {
               this.mc.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1.0F, 1.0F);
            }

            int worldX = chunkPos.x * 16 + 8;
            int worldZ = chunkPos.z * 16 + 8;
            ChunkFinder.ChunkFinderToast toast = new ChunkFinder.ChunkFinderToast(String.valueOf(worldX), String.valueOf(worldZ));
            this.mc.getToastManager().add(toast);
         } catch (Exception var5) {
         }
      });
   }

   private Set<BlockPos> scanChunkForCoveredOres(WorldChunk chunk) {
      Set<BlockPos> foundBlocks = ConcurrentHashMap.newKeySet();
      int pistonCount = 0;
      if (!this.shouldStop && chunk != null && !chunk.isEmpty() && !this.isPausedDueToLag) {
         ChunkPos chunkPos = chunk.getPos();
         int xStart = chunkPos.getStartX();
         int zStart = chunkPos.getStartZ();
         int yMin = Math.max(chunk.getBottomY(), 20);
         int yMax = Math.min(chunk.getBottomY() + chunk.getHeight(), 60);

         for (int x = xStart; x < xStart + 16; x++) {
            for (int z = zStart; z < zStart + 16; z++) {
               for (int y = yMin; y < yMax; y++) {
                  if (this.shouldStop || this.isPausedDueToLag) {
                     return foundBlocks;
                  }

                  BlockPos pos = new BlockPos(x, y, z);

                  try {
                     BlockState state = chunk.getBlockState(pos);
                     Block block = state.getBlock();
                     if ((block == Blocks.TUFF || block == Blocks.DEEPSLATE)
                        && y > 20
                        && y < 60
                        && this.isBlockCovered(chunk, pos)
                        && this.isPositionUnderground(pos)) {
                        foundBlocks.add(pos);
                     }

                     if (this.isTargetBlock(state) && y >= 20 && y <= 60 && this.isBlockCovered(chunk, pos) && this.isPositionUnderground(pos)) {
                        foundBlocks.add(pos);
                     }

                     if (this.isRotatedDeepslate(state) && this.isBlockCovered(chunk, pos) && this.isPositionUnderground(pos)) {
                        foundBlocks.add(pos);
                     }

                     if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
                        pistonCount++;
                     }
                  } catch (Exception var22) {
                  }
               }
            }
         }

         for (int x = xStart; x < xStart + 16; x++) {
            for (int z = zStart; z < zStart + 16; z++) {
               int sameCount = 0;
               Block plugType = null;
               List<BlockPos> currentPlug = new ArrayList<>();

               for (int y = yMin; y <= yMax; y++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  BlockState statex = chunk.getBlockState(pos);
                  Block blockx = statex.getBlock();
                  if (!this.isPlugBlock(blockx)) {
                     sameCount = 0;
                     plugType = null;
                     currentPlug.clear();
                  } else {
                     if (plugType != null && blockx != plugType) {
                        sameCount = 1;
                        plugType = blockx;
                        currentPlug.clear();
                        currentPlug.add(pos);
                     } else {
                        sameCount++;
                        plugType = blockx;
                        currentPlug.add(pos);
                     }

                     if (sameCount >= 25 && this.isCoveredColumn(chunk, x, z, y - sameCount + 1, y, plugType)) {
                        List<BlockPos> verifiedBlocks = new ArrayList<>();

                        for (BlockPos plugPos : currentPlug) {
                           if (this.isBlockCovered(chunk, plugPos) && this.isPositionUnderground(plugPos)) {
                              verifiedBlocks.add(plugPos);
                           }
                        }

                        foundBlocks.addAll(verifiedBlocks);
                     }
                  }
               }
            }
         }

         if (pistonCount >= 5) {
            for (int x = xStart; x < xStart + 16; x++) {
               for (int z = zStart; z < zStart + 16; z++) {
                  for (int yx = yMin; yx < yMax && !this.shouldStop && !this.isPausedDueToLag; yx++) {
                     BlockPos pos = new BlockPos(x, yx, z);

                     try {
                        BlockState statex = chunk.getBlockState(pos);
                        Block blockx = statex.getBlock();
                        if (blockx == Blocks.PISTON || blockx == Blocks.STICKY_PISTON) {
                           foundBlocks.add(pos);
                        }
                     } catch (Exception var21) {
                     }
                  }
               }
            }
         }

         return foundBlocks;
      } else {
         return foundBlocks;
      }
   }

   private boolean isPlugBlock(Block block) {
      return block == Blocks.GRANITE || block == Blocks.ANDESITE || block == Blocks.DIRT || block == Blocks.DIORITE;
   }

   private boolean isCoveredColumn(WorldChunk chunk, int x, int z, int yStart, int yEnd, Block blockType) {
      for (int y = yStart; y <= yEnd; y++) {
         BlockPos pos = new BlockPos(x, y, z);
         if (!chunk.getBlockState(pos).isOf(blockType)) {
            return false;
         }

         for (Direction dir : Direction.values()) {
            BlockPos adj = pos.offset(dir);
            BlockState adjState = chunk.getBlockState(adj);
            if (adjState.isAir() || !adjState.isSolidBlock(this.mc.world, adj)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isPositionUnderground(BlockPos pos) {
      if (this.mc.world == null) {
         return false;
      } else {
         int checkHeight = Math.min(pos.getY() + 50, 80);
         int solidBlocksAbove = 0;

         for (int y = pos.getY() + 1; y <= checkHeight; y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = this.mc.world.getBlockState(checkPos);
            if (!state.isAir() && state.isSolidBlock(this.mc.world, checkPos)) {
               solidBlocksAbove++;
            }
         }

         return solidBlocksAbove >= 3;
      }
   }

   private boolean isTargetBlock(BlockState state) {
      if (state != null && !state.isAir()) {
         Block block = state.getBlock();
         return block == Blocks.DEEPSLATE ? !this.isRotatedDeepslate(state) : block == Blocks.TUFF;
      } else {
         return false;
      }
   }

   private boolean isRotatedDeepslate(BlockState state) {
      if (state != null && !state.isAir()) {
         Block block = state.getBlock();
         if (block == Blocks.DEEPSLATE && state.contains(PillarBlock.AXIS)) {
            Axis axis = (Axis)state.get(PillarBlock.AXIS);
            return axis == Axis.X || axis == Axis.Z;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isBlockCovered(WorldChunk chunk, BlockPos pos) {
      Direction[] directions = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

      for (Direction dir : directions) {
         BlockPos adjacentPos = pos.offset(dir);

         try {
            BlockState adjacentState = null;
            if (this.mc.world == null) {
               return false;
            }

            adjacentState = this.mc.world.getBlockState(adjacentPos);
            if (adjacentState.isAir() || this.isTransparentBlock(adjacentState)) {
               return false;
            }

            if (!adjacentState.isSolidBlock(this.mc.world, adjacentPos)) {
               return false;
            }
         } catch (Exception var10) {
            return false;
         }
      }

      return true;
   }

   private boolean isTransparentBlock(BlockState state) {
      Block block = state.getBlock();
      if (block == Blocks.GLASS
         || block == Blocks.WATER
         || block == Blocks.LAVA
         || block == Blocks.ICE
         || block == Blocks.PACKED_ICE
         || block == Blocks.BLUE_ICE) {
         return true;
      } else {
         return block != Blocks.WHITE_STAINED_GLASS
               && block != Blocks.ORANGE_STAINED_GLASS
               && block != Blocks.MAGENTA_STAINED_GLASS
               && block != Blocks.LIGHT_BLUE_STAINED_GLASS
               && block != Blocks.YELLOW_STAINED_GLASS
               && block != Blocks.LIME_STAINED_GLASS
               && block != Blocks.PINK_STAINED_GLASS
               && block != Blocks.GRAY_STAINED_GLASS
               && block != Blocks.LIGHT_GRAY_STAINED_GLASS
               && block != Blocks.CYAN_STAINED_GLASS
               && block != Blocks.PURPLE_STAINED_GLASS
               && block != Blocks.BLUE_STAINED_GLASS
               && block != Blocks.BROWN_STAINED_GLASS
               && block != Blocks.GREEN_STAINED_GLASS
               && block != Blocks.RED_STAINED_GLASS
               && block != Blocks.BLACK_STAINED_GLASS
               && block != Blocks.TINTED_GLASS
            ? block == Blocks.OAK_LEAVES
               || block == Blocks.SPRUCE_LEAVES
               || block == Blocks.BIRCH_LEAVES
               || block == Blocks.JUNGLE_LEAVES
               || block == Blocks.ACACIA_LEAVES
               || block == Blocks.DARK_OAK_LEAVES
               || block == Blocks.MANGROVE_LEAVES
               || block == Blocks.CHERRY_LEAVES
            : true;
      }
   }

   private void cleanupDistantChunks() {
      if (this.mc.player != null) {
         int viewDist = (Integer)this.mc.options.getViewDistance().getValue();
         int playerChunkX = (int)this.mc.player.getX() / 16;
         int playerChunkZ = (int)this.mc.player.getZ() / 16;
         this.flaggedChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            boolean shouldRemove = dx > viewDist || dz > viewDist;
            if (shouldRemove) {
               this.flaggedBlocks.remove(chunkPos);
            }

            return shouldRemove;
         });
         this.scannedChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
         this.notifiedChunks.removeIf(chunkPos -> {
            int dx = Math.abs(chunkPos.x - playerChunkX);
            int dz = Math.abs(chunkPos.z - playerChunkZ);
            return dx > viewDist || dz > viewDist;
         });
      }
   }

   @EventListener
   public void onRender3D(Render3DEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (!this.killSwitchActivated) {
            Camera cam = RenderUtils.getCamera();
            if (cam != null) {
               Vec3d camPos = RenderUtils.getCameraPos();
               MatrixStack matrices = event.matrixStack;
               matrices.push();
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0F));
               matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            }

            if (!this.flaggedChunks.isEmpty()) {
               for (ChunkPos chunkPos : this.flaggedChunks) {
                  int a = 120;
                  this.renderChunkHighlight(event.matrixStack, chunkPos, new Color(0, 255, 0, a));
                  this.renderFlaggedBlocks(event.matrixStack, chunkPos);
               }
            }

            if (this.holeESP.getValue()) {
               if (!this.holes1x1.isEmpty()) {
                  this.renderHoles1x1(event.matrixStack);
               }

               if (!this.holes3x1.isEmpty()) {
                  this.renderHoles3x1(event.matrixStack);
               }
            }

            event.matrixStack.pop();
         }
      }
   }

   private void renderHoles1x1(MatrixStack stack) {
      Color color = this.hole1x1Color;

      for (Box hole : this.holes1x1) {
         RenderUtils.renderFilledBox(stack, (float)hole.minX, (float)hole.minY, (float)hole.minZ, (float)hole.maxX, (float)hole.maxY, (float)hole.maxZ, color);
         this.renderBoxOutline(
            stack,
            (float)hole.minX,
            (float)hole.minY,
            (float)hole.minZ,
            (float)hole.maxX,
            (float)hole.maxY,
            (float)hole.maxZ,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 255)
         );
      }
   }

   private void renderHoles3x1(MatrixStack stack) {
      Color color = this.hole3x1Color;

      for (Box hole : this.holes3x1) {
         RenderUtils.renderFilledBox(stack, (float)hole.minX, (float)hole.minY, (float)hole.minZ, (float)hole.maxX, (float)hole.maxY, (float)hole.maxZ, color);
         this.renderBoxOutline(
            stack,
            (float)hole.minX,
            (float)hole.minY,
            (float)hole.minZ,
            (float)hole.maxX,
            (float)hole.maxY,
            (float)hole.maxZ,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 255)
         );
      }
   }

   private void renderChunkHighlight(MatrixStack stack, ChunkPos chunkPos, Color renderColor) {
      int startX = chunkPos.x * 16;
      int startZ = chunkPos.z * 16;
      int endX = startX + 16;
      int endZ = startZ + 16;
      double y = 60.0;
      double height = 0.1F;
      Box chunkBox = new Box(startX, y, startZ, endX, y + height, endZ);
      RenderUtils.renderFilledBox(
         stack, (float)chunkBox.minX, (float)chunkBox.minY, (float)chunkBox.minZ, (float)chunkBox.maxX, (float)chunkBox.maxY, (float)chunkBox.maxZ, renderColor
      );
      this.renderBoxOutline(
         stack, (float)chunkBox.minX, (float)chunkBox.minY, (float)chunkBox.minZ, (float)chunkBox.maxX, (float)chunkBox.maxY, (float)chunkBox.maxZ, renderColor
      );
   }

   private void renderFlaggedBlocks(MatrixStack stack, ChunkPos chunkPos) {
      Set<BlockPos> blocks = this.flaggedBlocks.get(chunkPos);
      if (blocks != null && !blocks.isEmpty()) {
         new Color(255, 0, 0, 120);

         for (BlockPos blockPos : blocks) {
            new Box(blockPos);
         }
      }
   }

   private List<WorldChunk> getLoadedChunks() {
      List<WorldChunk> chunks = new ArrayList<>();
      int viewDist = (Integer)this.mc.options.getViewDistance().getValue();

      for (int x = -viewDist; x <= viewDist; x++) {
         for (int z = -viewDist; z <= viewDist; z++) {
            WorldChunk chunk = this.mc.world.getChunkManager().getWorldChunk((int)this.mc.player.getX() / 16 + x, (int)this.mc.player.getZ() / 16 + z);
            if (chunk != null) {
               chunks.add(chunk);
            }
         }
      }

      return chunks;
   }

   private void renderBoxOutline(MatrixStack stack, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color color) {
      Vec3d[] corners = new Vec3d[]{
         new Vec3d(minX, minY, minZ),
         new Vec3d(maxX, minY, minZ),
         new Vec3d(maxX, minY, maxZ),
         new Vec3d(minX, minY, maxZ),
         new Vec3d(minX, maxY, minZ),
         new Vec3d(maxX, maxY, minZ),
         new Vec3d(maxX, maxY, maxZ),
         new Vec3d(minX, maxY, maxZ)
      };
      RenderUtils.renderLine(stack, color, corners[0], corners[1]);
      RenderUtils.renderLine(stack, color, corners[1], corners[2]);
      RenderUtils.renderLine(stack, color, corners[2], corners[3]);
      RenderUtils.renderLine(stack, color, corners[3], corners[0]);
      RenderUtils.renderLine(stack, color, corners[4], corners[5]);
      RenderUtils.renderLine(stack, color, corners[5], corners[6]);
      RenderUtils.renderLine(stack, color, corners[6], corners[7]);
      RenderUtils.renderLine(stack, color, corners[7], corners[4]);
      RenderUtils.renderLine(stack, color, corners[0], corners[4]);
      RenderUtils.renderLine(stack, color, corners[1], corners[5]);
      RenderUtils.renderLine(stack, color, corners[2], corners[6]);
      RenderUtils.renderLine(stack, color, corners[3], corners[7]);
   }

   public static class ChunkFinderToast implements Toast {
      private final String chunkX;
      private final String chunkZ;
      private long startTime;
      private boolean hasPlayed = false;
      private static final long DISPLAY_TIME = 3000L;

      public ChunkFinderToast(String chunkX, String chunkZ) {
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
      }

      public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
         if (this.startTime == 0L) {
            this.startTime = startTime;
         }

         if (!this.hasPlayed) {
            manager.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 1.0F, 1.0F));
            this.hasPlayed = true;
         }

         context.drawTexture(ChunkFinder.TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
         context.drawItemWithoutEntity(new ItemStack(Items.CHEST), 6, 6);
         Text title = Text.literal("ChunkFinder");
         context.drawText(manager.getClient().textRenderer, title, 30, 7, 16586941, false);
         Text coords = Text.literal("X: " + this.chunkX + " Z: " + this.chunkZ);
         context.drawText(manager.getClient().textRenderer, coords, 30, 18, 16777215, false);
         return startTime - this.startTime < 3000L ? Visibility.SHOW : Visibility.HIDE;
      }

      public Object getType() {
         return ChunkFinder.ChunkFinderToast.Type.INSTANCE;
      }

      public int getWidth() {
         return 160;
      }

      public int getHeight() {
         return 32;
      }

      public static class Type {
         public static final ChunkFinder.ChunkFinderToast.Type INSTANCE = new ChunkFinder.ChunkFinderToast.Type();
      }
   }
}
