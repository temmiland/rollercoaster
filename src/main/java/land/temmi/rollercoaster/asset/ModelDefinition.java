package land.temmi.rollercoaster.asset;

/** Data needed to place and collide a map model. */
public final class ModelDefinition {
    public final String id;
    public final String source;
    public final float offsetX;
    public final float offsetY;
    public final float offsetZ;
    public final int collisionMinX;
    public final int collisionMaxX;
    public final int collisionMinZ;
    public final int collisionMaxZ;

    public ModelDefinition(String id, String source, float offsetX, float offsetY, float offsetZ,
                           int collisionMinX, int collisionMaxX, int collisionMinZ, int collisionMaxZ) {
        if (id == null || id.length() == 0 || source == null || source.length() == 0) {
            throw new IllegalArgumentException("Model ID and source are required");
        }
        if (collisionMinX > collisionMaxX || collisionMinZ > collisionMaxZ) {
            throw new IllegalArgumentException("Invalid model collision bounds: " + id);
        }
        this.id = id;
        this.source = source;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.collisionMinX = collisionMinX;
        this.collisionMaxX = collisionMaxX;
        this.collisionMinZ = collisionMinZ;
        this.collisionMaxZ = collisionMaxZ;
    }
}
