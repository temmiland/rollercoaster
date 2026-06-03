package land.temmi.rollercoaster.world;

import org.lwjgl.BufferUtils;
import land.temmi.rollercoaster.graphics.Sprite;
import land.temmi.rollercoaster.graphics.TextureAtlas;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * A square tile region with its own static OpenGL VBO.
 *
 * <p>The VBO is only rebuilt when {@code dirty == true} (set on every
 * {@link #setTile} call or an explicit {@link #markDirty}). In the normal case a
 * chunk render costs exactly one {@code glDrawArrays} call.</p>
 *
 * <p>Tile data is stored as raw {@code byte} IDs. The mapping from ID to texture
 * is resolved at rebuild time via a {@link Sprite} registry array passed to
 * {@link #rebuildIfDirty} — the chunk itself has no knowledge of game-specific
 * tile types.</p>
 *
 * <p>Coordinate conventions:
 * <ul>
 *   <li>{@code (localX, localY)} — position within the chunk (0..chunkSize-1)</li>
 *   <li>{@code (chunkX, chunkY)} — position of the chunk in the chunk grid</li>
 *   <li>Global tile: {@code gx = chunkX*chunkSize+localX}</li>
 *   <li>World space: {@code wx = gx * tileSize}, {@code wy = -gy * tileSize} (Y negated)</li>
 * </ul>
 * </p>
 */
public class TileChunk implements AutoCloseable {

    /**
     * Sentinel value for "no tile" (air).
     * Tile IDs start at 0; -1 as unsigned byte = 255, an ordinarily unused index.
     */
    public static final byte EMPTY = (byte) -1;

    /** Floats per vertex: x, y, u, v. */
    private static final int FLOATS_PER_VERTEX = 4;
    /** Vertices per tile quad (2 triangles). */
    private static final int VERTICES_PER_QUAD = 6;
    /** Index of v1 in the UV array {u0, v0, u1, v1}. */
    private static final int UV_V1_INDEX = 3;

    /** Attribute location: position (must match batch shader). */
    private static final int ATTRIB_POSITION = 0;
    /** Attribute location: UV. */
    private static final int ATTRIB_TEXCOORD = 1;
    /** Stride in bytes. */
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    /** UV offset in bytes (after x, y). */
    private static final long UV_OFFSET = 2L * Float.BYTES;

    /** Number of tiles per side. */
    private final int chunkSize;
    /** Tile size in world-space units. */
    private final float tileSize;

    /** X position in the chunk grid. */
    private final int chunkX;
    /** Y position in the chunk grid. */
    private final int chunkY;

    /** Tile IDs for this chunk; {@link #EMPTY} = air. */
    private final byte[] tiles;

    /** OpenGL VBO id. */
    private final int vboId;
    /** Number of vertices currently to be drawn. */
    private int vertexCount;
    /** Whether the VBO needs to be rebuilt. */
    private boolean dirty = true;

    /**
     * Creates a chunk at the given grid position and pre-allocates its GPU buffer.
     *
     * @param chunkX    X position in the chunk grid
     * @param chunkY    Y position in the chunk grid
     * @param chunkSize number of tiles per side
     * @param tileSize  tile size in world-space units
     */
    public TileChunk(final int chunkX, final int chunkY, final int chunkSize, final float tileSize) {
        this.chunkX    = chunkX;
        this.chunkY    = chunkY;
        this.chunkSize = chunkSize;
        this.tileSize  = tileSize;

        tiles = new byte[chunkSize * chunkSize];
        Arrays.fill(tiles, EMPTY);

        final int maxFloats = chunkSize * chunkSize * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, (long) maxFloats * Float.BYTES, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    // -------------------------------------------------------------------------
    // Tile access

    /**
     * Sets the tile ID at the local position and marks the chunk as dirty.
     *
     * @param localX local X within the chunk (0..chunkSize-1)
     * @param localY local Y within the chunk (0..chunkSize-1)
     * @param id     tile ID to set, or {@link #EMPTY} for air
     */
    public void setTile(final int localX, final int localY, final byte id) {
        tiles[localX + localY * chunkSize] = id;
        dirty = true;
    }

    /**
     * Returns the tile ID at the local position.
     *
     * @param localX local X within the chunk (0..chunkSize-1)
     * @param localY local Y within the chunk (0..chunkSize-1)
     * @return the tile ID, or {@link #EMPTY} for air
     */
    public byte getTileId(final int localX, final int localY) {
        return tiles[localX + localY * chunkSize];
    }

    // -------------------------------------------------------------------------
    // Rendering

    /**
     * Rebuilds the VBO if dirty, otherwise a no-op. Must be called on the GL thread.
     *
     * @param atlas    texture atlas for UV coordinates
     * @param registry sprite registry — index is tile ID, value provides the texture name
     * @return {@code true} if the VBO was actually rebuilt
     */
    public boolean rebuildIfDirty(final TextureAtlas atlas, final Sprite[] registry) {
        if (!dirty) {
            return false;
        }
        rebuild(atlas, registry);
        dirty = false;
        return true;
    }

    /**
     * Renders this chunk with a single {@code glDrawArrays} call.
     *
     * <p>Prerequisite: shader is bound, "projection" uniform is set, atlas is
     * bound to sampler unit 0.</p>
     */
    public void render() {
        if (vertexCount == 0) {
            return;
        }
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableVertexAttribArray(ATTRIB_POSITION);
        glEnableVertexAttribArray(ATTRIB_TEXCOORD);

        glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, false, STRIDE, 0L);
        glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, false, STRIDE, UV_OFFSET);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(ATTRIB_POSITION);
        glDisableVertexAttribArray(ATTRIB_TEXCOORD);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /** Forces a VBO rebuild on the next {@link #rebuildIfDirty} call. */
    public void markDirty() {
        dirty = true;
    }

    /** Releases the OpenGL resources. Must be called on the GL thread. */
    @Override
    public void close() {
        glDeleteBuffers(vboId);
    }

    // -------------------------------------------------------------------------

    private void rebuild(final TextureAtlas atlas, final Sprite[] registry) {
        final int maxFloats = chunkSize * chunkSize * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;
        final float[] buf = new float[maxFloats];
        int idx = 0;

        final float half = tileSize * 0.5f;

        for (int ly = 0; ly < chunkSize; ly++) {
            for (int lx = 0; lx < chunkSize; lx++) {
                final byte id = tiles[lx + ly * chunkSize];
                if (id == EMPTY) {
                    continue;
                }
                final int unsignedId = Byte.toUnsignedInt(id);
                if (unsignedId >= registry.length || registry[unsignedId] == null) {
                    continue;
                }
                final float[] uv = atlas.getUV(registry[unsignedId].getTexture());
                if (uv == null) {
                    continue;
                }

                final int gx = chunkX * chunkSize + lx;
                final int gy = chunkY * chunkSize + ly;

                final float cx = gx * tileSize;
                final float cy = -gy * tileSize;

                final float x0 = cx - half;
                final float x1 = cx + half;
                final float y0 = cy - half;
                final float y1 = cy + half;

                final float u0 = uv[0];
                final float v0 = uv[1];
                final float u1 = uv[2];
                final float v1 = uv[UV_V1_INDEX];

                // Triangle 1: TL → TR → BR
                buf[idx++] = x0; buf[idx++] = y1; buf[idx++] = u0; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y1; buf[idx++] = u1; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y0; buf[idx++] = u1; buf[idx++] = v1;
                // Triangle 2: TL → BR → BL
                buf[idx++] = x0; buf[idx++] = y1; buf[idx++] = u0; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y0; buf[idx++] = u1; buf[idx++] = v1;
                buf[idx++] = x0; buf[idx++] = y0; buf[idx++] = u0; buf[idx++] = v1;
            }
        }

        vertexCount = idx / FLOATS_PER_VERTEX;

        if (idx == 0) {
            return;
        }

        final FloatBuffer fb = BufferUtils.createFloatBuffer(idx);
        fb.put(buf, 0, idx);
        fb.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, fb);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
