package land.temmi.rollercoaster.world;

/** Atlas-backed tile metadata exported by the map editor. */
public final class TileDefinition {
    public final String id;
    public final int atlasX;
    public final int atlasY;
    public final int atlasWidth;
    public final int atlasHeight;

    public TileDefinition(String id, int atlasX, int atlasY, int atlasWidth, int atlasHeight) {
        if (id == null || id.length() == 0 || atlasX < 0 || atlasY < 0
            || atlasWidth <= 0 || atlasHeight <= 0) {
            throw new IllegalArgumentException("Invalid tile definition: " + id);
        }
        this.id = id;
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
    }
}
