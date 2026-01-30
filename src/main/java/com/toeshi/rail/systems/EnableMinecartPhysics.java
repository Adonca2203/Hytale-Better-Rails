package com.toeshi.rail.systems;

import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EnableMinecartPhysics extends HolderSystem<EntityStore> {
  private final double mass = 0.2;
  private final double dragCoefficient = 0.1;
  private final boolean invertedGravity = false;

  @Override
  public Query<EntityStore> getQuery() {
    return MinecartComponent.getComponentType();
  }

  @Override
  public void onEntityAdd(Holder<EntityStore> holder, AddReason reason,
                          Store<EntityStore> store) {
    holder.ensureComponent(Velocity.getComponentType());

    PhysicsValues physics =
        new PhysicsValues(mass, dragCoefficient, invertedGravity);
    PhysicsValues attached =
        holder.ensureAndGetComponent(PhysicsValues.getComponentType());

    attached.replaceValues(physics);
  }

  @Override
  public void onEntityRemoved(Holder<EntityStore> holder, RemoveReason reason,
                              Store<EntityStore> store) {}
}
