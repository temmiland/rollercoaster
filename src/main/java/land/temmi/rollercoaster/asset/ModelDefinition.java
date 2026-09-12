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

    public ModelDefinition(String id, String source, float offsetX, float offsetY, float offsetZ,
                           float scale, float height,
                           float boundsMinX, float boundsMinY, float boundsMinZ,
                           float boundsMaxX, float boundsMaxY, float boundsMaxZ,
                           int collisionMinX, int collisionMaxX, int collisionMinZ, int collisionMaxZ) {
        if (id == null || id.length() == 0 || source == null || source.length() == 0) {
            throw new IllegalArgumentException("Model ID and source are required");
        }
        if (collisionMinX > collisionMaxX || collisionMinZ > collisionMaxZ) {
            throw new IllegalArgumentException("Invalid model collision bounds: " + id);
        }
        if (scale <= 0f || height <= 0f || boundsMinX > boundsMaxX || boundsMinY > boundsMaxY || boundsMinZ > boundsMaxZ) {
            throw new IllegalArgumentException("Invalid model dimensions: " + id);
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
    }
}
