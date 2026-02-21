package com.toeshi.rail.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerInput;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSystems;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.RailPlugin;
import com.toeshi.rail.components.BetterRailsRiderComponent;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;

public class MinecartInputSystem extends EntityTickingSystem<EntityStore> {
  private static final double ACCELERATION_FORCE = 40.0;
  private final Query<EntityStore> query =
      Query.and(BetterRailsRiderComponent.getComponentType(),
                PlayerInput.getComponentType());

  private final Set<Dependency<EntityStore>> deps =
      Set.of(new SystemDependency<>(Order.BEFORE,
                                    PlayerSystems.ProcessPlayerInput.class));

  @Override
  @NonNull
  public Query<EntityStore> getQuery() {
    return query;
  }

  @Override
  @NonNull
  public Set<Dependency<EntityStore>> getDependencies() {
    return deps;
  }

  @Override
  public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk,
                   Store<EntityStore> store,
                   CommandBuffer<EntityStore> commandBuffer) {

    BetterRailsRiderComponent rider =
        chunk.getComponent(index, BetterRailsRiderComponent.getComponentType());

    assert rider != null;

    PlayerInput input =
        chunk.getComponent(index, PlayerInput.getComponentType());

    assert input != null;

    Ref<EntityStore> minecartRef = rider.getMinecartRef();

    if (minecartRef == null) {
      return;
    }

    Velocity velocity =
        commandBuffer.getComponent(minecartRef, Velocity.getComponentType());
    TransformComponent transform = commandBuffer.getComponent(
        minecartRef, TransformComponent.getComponentType());

    if (velocity == null || transform == null)
      return;

    List<PlayerInput.InputUpdate> queue = input.getMovementUpdateQueue();
    Vector3d inputDirection = new Vector3d();
    Vector3d lastPosition = transform.getPosition().clone();

    for (int i = 0; i < queue.size(); i++) {
      PlayerInput.InputUpdate update = queue.get(i);

      if (update instanceof PlayerInput.AbsoluteMovement abs) {
        double dx = abs.getX() - lastPosition.x;
        double dz = abs.getZ() - lastPosition.z;
        inputDirection.add(dx, 0, dz);
        lastPosition.assign(abs.getX(), abs.getY(), abs.getZ());
      } else if (update instanceof PlayerInput.RelativeMovement rel) {
        inputDirection.add(rel.getX(), 0, rel.getZ());
      } else if (update instanceof PlayerInput.WishMovement wish) {
        inputDirection.add(wish.getX(), 0, wish.getZ());
      }
    }

    queue.clear();

    if (inputDirection.squaredLength() > 0.001) {
      inputDirection.normalize();

      Vector3d currentVel = velocity.getVelocity();
      double currentSpeed =
          Math.sqrt(currentVel.x * currentVel.x + currentVel.z * currentVel.z);

      if (currentSpeed >= MinecartPhysicsSystem.MAX_SPEED * 0.95) {
        RailPlugin.LOGGER.atInfo().log("At max speed, not applying more force");
        return;
      }

      if (currentSpeed > 0.1) {
        Vector3d velDir =
            new Vector3d(currentVel.x, 0, currentVel.z).normalize();
        double alignment = inputDirection.dot(velDir);

        double forceMagnitude =
            alignment >= 0 ? ACCELERATION_FORCE : ACCELERATION_FORCE * 3.0;

        inputDirection.assign(velDir).scale(alignment * forceMagnitude * dt);
      } else {
        inputDirection.scale(ACCELERATION_FORCE * dt);
      }

      velocity.addForce(inputDirection);
    }
  }
}
