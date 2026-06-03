package land.temmi.rollercoaster.physics;

import org.joml.Vector2f;

/**
 * Immutable result of an AABB collision query.
 *
 * <p>Holds the distance vector between the two boxes (negative components mean
 * overlap on that axis) and a flag indicating whether they actually intersect.</p>
 */
public class Collision {

	/** The distance vector of the collision. */
	private Vector2f distance;
	/** Whether the objects intersect. */
	private boolean intersecting;

	/**
	 * Creates a collision result.
	 *
	 * @param distance   the distance vector of the collision
	 * @param intersects whether the two objects intersect
	 */
	public Collision(Vector2f distance, boolean intersects) {
		this.distance = distance;
		this.intersecting = intersects;
	}

	/** @return the distance vector of the collision */
	public Vector2f getDistance() {
		return distance;
	}

	/** @return {@code true} if the objects intersect */
	public boolean isIntersecting() {
		return intersecting;
	}
}
