package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Locale;
import land.temmi.rollercoaster.actor.Facing;

/** Reflection-free parser for editor-exported directional sprite metadata. The atlas is relative to the manifest. */
public final class SpriteManifest {
    public static final int CURRENT_VERSION = 1;

    public final int version;
    public final String atlas;
    private final Array<SpriteDefinition> sprites;

    private SpriteManifest(int version, String atlas, Array<SpriteDefinition> sprites) {
        this.version = version;
        this.atlas = atlas;
        this.sprites = sprites;
    }

    public static SpriteManifest load(FileHandle file) {
        if (file == null) throw new IllegalArgumentException("Sprite manifest is required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);
        String atlas = requiredString(root, "atlas");
        JsonValue spriteArray = required(root, "sprites");
        if (!spriteArray.isArray()) throw error("sprites must be an array");
        Array<SpriteDefinition> definitions = new Array<>();
        for (JsonValue sprite = spriteArray.child; sprite != null; sprite = sprite.next) {
            String id = requiredString(sprite, "id");
            float height = sprite.getFloat("height");
            float frameDuration = sprite.getFloat("frameDuration");
            float footOffset = sprite.getFloat("footOffset", 0f);
            JsonValue directions = required(sprite, "directions");
            if (!directions.isObject()) throw error("directions must be an object for " + id);
            String[] idleRegions = new String[Facing.values().length];
            String[][] walkRegions = new String[Facing.values().length][];
            for (Facing facing : Facing.values()) {
                String key = facing.name().toLowerCase(Locale.ROOT);
                JsonValue direction = directions.get(key);
                if (direction == null || !direction.isObject()) {
                    throw error("Missing direction " + key + " for " + id);
                }
                idleRegions[facing.ordinal()] = requiredString(direction, "idle");
                JsonValue walk = required(direction, "walk");
                if (!walk.isArray() || walk.size == 0) throw error("walk must contain frames for " + id + "/" + key);
                String[] frames = new String[walk.size];
                int index = 0;
                for (JsonValue frame = walk.child; frame != null; frame = frame.next) {
                    if (!frame.isString()) throw error("walk frame must be a string for " + id + "/" + key);
                    frames[index++] = frame.asString();
                }
                walkRegions[facing.ordinal()] = frames;
            }
            for (SpriteDefinition existing : definitions) {
                if (existing.id.equals(id)) throw error("Duplicate sprite ID: " + id);
            }
            definitions.add(new SpriteDefinition(id, height, frameDuration, footOffset, idleRegions, walkRegions));
        }
        if (definitions.size == 0) throw error("sprites must not be empty");
        return new SpriteManifest(version, atlas, definitions);
    }

    public SpriteDefinition sprite(String id) {
        if (id == null) throw new IllegalArgumentException("Sprite ID is required");
        for (SpriteDefinition definition : sprites) {
            if (definition.id.equals(id)) return definition;
        }
        throw new IllegalArgumentException("Unknown sprite: " + id);
    }

    public Array<SpriteDefinition> getSprites() { return sprites; }

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

    private static IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid sprite manifest: " + message);
    }
}
