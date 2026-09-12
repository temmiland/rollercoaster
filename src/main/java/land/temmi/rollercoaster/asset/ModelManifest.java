package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Reflection-free parser for model placement metadata. */
public final class ModelManifest {
    public static final int CURRENT_VERSION = 1;

    private ModelManifest() { }

    public static Array<ModelDefinition> load(FileHandle file) {
        if (file == null) throw new IllegalArgumentException("Model manifest is required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);
        JsonValue models = root.get("models");
        if (models == null || !models.isArray()) throw error("models must be an array");
        Array<ModelDefinition> definitions = new Array<>();
        for (JsonValue model = models.child; model != null; model = model.next) {
            String id = requiredString(model, "id");
            String source = requiredString(model, "source");
            JsonValue anchor = requiredArray(model, "anchor", 3);
            float scale = model.getFloat("scale");
            float height = model.getFloat("height");
            JsonValue bounds = required(model, "bounds");
            JsonValue boundsMin = requiredArray(bounds, "min", 3);
            JsonValue boundsMax = requiredArray(bounds, "max", 3);
            JsonValue collision = required(model, "collision");
            JsonValue min = requiredArray(collision, "min", 2);
            JsonValue max = requiredArray(collision, "max", 2);
            definitions.add(new ModelDefinition(id, source,
                anchor.getFloat(0), anchor.getFloat(1), anchor.getFloat(2),
                scale, height,
                boundsMin.getFloat(0), boundsMin.getFloat(1), boundsMin.getFloat(2),
                boundsMax.getFloat(0), boundsMax.getFloat(1), boundsMax.getFloat(2),
                min.getInt(0), max.getInt(0), min.getInt(1), max.getInt(1)));
        }
        return definitions;
    }

    private static JsonValue required(JsonValue parent, String key) {
        JsonValue value = parent.get(key);
        if (value == null) throw error("Missing field: " + key);
        return value;
    }

    private static JsonValue requiredArray(JsonValue parent, String key, int size) {
        JsonValue value = required(parent, key);
        if (!value.isArray() || value.size < size) throw error(key + " must contain " + size + " values");
        return value;
    }

    private static String requiredString(JsonValue parent, String key) {
        JsonValue value = required(parent, key);
        if (!value.isString()) throw error(key + " must be a string");
        return value.asString();
    }

    private static IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid model manifest: " + message);
    }
}
