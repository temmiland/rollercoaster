package land.temmi.rollercoaster.world;

/** A tile that warps the player to a spawn point on another map. Parsed data only - stepping onto
 * the tile and switching maps is the game's own responsibility, same as any other entity type. */
public final class MapTransition {
    public final String id;
    public final int x;
    public final int z;
    public final String targetMap;
    public final int targetX;
    public final int targetZ;

    public MapTransition(String id, int x, int z, String targetMap, int targetX, int targetZ) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Transition id is required");
        if (targetMap == null || targetMap.trim().isEmpty()) {
            throw new IllegalArgumentException("Transition target map is required: " + id);
        }
        this.id = id;
        this.x = x;
        this.z = z;
        this.targetMap = targetMap;
        this.targetX = targetX;
        this.targetZ = targetZ;
    }
}
