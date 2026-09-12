package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** A fixed lighting preset selected by the in-game clock. */
public enum LightingSituation {
    EARLY_MORNING("early morning", 5f, 0.35f,
        new Color(0.58f, 0.48f, 0.66f, 1f), 0.34f,
        new Vector3(-0.55f, 0.65f, -0.35f), new Color(1f, 0.40f, 0.20f, 1f), 0.38f),
    DAY("day", 8f, 0f,
        new Color(0.70f, 0.78f, 0.92f, 1f), 0.28f,
        new Vector3(-0.45f, 0.90f, -0.20f), new Color(1f, 0.92f, 0.80f, 1f), 0.70f),
    EARLY_EVENING("early evening", 17f, 0.40f,
        new Color(0.60f, 0.52f, 0.70f, 1f), 0.36f,
        new Vector3(0.55f, 0.55f, -0.35f), new Color(1f, 0.55f, 0.30f, 1f), 0.38f),
    NIGHT("night", 20f, 0.45f,
        new Color(0.30f, 0.38f, 0.58f, 1f), 0.48f,
        new Vector3(-0.25f, 0.85f, -0.35f), new Color(0.38f, 0.48f, 0.75f, 1f), 0.20f);

    private final String displayName;
    private final float startHour;
    private final float localLightFactor;
    private final Color ambientColor;
    private final float ambientIntensity;
    private final Vector3 sunDirection;
    private final Color sunColor;
    private final float sunIntensity;

    LightingSituation(String displayName, float startHour, float localLightFactor,
        Color ambientColor, float ambientIntensity, Vector3 sunDirection,
        Color sunColor, float sunIntensity) {
        this.displayName = displayName;
        this.startHour = startHour;
        this.localLightFactor = localLightFactor;
        this.ambientColor = ambientColor;
        this.ambientIntensity = ambientIntensity;
        this.sunDirection = sunDirection;
        this.sunColor = sunColor;
        this.sunIntensity = sunIntensity;
    }

    public static LightingSituation forTime(float hours) {
        float normalized = hours % 24f;
        if (normalized < 0f) normalized += 24f;
        if (normalized >= NIGHT.startHour || normalized < EARLY_MORNING.startHour) return NIGHT;
        if (normalized >= EARLY_EVENING.startHour) return EARLY_EVENING;
        if (normalized >= DAY.startHour) return DAY;
        return EARLY_MORNING;
    }

    public LightingSituation next() {
        LightingSituation[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public LightingSituation applyTo(LightingEnvironment environment) {
        if (environment == null) throw new IllegalArgumentException("Lighting environment is required");
        environment.setAmbient(ambientColor, ambientIntensity);
        environment.setSun(sunDirection, sunColor, sunIntensity);
        return this;
    }

    public String getDisplayName() { return displayName; }
    public float getStartHour() { return startHour; }
    public float getLocalLightFactor() { return localLightFactor; }
}
