package land.temmi.rollercoaster.asset;

/** Data needed to place and collide a map model. */
public final class ModelDefinition {
    public final String id;
    public final String source;
    public final float offsetX;
    public final float offsetY;
    public final float offsetZ;
    public final float scale;
    public final float height;
    public final float boundsMinX;
    public final float boundsMinY;
    public final float boundsMinZ;
    public final float boundsMaxX;
    public final float boundsMaxY;
    public final float boundsMaxZ;
    public final int collisionMinX;
    public final int collisionMaxX;
    public final int collisionMinZ;
    public final int collisionMaxZ;
    /** Tilt the prop onto the terrain slope instead of keeping it upright. */
    public final boolean alignToSlope;
    /** Whether the prop supplies a raised walkable surface over its collision footprint. */
    public final boolean walkable;
    /** Surface height above the prop anchor, used when {@link #walkable} is enabled. */
    public final float walkHeight;

    public ModelDefinition(String id, String source, float offsetX, float offsetY, float offsetZ,
                           float scale, float height,
                           float boundsMinX, float boundsMinY, float boundsMinZ,
                           float boundsMaxX, float boundsMaxY, float boundsMaxZ,
                           int collisionMinX, int collisionMaxX, int collisionMinZ, int collisionMaxZ) {
        this(id, source, offsetX, offsetY, offsetZ, scale, height,
            boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ,
            collisionMinX, collisionMaxX, collisionMinZ, collisionMaxZ, false, false, 0f);
    }

    public ModelDefinition(String id, String source, float offsetX, float offsetY, float offsetZ,
                           float scale, float height,
                           float boundsMinX, float boundsMinY, float boundsMinZ,
                           float boundsMaxX, float boundsMaxY, float boundsMaxZ,
                           int collisionMinX, int collisionMaxX, int collisionMinZ, int collisionMaxZ,
                           boolean alignToSlope) {
        this(id, source, offsetX, offsetY, offsetZ, scale, height,
            boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ,
            collisionMinX, collisionMaxX, collisionMinZ, collisionMaxZ,
            alignToSlope, false, 0f);
    }

    public ModelDefinition(String id, String source, float offsetX, float offsetY, float offsetZ,
                           float scale, float height,
                           float boundsMinX, float boundsMinY, float boundsMinZ,
                           float boundsMaxX, float boundsMaxY, float boundsMaxZ,
                           int collisionMinX, int collisionMaxX, int collisionMinZ, int collisionMaxZ,
                           boolean alignToSlope, boolean walkable, float walkHeight) {
        if (id == null || id.length() == 0 || source == null || source.length() == 0) {
            throw new IllegalArgumentException("Model ID and source are required");
        }
        if (collisionMinX > collisionMaxX || collisionMinZ > collisionMaxZ) {
            throw new IllegalArgumentException("Invalid model collision bounds: " + id);
        }
        if (scale <= 0f || height <= 0f || boundsMinX > boundsMaxX || boundsMinY > boundsMaxY || boundsMinZ > boundsMaxZ) {
            throw new IllegalArgumentException("Invalid model dimensions: " + id);
        }
        if (!Float.isFinite(walkHeight) || (walkable && walkHeight < 0f)) {
            throw new IllegalArgumentException("Invalid walkable surface metadata: " + id);
        }
        this.id = id;
        this.source = source;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.scale = scale;
        this.height = height;
        this.boundsMinX = boundsMinX;
        this.boundsMinY = boundsMinY;
        this.boundsMinZ = boundsMinZ;
        this.boundsMaxX = boundsMaxX;
        this.boundsMaxY = boundsMaxY;
        this.boundsMaxZ = boundsMaxZ;
        this.collisionMinX = collisionMinX;
        this.collisionMaxX = collisionMaxX;
        this.collisionMinZ = collisionMinZ;
        this.collisionMaxZ = collisionMaxZ;
        this.alignToSlope = alignToSlope;
        this.walkable = walkable;
        this.walkHeight = walkHeight;
    }
}
