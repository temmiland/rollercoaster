package land.temmi.rollercoaster.world;

/** X/Z tile grid with world-Y elevations. Prototype ownership stays with the caller. */
public final class TileMap {
    private final int width;
    private final int depth;
    private final TilePrototype[] tiles;
    private final float[] heights;

    public TileMap(int width, int depth) {
        if (width <= 0 || depth <= 0) throw new IllegalArgumentException("Map dimensions must be positive");
        this.width = width;
        this.depth = depth;
        tiles = new TilePrototype[Math.multiplyExact(width, depth)];
        heights = new float[tiles.length];
    }

    public void set(int x, int z, TilePrototype tile, float height) {
        int index = index(x, z);
        tiles[index] = tile;
        heights[index] = height;
    }

    public TilePrototype getTile(int x, int z) { return tiles[index(x, z)]; }
    public float getHeight(int x, int z) { return heights[index(x, z)]; }
    public int getWidth() { return width; }
    public int getDepth() { return depth; }

    private int index(int x, int z) {
        if (x < 0 || x >= width || z < 0 || z >= depth) throw new IndexOutOfBoundsException();
        return z * width + x;
    }
}
