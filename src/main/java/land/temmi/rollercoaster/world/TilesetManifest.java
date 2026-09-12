package land.temmi.rollercoaster.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Reflection-free parser for editor-exported tileset metadata. */
public final class TilesetManifest {
    public static final int CURRENT_VERSION = 1;

    public final int version;
    public final String id;
    public final String texture;
    public final int tileWidth;
    public final int tileHeight;
    public final Array<TileDefinition> tiles;

    private TilesetManifest(int version, String id, String texture, int tileWidth, int tileHeight,
                            Array<TileDefinition> tiles) {
        this.version = version;
        this.id = id;
        this.texture = texture;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = tiles;
    }

    public static TilesetManifest load(FileHandle file) {
        if (file == null) throw new IllegalArgumentException("Tileset manifest is required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);
        String id = requiredString(root, "id");
        String texture = requiredString(root, "texture");
        JsonValue tileSize = requiredArray(root, "tileSize", 2);
        int tileWidth = tileSize.getInt(0);
        int tileHeight = tileSize.getInt(1);
        if (tileWidth <= 0 || tileHeight <= 0) throw error("tileSize must be positive");
        JsonValue tileArray = required(root, "tiles");
        if (!tileArray.isArray()) throw error("tiles must be an array");
        Array<TileDefinition> tiles = new Array<>();
        for (JsonValue tile = tileArray.child; tile != null; tile = tile.next) {
            String tileId = requiredString(tile, "id");
            JsonValue region = requiredArray(tile, "region", 4);
            JsonValue side = tile.get("side");
            if (side != null && (!side.isArray() || side.size < 4)) throw error("side must contain 4 values");
            if (side == null) side = region;
            TileDefinition definition = new TileDefinition(tileId, region.getInt(0), region.getInt(1),
                region.getInt(2), region.getInt(3), side.getInt(0), side.getInt(1),
                side.getInt(2), side.getInt(3), tile.getBoolean("walkable", true));
            for (TileDefinition existing : tiles) {
                if (existing.id.equals(definition.id)) throw error("Duplicate tile ID: " + definition.id);
            }
            tiles.add(definition);
        }
        if (tiles.size == 0) throw error("tiles must not be empty");
        return new TilesetManifest(version, id, texture, tileWidth, tileHeight, tiles);
    }

    private static JsonValue required(JsonValue parent, String key) {
        JsonValue value = parent.get(key);
        if (value == null) throw error("Missing field: " + key);
        return value;
    }

    private static String requiredString(JsonValue parent, String key) {
        JsonValue value = required(parent, key);
        if (!value.isString() || value.asString().length() == 0) throw error(key + " must be a nonempty string");
        return value.asString();
    }

    private static JsonValue requiredArray(JsonValue parent, String key, int size) {
        JsonValue value = required(parent, key);
        if (!value.isArray() || value.size < size) throw error(key + " must contain " + size + " values");
        return value;
    }

    private static IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid tileset manifest: " + message);
    }
}
