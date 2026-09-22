package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Bakes terrain geometry from the map itself rather than copying per-tile prototype meshes.
 *
 * <p>Each tile contributes a top face through its four corner heights, so a ramp is a sloped quad
 * that meets its neighbours exactly. Side faces are emitted only where a neighbour's shared edge
 * sits lower, which produces cliff faces where they belong and no geometry at all between two
 * tiles of equal height — the interior walls that used to z-fight against the surface above them.
 */
public final class ChunkMesher {
    public static final int CHUNK_SIZE = 16;
    /**
     * Normals are part of the set because the world shader falls back to a constant up vector
     * without them, which would light ramps and cliff faces as if they were flat ground.
     */
    public static final long ATTRIBUTES =
        Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates;

    private static final int[][] EDGES = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    private final Vector3 normal = new Vector3();
    private float borderDepth = 0.35f;

    /** How far the skirt drops at the map border, where there is no neighbour to meet. */
    public ChunkMesher setBorderDepth(float borderDepth) {
        if (borderDepth < 0f) throw new IllegalArgumentException("Border depth must not be negative");
        this.borderDepth = borderDepth;
        return this;
    }

    /** One mesh/material per nonempty chunk. Caller owns the returned models and the material's textures. */
    public Array<Model> build(TileMap map, Material material) {
        if (map == null || material == null) throw new IllegalArgumentException("Map and material are required");
        Array<Model> chunks = new Array<>();
        try {
            for (int z0 = 0; z0 < map.getDepth(); z0 += CHUNK_SIZE) {
                for (int x0 = 0; x0 < map.getWidth(); x0 += CHUNK_SIZE) {
                    ModelBuilder builder = new ModelBuilder();
                    builder.begin();
                    MeshPartBuilder mesh = appendChunk(builder, map, material, x0, z0);
                    Model chunk = builder.end();
                    if (mesh == null) chunk.dispose();
                    else chunks.add(chunk);
                }
            }
            return chunks;
        } catch (RuntimeException failure) {
            for (Model chunk : chunks) chunk.dispose();
            throw failure;
        }
    }

    /**
     * Rebuilds a single chunk at grid origin (x0, z0) - the exact same geometry {@link #build}
     * would produce there, since both share {@link #appendChunk}. Returns null if the chunk has
     * no painted tiles, matching build()'s convention of omitting empty chunks rather than
     * returning a zero-triangle model. Lets a caller remesh just the chunks a small edit actually
     * touched instead of every chunk in the map.
     */
    public Model buildChunk(TileMap map, Material material, int x0, int z0) {
        if (map == null || material == null) throw new IllegalArgumentException("Map and material are required");
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder mesh = appendChunk(builder, map, material, x0, z0);
        Model chunk = builder.end();
        if (mesh == null) {
            chunk.dispose();
            return null;
        }
        return chunk;
    }

    private MeshPartBuilder appendChunk(ModelBuilder builder, TileMap map, Material material, int x0, int z0) {
        MeshPartBuilder mesh = null;
        for (int z = z0; z < Math.min(z0 + CHUNK_SIZE, map.getDepth()); z++) {
            for (int x = x0; x < Math.min(x0 + CHUNK_SIZE, map.getWidth()); x++) {
                if (map.getSurface(x, z) == null) continue;
                if (mesh == null) mesh = builder.part("chunk-" + x0 + "-" + z0,
                    GL20.GL_TRIANGLES, ATTRIBUTES, material);
                appendTile(mesh, map, x, z);
            }
        }
        return mesh;
    }

    private void appendTile(MeshPartBuilder mesh, TileMap map, int x, int z) {
        TileSurface surface = map.getSurface(x, z);
        float minX = x - 1f;
        float maxX = x;
        float minZ = z - 1f;
        float maxZ = z;

        float h00 = map.cornerHeight(x, z, 0, 0);
        float h10 = map.cornerHeight(x, z, 1, 0);
        float h11 = map.cornerHeight(x, z, 1, 1);
        float h01 = map.cornerHeight(x, z, 0, 1);

        map.getShape(x, z).normal(normal);
        mesh.setColor(surface.topColor);
        mesh.setUVRange(surface.topU(), surface.topV(), surface.topU2(), surface.topV2());
        // Counter-clockwise seen from above: (minX,maxZ) (maxX,maxZ) (maxX,minZ) (minX,minZ).
        mesh.rect(minX, h01, maxZ, maxX, h11, maxZ, maxX, h10, minZ, minX, h00, minZ,
            normal.x, normal.y, normal.z);

        mesh.setColor(surface.sideColor);
        mesh.setUVRange(surface.sideU(), surface.sideV(), surface.sideU2(), surface.sideV2());
        for (int[] edge : EDGES) appendSide(mesh, map, x, z, edge[0], edge[1]);
    }

    private void appendSide(MeshPartBuilder mesh, TileMap map, int x, int z, int dx, int dz) {
        // The two corners of this tile that lie on the shared edge, and the neighbour's corners
        // facing them. Equal heights mean the tiles meet flush and no wall is needed.
        int aX, aZ, bX, bZ;
        if (dz != 0) {
            int corner = dz < 0 ? 0 : 1;
            aX = 0; aZ = corner; bX = 1; bZ = corner;
        } else {
            int corner = dx < 0 ? 0 : 1;
            aX = corner; aZ = 0; bX = corner; bZ = 1;
        }
        float topA = map.cornerHeight(x, z, aX, aZ);
        float topB = map.cornerHeight(x, z, bX, bZ);

        float bottomA;
        float bottomB;
        int nx = x + dx;
        int nz = z + dz;
        if (map.contains(nx, nz) && map.getSurface(nx, nz) != null) {
            bottomA = map.cornerHeight(nx, nz, dz != 0 ? aX : 1 - aX, dz != 0 ? 1 - aZ : aZ);
            bottomB = map.cornerHeight(nx, nz, dz != 0 ? bX : 1 - bX, dz != 0 ? 1 - bZ : bZ);
        } else {
            bottomA = topA - borderDepth;
            bottomB = topB - borderDepth;
        }
        bottomA = Math.min(bottomA, topA);
        bottomB = Math.min(bottomB, topB);
        if (topA - bottomA <= 0f && topB - bottomB <= 0f) return;

        float minX = x - 1f;
        float maxX = x;
        float minZ = z - 1f;
        float maxZ = z;
        if (dz < 0) {
            // Facing -Z: x runs from max to min so the face winds outward.
            mesh.rect(maxX, bottomB, minZ, minX, bottomA, minZ, minX, topA, minZ, maxX, topB, minZ, 0f, 0f, -1f);
        } else if (dz > 0) {
            mesh.rect(minX, bottomA, maxZ, maxX, bottomB, maxZ, maxX, topB, maxZ, minX, topA, maxZ, 0f, 0f, 1f);
        } else if (dx < 0) {
            mesh.rect(minX, bottomA, minZ, minX, bottomB, maxZ, minX, topB, maxZ, minX, topA, minZ, -1f, 0f, 0f);
        } else {
            mesh.rect(maxX, bottomB, maxZ, maxX, bottomA, minZ, maxX, topA, minZ, maxX, topB, maxZ, 1f, 0f, 0f);
        }
    }
}
