package land.temmi.rollercoaster.event;

/** Checks a GameEvent's conditions against GameState and, if every one passes, runs its actions
 * through an EventActionHandler. SET_FLAG writes straight to GameState, since a flag is plain data;
 * every other action type is the handler's call. Trigger detection - deciding *when* an event
 * should fire - stays the caller's job; this only runs an event once asked to. */
public final class EventDispatcher {
    private EventDispatcher() {
    }

    /** Returns whether the event's conditions passed and its actions ran. */
    public static boolean fire(GameEvent event, GameState state, EventActionHandler handler) {
        for (Condition condition : event.conditions) {
            if (!ConditionEvaluator.evaluate(condition, state)) return false;
        }
        for (Action action : event.actions) {
            perform(action, state, handler);
        }
        return true;
    }

    private static void perform(Action action, GameState state, EventActionHandler handler) {
        switch (action.type) {
            case START_DIALOGUE:
                handler.onStartDialogue(action.targetId);
                break;
            case MOVE_NPC:
                handler.onMoveNpc(action.targetId, action.x, action.z);
                break;
            case OPEN_DOOR:
                handler.onOpenDoor(action.targetId, Boolean.parseBoolean(action.value));
                break;
            case CHANGE_MAP:
                handler.onChangeMap(action.targetMap, action.x, action.z);
                break;
            case SET_FLAG:
                state.setFlag(action.targetId, action.value);
                handler.onSetFlag(action.targetId, action.value);
                break;
            case TOGGLE_LIGHT:
                handler.onToggleLight(action.targetId, Boolean.parseBoolean(action.value));
                break;
            default:
                throw new IllegalArgumentException("Unknown action type: " + action.type);
        }
    }
}
