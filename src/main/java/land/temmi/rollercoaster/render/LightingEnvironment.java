package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** Shared sun, ambient, and custom-light state for one ModelBatch. */
public final class LightingEnvironment {
    public static final int MAX_POINT_LIGHTS = 8;

    private final Color ambientColor = new Color(Color.WHITE);
    private final Vector3 sunDirection = new Vector3(0f, 1f, 0f);
    private final Color sunColor = new Color(Color.WHITE);
    private final Array<PointLightSource> pointLights = new Array<>();
    private float ambientIntensity = 1f;
    private float sunIntensity;

    public LightingEnvironment setAmbient(Color color, float intensity) {
        if (color == null || intensity < 0f) throw new IllegalArgumentException("Invalid ambient light");
        ambientColor.set(color);
        ambientIntensity = intensity;
        return this;
    }

    public LightingEnvironment setSun(Vector3 directionToLight, Color color, float intensity) {
        if (directionToLight == null || directionToLight.len2() < 0.000001f || color == null || intensity < 0f) {
            throw new IllegalArgumentException("Invalid sun light");
        }
        sunDirection.set(directionToLight).nor();
        sunColor.set(color);
        sunIntensity = intensity;
        return this;
    }

    public LightingEnvironment setAmbientIntensity(float intensity) {
        if (intensity < 0f) throw new IllegalArgumentException("Ambient intensity must be nonnegative");
        ambientIntensity = intensity;
        return this;
    }

    public LightingEnvironment setSunDirection(Vector3 directionToLight) {
        if (directionToLight == null || directionToLight.len2() < 0.000001f) {
            throw new IllegalArgumentException("Sun direction is required");
        }
        sunDirection.set(directionToLight).nor();
        return this;
    }

    public LightingEnvironment setSunColor(Color color) {
        if (color == null) throw new IllegalArgumentException("Sun color is required");
        sunColor.set(color);
        return this;
    }

    public LightingEnvironment setSunIntensity(float intensity) {
        if (intensity < 0f) throw new IllegalArgumentException("Sun intensity must be nonnegative");
        sunIntensity = intensity;
        return this;
    }

    public LightingEnvironment addPointLight(PointLightSource light) {
        if (light == null || pointLights.size >= MAX_POINT_LIGHTS) {
            throw new IllegalArgumentException("Maximum point lights exceeded");
        }
        pointLights.add(light);
        return this;
    }

    public LightingEnvironment removePointLight(PointLightSource light) {
        pointLights.removeValue(light, true);
        return this;
    }

    public LightingEnvironment clearPointLights() {
        pointLights.clear();
        return this;
    }

    public Color getAmbientColor() { return ambientColor; }
    public float getAmbientIntensity() { return ambientIntensity; }
    public Vector3 getSunDirection() { return sunDirection; }
    public Color getSunColor() { return sunColor; }
    public float getSunIntensity() { return sunIntensity; }
    public Array<PointLightSource> getPointLights() { return pointLights; }
}
