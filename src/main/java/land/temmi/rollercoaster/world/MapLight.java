package land.temmi.rollercoaster.world;

/** A placed point or spot light, parsed straight off a map document. */
public final class MapLight {
    public final String id;
    public final float x;
    public final float y;
    public final float z;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float intensity;
    public final float range;
    public final boolean enabled;
    public final boolean spot;
    public final float directionX;
    public final float directionY;
    public final float directionZ;
    public final float innerAngle;
    public final float outerAngle;

    /** A plain point light. */
    public MapLight(String id, float x, float y, float z, float colorR, float colorG, float colorB,
                    float intensity, float range, boolean enabled) {
        this(id, x, y, z, colorR, colorG, colorB, intensity, range, enabled, false, 0f, -1f, 0f, 0f, 0f);
    }

    public MapLight(String id, float x, float y, float z, float colorR, float colorG, float colorB,
                    float intensity, float range, boolean enabled, boolean spot,
                    float directionX, float directionY, float directionZ, float innerAngle, float outerAngle) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Light id is required");
        if (intensity < 0f) throw new IllegalArgumentException("Light intensity must be nonnegative: " + id);
        if (range <= 0f) throw new IllegalArgumentException("Light range must be positive: " + id);
        if (spot && (innerAngle < 0f || outerAngle <= innerAngle || outerAngle > 180f)) {
            throw new IllegalArgumentException("Invalid spot light cone: " + id);
        }
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.intensity = intensity;
        this.range = range;
        this.enabled = enabled;
        this.spot = spot;
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
        this.innerAngle = innerAngle;
        this.outerAngle = outerAngle;
    }
}
