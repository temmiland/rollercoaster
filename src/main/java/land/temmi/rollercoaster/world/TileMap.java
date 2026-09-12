package land.temmi.rollercoaster.world;

/**
 * X/Z tile grid with world-Y elevations.
 *
 * <p>Tile {@code (x, z)} covers world X in {@code [x - 1, x]} and world Z in {@code [z - 1, z]}.
 * A tile's stored height is its surface height at the tile centre, so a ramp stores the midpoint
 * of the level it bridges. Flat tiles are whole unit levels and ramps are half-unit midpoints,
 * which limits every ramp to a single 1×1×1 terrain level. Shape is per cell rather than per tile
 * type, so the editor can turn a tile into a ramp and rotate it without swapping its appearance.
 */
public final class TileMap {
    /** Terrain levels are unit cubes; ramps store the midpoint of one such level. */
    public static final float LEVEL_HEIGHT = 1f;
    private static final float HEIGHT_EPSILON = 0.0001f;
    private final int width;
    private final int depth;
    private final TileSurface[] surfaces;
    private final float[] heights;
    private final float[] walkableSurfaceHeights;
    private final TileShape[] shapes;
    private final boolean[] blocked;

    public TileMap(int width, int depth) {
        if (width <= 0 || depth <= 0) throw new IllegalArgumentException("Map dimensions must be positive");
        this.width = width;
        this.depth = depth;
        surfaces = new TileSurface[Math.multiplyExact(width, depth)];
        heights = new float[surfaces.length];
        walkableSurfaceHeights = new float[surfaces.length];
        java.util.Arrays.fill(walkableSurfaceHeights, Float.NaN);
        shapes = new TileShape[surfaces.length];
        blocked = new boolean[surfaces.length];
        java.util.Arrays.fill(shapes, TileShape.FLAT);
    }

    public void set(int x, int z, TileSurface surface, float height) {
        set(x, z, surface, height, TileShape.FLAT, false);
    }

    public void set(int x, int z, TileSurface surface, float height, TileShape shape, boolean blocked) {
        if (shape == null) throw new IllegalArgumentException("Tile shape is required");
        int index = index(x, z);
        requireGridHeight(height, shape);
        surfaces[index] = surface;
        heights[index] = height;
        shapes[index] = shape;
        this.blocked[index] = blocked;
    }

    /** Changes height and shape together so a tile always remains on the terrain grid. */
    public void setTerrain(int x, int z, float height, TileShape shape) {
        if (shape == null) throw new IllegalArgumentException("Tile shape is required");
        int index = index(x, z);
        requireGridHeight(height, shape);
        heights[index] = height;
        shapes[index] = shape;
    }

    /** Marks a tile impassable without changing its geometry; used for derived prop footprints. */
    public void setBlocked(int x, int z, boolean blocked) {
        this.blocked[index(x, z)] = blocked;
    }

    /** Adds a raised walkable surface supplied by a prop, such as a bridge deck. */
    public void setWalkableSurface(int x, int z, float height) {
        requireGridHeight(height, TileShape.FLAT);
        walkableSurfaceHeights[index(x, z)] = height;
    }

    /** Changes shape when the existing height already fits it; otherwise use {@link #setTerrain}. */
    public void setShape(int x, int z, TileShape shape) {
        if (shape == null) throw new IllegalArgumentException("Tile shape is required");
        int index = index(x, z);
        requireGridHeight(heights[index], shape);
        shapes[index] = shape;
    }

    /** Changes height when it already fits the current shape; otherwise use {@link #setTerrain}. */
    public void setHeight(int x, int z, float height) {
        int index = index(x, z);
        requireGridHeight(height, shapes[index]);
        heights[index] = height;
    }

    public TileSurface getSurface(int x, int z) { return surfaces[index(x, z)]; }
    public float getHeight(int x, int z) { return heights[index(x, z)]; }
    public TileShape getShape(int x, int z) { return shapes[index(x, z)]; }
    public boolean isBlocked(int x, int z) { return blocked[index(x, z)]; }

    public boolean hasWalkableSurface(int x, int z) { return !Float.isNaN(walkableSurfaceHeights[index(x, z)]); }

    public float getWalkableSurfaceHeight(int x, int z) { return walkableSurfaceHeights[index(x, z)]; }

    /** Combines the map's collision layer with the tile type's own walkability. */
    public boolean isWalkable(int x, int z) {
        int index = index(x, z);
        if (!Float.isNaN(walkableSurfaceHeights[index])) return true;
        if (blocked[index]) return false;
        TileSurface surface = surfaces[index];
        return surface == null || surface.isWalkable();
    }

    /** Surface height at a tile corner; corner units are 0 or 1 along each axis. */
    public float cornerHeight(int x, int z, int cornerX, int cornerZ) {
        int index = index(x, z);
        return heights[index] + shapes[index].cornerOffset(cornerX, cornerZ);
    }

    public boolean contains(int x, int z) { return x >= 0 && x < width && z >= 0 && z < depth; }
    public int getWidth() { return width; }
    public int getDepth() { return depth; }

    private int index(int x, int z) {
        if (!contains(x, z)) throw new IndexOutOfBoundsException("Tile outside map: " + x + "," + z);
        return z * width + x;
    }

    private static void requireGridHeight(float height, TileShape shape) {
        if (!Float.isFinite(height)) throw new IllegalArgumentException("Terrain height must be finite");
        float midpoint = shape.isRamp() ? LEVEL_HEIGHT * 0.5f : 0f;
        float level = (height - midpoint) / LEVEL_HEIGHT;
        if (Math.abs(level - Math.round(level)) > HEIGHT_EPSILON) {
            throw new IllegalArgumentException("Height does not fit the terrain grid: " + height);
        }
    }
}
