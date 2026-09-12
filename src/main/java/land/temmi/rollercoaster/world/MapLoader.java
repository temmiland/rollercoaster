package land.temmi.rollercoaster.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Manual JSON parser kept reflection-free for RoboVM. */
public final class MapLoader {
    public static final int CURRENT_VERSION = 1;

    public LoadedMap load(FileHandle file, Tileset tileset) {
        if (file == null || tileset == null) throw new IllegalArgumentException("Map file and tileset are required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);
        String name = requiredString(root, "name");
        JsonValue size = required(root, "size");
        if (!size.isArray() || size.size < 2) throw error("size must contain width and depth");
        int width = size.getInt(0);
        int depth = size.getInt(1);
        TileMap map = new TileMap(width, depth);
        JsonValue layers = required(root, "layers");
        JsonValue tileLayer = required(layers, "tile");
        JsonValue heightLayer = layers.get("height");
        JsonValue collisionLayer = layers.get("collision");
        for (int z = 0; z < depth; z++) {
            JsonValue tileRow = row(tileLayer, z, depth, "tile");
            for (int x = 0; x < width; x++) {
                String tileName = tileRow.getString(x);
                float height = heightLayer == null ? 0f : row(heightLayer, z, depth, "height").getFloat(x);
                boolean blocked = collisionLayer != null && row(collisionLayer, z, depth, "collision").getInt(x) != 0;
                map.set(x, z, tileset.get(tileName), height, blocked);
            }
        }
        Array<MapProp> props = new Array<>();
        JsonValue propArray = root.get("props");
        if (propArray != null) for (JsonValue prop = propArray.child; prop != null; prop = prop.next) {
            props.add(new MapProp(requiredString(prop, "model"), prop.getFloat("x"), prop.getFloat("y", 0f),
                prop.getFloat("elevation", 0f), prop.getFloat("rot", 0f)));
        }
        Array<MapEntity> entities = new Array<>();
        JsonValue entityArray = root.get("entities");
        if (entityArray != null) for (JsonValue entity = entityArray.child; entity != null; entity = entity.next) {
            entities.add(new MapEntity(requiredString(entity, "type"), entity.getString("sprite", null),
                entity.getInt("x"), entity.getInt("y", 0)));
        }
        return new LoadedMap(name, map, props, entities);
    }

    private static JsonValue required(JsonValue parent, String key) {
        JsonValue value = parent.get(key);
        if (value == null) throw error("Missing field: " + key);
        return value;
    }

    private static String requiredString(JsonValue parent, String key) {
        JsonValue value = required(parent, key);
        if (!value.isString()) throw error(key + " must be a string");
        return value.asString();
    }

    private static JsonValue row(JsonValue layer, int index, int expected, String name) {
        JsonValue value = layer.get(index);
        if (value == null || !value.isArray() || value.size < expected) throw error(name + " layer has invalid row " + index);
        return value;
    }

    private static IllegalArgumentException error(String message) { return new IllegalArgumentException("Invalid map: " + message); }
}
