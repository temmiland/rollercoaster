package temmiland.rollercoaster.world;

/**
 * Listener for tile changes in a {@link TileWorld}.
 *
 * <p>Registered listeners are notified whenever tiles are set or the world is
 * fully reloaded. Primary use cases are dirty-chunk flags, lighting caches,
 * and pathfinding invalidation.</p>
 */
public interface WorldListener {

    /**
     * Called when a single tile has been changed.
     *
     * @param x global tile X coordinate
     * @param y global tile Y coordinate
     */
    void tileChanged(int x, int y);

    /**
     * Called when the entire world has been reloaded or generated.
     * All caches and dirty flags should be fully invalidated.
     */
    void worldReloaded();
}
