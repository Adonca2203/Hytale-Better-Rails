package com.toeshi.rail.systems;

import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProvider;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdater;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdaterSymplecticEuler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.components.BetterRailsRiddenComponent;
import com.toeshi.rail.providers.MinecartForceProvider;
import javax.annotation.Nonnull;

public class MinecartPhysicsSystem extends EntityTickingSystem<EntityStore> {
  public static final double MAX_SPEED = 40.0;

  private final PhysicsBodyStateUpdater updater =
      new PhysicsBodyStateUpdaterSymplecticEuler();
  private final PhysicsBodyState stateBefore = new PhysicsBodyState();
  private final PhysicsBodyState stateAfter = new PhysicsBodyState();
  private boolean onGround = true; // Testing

  private final Query<EntityStore> query =
      Query.and(MinecartComponent.getComponentType(),
                TransformComponent.getComponentType(),
                Velocity.getComponentType(), PhysicsValues.getComponentType());

  @Override
  public void tick(float dt, int index, ArchetypeChunk<EntityStore> chunk,
                   Store<EntityStore> store,
                   CommandBuffer<EntityStore> commandBuffer) {
    TransformComponent cartTransform =
        chunk.getComponent(index, TransformComponent.getComponentType());
    Velocity velocity = chunk.getComponent(index, Velocity.getComponentType());
    PhysicsValues physics =
        chunk.getComponent(index, PhysicsValues.getComponentType());

    stateBefore.position.assign(cartTransform.getPosition());
    velocity.assignVelocityTo(stateBefore.velocity);

    ForceProvider[] providers = {new MinecartForceProvider(physics)};

    updater.update(stateBefore, stateAfter, physics.getMass(), dt, onGround,
                   providers);

    Vector3d vel = stateAfter.velocity;
    double speed = vel.length();

    if (speed > MAX_SPEED)
      vel.scale(MAX_SPEED / speed);

    cartTransform.getPosition().assign(stateAfter.position);
    velocity.set(stateAfter.velocity);
  }

  @Nonnull
  @Override
  public Query<EntityStore> getQuery() {
    return query;
  }
}
