package land.temmi.rollercoaster.world;

public final class MapEntity {
    public final String type;
    public final String sprite;
    public final int x;
    public final int z;

    public MapEntity(String type, String sprite, int x, int z) {
        this.type = type;
        this.sprite = sprite;
        this.x = x;
        this.z = z;
    }
}
