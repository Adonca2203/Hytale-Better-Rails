package com.toeshi.rail;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.toeshi.rail.interactions.RailBoostInteraction;
import com.toeshi.rail.systems.MinecartBoostSystem;

public class RailPlugin extends JavaPlugin {
  public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static RailPlugin instance;

  public RailPlugin(JavaPluginInit init) {
    super(init);
    instance = this;
  }

  @Override
  protected void setup() {
    super.setup();

    LOGGER.atInfo().log("Setting up better rail");

    // None of these are properly boosting the minecart and idk why
    // TODO: Figure it out
    // Looks like the current implementation for mounted input doesn't use the physics system
    // might try to rewrite it or smth
    Interaction.CODEC.register("RailBoost", RailBoostInteraction.class, RailBoostInteraction.CODEC);
    this.getEntityStoreRegistry().registerSystem(new MinecartBoostSystem());
  }
}
