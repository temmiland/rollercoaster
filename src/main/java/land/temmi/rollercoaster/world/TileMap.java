package land.temmi.rollercoaster.world;

/** X/Z tile grid with world-Y elevations. Prototype ownership stays with the caller. */
public final class TileMap {
    private final int width;
    private final int depth;
    private final TilePrototype[] tiles;
    private final float[] heights;
    private final boolean[] blocked;

    public TileMap(int width, int depth) {
        if (width <= 0 || depth <= 0) throw new IllegalArgumentException("Map dimensions must be positive");
        this.width = width;
        this.depth = depth;
        tiles = new TilePrototype[Math.multiplyExact(width, depth)];
        heights = new float[tiles.length];
        blocked = new boolean[tiles.length];
    }

    public void set(int x, int z, TilePrototype tile, float height) {
        set(x, z, tile, height, false);
    }

    public void set(int x, int z, TilePrototype tile, float height, boolean blocked) {
        int index = index(x, z);
        tiles[index] = tile;
        heights[index] = height;
        this.blocked[index] = blocked;
    }

    /** Marks a tile impassable without changing its geometry; used for derived prop footprints. */
    public void setBlocked(int x, int z, boolean blocked) {
        this.blocked[index(x, z)] = blocked;
    }

    public TilePrototype getTile(int x, int z) { return tiles[index(x, z)]; }
    public float getHeight(int x, int z) { return heights[index(x, z)]; }
    public boolean isBlocked(int x, int z) { return blocked[index(x, z)]; }

    /** Surface form of the tile; an empty cell counts as flat. */
    public TileShape getShape(int x, int z) {
        TilePrototype tile = tiles[index(x, z)];
        return tile == null ? TileShape.FLAT : tile.getShape();
    }

    /** Combines the map's collision layer with the tile type's own walkability. */
    public boolean isWalkable(int x, int z) {
        int index = index(x, z);
        if (blocked[index]) return false;
        TilePrototype tile = tiles[index];
        return tile == null || tile.isWalkable();
    }

    public boolean contains(int x, int z) { return x >= 0 && x < width && z >= 0 && z < depth; }
    public int getWidth() { return width; }
    public int getDepth() { return depth; }

    private int index(int x, int z) {
        if (!contains(x, z)) throw new IndexOutOfBoundsException("Tile outside map: " + x + "," + z);
        return z * width + x;
    }
}
