package land.temmi.rollercoaster.render;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Camera orientation for a room whose walking plane can change.
 *
 * <p>The camera keeps the world's up axis and never rolls: pillars stay vertical on screen no
 * matter which plane the player walks on. What changes is where it looks from - it always moves to
 * the free side of the current plane, so a ceiling is watched from below - and the orientation of
 * the sprite, which turns in the image plane to keep its feet on that plane.
 *
 * <p>Each plane's own right axis is what the framing is built from, which is what keeps the
 * controls honest: the right-hand input always moves the player right across the screen.
 */
public final class SurfaceCamera {
    /** The heading the ordinary field camera looks along; walls are swung half way off it. */
    private static final Vector3 DEFAULT_HEADING = new Vector3(0f, 0f, -1f);

    private final Quaternion current = new Quaternion();
    private final Quaternion start = new Quaternion();
    private final Quaternion target = new Quaternion();
    private float roll;
    private float startRoll;
    private float targetRoll;
    private final Vector3 direction = new Vector3();
    private final Vector3 up = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 horizontal = new Vector3();
    private final Vector3 heading = new Vector3();
    private final Matrix4 basis = new Matrix4();
    private float pitchDegrees = 45f;
    private float blendSeconds = 0.28f;
    private float elapsed;
    private boolean blending;

    /** Matches the pitch of the ordinary field camera so both rooms frame a subject alike. */
    public SurfaceCamera setPitch(float pitchDegrees) {
        if (pitchDegrees <= 0f || pitchDegrees >= 90f) throw new IllegalArgumentException("Pitch must lie between 0 and 90 degrees");
        this.pitchDegrees = pitchDegrees;
        return this;
    }

    public SurfaceCamera setBlendSeconds(float blendSeconds) {
        if (blendSeconds <= 0f) throw new IllegalArgumentException("Blend duration must be positive");
        this.blendSeconds = blendSeconds;
        return this;
    }

    public float getPitchDegrees() { return pitchDegrees; }
    public boolean isBlending() { return blending; }

    public void snapTo(Vector3 planeNormal, Vector3 planeRight) {
        orientation(planeNormal, planeRight, current);
        start.set(current);
        target.set(current);
        roll = startRoll = targetRoll = rollFor(planeNormal, current);
        blending = false;
        elapsed = 0f;
    }

    /** Ignored while already heading for the same plane, so repeated steps do not restart it. */
    public void blendTo(Vector3 planeNormal, Vector3 planeRight) {
        orientation(planeNormal, planeRight, target);
        float wanted = rollFor(planeNormal, target);
        if (Math.abs(current.dot(target)) > 0.99999f && Math.abs(shortestDelta(roll, wanted)) < 0.01f) {
            current.set(target);
            roll = targetRoll = wanted;
            blending = false;
            return;
        }
        start.set(current);
        startRoll = roll;
        targetRoll = wanted;
        elapsed = 0f;
        blending = true;
    }

    public void update(float delta) {
        if (!blending) return;
        elapsed += Math.max(0f, delta);
        if (elapsed >= blendSeconds) {
            current.set(target);
            roll = targetRoll;
            blending = false;
            return;
        }
        float t = elapsed / blendSeconds;
        t = t * t * (3f - 2f * t);
        current.set(start).slerp(target, t);
        // The roll turns as an angle rather than as a vector, so turning fully over stays defined.
        roll = startRoll + shortestDelta(startRoll, targetRoll) * t;
    }

    public Vector3 direction(Vector3 out) { return current.transform(out.set(0f, 0f, -1f)); }
    public Vector3 up(Vector3 out) { return current.transform(out.set(0f, 1f, 0f)); }

    /**
     * Billboard axes for a sprite standing on the current plane: the camera's own axes turned in
     * the image plane until the sprite's up points the way the plane's normal points on screen.
     * Under a ceiling that is half a turn, which is what hangs the sprite upside down.
     */
    public void spriteBasis(Vector3 outRight, Vector3 outUp) {
        direction(direction);
        up(up);
        right.set(direction).crs(up).nor();
        float cos = MathUtils.cosDeg(roll);
        float sin = MathUtils.sinDeg(roll);
        outRight.set(right).scl(cos).mulAdd(up, -sin);
        outUp.set(up).scl(cos).mulAdd(right, sin);
    }

    /** Angle from the camera's up axis to the plane's normal, measured in the image plane. */
    private float rollFor(Vector3 planeNormal, Quaternion orientation) {
        orientation.transform(direction.set(0f, 0f, -1f));
        orientation.transform(up.set(0f, 1f, 0f));
        right.set(direction).crs(up).nor();
        float alongRight = planeNormal.dot(right);
        float alongUp = planeNormal.dot(up);
        // A normal pointing straight at the camera has no image-plane direction to align with.
        if (alongRight * alongRight + alongUp * alongUp < 0.0001f) return 0f;
        return MathUtils.atan2(alongRight, alongUp) * MathUtils.radiansToDegrees;
    }

    private static float shortestDelta(float from, float to) {
        return ((to - from + 180f) % 360f + 360f) % 360f - 180f;
    }

    private void orientation(Vector3 planeNormal, Vector3 planeRight, Quaternion out) {
        if (planeNormal == null || planeRight == null) throw new IllegalArgumentException("Plane normal and right axis are required");
        // Looking against the plane's right axis puts screen right on it, whatever the plane is.
        horizontal.set(planeRight).crs(Vector3.Y).nor().scl(-1f);
        // A wall is only swung half way onto its face. Facing it squarely would stand the sprite
        // back up and flatten the room; half way keeps the ordinary heading and lays the sprite
        // on its side, while the climbing axis still runs straight up the screen.
        heading.set(horizontal).add(DEFAULT_HEADING);
        if (heading.len2() > 0.0001f) horizontal.set(heading).nor();
        // The pitch follows how much the plane faces up: down onto the ground, up under a
        // ceiling, and level at a wall, whose face needs neither.
        float pitch = pitchDegrees * planeNormal.y * MathUtils.degreesToRadians;
        direction.set(horizontal).scl(MathUtils.cos(pitch)).add(0f, -MathUtils.sin(pitch), 0f).nor();
        up.set(Vector3.Y).mulAdd(direction, -Vector3.Y.dot(direction)).nor();
        right.set(direction).crs(up).nor();
        float[] m = basis.idt().val;
        m[Matrix4.M00] = right.x; m[Matrix4.M10] = right.y; m[Matrix4.M20] = right.z;
        m[Matrix4.M01] = up.x; m[Matrix4.M11] = up.y; m[Matrix4.M21] = up.z;
        m[Matrix4.M02] = -direction.x; m[Matrix4.M12] = -direction.y; m[Matrix4.M22] = -direction.z;
        basis.getRotation(out, true);
    }
}
