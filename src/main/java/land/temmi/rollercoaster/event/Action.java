package land.temmi.rollercoaster.event;

/** A single effect an event can cause. Parsed data only - the game decides what "opening a door"
 * or "setting a flag" actually does; the engine just carries the labelled parameters. */
public final class Action {
    public enum Type { START_DIALOGUE, MOVE_NPC, OPEN_DOOR, CHANGE_MAP, SET_FLAG, TOGGLE_LIGHT }

    public final Type type;
    public final String targetId;
    public final String value;
    public final int x;
    public final int z;
    public final String targetMap;

    public static Action startDialogue(String dialogueId) {
        return new Action(Type.START_DIALOGUE, dialogueId, null, 0, 0, null);
    }

    public static Action moveNpc(String entityId, int x, int z) {
        return new Action(Type.MOVE_NPC, entityId, null, x, z, null);
    }

    public static Action openDoor(String entityId, boolean open) {
        return new Action(Type.OPEN_DOOR, entityId, Boolean.toString(open), 0, 0, null);
    }

    public static Action changeMap(String targetMap, int x, int z) {
        return new Action(Type.CHANGE_MAP, null, null, x, z, targetMap);
    }

    public static Action setFlag(String key, String value) {
        return new Action(Type.SET_FLAG, key, value, 0, 0, null);
    }

    public static Action toggleLight(String lightId, boolean enabled) {
        return new Action(Type.TOGGLE_LIGHT, lightId, Boolean.toString(enabled), 0, 0, null);
    }

    public Action(Type type, String targetId, String value, int x, int z, String targetMap) {
        if (type == null) throw new IllegalArgumentException("Action type is required");
        if (type == Type.CHANGE_MAP) {
            if (targetMap == null || targetMap.trim().isEmpty()) {
                throw new IllegalArgumentException("CHANGE_MAP action requires a target map");
            }
        } else if (targetId == null || targetId.trim().isEmpty()) {
            throw new IllegalArgumentException(type + " action requires a target id");
        }
        this.type = type;
        this.targetId = targetId;
        this.value = value;
        this.x = x;
        this.z = z;
        this.targetMap = targetMap;
    }
}
