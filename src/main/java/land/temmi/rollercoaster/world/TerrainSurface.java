package land.temmi.rollercoaster.world;

import com.badlogic.gdx.math.MathUtils;

/**
 * Height queries against a tile map, including positions inside a sloped tile.
 *
 * <p>Tile {@code (x, z)} covers world X in {@code [x - 1, x]} and world Z in {@code [z - 1, z]},
 * so its centre is at {@code (x - 0.5, z - 0.5)} — the same anchor {@code ChunkMesher} bakes the
 * geometry at and {@code GridActor} places actors at. Positions outside the map clamp to the
 * border tile, so an editor cursor beyond the edge still yields a usable height.
 */
public final class TerrainSurface {
    private final TileMap map;

    public TerrainSurface(TileMap map) {
        if (map == null) throw new IllegalArgumentException("Tile map is required");
        this.map = map;
    }

    public TileMap getMap() { return map; }

    /** Grid X of the tile covering a world X coordinate. */
    public static int tileX(float worldX) { return MathUtils.floor(worldX) + 1; }

    /** Grid Z of the tile covering a world Z coordinate. */
    public static int tileZ(float worldZ) { return MathUtils.floor(worldZ) + 1; }

    /** World X of a tile's centre. */
    public static float centerX(int tileX) { return tileX - 0.5f; }

    /** World Z of a tile's centre. */
    public static float centerZ(int tileZ) { return tileZ - 0.5f; }

    /** Surface height at an arbitrary world position, interpolated across ramps. */
    public float heightAt(float worldX, float worldZ) {
        int x = MathUtils.clamp(tileX(worldX), 0, map.getWidth() - 1);
        int z = MathUtils.clamp(tileZ(worldZ), 0, map.getDepth() - 1);
        float localX = worldX - (x - 1);
        float localZ = worldZ - (z - 1);
        return map.getHeight(x, z)
            + map.getShape(x, z).surfaceOffset(MathUtils.clamp(localX, 0f, 1f), MathUtils.clamp(localZ, 0f, 1f));
    }

    /** Surface height at a tile's centre, which is the tile's stored height for every shape. */
    public float heightAtCenter(int x, int z) {
        return map.getHeight(x, z);
    }

    /** Surface height at the tile edge facing {@code dx}/{@code dz}. */
    public float heightAtEdge(int x, int z, int dx, int dz) {
        return map.getHeight(x, z) + map.getShape(x, z).edgeOffset(dx, dz);
    }
}
