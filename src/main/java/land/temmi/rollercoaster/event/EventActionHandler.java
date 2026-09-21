package land.temmi.rollercoaster.event;

/** What an EventDispatcher calls when a triggered event's actions run - the game, or a test tool,
 * decides what "open a door" or "start a dialogue" actually does. Every method defaults to a no-op
 * so a handler only needs to implement the actions it cares about. */
public interface EventActionHandler {
    default void onStartDialogue(String dialogueId) {
    }

    default void onMoveNpc(String entityId, int x, int z) {
    }

    default void onOpenDoor(String entityId, boolean open) {
    }

    default void onChangeMap(String targetMap, int x, int z) {
    }

    default void onSetFlag(String key, String value) {
    }

    default void onToggleLight(String lightId, boolean enabled) {
    }
}
