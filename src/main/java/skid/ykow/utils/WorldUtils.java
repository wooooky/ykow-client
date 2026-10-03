package skid.ykow.utils;

import skid.ykow.Ykow;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.chunk.WorldChunk;

public final class WorldUtils {
   private static final MinecraftClient mc = Ykow.mc;

   public static boolean isDeadBodyNearby() {
      return mc.world
         .getPlayers()
         .parallelStream()
         .filter(player -> player != mc.player)
         .filter(player -> player.squaredDistanceTo(mc.player) <= 36.0)
         .anyMatch(LivingEntity::isDead);
   }

   public static Entity findNearestEntity(PlayerEntity targetPlayer, float radius, boolean seeOnly) {
      float minDistance = Float.MAX_VALUE;
      Entity nearestEntity = null;

      assert mc.world != null;

      for (Entity entity : mc.world.getEntities()) {
         float distance = entity.distanceTo(targetPlayer);
         if (entity != targetPlayer && distance <= radius && mc.player.canSee(entity) == seeOnly && distance < minDistance) {
            minDistance = distance;
            nearestEntity = entity;
         }
      }

      return nearestEntity;
   }

   public static double getDistance(Vec3d fromVec, Vec3d toVec) {
      return Math.sqrt(Math.pow(toVec.x - fromVec.x, 2.0) + Math.pow(toVec.y - fromVec.y, 2.0) + Math.pow(toVec.z - fromVec.z, 2.0));
   }

   public static PlayerEntity findNearestPlayer(PlayerEntity targetPlayer, float range, boolean seeOnly, boolean excludeFriends) {
      float minDistance = Float.MAX_VALUE;
      PlayerEntity nearestPlayer = null;

      for (PlayerEntity player : mc.world.getPlayers()) {
         float distance = (float)getDistance(targetPlayer.getPos(), player.getPos());
         if (player != targetPlayer && distance <= range && player.canSee(targetPlayer) == seeOnly && distance < minDistance) {
            minDistance = distance;
            nearestPlayer = player;
         }
      }

      return nearestPlayer;
   }

   public static Vec3d getPlayerLookVec(float yaw, float pitch) {
      float pitchRadians = pitch * (float) (Math.PI / 180.0);
      float yawRadians = -yaw * (float) (Math.PI / 180.0);
      float cosYaw = MathHelper.cos(yawRadians);
      float sinYaw = MathHelper.sin(yawRadians);
      float cosPitch = MathHelper.cos(pitchRadians);
      float sinPitch = MathHelper.sin(pitchRadians);
      return new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
   }

   public static Vec3d getPlayerLookVec(PlayerEntity player) {
      return getPlayerLookVec(player.getYaw(), player.getPitch());
   }

   public static HitResult getHitResult(double radius) {
      return getHitResult(mc.player, false, mc.player.getYaw(), mc.player.getPitch(), radius);
   }

   public static HitResult getHitResult(PlayerEntity entity, boolean ignoreInvisibles, float yaw, float pitch, double distance) {
      if (entity != null && mc.world != null) {
         Vec3d cameraPosVec = entity.getCameraPosVec(RenderTickCounter.ONE.getTickDelta(true));
         Vec3d rotationVec = getPlayerLookVec(yaw, pitch);
         Vec3d range = cameraPosVec.add(rotationVec.x * distance, rotationVec.y * distance, rotationVec.z * distance);
         HitResult result = mc.world.raycast(new RaycastContext(cameraPosVec, range, ShapeType.OUTLINE, FluidHandling.NONE, entity));
         double squaredDistance = distance * distance;
         if (result != null) {
            squaredDistance = result.getPos().squaredDistanceTo(cameraPosVec);
         }

         Vec3d vec3d3 = cameraPosVec.add(rotationVec.x * distance, rotationVec.y * distance, rotationVec.z * distance);
         Box box = entity.getBoundingBox().stretch(rotationVec.multiply(distance)).expand(1.0, 1.0, 1.0);
         EntityHitResult entityHitResult = ProjectileUtil.raycast(
            entity,
            cameraPosVec,
            vec3d3,
            box,
            entityx -> !entityx.isSpectator() && entityx.canHit() && (!entityx.isInvisible() || !ignoreInvisibles),
            squaredDistance
         );
         if (entityHitResult != null) {
            Vec3d vec3d4 = entityHitResult.getPos();
            double entitySquaredDistance = cameraPosVec.squaredDistanceTo(vec3d4);
            if (distance > distance && entitySquaredDistance > Math.pow(distance, 2.0) || entitySquaredDistance < squaredDistance || result == null) {
               result = (HitResult)(entitySquaredDistance > Math.pow(distance, 2.0)
                  ? BlockHitResult.createMissed(vec3d4, Direction.getFacing(rotationVec.x, rotationVec.y, rotationVec.z), BlockPos.ofFloored(vec3d4))
                  : entityHitResult);
            }
         }

         return result;
      } else {
         return null;
      }
   }

   public static void placeBlock(BlockHitResult blockHit, boolean swingHand) {
      ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
      if (result.isAccepted() && result.shouldSwingHand() && swingHand) {
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   public static Stream<WorldChunk> getLoadedChunks() {
      int viewRadius = Math.max(2, mc.options.getClampedViewDistance()) + 3;
      int diameter = viewRadius * 2 + 1;
      ChunkPos centerChunk = mc.player.getChunkPos();
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
         .filter(chunkPos -> mc.world.isChunkLoaded(chunkPos.x, chunkPos.z))
         .map(chunkPos -> mc.world.getChunk(chunkPos.x, chunkPos.z))
         .filter(Objects::nonNull);
   }

   public static boolean isShieldFacingAway(PlayerEntity player) {
      if (mc.player != null && player != null) {
         Vec3d playerPos = mc.player.getPos();
         Vec3d targetPos = player.getPos();
         Vec3d directionToPlayer = playerPos.subtract(targetPos).normalize();
         float yaw = player.getYaw();
         float pitch = player.getPitch();
         Vec3d facingDirection = new Vec3d(
               -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)),
               -Math.sin(Math.toRadians(pitch)),
               Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))
            )
            .normalize();
         double dotProduct = facingDirection.dotProduct(directionToPlayer);
         return dotProduct < 0.0;
      } else {
         return false;
      }
   }

   public static boolean isTool(ItemStack itemStack) {
      if (!(itemStack.getItem() instanceof ToolItem)) {
         return false;
      } else {
         ToolMaterial material = ((ToolItem)itemStack.getItem()).getMaterial();
         return material == ToolMaterials.DIAMOND || material == ToolMaterials.NETHERITE;
      }
   }

   public static boolean isCrit(PlayerEntity player, Entity target) {
      return player.getAttackCooldownProgress(0.5F) > 0.9F
         && player.fallDistance > 0.0F
         && !player.isOnGround()
         && !player.isClimbing()
         && !player.isSubmergedInWater()
         && !player.hasStatusEffect(StatusEffects.BLINDNESS)
         && target instanceof LivingEntity;
   }

   public static void hitEntity(Entity entity, boolean swingHand) {
      mc.interactionManager.attackEntity(mc.player, entity);
      if (swingHand) {
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }
}
