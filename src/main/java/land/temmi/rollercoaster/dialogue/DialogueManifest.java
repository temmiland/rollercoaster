package land.temmi.rollercoaster.dialogue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Locale;
import land.temmi.rollercoaster.event.Condition;

/** Reflection-free parser for editor-exported dialogue trees. */
public final class DialogueManifest {
    public static final int CURRENT_VERSION = 1;

    public final int version;
    private final Array<Dialogue> dialogues;

    private DialogueManifest(int version, Array<Dialogue> dialogues) {
        this.version = version;
        this.dialogues = dialogues;
    }

    public static DialogueManifest load(FileHandle file) {
        if (file == null) throw new IllegalArgumentException("Dialogue manifest is required");
        JsonValue root = new JsonReader().parse(file);
        int version = root.getInt("version", CURRENT_VERSION);
        if (version != CURRENT_VERSION) throw error("Unsupported version: " + version);
        JsonValue dialogueArray = required(root, "dialogues");
        if (!dialogueArray.isArray()) throw error("dialogues must be an array");
        Array<Dialogue> dialogues = new Array<>();
        for (JsonValue dialogue = dialogueArray.child; dialogue != null; dialogue = dialogue.next) {
            String id = requiredString(dialogue, "id");
            String startNodeId = requiredString(dialogue, "startNode");
            JsonValue nodeArray = required(dialogue, "nodes");
            if (!nodeArray.isArray()) throw error("nodes must be an array for " + id);
            Array<DialogueNode> nodes = new Array<>();
            for (JsonValue node = nodeArray.child; node != null; node = node.next) {
                Array<DialogueResponse> responses = new Array<>();
                JsonValue responseArray = node.get("responses");
                if (responseArray != null) for (JsonValue response = responseArray.child; response != null; response = response.next) {
                    responses.add(new DialogueResponse(requiredString(response, "textId"),
                        response.getString("targetNode", null), readConditions(response)));
                }
                nodes.add(new DialogueNode(requiredString(node, "id"), requiredString(node, "speaker"),
                    requiredString(node, "textId"), node.getString("portrait", null), responses));
            }
            for (Dialogue existing : dialogues) {
                if (existing.id.equals(id)) throw error("Duplicate dialogue ID: " + id);
            }
            dialogues.add(new Dialogue(id, startNodeId, nodes));
        }
        return new DialogueManifest(version, dialogues);
    }

    public Dialogue dialogue(String id) {
        if (id == null) throw new IllegalArgumentException("Dialogue id is required");
        for (Dialogue dialogue : dialogues) if (dialogue.id.equals(id)) return dialogue;
        throw new IllegalArgumentException("Unknown dialogue: " + id);
    }

    public Array<Dialogue> getDialogues() { return dialogues; }

    static Array<Condition> readConditions(JsonValue parent) {
        Array<Condition> conditions = new Array<>();
        JsonValue conditionArray = parent.get("conditions");
        if (conditionArray != null) for (JsonValue condition = conditionArray.child; condition != null; condition = condition.next) {
            Condition.Type type = Condition.Type.valueOf(requiredString(condition, "type").toUpperCase(Locale.ROOT));
            Condition.Comparison comparison = Condition.Comparison.valueOf(
                condition.getString("comparison", "EQUALS").toUpperCase(Locale.ROOT));
            conditions.add(new Condition(type, requiredString(condition, "key"), comparison, requiredString(condition, "value")));
        }
        return conditions;
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

    private static IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Invalid dialogue manifest: " + message);
    }
}
