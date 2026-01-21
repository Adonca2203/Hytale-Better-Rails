package com.toeshi.rail;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

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
  }
}
