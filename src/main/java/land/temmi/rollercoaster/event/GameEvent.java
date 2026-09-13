package land.temmi.rollercoaster.event;

import com.badlogic.gdx.utils.Array;

/** A placed event: something that starts it, optional conditions gating it, and the actions it
 * causes. Parsed data only - the game evaluates conditions and performs actions itself. */
public final class GameEvent {
    public final String id;
    public final EventTrigger trigger;
    public final Array<Condition> conditions;
    public final Array<Action> actions;

    public GameEvent(String id, EventTrigger trigger, Array<Condition> conditions, Array<Action> actions) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Event id is required");
        if (trigger == null) throw new IllegalArgumentException("Event trigger is required: " + id);
        if (actions == null || actions.size == 0) {
            throw new IllegalArgumentException("Event must have at least one action: " + id);
        }
        this.id = id;
        this.trigger = trigger;
        this.conditions = conditions == null ? new Array<>() : conditions;
        this.actions = actions;
    }
}
