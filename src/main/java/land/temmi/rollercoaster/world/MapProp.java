package land.temmi.rollercoaster.world;

public final class MapProp {
    public final String model;
    public final float x;
    public final float z;
    /** Additional world-Y offset relative to the anchor tile's terrain height. */
    public final float elevation;
    public final float rotation;

    public MapProp(String model, float x, float z, float rotation) {
        this(model, x, z, 0f, rotation);
    }

    public MapProp(String model, float x, float z, float elevation, float rotation) {
        this.model = model;
        this.x = x;
        this.z = z;
        this.elevation = elevation;
        this.rotation = rotation;
    }
}
