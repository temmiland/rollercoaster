package land.temmi.rollercoaster.event;

/** What starts a game event. Parsed data only - the game watches for these moments and decides
 * when they occur; the engine only carries which one and its parameters. */
public final class EventTrigger {
    public enum Type { MAP_START, INTERACTION, ENTER_AREA, TIME_CHANGE }

    public final Type type;
    public final String entityId;
    public final int x;
    public final int z;
    public final String timeOfDay;

    public static EventTrigger mapStart() {
        return new EventTrigger(Type.MAP_START, null, 0, 0, null);
    }

    public static EventTrigger interaction(String entityId) {
        return new EventTrigger(Type.INTERACTION, entityId, 0, 0, null);
    }

    public static EventTrigger enterArea(int x, int z) {
        return new EventTrigger(Type.ENTER_AREA, null, x, z, null);
    }

    public static EventTrigger timeChange(String timeOfDay) {
        return new EventTrigger(Type.TIME_CHANGE, null, 0, 0, timeOfDay);
    }

    public EventTrigger(Type type, String entityId, int x, int z, String timeOfDay) {
        if (type == null) throw new IllegalArgumentException("Trigger type is required");
        if (type == Type.INTERACTION && (entityId == null || entityId.trim().isEmpty())) {
            throw new IllegalArgumentException("INTERACTION trigger requires an entity id");
        }
        if (type == Type.TIME_CHANGE && (timeOfDay == null || timeOfDay.trim().isEmpty())) {
            throw new IllegalArgumentException("TIME_CHANGE trigger requires a time of day");
        }
        this.type = type;
        this.entityId = entityId;
        this.x = x;
        this.z = z;
        this.timeOfDay = timeOfDay;
    }
}
