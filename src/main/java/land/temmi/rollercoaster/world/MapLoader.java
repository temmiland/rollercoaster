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
        return parse(root, tileset);
    }

    /**
     * Reads a map out of an already parsed document, so a document that holds several maps - a
     * map folded out of walking planes, say - can carry them inline rather than by file name.
     */
    public LoadedMap parse(JsonValue root, Tileset tileset) {
        if (root == null || tileset == null) throw new IllegalArgumentException("Map data and tileset are required");
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
        JsonValue shapeLayer = layers.get("shape");
        // JsonValue is a linked list, so rows and cells are walked instead of indexed -
        // get(int) would make loading quadratic in the map size.
        JsonValue tileRow = tileLayer.child;
        JsonValue heightRow = heightLayer == null ? null : heightLayer.child;
        JsonValue collisionRow = collisionLayer == null ? null : collisionLayer.child;
        JsonValue shapeRow = shapeLayer == null ? null : shapeLayer.child;
        for (int z = 0; z < depth; z++) {
            JsonValue tileCell = cells(tileRow, z, width, "tile");
            JsonValue heightCell = heightRow == null ? null : cells(heightRow, z, width, "height");
            JsonValue collisionCell = collisionRow == null ? null : cells(collisionRow, z, width, "collision");
            JsonValue shapeCell = shapeRow == null ? null : cells(shapeRow, z, width, "shape");
            for (int x = 0; x < width; x++) {
                map.set(x, z, tileset.get(tileCell.asString()),
                    heightCell == null ? 0f : heightCell.asFloat(),
                    shapeCell == null ? TileShape.FLAT : TileShape.parse(shapeCell.asString()),
                    collisionCell != null && collisionCell.asInt() != 0);
                tileCell = tileCell.next;
                if (heightCell != null) heightCell = heightCell.next;
                if (collisionCell != null) collisionCell = collisionCell.next;
                if (shapeCell != null) shapeCell = shapeCell.next;
            }
            tileRow = tileRow.next;
            if (heightRow != null) heightRow = heightRow.next;
            if (collisionRow != null) collisionRow = collisionRow.next;
            if (shapeRow != null) shapeRow = shapeRow.next;
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
            entities.add(new MapEntity(entity.getString("id", null), requiredString(entity, "type"), entity.getString("sprite", null),
                entity.getInt("x"), entity.getInt("y", 0)));
        }
        Array<MapLight> lights = new Array<>();
        JsonValue lightArray = root.get("lights");
        if (lightArray != null) for (JsonValue light = lightArray.child; light != null; light = light.next) {
            JsonValue color = required(light, "color");
            if (!color.isArray() || color.size < 3) throw error("light color must have 3 components");
            boolean spot = light.getBoolean("spot", false);
            JsonValue direction = light.get("direction");
            float dirX = direction == null ? 0f : direction.getFloat(0);
            float dirY = direction == null ? -1f : direction.getFloat(1);
            float dirZ = direction == null ? 0f : direction.getFloat(2);
            lights.add(new MapLight(requiredString(light, "id"), light.getFloat("x"), light.getFloat("y"),
                light.getFloat("z"), color.getFloat(0), color.getFloat(1), color.getFloat(2),
                light.getFloat("intensity", 1f), light.getFloat("range", 4f), light.getBoolean("enabled", true),
                spot, dirX, dirY, dirZ, light.getFloat("innerAngle", 0f), light.getFloat("outerAngle", 0f)));
        }
        Array<MapTransition> transitions = new Array<>();
        JsonValue transitionArray = root.get("transitions");
        if (transitionArray != null) {
            for (JsonValue transition = transitionArray.child; transition != null; transition = transition.next) {
                transitions.add(new MapTransition(requiredString(transition, "id"), transition.getInt("x"),
                    transition.getInt("y", 0), requiredString(transition, "targetMap"),
                    transition.getInt("targetX"), transition.getInt("targetY", 0)));
            }
        }
        return new LoadedMap(name, map, props, entities, lights, transitions);
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

    /** Validates a layer row and returns its first cell. A row holds one cell per column. */
    private static JsonValue cells(JsonValue row, int index, int columns, String name) {
        if (row == null || !row.isArray() || row.size < columns) throw error(name + " layer has invalid row " + index);
        return row.child;
    }

    private static IllegalArgumentException error(String message) { return new IllegalArgumentException("Invalid map: " + message); }
}
