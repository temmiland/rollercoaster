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
    private final Vector3 tmpProjected = new Vector3();

    private float fovDegrees = 30f;
    private float pitchDegrees = 45f;

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

        // Anchor the distance on the billboard's vertical centre, not its feet - under
        // perspective + pitch, those two points sit at different distances from the camera.
        billboardCenter.set(footPosition).add(0f, subjectWorldHeight * 0.5f, 0f);

        float halfFovRad = (camera.fieldOfView * 0.5f) * MathUtils.degreesToRadians;
        float distance = (subjectWorldHeight * camera.viewportHeight)
            / (2f * subjectPixelHeight * MathUtils.tan(halfFovRad));

        camera.position.set(billboardCenter).mulAdd(camera.direction, -distance);
        camera.update();
    }

    /**
     * Nudges the projection matrix by a sub-pixel amount so the rendered image locks to the
     * low-res pixel grid instead of swimming as the camera moves smoothly. Must run after
     * {@link #follow}. Depth is untouched - only the projected x/y position shifts.
     */
    public void snapToPixelGrid(int internalWidth, int internalHeight) {
        tmpProjected.set(billboardCenter).prj(camera.combined);
        float pixelX = (tmpProjected.x * 0.5f + 0.5f) * internalWidth;
        float pixelY = (tmpProjected.y * 0.5f + 0.5f) * internalHeight;
        float deltaNdcX = (Math.round(pixelX) - pixelX) / internalWidth * 2f;
        float deltaNdcY = (Math.round(pixelY) - pixelY) / internalHeight * 2f;

        // M02/M12 add a z-proportional term to clip.x/y; since clip.w also derives from z,
        // this produces a constant NDC-space shift regardless of depth - a plain translation
        // would instead vanish after the perspective divide.
        Matrix4 projection = camera.projection;
        projection.val[Matrix4.M02] -= deltaNdcX;
        projection.val[Matrix4.M12] -= deltaNdcY;
        camera.combined.set(projection).mul(camera.view);
    }
}
