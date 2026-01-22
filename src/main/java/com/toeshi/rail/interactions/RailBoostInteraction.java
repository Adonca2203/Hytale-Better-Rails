package com.toeshi.rail.interactions;

import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.RailPlugin;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RailBoostInteraction extends SimpleInstantInteraction {

  @Nonnull public static final BuilderCodec<RailBoostInteraction> CODEC;

  private static final Map<Ref<EntityStore>, Long> lastBoostTime =
      new HashMap<>();
  private static final long BOOST_COOLDOWN_MS = 500;

  static {
    CODEC =
        BuilderCodec
            .builder(RailBoostInteraction.class, RailBoostInteraction::new,
                     SimpleInstantInteraction.CODEC)
            .documentation("Applies a speed boost using knockback component.")
            .appendInherited(
                new KeyedCodec<>("BoostAmount", Codec.DOUBLE),
                (interaction, amount)
                    -> interaction.boostAmount = amount,
                interaction
                -> interaction.boostAmount,
                (interaction,
                 parent) -> interaction.boostAmount = parent.boostAmount)
            .addValidator(Validators.nonNull())
            .documentation("The boost amount to apply")
            .add()
            .build();
  }

  @Nullable private Double boostAmount = 10.0;

  @Override
  protected void firstRun(@Nonnull InteractionType type,
                          @Nonnull InteractionContext context,
                          @Nonnull CooldownHandler cooldownHandler) {

    assert this.boostAmount != null;

    CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
    assert commandBuffer != null;

    Ref<EntityStore> entityRef = context.getEntity();

    MountedComponent mounted = commandBuffer.getComponent(
        entityRef, MountedComponent.getComponentType());

    if (mounted == null ||
        mounted.getControllerType() != MountController.Minecart)
      return;

    long currentTime = System.currentTimeMillis();
    Long lastBoost = lastBoostTime.get(entityRef);

    if (lastBoost != null && (currentTime - lastBoost) < BOOST_COOLDOWN_MS) {
      return;
    }

    Ref<EntityStore> minecartRef = mounted.getMountedToEntity();
    if (minecartRef == null)
      return;

    MinecartComponent minecart = commandBuffer.getComponent(
        minecartRef, MinecartComponent.getComponentType());

    if (minecart == null)
      return;

    TransformComponent transform = commandBuffer.getComponent(
        minecartRef, TransformComponent.getComponentType());

    if (transform == null) {
      return;
    }

    Velocity playerVel =
        commandBuffer.getComponent(entityRef, Velocity.getComponentType());

    TransformComponent playerTransform = commandBuffer.getComponent(
        entityRef, TransformComponent.getComponentType());

    Vector3d direction =
        new Vector3d(playerVel.getVelocity().x, 0, playerVel.getVelocity().z)
            .normalize()
            .scale(this.boostAmount);

    transform.getPosition().add(direction);

    // Cleanup
    if (lastBoostTime.size() > 100) {
      lastBoostTime.entrySet().removeIf(
          entry
          -> !entry.getKey().isValid() ||
                 (currentTime - entry.getValue()) > 10000);
    }
  }

  private void applyBoost(@Nonnull Ref<EntityStore> entityRef,
                          @Nonnull CommandBuffer<EntityStore> commandBuffer) {}
}
