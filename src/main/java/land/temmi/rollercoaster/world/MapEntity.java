package land.temmi.rollercoaster.world;

import com.badlogic.gdx.utils.ObjectMap;

public final class MapEntity {
    /** Stable editor identity; null for legacy maps that only supplied a type. */
    public final String id;
    public final String type;
    public final String sprite;
    public final int x;
    public final int z;
    /** Free-form key/value data, shaped by whatever the game's own schema for this type expects. */
    public final ObjectMap<String, String> properties;

    public MapEntity(String type, String sprite, int x, int z) {
        this(null, type, sprite, x, z);
    }

    public MapEntity(String id, String type, String sprite, int x, int z) {
        this(id, type, sprite, x, z, null);
    }

    public MapEntity(String id, String type, String sprite, int x, int z, ObjectMap<String, String> properties) {
        if (id != null && id.trim().isEmpty()) throw new IllegalArgumentException("Entity id must be nonempty when present");
        if (type == null || type.trim().isEmpty()) throw new IllegalArgumentException("Entity type is required");
        if (sprite != null && sprite.trim().isEmpty()) throw new IllegalArgumentException("Entity sprite must be nonempty when present");
        this.id = id;
        this.type = type;
        this.sprite = sprite;
        this.x = x;
        this.z = z;
        this.properties = properties == null ? new ObjectMap<>() : properties;
    }
}
