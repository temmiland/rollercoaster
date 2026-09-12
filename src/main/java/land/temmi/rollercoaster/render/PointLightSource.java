package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;

/** A runtime point light with linear range attenuation. */
public final class PointLightSource {
    public final Vector3 position = new Vector3();
    /** Direction from the light into the scene when this source is a spot light. */
    public final Vector3 direction = new Vector3(0f, -1f, 0f);
    public final Color color = new Color(Color.WHITE);
    public float intensity = 1f;
    public float range = 4f;
    public boolean enabled = true;
    private boolean spot;
    private float innerConeCos = 1f;
    private float outerConeCos = 0f;

    public PointLightSource(float x, float y, float z, Color color, float intensity, float range) {
        setPosition(x, y, z);
        setColor(color);
        setIntensity(intensity);
        setRange(range);
    }

    public PointLightSource setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public PointLightSource setColor(Color color) {
        if (color == null) throw new IllegalArgumentException("Light color is required");
        this.color.set(color);
        return this;
    }

    public PointLightSource setIntensity(float intensity) {
        if (intensity < 0f) throw new IllegalArgumentException("Light intensity must be nonnegative");
        this.intensity = intensity;
        return this;
    }

    public PointLightSource setRange(float range) {
        if (range <= 0f) throw new IllegalArgumentException("Light range must be positive");
        this.range = range;
        return this;
    }

    /** Turns this source into a spot light. Angles are full cone angles in degrees. */
    public PointLightSource setSpot(Vector3 direction, float innerAngle, float outerAngle) {
        if (direction == null || direction.len2() < 0.000001f || innerAngle < 0f
            || outerAngle <= innerAngle || outerAngle > 180f) {
            throw new IllegalArgumentException("Invalid spot light cone");
        }
        this.direction.set(direction).nor();
        innerConeCos = MathUtils.cosDeg(innerAngle * 0.5f);
        outerConeCos = MathUtils.cosDeg(outerAngle * 0.5f);
        spot = true;
        return this;
    }

    public PointLightSource setPoint() {
        spot = false;
        return this;
    }

    public boolean isSpot() { return spot; }
    public float getInnerConeCos() { return innerConeCos; }
    public float getOuterConeCos() { return outerConeCos; }
}
