package skid.ykow.module.modules.donut;

import skid.ykow.event.EventListener;
import skid.ykow.event.events.ChunkDataEvent;
import skid.ykow.event.events.Render3DEvent;
import skid.ykow.event.events.SetBlockStateEvent;
import skid.ykow.module.Category;
import skid.ykow.module.Module;
import skid.ykow.module.setting.NumberSetting;
import skid.ykow.module.setting.Setting;
import skid.ykow.module.setting.StringSetting;
import skid.ykow.utils.BlockUtil;
import skid.ykow.utils.EncryptedString;
import skid.ykow.utils.RenderUtils;
import skid.ykow.utils.meteorrejects.Ore;
import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.ChunkRandom.RandomProvider;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public final class NetheriteFinder extends Module {
   private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 1.0, 255.0, 125.0, 1.0);
   private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 1.0, 10.0, 5.0, 1.0);
   private final StringSetting customSeed = new StringSetting(EncryptedString.of("Custom Seed"), "6608149111735331168");
   private final Map<Long, Map<Ore, Set<Vec3d>>> chunkOreData = new ConcurrentHashMap<>();
   private Map<RegistryKey<Biome>, List<Ore>> biomeOreMap;

   public NetheriteFinder() {
      super(EncryptedString.of("Netherite Finder"), EncryptedString.of("Finds netherites"), -1, Category.DONUT);
      this.addsettings(new Setting[]{this.alpha, this.range, this.customSeed});
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.initializeOreLocations();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.chunkOreData.clear();
   }

   @EventListener
   public void onRender3D(Render3DEvent renderEvent) {
      if (this.mc.player != null && this.biomeOreMap != null) {
         Camera camera = this.mc.gameRenderer.getCamera();
         if (camera != null) {
            MatrixStack matrixStack = renderEvent.matrixStack;
            renderEvent.matrixStack.push();
            Vec3d cameraPos = camera.getPos();
            RotationAxis xAxis = RotationAxis.POSITIVE_X;
            matrixStack.multiply(xAxis.rotationDegrees(camera.getPitch()));
            RotationAxis yAxis = RotationAxis.POSITIVE_Y;
            matrixStack.multiply(yAxis.rotationDegrees(camera.getYaw() + 180.0F));
            matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
         }

         int playerChunkX = this.mc.player.getChunkPos().x;
         int playerChunkZ = this.mc.player.getChunkPos().z;
         int renderRange = this.range.getIntValue();

         for (int x = playerChunkX - renderRange; x <= playerChunkX + renderRange; x++) {
            for (int z = playerChunkZ - renderRange; z <= playerChunkZ + renderRange; z++) {
               this.renderChunk(x, z, renderEvent);
            }
         }

         renderEvent.matrixStack.pop();
      }
   }

   private void renderChunk(int chunkX, int chunkZ, Render3DEvent renderEvent) {
      long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
      Map<Ore, Set<Vec3d>> chunkData = this.chunkOreData.get(chunkKey);
      if (chunkData != null) {
         for (Entry<Ore, Set<Vec3d>> entry : chunkData.entrySet()) {
            for (Vec3d pos : entry.getValue()) {
               MatrixStack matrixStack = renderEvent.matrixStack;
               float x = (float)pos.x;
               float y = (float)pos.y;
               float z = (float)pos.z;
               RenderUtils.renderFilledBox(
                  matrixStack, x, y, z, (float)(pos.x + 1.0), (float)(pos.y + 1.0), (float)(pos.z + 1.0), this.getAlphaColor(this.alpha.getIntValue())
               );
            }
         }
      }
   }

   private void initializeOreLocations() {
      this.chunkOreData.clear();
      if (this.mc.world != null) {
         this.biomeOreMap = Ore.register();
         this.populateOreLocations();
      }
   }

   private Color getAlphaColor(int alphaValue) {
      return new Color(191, 64, 191, alphaValue);
   }

   @EventListener
   public void onChunkDataReceived(ChunkDataEvent chunkEvent) {
      if (this.biomeOreMap == null && this.mc.world != null) {
         this.biomeOreMap = Ore.register();
      }

      ClientWorld world = this.mc.world;
      this.processChunk(world.getChunk(chunkEvent.packet.getChunkX(), chunkEvent.packet.getChunkZ()));
   }

   @EventListener
   public void onBlockStateChange(SetBlockStateEvent blockEvent) {
      if (blockEvent.oldState.getBlock().equals(Blocks.AIR)) {
         long chunkKey = ChunkPos.toLong(blockEvent.pos);
         Map<Ore, Set<Vec3d>> chunkData = this.chunkOreData.get(chunkKey);
         if (chunkData != null) {
            Vec3d blockPos = Vec3d.of(blockEvent.pos);

            for (Set<Vec3d> oreSet : chunkData.values()) {
               oreSet.remove(blockPos);
            }
         }
      }
   }

   private void populateOreLocations() {
      if (this.mc.player != null) {
         Iterator<WorldChunk> loadedChunks = BlockUtil.getLoadedChunks().iterator();

         while (loadedChunks.hasNext()) {
            this.processChunk((Chunk)loadedChunks.next());
         }
      }
   }

   private void processChunk(Chunk chunk) {
      if (this.biomeOreMap != null) {
         ChunkPos chunkPos = chunk.getPos();
         long chunkKey = chunkPos.toLong();
         ClientWorld world = this.mc.world;
         if (!this.chunkOreData.containsKey(chunkKey) && world != null) {
            HashSet<RegistryKey<Biome>> biomesInChunk = new HashSet<>();
            ChunkPos.stream(chunkPos, 1).forEach(chunkPos2 -> {
               Chunk neighborChunk = world.getChunk(chunkPos2.x, chunkPos2.z, ChunkStatus.BIOMES, false);
               if (neighborChunk != null) {
                  ChunkSection[] sections = neighborChunk.getSectionArray();

                  for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                     sections[sectionIndex].getBiomeContainer().forEachValue(biome -> biomesInChunk.add((RegistryKey)biome.getKey().get()));
                  }
               }
            });
            Set<Ore> oresInBiomes = biomesInChunk.stream()
               .flatMap(biome -> this.getOresForBiome((RegistryKey<Biome>)biome).stream())
               .collect(Collectors.toSet());
            int chunkStartX = chunkPos.x << 4;
            int chunkStartZ = chunkPos.z << 4;
            ChunkRandom random = new ChunkRandom(RandomProvider.XOROSHIRO.create(0L));

            long seedValue;
            try {
               seedValue = Long.parseLong(this.customSeed.getValue());
            } catch (NumberFormatException var25) {
               seedValue = 6608149111735331168L;
            }

            long populationSeed = random.setPopulationSeed(seedValue, chunkStartX, chunkStartZ);
            HashMap<Ore, Set<Vec3d>> oreLocations = new HashMap<>();

            for (Ore ore : oresInBiomes) {
               HashSet<Vec3d> orePositions = new HashSet<>();
               random.setDecoratorSeed(populationSeed, ore.featureIndex, ore.generationStep);
               int oreCount = ore.countProvider.get(random);

               for (int oreIndex = 0; oreIndex < oreCount; oreIndex++) {
                  if (ore.rarityChance != 1.0F) {
                     float rarityThreshold = 1.0F / ore.rarityChance;
                     if (random.nextFloat() >= rarityThreshold) {
                        continue;
                     }
                  }

                  int oreX = random.nextInt(16) + chunkStartX;
                  int oreZ = random.nextInt(16) + chunkStartZ;
                  int oreY = ore.heightProvider.get(random, ore.heightContext);
                  BlockPos orePos = new BlockPos(oreX, oreY, oreZ);
                  if (this.getOresForBiome((RegistryKey<Biome>)chunk.getBiomeForNoiseGen(oreX, oreY, oreZ).getKey().get()).contains(ore)) {
                     if (ore.isScatteredOre) {
                        orePositions.addAll(this.generateOreLocations(world, random, orePos, ore.oreSize));
                     } else {
                        orePositions.addAll(this.generateOreLocations(world, random, orePos, ore.oreSize, ore.discardOnAirChance));
                     }
                  }
               }

               if (!orePositions.isEmpty()) {
                  oreLocations.put(ore, orePositions);
               }
            }

            this.chunkOreData.put(chunkKey, oreLocations);
         }
      }
   }

   private List<Ore> getOresForBiome(RegistryKey<Biome> biome) {
      if (this.biomeOreMap == null) {
         this.biomeOreMap = Ore.register();
      }

      return this.biomeOreMap.containsKey(biome) ? this.biomeOreMap.get(biome) : this.biomeOreMap.values().stream().findAny().get();
   }

   private ArrayList<Vec3d> generateOreLocations(ClientWorld world, ChunkRandom random, BlockPos centerPos, int oreSize, float discardChance) {
      float angle = random.nextFloat() * (float) Math.PI;
      float radius = oreSize / 8.0F;
      int maxRadius = MathHelper.ceil((oreSize / 16.0F * 2.0F + 1.0F) / 2.0F);
      int centerX = centerPos.getX();
      int centerX2 = centerPos.getX();
      double sinAngle = Math.sin(angle);
      int centerZ = centerPos.getZ();
      int centerZ2 = centerPos.getZ();
      double cosAngle = Math.cos(angle);
      int centerY = centerPos.getY();
      int centerY2 = centerPos.getY();
      int minX = centerPos.getX() - MathHelper.ceil(radius) - maxRadius;
      int minY = centerPos.getY() - 2 - maxRadius;
      int minZ = centerPos.getZ() - MathHelper.ceil(radius) - maxRadius;
      int range = 2 * (MathHelper.ceil(radius) + maxRadius);

      for (int x = minX; x <= minX + range; x++) {
         for (int z = minZ; z <= minZ + range; z++) {
            if (minY <= world.getTopY(Type.MOTION_BLOCKING, x, z)) {
               return this.generateOreLocations(
                  world,
                  random,
                  oreSize,
                  centerX + Math.sin(angle) * radius,
                  centerX2 - sinAngle * radius,
                  centerZ + Math.cos(angle) * radius,
                  centerZ2 - cosAngle * radius,
                  centerY + random.nextInt(3) - 2,
                  centerY2 + random.nextInt(3) - 2,
                  minX,
                  minY,
                  minZ,
                  range,
                  2 * (2 + maxRadius),
                  discardChance
               );
            }
         }
      }

      return new ArrayList<>();
   }

   private ArrayList<Vec3d> generateOreLocations(
      ClientWorld world,
      ChunkRandom random,
      int oreSize,
      double startX,
      double endX,
      double startZ,
      double endZ,
      double startY,
      double endY,
      int minX,
      int minY,
      int minZ,
      int range,
      int heightRange,
      float discardChance
   ) {
      BitSet occupiedPositions = new BitSet(range * heightRange * range);
      Mutable blockPos = new Mutable();
      double[] orePoints = new double[oreSize * 4];
      ArrayList<Vec3d> oreLocations = new ArrayList<>();

      for (int pointIndex = 0; pointIndex < oreSize; pointIndex++) {
         float progress = (float)pointIndex / oreSize;
         double x = MathHelper.lerp(progress, startX, endX);
         double y = MathHelper.lerp(progress, startY, endY);
         double z = MathHelper.lerp(progress, startZ, endZ);
         orePoints[pointIndex * 4] = x;
         orePoints[pointIndex * 4 + 1] = y;
         orePoints[pointIndex * 4 + 2] = z;
         orePoints[pointIndex * 4 + 3] = ((MathHelper.sin((float) Math.PI * progress) + 1.0F) * (random.nextDouble() * oreSize / 16.0) + 1.0) / 2.0;
      }

      for (int i = 0; i < oreSize - 1; i++) {
         double radius1 = orePoints[i * 4 + 3];
         if (!(radius1 <= 0.0)) {
            for (int j = i + 1; j < oreSize; j++) {
               double radius2 = orePoints[j * 4 + 3];
               if (!(radius2 <= 0.0)) {
                  double x1 = orePoints[i * 4];
                  double x2 = orePoints[j * 4];
                  double dx = x1 - x2;
                  double y1 = orePoints[i * 4 + 1];
                  double y2 = orePoints[j * 4 + 1];
                  double dy = y1 - y2;
                  double z1 = orePoints[i * 4 + 2];
                  double z2 = orePoints[j * 4 + 2];
                  double dz = z1 - z2;
                  double r1 = orePoints[i * 4 + 3];
                  double r2 = orePoints[j * 4 + 3];
                  double dr = r1 - r2;
                  if (dr * dr > dx * dx + dy * dy + dz * dz) {
                     if (dr > 0.0) {
                        orePoints[j * 4 + 3] = -1.0;
                     } else {
                        orePoints[i * 4 + 3] = -1.0;
                     }
                  }
               }
            }
         }
      }

      for (int pointIndex = 0; pointIndex < oreSize; pointIndex++) {
         double radius = orePoints[pointIndex * 4 + 3];
         if (!(radius < 0.0)) {
            double centerX = orePoints[pointIndex * 4];
            double centerY = orePoints[pointIndex * 4 + 1];
            double centerZ = orePoints[pointIndex * 4 + 2];
            int minBlockX = Math.max(MathHelper.floor(centerX - radius), minX);
            int minBlockY = Math.max(MathHelper.floor(centerY - radius), minY);
            int minBlockZ = Math.max(MathHelper.floor(centerZ - radius), minZ);
            int maxBlockX = Math.max(MathHelper.floor(centerX + radius), minBlockX);
            int maxBlockY = Math.max(MathHelper.floor(centerY + radius), minBlockY);

            for (int maxBlockZ = Math.max(MathHelper.floor(centerZ + radius), minBlockZ); minBlockX <= maxBlockX; minBlockX++) {
               double normalizedX = (minBlockX + 0.5 - centerX) / radius;
               if (normalizedX * normalizedX < 1.0) {
                  for (; minBlockY <= maxBlockY; minBlockY++) {
                     double normalizedY = (minBlockY + 0.5 - centerY) / radius;
                     if (normalizedX * normalizedX + normalizedY * normalizedY < 1.0 && minBlockZ <= maxBlockZ) {
                        double normalizedZ = (minBlockZ + 0.5 - centerZ) / radius;
                        if (normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ < 1.0) {
                           int bitIndex = minBlockX - minX + (minBlockY - minY) * range + (minBlockZ - minZ) * range * heightRange;
                           if (!occupiedPositions.get(bitIndex)) {
                              occupiedPositions.set(bitIndex);
                              blockPos.set(minBlockX, minBlockY, minBlockZ);
                              if (minBlockY >= -64
                                 && minBlockY < 320
                                 && world.getBlockState(blockPos).isOpaque()
                                 && this.isValidOreLocation(world, blockPos, discardChance, random)) {
                                 oreLocations.add(new Vec3d(minBlockX, minBlockY, minBlockZ));
                              }
                           }
                        }

                        minBlockZ++;
                     }
                  }
               }
            }
         }
      }

      return oreLocations;
   }

   private boolean isValidOreLocation(ClientWorld world, BlockPos pos, float discardChance, ChunkRandom random) {
      if (discardChance != 0.0F && (discardChance == 1.0F || !(random.nextFloat() >= discardChance))) {
         Direction[] directions = Direction.values();

         for (Direction direction : directions) {
            if (!world.getBlockState(pos.add(direction.getVector())).isOpaque() && discardChance != 1.0F) {
               return false;
            }
         }
      }

      return true;
   }

   private ArrayList<Vec3d> generateOreLocations(ClientWorld world, ChunkRandom random, BlockPos centerPos, int oreSize) {
      ArrayList<Vec3d> oreLocations = new ArrayList<>();
      int actualOreSize = random.nextInt(oreSize + 1);

      for (int oreIndex = 0; oreIndex < actualOreSize; oreIndex++) {
         int maxOffset = Math.min(oreIndex, 7);
         int offsetX = this.random(random, maxOffset) + centerPos.getX();
         int offsetY = this.random(random, maxOffset) + centerPos.getY();
         int offsetZ = this.random(random, maxOffset) + centerPos.getZ();
         if (world.getBlockState(new BlockPos(offsetX, offsetY, offsetZ)).isOpaque()
            && this.isValidOreLocation(world, new BlockPos(offsetX, offsetY, offsetZ), 1.0F, random)) {
            oreLocations.add(new Vec3d(offsetX, offsetY, offsetZ));
         }
      }

      return oreLocations;
   }

   private int random(ChunkRandom random, int maxOffset) {
      return Math.round((random.nextFloat() - random.nextFloat()) * maxOffset);
   }
}
