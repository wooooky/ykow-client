package skid.ykow.utils;

import skid.ykow.Ykow;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class BlockUtil {
   public static Stream<WorldChunk> getLoadedChunks() {
      int viewRadius = Math.max(2, Ykow.mc.options.getClampedViewDistance()) + 3;
      int diameter = viewRadius * 2 + 1;
      ChunkPos centerChunk = Ykow.mc.player.getChunkPos();
      ChunkPos minChunk = new ChunkPos(centerChunk.x - viewRadius, centerChunk.z - viewRadius);
      ChunkPos maxChunk = new ChunkPos(centerChunk.x + viewRadius, centerChunk.z + viewRadius);
      return Stream.<ChunkPos>iterate(minChunk, currentPos -> {
            int nextX = currentPos.x;
            int nextZ = currentPos.z;
            if (++nextX > maxChunk.x) {
               nextX = minChunk.x;
               nextZ++;
            }

            if (nextZ > maxChunk.z) {
               throw new IllegalStateException("Stream limit didn't work.");
            } else {
               return new ChunkPos(nextX, nextZ);
            }
         })
         .limit((long)diameter * diameter)
         .filter(chunkPos -> Ykow.mc.world.isChunkLoaded(chunkPos.x, chunkPos.z))
         .map(chunkPos -> Ykow.mc.world.getChunk(chunkPos.x, chunkPos.z))
         .filter(Objects::nonNull);
   }

   public static Iterable<WorldChunk> getLoadedChunkss() {
      int viewRadius = Math.max(2, Ykow.mc.options.getClampedViewDistance()) + 3;
      int diameter = viewRadius * 2 + 1;
      ChunkPos centerChunk = Ykow.mc.player.getChunkPos();
      ChunkPos minChunk = new ChunkPos(centerChunk.x - viewRadius, centerChunk.z - viewRadius);
      ChunkPos maxChunk = new ChunkPos(centerChunk.x + viewRadius, centerChunk.z + viewRadius);
      return Stream.<ChunkPos>iterate(minChunk, currentPos -> {
            int nextX = currentPos.x;
            int nextZ = currentPos.z;
            if (++nextX > maxChunk.x) {
               nextX = minChunk.x;
               nextZ++;
            }

            if (nextZ > maxChunk.z) {
               throw new IllegalStateException("Stream limit didn't work.");
            } else {
               return new ChunkPos(nextX, nextZ);
            }
         })
         .limit((long)diameter * diameter)
         .filter(chunkPos -> Ykow.mc.world.isChunkLoaded(chunkPos.x, chunkPos.z))
         .map(chunkPos -> Ykow.mc.world.getChunk(chunkPos.x, chunkPos.z))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   public static boolean isBlockAtPosition(BlockPos blockPos, Block block) {
      return Ykow.mc.world.getBlockState(blockPos).getBlock() == block;
   }

   public static boolean isRespawnAnchorCharged(BlockPos blockPos) {
      return isBlockAtPosition(blockPos, Blocks.RESPAWN_ANCHOR) && (Integer)Ykow.mc.world.getBlockState(blockPos).get(RespawnAnchorBlock.CHARGES) != 0;
   }

   public static boolean isRespawnAnchorUncharged(BlockPos blockPos) {
      return isBlockAtPosition(blockPos, Blocks.RESPAWN_ANCHOR) && (Integer)Ykow.mc.world.getBlockState(blockPos).get(RespawnAnchorBlock.CHARGES) == 0;
   }

   public static void interactWithBlock(BlockHitResult blockHitResult, boolean shouldSwingHand) {
      ActionResult result = Ykow.mc.interactionManager.interactBlock(Ykow.mc.player, Hand.MAIN_HAND, blockHitResult);
      if (result.isAccepted() && result.shouldSwingHand() && shouldSwingHand) {
         Ykow.mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   public static boolean canPlaceBlockClient(BlockPos blockPos) {
      return Ykow.mc.world.getBlockState(blockPos).isAir();
   }

   public static boolean isBlock(BlockPos blockPos, Block block) {
      return Ykow.mc.world.getBlockState(blockPos).getBlock() == block;
   }

   public static boolean isAnchorCharged(BlockPos blockPos) {
      return isRespawnAnchorCharged(blockPos);
   }
}
