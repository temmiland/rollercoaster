package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class PixelCamera {

    private static final float NEAR = 1f;
    private static final float FAR = 100f;

    public final PerspectiveCamera camera = new PerspectiveCamera();

    private final Vector3 billboardCenter = new Vector3();
    private final Vector3 right = new Vector3();

    private float fovDegrees = 30f;
    private float pitchDegrees = 45f;
    private float halfFovRad;
    private float distance;

    public void setFov(float fovDegrees) {
        this.fovDegrees = fovDegrees;
    }

    public void setPitch(float pitchDegrees) {
        this.pitchDegrees = pitchDegrees;
    }

    public void resize(int internalWidth, int internalHeight) {
        camera.viewportWidth = internalWidth;
        camera.viewportHeight = internalHeight;
        camera.near = NEAR;
        camera.far = FAR;
    }

    /**
     * Positions the camera so a subject standing at {@code footPosition}, {@code subjectWorldHeight}
     * world units tall, renders at exactly {@code subjectPixelHeight} pixels tall at screen centre.
     */
    public void follow(Vector3 footPosition, float subjectWorldHeight, float subjectPixelHeight) {
        camera.fieldOfView = fovDegrees;

        float pitchRad = pitchDegrees * MathUtils.degreesToRadians;
        camera.direction.set(0f, -MathUtils.sin(pitchRad), -MathUtils.cos(pitchRad));
        camera.up.set(Vector3.Y);
        camera.normalizeUp();
        right.set(camera.direction).crs(camera.up).nor();

        // Anchor the distance on the billboard's vertical centre, not its feet - under
        // perspective + pitch, those two points sit at different distances from the camera.
        billboardCenter.set(footPosition).add(0f, subjectWorldHeight * 0.5f, 0f);

        halfFovRad = (camera.fieldOfView * 0.5f) * MathUtils.degreesToRadians;
        distance = (subjectWorldHeight * camera.viewportHeight)
            / (2f * subjectPixelHeight * MathUtils.tan(halfFovRad));

        camera.position.set(billboardCenter).mulAdd(camera.direction, -distance);
        camera.update();
    }

    /**
     * Nudges the projection matrix by a sub-pixel amount so static world geometry locks to the
     * low-res pixel grid instead of swimming as the camera follows a smoothly-moving subject.
     * Must run after {@link #follow}. Depth is untouched - only the projected x/y position shifts.
     *
     * <p>The camera's own follow target always re-centres exactly on screen by construction, so
     * snapping *that* point would be a no-op; instead this measures how far the camera's own
     * position has drifted off a world-space pixel grid (at the current focus distance) and
     * corrects for that drift, which affects every point in the scene uniformly.
     */
    public void snapToPixelGrid(int internalWidth, int internalHeight) {
        float worldUnitsPerPixel = (2f * distance * MathUtils.tan(halfFovRad)) / internalHeight;

        float rightComponent = camera.position.dot(right);
        float upComponent = camera.position.dot(camera.up);
        float remainderRight = rightComponent - Math.round(rightComponent / worldUnitsPerPixel) * worldUnitsPerPixel;
        float remainderUp = upComponent - Math.round(upComponent / worldUnitsPerPixel) * worldUnitsPerPixel;

        float deltaNdcX = (remainderRight / worldUnitsPerPixel) * (2f / internalWidth);
        float deltaNdcY = (remainderUp / worldUnitsPerPixel) * (2f / internalHeight);

        // M02/M12 add a z-proportional term to clip.x/y; since clip.w also derives from z,
        // this produces a constant NDC-space shift regardless of depth - a plain translation
        // would instead vanish after the perspective divide. Subtracting cancels the drift
        // (NDC.x works out to a*viewX/w - M02, so a positive M02 shifts the image left).
        Matrix4 projection = camera.projection;
        projection.val[Matrix4.M02] -= deltaNdcX;
        projection.val[Matrix4.M12] -= deltaNdcY;
        camera.combined.set(projection).mul(camera.view);
    }
}
