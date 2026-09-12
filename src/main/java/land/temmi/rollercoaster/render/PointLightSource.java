package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/** A runtime point light with linear range attenuation. */
public final class PointLightSource {
    public final Vector3 position = new Vector3();
    public final Color color = new Color(Color.WHITE);
    public float intensity = 1f;
    public float range = 4f;
    public boolean enabled = true;

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
}
