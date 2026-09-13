package land.temmi.rollercoaster.asset;

import land.temmi.rollercoaster.actor.Facing;

/** Atlas regions and timing metadata for one directional actor sprite. */
public final class SpriteDefinition {
    public final String id;
    public final float worldHeight;
    public final float frameDuration;
    /** Fraction of the sprite image below its feet, for BillboardRenderer bottom padding. */
    public final float footOffset;
    private final String[] idleRegions;
    private final String[][] walkRegions;

    SpriteDefinition(String id, float worldHeight, float frameDuration,
                     String[] idleRegions, String[][] walkRegions) {
        this(id, worldHeight, frameDuration, 0f, idleRegions, walkRegions);
    }

    SpriteDefinition(String id, float worldHeight, float frameDuration, float footOffset,
                     String[] idleRegions, String[][] walkRegions) {
        if (id == null || id.length() == 0 || worldHeight <= 0f || frameDuration <= 0f
            || footOffset < 0f || footOffset >= 1f) {
            throw new IllegalArgumentException("Invalid sprite definition: " + id);
        }
        this.id = id;
        this.worldHeight = worldHeight;
        this.frameDuration = frameDuration;
        this.footOffset = footOffset;
        this.idleRegions = idleRegions.clone();
        this.walkRegions = new String[walkRegions.length][];
        for (int i = 0; i < walkRegions.length; i++) this.walkRegions[i] = walkRegions[i].clone();
    }

    public String idleRegion(Facing facing) {
        if (facing == null) throw new IllegalArgumentException("Facing is required");
        return idleRegions[facing.ordinal()];
    }

    public String[] walkRegions(Facing facing) {
        if (facing == null) throw new IllegalArgumentException("Facing is required");
        return walkRegions[facing.ordinal()].clone();
    }
}
