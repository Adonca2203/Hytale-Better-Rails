package com.toeshi.rail.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.toeshi.rail.RailPlugin;
import javax.annotation.Nullable;

public class BetterRailsRiderComponent implements Component<EntityStore> {

  public static final BuilderCodec<BetterRailsRiderComponent> CODEC =
      BuilderCodec
          .builder(BetterRailsRiderComponent.class,
                   BetterRailsRiderComponent::new)
          .append(new KeyedCodec<>("AttachmentOffsetX", Codec.FLOAT),
                  (data, value)
                      -> data.attachmentOffsetX = value,
                  BetterRailsRiderComponent::getAttachmentOffsetX)
          .add()
          .append(new KeyedCodec<>("AttachmentOffsetY", Codec.FLOAT),
                  (data, value)
                      -> data.attachmentOffsetY = value,
                  BetterRailsRiderComponent::getAttachmentOffsetY)
          .add()
          .append(new KeyedCodec<>("AttachmentOffsetZ", Codec.FLOAT),
                  (data, value)
                      -> data.attachmentOffsetZ = value,
                  BetterRailsRiderComponent::getAttachmentOffsetZ)
          .add()
          .build();

  private float attachmentOffsetX;
  private float attachmentOffsetY;
  private float attachmentOffsetZ;
  private Ref<EntityStore> minecartRef; // Not serialized, managed at runtime

  public BetterRailsRiderComponent() {
    this.attachmentOffsetX = 0.0F;
    this.attachmentOffsetY = 1.0F;
    this.attachmentOffsetZ = 0.3F;
  }

  public BetterRailsRiderComponent(Ref<EntityStore> minecartRef,
                                   Vector3f attachmentOffset) {
    this.minecartRef = minecartRef;
    this.attachmentOffsetX = attachmentOffset.x;
    this.attachmentOffsetY = attachmentOffset.y;
    this.attachmentOffsetZ = attachmentOffset.z;
  }

  public BetterRailsRiderComponent(BetterRailsRiderComponent other) {
    this.minecartRef = other.minecartRef;
    this.attachmentOffsetX = other.attachmentOffsetX;
    this.attachmentOffsetY = other.attachmentOffsetY;
    this.attachmentOffsetZ = other.attachmentOffsetZ;
  }

  @Nullable
  @Override
  public Component<EntityStore> clone() {
    return new BetterRailsRiderComponent(this);
  }

  public static ComponentType<EntityStore, BetterRailsRiderComponent>
  getComponentType() {
    return RailPlugin.get().getBetterRailsRiderComponentType();
  }

  public Ref<EntityStore> getMinecartRef() { return minecartRef; }

  public void setMinecartRef(Ref<EntityStore> ref) { this.minecartRef = ref; }

  public float getAttachmentOffsetX() { return attachmentOffsetX; }

  public float getAttachmentOffsetY() { return attachmentOffsetY; }

  public float getAttachmentOffsetZ() { return attachmentOffsetZ; }

  public Vector3f getAttachmentOffset() {
    return new Vector3f(attachmentOffsetX, attachmentOffsetY,
                        attachmentOffsetZ);
  }

  public void setAttachmentOffset(Vector3f offset) {
    this.attachmentOffsetX = offset.x;
    this.attachmentOffsetY = offset.y;
    this.attachmentOffsetZ = offset.z;
  }
}
