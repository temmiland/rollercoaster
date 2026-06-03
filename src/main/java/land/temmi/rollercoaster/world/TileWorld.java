package land.temmi.rollercoaster.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.rollercoaster.graphics.Shader;
import land.temmi.rollercoaster.graphics.Sprite;
import land.temmi.rollercoaster.graphics.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic chunk-based 2D tile world.
 *
 * <p>Manages a {@link TileChunk} grid, viewport culling, chunk rendering and
 * tile-change listeners. Game-specific concerns (entity management, collision
 * solidity, spawn logic) belong in a subclass.</p>
 *
 * <p>Tile data is stored as raw {@code byte} IDs. The game supplies a
 * {@link Sprite} registry (index = tile ID) which is used at VBO rebuild time
 * to look up texture names. The registry is passed once at construction and
 * can be replaced via {@link #setRegistry}.</p>
 */
public class TileWorld implements AutoCloseable {

    /** Tiles to render outside the calculated viewport on each side. */
    private static final int VIEW_OFFSET  = 4;
    /** Divisor for halving. */
    private static final int HALF         = 2;
    /** Extra tile padding for the render boundary. */
    private static final int RENDER_PAD   = 1;

    /** World width in tiles. */
    private final int width;
    /** World height in tiles. */
    private final int height;
    /** Number of tiles per chunk side. */
    private final int chunkSize;
    /** Tile size in world-space units. */
    private final float tileSize;

    /** Sprite registry: index = tile ID, entry = sprite for texture lookup. */
    private Sprite[] registry;

    /** Chunk grid [chunkX][chunkY]. */
    private final TileChunk[][] chunks;
    /** Number of chunks horizontally. */
    private final int chunkCountX;
    /** Number of chunks vertically. */
    private final int chunkCountY;

    /** World transform matrix (scale). */
    private final Matrix4f worldMatrix;

    /** Viewport half-range in tiles (horizontal). */
    private int viewX;
    /** Viewport half-range in tiles (vertical). */
    private int viewY;

    /** Listeners notified on tile changes. */
    private final List<WorldListener> listeners = new ArrayList<>();

    /** Number of rendered chunks in the last frame (for debug). */
    private int lastRenderedChunks;
    /** Number of rebuilt chunks in the last frame (for debug). */
    private int lastRebuiltChunks;

    /**
     * Creates the world and allocates all chunks.
     *
     * @param width     world width in tiles
     * @param height    world height in tiles
     * @param scale     world scale (pixel multiplier applied to the world matrix)
     * @param chunkSize number of tiles per chunk side
     * @param tileSize  tile size in world-space units
     * @param registry  sprite registry (index = tile ID)
     */
    public TileWorld(final int width, final int height, final int scale,
                     final int chunkSize, final float tileSize, final Sprite[] registry) {
        this.width     = width;
        this.height    = height;
        this.chunkSize = chunkSize;
        this.tileSize  = tileSize;
        this.registry  = registry;

        chunkCountX = (width  + chunkSize - 1) / chunkSize;
        chunkCountY = (height + chunkSize - 1) / chunkSize;
        chunks      = new TileChunk[chunkCountX][chunkCountY];
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy] = new TileChunk(cx, cy, chunkSize, tileSize);
            }
        }

        worldMatrix = new Matrix4f().setTranslation(new Vector3f(0));
        worldMatrix.scale(scale);
    }

    // -------------------------------------------------------------------------
    // Tile access

    /**
     * Sets the tile ID at the global position and notifies listeners.
     *
     * @param x  global tile X
     * @param y  global tile Y
     * @param id tile ID, or {@link TileChunk#EMPTY} for air
     */
    public void setTileId(final int x, final int y, final byte id) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        chunks[x / chunkSize][y / chunkSize].setTile(x % chunkSize, y % chunkSize, id);
        for (final WorldListener l : listeners) {
            l.tileChanged(x, y);
        }
    }

    /**
     * Returns the tile ID at the global position.
     *
     * @param x global tile X
     * @param y global tile Y
     * @return tile ID, or {@link TileChunk#EMPTY} if out of bounds or air
     */
    public byte getTileId(final int x, final int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TileChunk.EMPTY;
        }
        return chunks[x / chunkSize][y / chunkSize].getTileId(x % chunkSize, y % chunkSize);
    }

    // -------------------------------------------------------------------------
    // Listeners

    /** Registers a listener to be notified on tile changes. */
    public void addListener(final WorldListener listener) {
        listeners.add(listener);
    }

    /** Removes a previously registered listener. */
    public void removeListener(final WorldListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that the whole world has been reloaded.
     * Call this after bulk-loading tile data (e.g. from a file).
     */
    public void notifyWorldReloaded() {
        for (final WorldListener l : listeners) {
            l.worldReloaded();
        }
    }

    // -------------------------------------------------------------------------
    // Camera

    /**
     * Recalculates the visible tile range based on the camera's virtual size.
     *
     * @param camera the active camera
     * @param scale  world scale (pixel multiplier)
     */
    public void calculateView(final Camera camera, final int scale) {
        viewX = (camera.getVirtualWidth()  / (scale * HALF)) + VIEW_OFFSET;
        viewY = (camera.getVirtualHeight() / (scale * HALF)) + VIEW_OFFSET;
    }

    /**
     * Clamps the camera position so it stays within the world bounds.
     *
     * @param camera the camera to correct
     * @param scale  world scale
     */
    public void correctCamera(final Camera camera, final int scale) {
        final int w = -width  * scale * HALF;
        final int h =  height * scale * HALF;

        final int halfVW = camera.getVirtualWidth()  / HALF;
        final int halfVH = camera.getVirtualHeight() / HALF;

        float x = camera.getPosition().x;
        float y = camera.getPosition().y;

        if (x > -halfVW + scale)      { x = -halfVW + scale; }
        if (x < w + halfVW + scale)   { x =  w + halfVW + scale; }
        if (y < halfVH - scale)       { y = halfVH - scale; }
        if (y > h - halfVH - scale)   { y = h - halfVH - scale; }

        camera.setPosition(x, y);
    }

    // -------------------------------------------------------------------------
    // Rendering

    /**
     * Rebuilds dirty chunks in the visible range and renders them all.
     *
     * @param atlas      texture atlas
     * @param shader     batch shader (must expose "sampler" and "projection" uniforms)
     * @param cam        active camera
     * @param alpha      render interpolation factor (0.0–1.0)
     * @param scale      world scale
     */
    public void renderChunks(final TextureAtlas atlas, final Shader shader,
                             final Camera cam, final float alpha, final int scale) {
        final int posX = (int) cam.getPosition().x / (scale * HALF);
        final int posY = (int) cam.getPosition().y / (scale * HALF);

        final int halfX = viewX / HALF;
        final int halfY = viewY / HALF;

        final int minTileX = -posX - halfX - RENDER_PAD;
        final int maxTileX = -posX + halfX + RENDER_PAD;
        final int minTileY =  posY - halfY - RENDER_PAD;
        final int maxTileY =  posY + halfY + RENDER_PAD;

        final int minCX = Math.max(0,              Math.floorDiv(minTileX, chunkSize));
        final int maxCX = Math.min(chunkCountX - 1, Math.floorDiv(maxTileX, chunkSize));
        final int minCY = Math.max(0,              Math.floorDiv(minTileY, chunkSize));
        final int maxCY = Math.min(chunkCountY - 1, Math.floorDiv(maxTileY, chunkSize));

        int rebuilt  = 0;
        for (int cy = minCY; cy <= maxCY; cy++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                if (chunks[cx][cy].rebuildIfDirty(atlas, registry)) {
                    rebuilt++;
                }
            }
        }

        final Matrix4f viewProj = cam.getProjection(alpha).mul(worldMatrix);
        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", viewProj);
        atlas.bind(0);

        int rendered = 0;
        for (int cy = minCY; cy <= maxCY; cy++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                chunks[cx][cy].render();
                rendered++;
            }
        }

        lastRenderedChunks = rendered;
        lastRebuiltChunks  = rebuilt;
    }

    /** Marks all chunks dirty so their VBOs are fully rebuilt on the next render. */
    public void markAllChunksDirty() {
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy].markDirty();
            }
        }
    }

    /** Replaces the sprite registry (e.g. after a resource reload). Marks all chunks dirty. */
    public void setRegistry(final Sprite[] registry) {
        this.registry = registry;
        markAllChunksDirty();
    }

    // -------------------------------------------------------------------------
    // Accessors

    /** @return world width in tiles */
    public int getWidth()  { return width; }

    /** @return world height in tiles */
    public int getHeight() { return height; }

    /** @return number of chunks horizontally */
    public int getChunkCountX() { return chunkCountX; }

    /** @return number of chunks vertically */
    public int getChunkCountY() { return chunkCountY; }

    /** @return tile size in world-space units */
    public float getTileSize() { return tileSize; }

    /** @return the world transform matrix */
    public Matrix4f getWorldMatrix() { return worldMatrix; }

    /** @return viewport half-range in tiles (horizontal) — updated by {@link #calculateView} */
    public int getViewX() { return viewX; }

    /** @return viewport half-range in tiles (vertical) — updated by {@link #calculateView} */
    public int getViewY() { return viewY; }

    /** @return number of chunks rendered in the last frame */
    public int getLastRenderedChunks() { return lastRenderedChunks; }

    /** @return number of chunks rebuilt in the last frame */
    public int getLastRebuiltChunks()  { return lastRebuiltChunks; }

    /** Releases all OpenGL resources of the chunks. Must be called on the GL thread. */
    @Override
    public void close() {
        for (int cy = 0; cy < chunkCountY; cy++) {
            for (int cx = 0; cx < chunkCountX; cx++) {
                chunks[cx][cy].close();
            }
        }
    }
}
