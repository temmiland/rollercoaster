package land.temmi.rollercoaster.world;

/** Atlas-backed tile metadata exported by the map editor. */
public final class TileDefinition {
    public final String id;
    public final int atlasX;
    public final int atlasY;
    public final int atlasWidth;
    public final int atlasHeight;
    /** Region used on exposed cliff and ramp sides; falls back to the top region. */
    public final int sideX;
    public final int sideY;
    public final int sideWidth;
    public final int sideHeight;
    public final boolean walkable;

    public TileDefinition(String id, int atlasX, int atlasY, int atlasWidth, int atlasHeight) {
        this(id, atlasX, atlasY, atlasWidth, atlasHeight, atlasX, atlasY, atlasWidth, atlasHeight, true);
    }

    public TileDefinition(String id, int atlasX, int atlasY, int atlasWidth, int atlasHeight,
                          int sideX, int sideY, int sideWidth, int sideHeight, boolean walkable) {
        if (id == null || id.length() == 0 || atlasX < 0 || atlasY < 0
            || atlasWidth <= 0 || atlasHeight <= 0 || sideX < 0 || sideY < 0
            || sideWidth <= 0 || sideHeight <= 0) {
            throw new IllegalArgumentException("Invalid tile definition: " + id);
        }
        this.id = id;
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.sideX = sideX;
        this.sideY = sideY;
        this.sideWidth = sideWidth;
        this.sideHeight = sideHeight;
        this.walkable = walkable;
    }
}
