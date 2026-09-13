package land.temmi.rollercoaster.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import land.temmi.rollercoaster.input.MoveIntent;

/**
 * Reads a map folded out of walking planes from one document.
 *
 * <p>Every plane is an ordinary map with a gravity state and the world tile its local (0, 0) sits
 * on, so plane content is authored, textured and populated with props exactly like the ground of
 * any other map. Crossings carry a step and a count because the corners of a map repeat along the
 * axis the planes share.
 */
public final class FoldedMapLoader {
    public static final int CURRENT_VERSION = 1;

    private final MapLoader mapLoader;

    public FoldedMapLoader() {
        this(new MapLoader());
    }

    public FoldedMapLoader(MapLoader mapLoader) {
        if (mapLoader == null) throw new IllegalArgumentException("Map loader is required");
        this.mapLoader = mapLoader;
    }

    public FoldedMap load(FileHandle file, Tileset tileset) {
        if (file == null || tileset == null) throw new IllegalArgumentException("Map file and tileset are required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);

        FoldedMap map = new FoldedMap();
        if (root.has("crossingArc")) map.setCrossingArcHeight(root.getFloat("crossingArc"));

        JsonValue planes = required(root, "planes");
        for (JsonValue plane = planes.child; plane != null; plane = plane.next) {
            Vector3 origin = tile(required(plane, "origin"), "origin");
            map.add(new MapPlane(requiredString(plane, "name"),
                gravity(requiredString(plane, "gravity")), mapLoader.parse(plane, tileset),
                Math.round(origin.x), Math.round(origin.y), Math.round(origin.z)));
        }

        JsonValue crossings = root.get("crossings");
        if (crossings != null) {
            for (JsonValue crossing = crossings.child; crossing != null; crossing = crossing.next) {
                addCrossing(map, crossing);
            }
        }
        return map;
    }

    private void addCrossing(FoldedMap map, JsonValue crossing) {
        Vector3 from = tile(required(crossing, "from"), "from");
        Vector3 to = tile(required(crossing, "to"), "to");
        MoveIntent fromInput = input(requiredString(crossing, "fromInput"));
        MoveIntent toInput = input(requiredString(crossing, "toInput"));
        int count = crossing.getInt("count", 1);
        if (count < 1) throw error("Crossing count must be positive");
        Vector3 step = crossing.has("step") ? tile(crossing.get("step"), "step") : new Vector3();
        if (count > 1 && step.isZero()) throw error("A repeated crossing needs a step");
        for (int i = 0; i < count; i++) {
            map.crossing(from, fromInput, to, toInput);
            from.add(step);
            to.add(step);
        }
    }

    private static Vector3 tile(JsonValue value, String name) {
        if (!value.isArray() || value.size < 3) throw error(name + " must hold three tile coordinates");
        return new Vector3(value.getFloat(0), value.getFloat(1), value.getFloat(2));
    }

    private static GravityState gravity(String value) {
        if ("floor".equals(value)) return GravityState.FLOOR;
        if ("westWall".equals(value)) return GravityState.WEST_WALL;
        if ("eastWall".equals(value)) return GravityState.EAST_WALL;
        if ("ceiling".equals(value)) return GravityState.CEILING;
        throw error("Unknown gravity: " + value);
    }

    private static MoveIntent input(String value) {
        if ("up".equals(value)) return MoveIntent.UP;
        if ("down".equals(value)) return MoveIntent.DOWN;
        if ("left".equals(value)) return MoveIntent.LEFT;
        if ("right".equals(value)) return MoveIntent.RIGHT;
        throw error("Unknown input: " + value);
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

    private static IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid folded map: " + message);
    }
}
