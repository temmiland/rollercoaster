package land.temmi.rollercoaster.event;

import com.badlogic.gdx.utils.ObjectMap;

/** Flags, variables and time of day that a ConditionEvaluator reads and SET_FLAG actions write to.
 * Plain mutable state - whatever holds it (the game, or a test tool) decides when to reset it. */
public final class GameState {
    private final ObjectMap<String, String> flags = new ObjectMap<>();
    private final ObjectMap<String, String> variables = new ObjectMap<>();
    private float timeOfDay;

    public String getFlag(String key) {
        return flags.get(key);
    }

    public void setFlag(String key, String value) {
        flags.put(key, value);
    }

    public String getVariable(String key) {
        return variables.get(key);
    }

    public void setVariable(String key, String value) {
        variables.put(key, value);
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(float timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public void clearFlags() {
        flags.clear();
    }

    public void clearVariables() {
        variables.clear();
    }
}
