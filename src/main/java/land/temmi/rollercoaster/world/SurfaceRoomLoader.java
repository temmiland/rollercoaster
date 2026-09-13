package land.temmi.rollercoaster.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import land.temmi.rollercoaster.input.MoveIntent;

/**
 * Reads a room folded out of walking planes from one document.
 *
 * <p>Every plane is an ordinary map with a gravity state and the room tile its local (0, 0) sits
 * on, so plane content is authored, textured and populated with props exactly like the ground of
 * any other map. Seams carry a step and a count because the corners of a room repeat along the
 * axis the planes share.
 */
public final class SurfaceRoomLoader {
    public static final int CURRENT_VERSION = 1;

    private final MapLoader mapLoader;

    public SurfaceRoomLoader() {
        this(new MapLoader());
    }

    public SurfaceRoomLoader(MapLoader mapLoader) {
        if (mapLoader == null) throw new IllegalArgumentException("Map loader is required");
        this.mapLoader = mapLoader;
    }

    public SurfaceRoom load(FileHandle file, Tileset tileset) {
        if (file == null || tileset == null) throw new IllegalArgumentException("Room file and tileset are required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);

        SurfaceRoom room = new SurfaceRoom();
        if (root.has("seamArc")) room.setSeamArcHeight(root.getFloat("seamArc"));

        JsonValue planes = required(root, "planes");
        for (JsonValue plane = planes.child; plane != null; plane = plane.next) {
            Vector3 origin = tile(required(plane, "origin"), "origin");
            room.add(new SurfacePlatform(requiredString(plane, "name"),
                gravity(requiredString(plane, "gravity")), mapLoader.parse(plane, tileset),
                Math.round(origin.x), Math.round(origin.y), Math.round(origin.z)));
        }

        JsonValue seams = root.get("seams");
        if (seams != null) for (JsonValue seam = seams.child; seam != null; seam = seam.next) addSeam(room, seam);
        return room;
    }

    private void addSeam(SurfaceRoom room, JsonValue seam) {
        Vector3 from = tile(required(seam, "from"), "from");
        Vector3 to = tile(required(seam, "to"), "to");
        MoveIntent fromInput = input(requiredString(seam, "fromInput"));
        MoveIntent toInput = input(requiredString(seam, "toInput"));
        int count = seam.getInt("count", 1);
        if (count < 1) throw error("Seam count must be positive");
        Vector3 step = seam.has("step") ? tile(seam.get("step"), "step") : new Vector3();
        if (count > 1 && step.isZero()) throw error("A repeated seam needs a step");
        for (int i = 0; i < count; i++) {
            room.seam(from, fromInput, to, toInput);
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
        return new IllegalArgumentException("Invalid surface room: " + message);
    }
}
