package land.temmi.rollercoaster.world;

/**
 * Surface form of a tile. A tile's stored height is its surface height at the tile centre, so a
 * ramp's height is the midpoint of the level it bridges, not its high or low edge.
 *
 * <p>Ramps rise towards the named direction and span exactly one level across the tile. Local
 * coordinates run 0..1 from the tile's minimum X/Z corner.
 */
public enum TileShape {
    FLAT(0f, 0f),
    /** Rises towards -Z. */
    RAMP_NORTH(0f, -1f),
    /** Rises towards +X. */
    RAMP_EAST(1f, 0f),
    /** Rises towards +Z. */
    RAMP_SOUTH(0f, 1f),
    /** Rises towards -X. */
    RAMP_WEST(-1f, 0f);

    /** Height change per unit of local X, across the full tile. */
    public final float slopeX;
    /** Height change per unit of local Z, across the full tile. */
    public final float slopeZ;

    TileShape(float slopeX, float slopeZ) {
        this.slopeX = slopeX;
        this.slopeZ = slopeZ;
    }

    public boolean isRamp() { return this != FLAT; }

    /**
     * Height of the surface above the tile's stored height.
     *
     * @param localX position inside the tile, 0 at its minimum X edge, 1 at its maximum
     * @param localZ position inside the tile, 0 at its minimum Z edge, 1 at its maximum
     */
    public float surfaceOffset(float localX, float localZ) {
        return slopeX * (localX - 0.5f) + slopeZ * (localZ - 0.5f);
    }

    /** Height at the tile edge facing {@code dx}/{@code dz}, relative to the stored height. */
    public float edgeOffset(int dx, int dz) {
        return surfaceOffset(0.5f + dx * 0.5f, 0.5f + dz * 0.5f);
    }
}
