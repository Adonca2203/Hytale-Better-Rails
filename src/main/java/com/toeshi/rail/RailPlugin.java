package com.toeshi.rail;

import com.hypixel.hytale.builtin.mounts.MountSystems;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.components.BetterRailsRiddenComponent;
import com.toeshi.rail.components.BetterRailsRiderComponent;
import com.toeshi.rail.interactions.BetterRailsMountInteraction;
import com.toeshi.rail.interactions.RailBoostInteraction;
import com.toeshi.rail.systems.EnableMinecartPhysics;
import com.toeshi.rail.systems.MinecartInputSystem;
import com.toeshi.rail.systems.MinecartPhysicsSystem;

public class RailPlugin extends JavaPlugin {
  public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private ComponentType<EntityStore, BetterRailsRiderComponent>
      riderComponentType;
  private ComponentType<EntityStore, BetterRailsRiddenComponent>
      riddenComponentType;
  private static RailPlugin instance;

  public RailPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  @Override
  protected void setup() {
    super.setup();

    LOGGER.atInfo().log("Setting up better rail");

    Interaction.CODEC.register("RailBoost", RailBoostInteraction.class,
                               RailBoostInteraction.CODEC);

    Interaction.CODEC.register("BetterRailsMountInteraction",
                               BetterRailsMountInteraction.class,
                               BetterRailsMountInteraction.CODEC);
    // this.getEntityStoreRegistry().registerSystem(new MinecartBoostSystem());

    this.riderComponentType = this.getEntityStoreRegistry().registerComponent(
        BetterRailsRiderComponent.class, "BetterRailsRiderComponent",
        BetterRailsRiderComponent.CODEC);
    this.riddenComponentType = this.getEntityStoreRegistry().registerComponent(
        BetterRailsRiddenComponent.class, "BetterRailsRiddenComponent",
        BetterRailsRiddenComponent.CODEC);

    this.getEntityStoreRegistry().registerSystem(new EnableMinecartPhysics());
    this.getEntityStoreRegistry().registerSystem(new MinecartInputSystem());
    this.getEntityStoreRegistry().registerSystem(new MinecartPhysicsSystem());
  }

  @Override
  protected void start() {
    EntityStore.REGISTRY.unregisterSystem(MountSystems.HandleMountInput.class);
  }

  public static RailPlugin get() { return instance; }

  public ComponentType<EntityStore, BetterRailsRiderComponent>
  getBetterRailsRiderComponentType() {
    return this.riderComponentType;
  }

  public ComponentType<EntityStore, BetterRailsRiddenComponent>
  getBetterRailsRiddenComponentType() {
    return this.riddenComponentType;
  }
}
