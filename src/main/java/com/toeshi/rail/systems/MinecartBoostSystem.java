package com.toeshi.rail.systems;

import com.hypixel.hytale.builtin.mounts.MountSystems;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class MinecartBoostSystem extends EntityTickingSystem<EntityStore> {

  private final Query<EntityStore> query =
      Query.or(MountedComponent.getComponentType());
  private final Set<Dependency<EntityStore>> deps = Set.of(
      new SystemDependency<>(Order.AFTER, MountSystems.HandleMountInput.class));

  private static final Map<Ref<EntityStore>, Vector3d> lastPosition =
      new HashMap<>();

  // Debounce
  private static final Map<Ref<EntityStore>, Long> lastBoostTime =
      new HashMap<>();
  private static final long BOOST_COOLDOWN_MS = 1000; // 1 second between boosts
  private static final double BOOST_AMOUNT = 10.0;    // Single large boost

  @Override
  public void tick(float dt, int index,
                   @Nonnull ArchetypeChunk<EntityStore> chunk,
                   @Nonnull Store<EntityStore> store,
                   @Nonnull CommandBuffer<EntityStore> commandBuffer) {

    World world = ((EntityStore)store.getExternalData()).getWorld();

    MountedComponent mounted =
        chunk.getComponent(index, MountedComponent.getComponentType());

    if (mounted == null ||
        mounted.getControllerType() != MountController.Minecart) {
      return;
    }

    Ref<EntityStore> minecartRef = mounted.getMountedToEntity();
    TransformComponent transform = commandBuffer.getComponent(
        minecartRef, TransformComponent.getComponentType());

    if (transform == null) {
      return;
    }

    Vector3d position = transform.getPosition();

    // Check block below minecart
    int blockX = (int)Math.floor(position.x);
    int blockY = (int)Math.floor(position.y - 1);
    int blockZ = (int)Math.floor(position.z);

    long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
    WorldChunk worldChunk = world.getChunkIfInMemory(chunkIndex);

    if (worldChunk == null) {
      return;
    }

    BlockType blockType = worldChunk.getBlockType(blockX, blockY, blockZ);

    if (blockType == null || !blockType.getId().equals("Example_Block")) {
      // Track last position for direction
      lastPosition.put(minecartRef, position.clone());
      return;
    }

    // Debounce
    long currentTime = System.currentTimeMillis();
    Long lastBoost = lastBoostTime.get(minecartRef);

    if (lastBoost != null && (currentTime - lastBoost) < BOOST_COOLDOWN_MS) {
      return;
    }

    // Calculate direction
    Vector3d lastPos = lastPosition.get(minecartRef);
    Vector3d direction;

    if (lastPos != null) {
      direction =
          new Vector3d(position.x - lastPos.x, 0, position.z - lastPos.z);
      double speed = direction.length();

      if (speed > 0.001) {
        direction.normalize();
      } else {
        direction = new Vector3d(0, 0, 1);
      }
    } else {
      direction = new Vector3d(0, 0, 1);
    }

    direction.scale(BOOST_AMOUNT);
    // Why is this not working :(
    position.add(direction);

    lastBoostTime.put(minecartRef, currentTime);

    // Cleanup
    if (lastPosition.size() > 100) {
      lastPosition.entrySet().removeIf(entry -> !entry.getKey().isValid());
      lastBoostTime.entrySet().removeIf(entry -> !entry.getKey().isValid());
    }
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Nonnull
  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return deps;
  }
}
