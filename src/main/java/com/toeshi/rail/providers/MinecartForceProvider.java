package com.toeshi.rail.providers;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.util.ForceAccumulator;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProvider;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsConstants;
import com.toeshi.rail.RailPlugin;

public class MinecartForceProvider implements ForceProvider {
  private static final double RAIL_FRICTION = 2.0;
  private final PhysicsValues physics;

  public MinecartForceProvider(PhysicsValues physics) {
    this.physics = physics;
  }

  @Override
  public void update(PhysicsBodyState state, ForceAccumulator accumulator,
                     boolean onGround) {

    double mass = physics.getMass();
    double speed = accumulator.speed;

    if (!onGround) {
      accumulator.force.y -= PhysicsConstants.GRAVITY_ACCELERATION * mass;
    } else {
      Vector3d railDir = new Vector3d(state.velocity);
      if (railDir.squaredLength() > 0.00001) {
        railDir.normalize();

        Vector3d gravity =
            new Vector3d(0, -PhysicsConstants.GRAVITY_ACCELERATION * mass, 0);

        double projected = gravity.dot(railDir);
        Vector3d gravityAlongRail = new Vector3d(railDir).scale(projected);

        accumulator.force.add(gravityAlongRail);
      }
    }

    if (speed > 0.001) {
      double frictionForce = RAIL_FRICTION * mass;
      Vector3d friction =
          new Vector3d(state.velocity).normalize().scale(-frictionForce);
      accumulator.force.add(friction);

      double dragCoeff = physics.getDragCoefficient();
      double dragForce = dragCoeff * speed * speed;
      Vector3d drag =
          new Vector3d(state.velocity).normalize().scale(-dragForce);
      accumulator.force.add(drag);
    }
  }
}
