package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Advances a configurable 24-hour clock and updates a shared lighting environment. */
public final class DayNightCycle {
    private final LightingEnvironment environment;
    private final Vector3 sunDirection = new Vector3();
    private final Color ambient = new Color();
    private final Color sun = new Color();
    private float timeOfDay = 12f;
    private float secondsPerDay = 600f;
    private boolean paused;

    public DayNightCycle(LightingEnvironment environment) {
        if (environment == null) throw new IllegalArgumentException("Lighting environment is required");
        this.environment = environment;
        apply();
    }

    public void update(float deltaSeconds) {
        if (!paused && deltaSeconds > 0f) {
            timeOfDay = (timeOfDay + deltaSeconds * 24f / secondsPerDay) % 24f;
            apply();
        }
    }

    public DayNightCycle setTimeOfDay(float hours) {
        if (hours < 0f || hours >= 24f) throw new IllegalArgumentException("Time must be in [0, 24)");
        timeOfDay = hours;
        apply();
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

    public float getTimeOfDay() { return timeOfDay; }
    public float getSecondsPerDay() { return secondsPerDay; }
    public boolean isPaused() { return paused; }

    private void apply() {
        float solarAngle = (timeOfDay - 6f) * MathUtils.PI / 12f;
        float elevation = MathUtils.sin(solarAngle);
        float daylight = MathUtils.clamp((elevation + 0.12f) / 0.30f, 0f, 1f);
        daylight = daylight * daylight * (3f - 2f * daylight);
        float azimuth = (timeOfDay / 24f) * MathUtils.PI2;
        float horizontal = MathUtils.cos(elevation);
        sunDirection.set(MathUtils.cos(azimuth) * horizontal, Math.max(0.05f, elevation),
            MathUtils.sin(azimuth) * horizontal).nor();
        ambient.set(0.10f + 0.90f * daylight, 0.14f + 0.82f * daylight, 0.28f + 0.62f * daylight, 1f);
        sun.set(1f, 0.72f + 0.25f * daylight, 0.48f + 0.45f * daylight, 1f);
        environment.setAmbient(ambient, 0.28f + 0.72f * daylight);
        environment.setSun(sunDirection, sun, 0.95f * daylight);
    }
}
