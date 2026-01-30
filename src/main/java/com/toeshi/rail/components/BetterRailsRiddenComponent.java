package com.toeshi.rail.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.RailPlugin;
import javax.annotation.Nullable;

public class BetterRailsRiddenComponent implements Component<EntityStore> {

  public static final BuilderCodec<BetterRailsRiddenComponent> CODEC =
      BuilderCodec
          .builder(BetterRailsRiddenComponent.class,
                   BetterRailsRiddenComponent::new)
          .build();

  private Ref<EntityStore> riderRef; // Not serialized, managed at runtime

  public BetterRailsRiddenComponent() {}

  public BetterRailsRiddenComponent(Ref<EntityStore> riderRef) {
    this.riderRef = riderRef;
  }

  public BetterRailsRiddenComponent(BetterRailsRiddenComponent other) {
    this.riderRef = other.riderRef;
  }

  @Nullable
  @Override
  public Component<EntityStore> clone() {
    return new BetterRailsRiddenComponent(this);
  }

  public static ComponentType<EntityStore, BetterRailsRiddenComponent>
  getComponentType() {
    return RailPlugin.get().getBetterRailsRiddenComponentType();
  }

  public Ref<EntityStore> getRiderRef() { return riderRef; }

  public void setRiderRef(Ref<EntityStore> ref) { this.riderRef = ref; }
}
