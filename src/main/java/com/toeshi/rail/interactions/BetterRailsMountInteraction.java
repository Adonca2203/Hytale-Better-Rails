package com.toeshi.rail.interactions;

import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.interaction.MountNPC;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.RailPlugin;
import com.toeshi.rail.components.BetterRailsRiddenComponent;
import com.toeshi.rail.components.BetterRailsRiderComponent;
import javax.annotation.Nonnull;

public class BetterRailsMountInteraction extends SimpleInstantInteraction {

  @Nonnull public static final BuilderCodec<BetterRailsMountInteraction> CODEC;

  static {
    CODEC =
        BuilderCodec
            .builder(BetterRailsMountInteraction.class,
                     BetterRailsMountInteraction::new,
                     SimpleInstantInteraction.CODEC)
            .appendInherited(
                new KeyedCodec<>("AttachmentOffset", ProtocolCodecs.VECTOR3F),
                (o, v)
                    -> o.attachmentOffset = new Vector3f(v.x, v.y, v.z),
                o
                -> new Vector3f(o.attachmentOffset.x, o.attachmentOffset.y,
                                o.attachmentOffset.z),
                (o, p) -> o.attachmentOffset = p.attachmentOffset)
            .add()
            .build();
  }

  private Vector3f attachmentOffset = new Vector3f(0.0F, 1.0F, 0.3F);

  @Override
  protected void firstRun(@Nonnull InteractionType type,
                          @Nonnull InteractionContext context,
                          @Nonnull CooldownHandler cooldownHandler) {
    Ref<EntityStore> minecartRef = context.getTargetEntity();
    if (minecartRef == null) {
      context.getState().state = InteractionState.Failed;
      return;
    }

    Ref<EntityStore> playerRef = context.getEntity();
    CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

    Player playerComponent =
        commandBuffer.getComponent(playerRef, Player.getComponentType());
    if (playerComponent == null) {
      context.getState().state = InteractionState.Failed;
      return;
    }

    BetterRailsRiderComponent alreadyRiding = commandBuffer.getComponent(
        playerRef, BetterRailsRiderComponent.getComponentType());

    if (alreadyRiding != null) {
      commandBuffer.removeComponent(
          playerRef, BetterRailsRiderComponent.getComponentType());

      Ref<EntityStore> oldMinecart = alreadyRiding.getMinecartRef();
      if (oldMinecart != null && oldMinecart.isValid()) {
        commandBuffer.removeComponent(
            oldMinecart, BetterRailsRiddenComponent.getComponentType());
      }

      playerComponent.setMountEntityId(0);

      RailPlugin.LOGGER.atInfo().log("Player dismounted");
      return;
    }

    BetterRailsRiddenComponent alreadyRidden = commandBuffer.getComponent(
        minecartRef, BetterRailsRiddenComponent.getComponentType());
    if (alreadyRidden != null) {
      context.getState().state = InteractionState.Failed;
      RailPlugin.LOGGER.atInfo().log("Minecart already has a rider");
      return;
    }

    MinecartComponent minecart = commandBuffer.getComponent(
        minecartRef, MinecartComponent.getComponentType());
    if (minecart == null) {
      context.getState().state = InteractionState.Failed;
      return;
    }

    NetworkId minecartNetworkId =
        commandBuffer.getComponent(minecartRef, NetworkId.getComponentType());
    if (minecartNetworkId == null) {
      context.getState().state = InteractionState.Failed;
      return;
    }

    commandBuffer.addComponent(
        playerRef, BetterRailsRiderComponent.getComponentType(),
        new BetterRailsRiderComponent(minecartRef, this.attachmentOffset));

    commandBuffer.addComponent(minecartRef,
                               BetterRailsRiddenComponent.getComponentType(),
                               new BetterRailsRiddenComponent(playerRef));

    RailPlugin.LOGGER.atInfo().log("Player mounted");
  }
}
