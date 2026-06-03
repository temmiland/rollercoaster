package land.temmi.rollercoaster.physics;

import org.joml.Vector2f;

/**
 * Axis-aligned bounding box — stored as min/max corners.
 *
 * <p>Enables per-axis velocity clipping that prevents overlaps before they
 * occur.</p>
 */
public class AABB {

    /** Factor for computing the center from min+max. */
    private static final float HALF = 0.5f;

    /**
     * Tolerance for the axis guards in {@link #clipX} and {@link #clipY}.
     *
     * <p>Due to floating-point accumulation (movementSpeed × delta) an AABB
     * drifts slightly away from exact boundaries (e.g. maxY = -8.9999 instead
     * of -9.0). Without this value the guard would trigger incorrectly even
     * though the box is geometrically in free space. The value equals ≈ 0.01 %
     * of a world unit — far below the minimum distance between edges (1 unit),
     * so it does not affect real collisions.</p>
     */
    private static final float CLIP_EPSILON = 1e-4f;

    /** Left edge (smallest X). */
    public float minX;
    /** Bottom edge (smallest Y). */
    public float minY;
    /** Right edge (largest X). */
    public float maxX;
    /** Top edge (largest Y). */
    public float maxY;

    /**
     * Creates a new AABB from min/max coordinates.
     *
     * @param minX  left edge
     * @param minY  bottom edge
     * @param maxX  right edge
     * @param maxY  top edge
     */
    public AABB(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /** @return X coordinate of the center */
    public float getCenterX() {
        return (minX + maxX) * HALF;
    }

    /** @return Y coordinate of the center */
    public float getCenterY() {
        return (minY + maxY) * HALF;
    }

    /** @return half width */
    public float getHalfWidth() {
        return (maxX - minX) * HALF;
    }

    /** @return half height */
    public float getHalfHeight() {
        return (maxY - minY) * HALF;
    }

    /** @return the center as a new Vector2f */
    public Vector2f getCenter() {
        return new Vector2f(getCenterX(), getCenterY());
    }

    /** @return the half extent as a new Vector2f */
    public Vector2f getHalfExtent() {
        return new Vector2f(getHalfWidth(), getHalfHeight());
    }

    /**
     * Checks whether a point lies inside this AABB.
     * Used for GUI hover checks.
     *
     * @param point  the point to test
     * @return collision data; {@code isIntersecting == true} if the point is inside
     */
    public Collision getCollision(Vector2f point) {
        final float dx = Math.abs(point.x - getCenterX()) - getHalfWidth();
        final float dy = Math.abs(point.y - getCenterY()) - getHalfHeight();
        return new Collision(new Vector2f(dx, dy), dx < 0 && dy < 0);
    }

    /**
     * Moves this AABB in place.
     *
     * @param dx  offset on the X axis
     * @param dy  offset on the Y axis
     */
    public void move(float dx, float dy) {
        minX += dx;
        maxX += dx;
        minY += dy;
        maxY += dy;
    }

    /**
     * Returns a new AABB expanded in the direction of the given velocity.
     * Used to find all potential collision candidates before clipping.
     *
     * @param xa  intended X velocity
     * @param ya  intended Y velocity
     * @return an expanded copy of this AABB
     */
    public AABB expand(float xa, float ya) {
        float eMinX = minX;
        float eMinY = minY;
        float eMaxX = maxX;
        float eMaxY = maxY;
        if (xa < 0) {
            eMinX += xa;
        } else {
            eMaxX += xa;
        }
        if (ya < 0) {
            eMinY += ya;
        } else {
            eMaxY += ya;
        }
        return new AABB(eMinX, eMinY, eMaxX, eMaxY);
    }

    /**
     * Writes the velocity-expanded AABB into {@code target} — allocation-free.
     * Equivalent to {@link #expand(float, float)} but without heap allocation.
     *
     * @param xa     intended X velocity
     * @param ya     intended Y velocity
     * @param target target AABB (overwritten in place)
     */
    public void expandInto(float xa, float ya, AABB target) {
        if (xa < 0) {
            target.minX = minX + xa;
            target.maxX = maxX;
        } else {
            target.minX = minX;
            target.maxX = maxX + xa;
        }
        if (ya < 0) {
            target.minY = minY + ya;
            target.maxY = maxY;
        } else {
            target.minY = minY;
            target.maxY = maxY + ya;
        }
    }

    /**
     * @param other  the other AABB
     * @return true if this AABB overlaps {@code other}
     */
    public boolean intersects(AABB other) {
        return maxX > other.minX && minX < other.maxX
            && maxY > other.minY && minY < other.maxY;
    }

    /**
     * Clips the X velocity {@code xa} of the moving AABB so that it does not
     * penetrate this (static) AABB.
     *
     * <p>Returns {@code xa} unchanged if there is no Y overlap.
     *
     * @param moving  the AABB that wants to move
     * @param xa      intended X velocity (positive = right)
     * @return the clipped X velocity
     */
    public float clipX(AABB moving, float xa) {
        if (moving.maxY - CLIP_EPSILON <= minY || moving.minY + CLIP_EPSILON >= maxY) {
            return xa;
        }
        if (xa > 0 && moving.maxX <= minX) {
            final float d = minX - moving.maxX;
            if (d < xa) {
                xa = d;
            }
        } else if (xa < 0 && moving.minX >= maxX) {
            final float d = maxX - moving.minX;
            if (d > xa) {
                xa = d;
            }
        }
        return xa;
    }

    /**
     * Clips the Y velocity {@code ya} of the moving AABB so that it does not
     * penetrate this (static) AABB.
     *
     * <p>Returns {@code ya} unchanged if there is no X overlap.
     *
     * @param moving  the AABB that wants to move
     * @param ya      intended Y velocity (positive = up)
     * @return the clipped Y velocity
     */
    public float clipY(AABB moving, float ya) {
        if (moving.maxX - CLIP_EPSILON <= minX || moving.minX + CLIP_EPSILON >= maxX) {
            return ya;
        }
        if (ya > 0 && moving.maxY <= minY) {
            final float d = minY - moving.maxY;
            if (d < ya) {
                ya = d;
            }
        } else if (ya < 0 && moving.minY >= maxY) {
            final float d = maxY - moving.minY;
            if (d > ya) {
                ya = d;
            }
        }
        return ya;
    }

    /**
     * Computes the penetration depth between this AABB and {@code box2}.
     * Negative values mean overlap.
     *
     * @param box2  the other AABB
     * @return collision data with distance vector and intersection flag
     */
    public Collision getCollision(AABB box2) {
        final float dx = Math.abs(box2.getCenterX() - getCenterX()) - (getHalfWidth()  + box2.getHalfWidth());
        final float dy = Math.abs(box2.getCenterY() - getCenterY()) - (getHalfHeight() + box2.getHalfHeight());
        return new Collision(new Vector2f(dx, dy), dx < 0 && dy < 0);
    }

    /**
     * Push-out correction: moves this AABB along the axis of least penetration
     * depth until it no longer overlaps {@code box2}.
     *
     * <p>{@code data.distance} must contain the desired separation. For an even
     * split between two dynamic bodies the caller must halve the value beforehand
     * and apply the correction to both sides symmetrically.</p>
     *
     * @param box2  the other AABB
     * @param data  collision data from {@link #getCollision(AABB)}
     */
    public void correctPosition(AABB box2, Collision data) {
        if (data.getDistance().x > data.getDistance().y) {
            final float dirX;
            if (box2.getCenterX() - getCenterX() > 0) {
                dirX = 1f;
            } else {
                dirX = -1f;
            }
            move(data.getDistance().x * dirX, 0);
        } else {
            final float dirY;
            if (box2.getCenterY() - getCenterY() > 0) {
                dirY = 1f;
            } else {
                dirY = -1f;
            }
            move(0, data.getDistance().y * dirY);
        }
    }
}
