package land.temmi.rollercoaster.render;

/** Advances a configurable 24-hour clock and applies fixed lighting presets on map entry. */
public final class DayNightCycle {
    private final LightingEnvironment environment;
    private float timeOfDay = 12f;
    private float secondsPerDay = 600f;
    private boolean paused;
    private LightingSituation situation;
    private LightingSituation pendingSituation;

    public DayNightCycle(LightingEnvironment environment) {
        if (environment == null) throw new IllegalArgumentException("Lighting environment is required");
        this.environment = environment;
        situation = LightingSituation.forTime(timeOfDay);
        pendingSituation = situation;
        situation.applyTo(environment);
    }

    public void update(float deltaSeconds) {
        if (!paused && deltaSeconds > 0f) {
            timeOfDay = (timeOfDay + deltaSeconds * 24f / secondsPerDay) % 24f;
            pendingSituation = LightingSituation.forTime(timeOfDay);
        }
    }

    public DayNightCycle setTimeOfDay(float hours) {
        if (hours < 0f || hours >= 24f) throw new IllegalArgumentException("Time must be in [0, 24)");
        timeOfDay = hours;
        pendingSituation = LightingSituation.forTime(hours);
        return this;
    }

    public DayNightCycle setSecondsPerDay(float seconds) {
        if (seconds <= 0f) throw new IllegalArgumentException("Day duration must be positive");
        secondsPerDay = seconds;
        return this;
    }

    public DayNightCycle setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    /** Applies the clock-selected preset after a map has been loaded. */
    public DayNightCycle enterMap() {
        situation = pendingSituation;
        situation.applyTo(environment);
        return this;
    }

    /** Cycles presets immediately, useful for previews and debug controls. */
    public DayNightCycle cycleSituation() {
        situation = situation.next();
        situation.applyTo(environment);
        return this;
    }

    public float getTimeOfDay() { return timeOfDay; }
    public float getSecondsPerDay() { return secondsPerDay; }
    public boolean isPaused() { return paused; }
    public LightingSituation getSituation() { return situation; }
    public LightingSituation getPendingSituation() { return pendingSituation; }
}
