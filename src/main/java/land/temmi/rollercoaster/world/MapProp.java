package land.temmi.rollercoaster.world;

public final class MapProp {
    public final String model;
    public final float x;
    public final float z;
    public final float rotation;

    public MapProp(String model, float x, float z, float rotation) {
        this.model = model;
        this.x = x;
        this.z = z;
        this.rotation = rotation;
    }
}
