package land.temmi.rollercoaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.render.LowResTarget;
import land.temmi.rollercoaster.render.PixelCamera;

public class RollercoasterGame extends ApplicationAdapter {

    // Stand-in for a character sprite: a flat upright box of known world height,
    // used to verify PixelCamera's distance formula. Real billboards come later.
    private static final float SUBJECT_WORLD_HEIGHT = 1.8f;
    private static final float SUBJECT_PIXEL_HEIGHT = 48f;

    private LowResTarget lowRes;
    private PixelCamera pixelCamera;
    private SpriteBatch blitBatch;
    private ShapeRenderer shapes;
    private final Matrix4 overlayProjection = new Matrix4();

    private ModelBatch modelBatch;
    private Environment environment;
    private Model subjectModel;
    private ModelInstance subjectInstance;
    private final Vector3 subjectFootPosition = new Vector3(0f, 0f, 0f);
    private final Vector3 billboardCenter = new Vector3();
    private final Vector3 billboardForward = new Vector3();
    private final Vector3 billboardUp = new Vector3();
    private final Vector3 billboardRight = new Vector3();

    @Override
    public void create() {
        lowRes = new LowResTarget();
        lowRes.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        pixelCamera = new PixelCamera();
        pixelCamera.resize(lowRes.getWidth(), lowRes.getHeight());

        blitBatch = new SpriteBatch();
        shapes = new ShapeRenderer();

        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(ColorAttribute.createAmbientLight(1f, 1f, 1f, 1f));

        ModelBuilder builder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(Color.ORANGE));
        subjectModel = builder.createBox(0.6f, SUBJECT_WORLD_HEIGHT, 0.1f, material,
            Usage.Position | Usage.Normal);
        subjectInstance = new ModelInstance(subjectModel);
    }

    @Override
    public void resize(int width, int height) {
        lowRes.resize(width, height);
        pixelCamera.resize(lowRes.getWidth(), lowRes.getHeight());
    }

    private float driftTime;

    @Override
    public void render() {
        // Slow drift so the camera sits at arbitrary sub-pixel offsets, which is what
        // pixel-snapping has to absorb. Replaced by real input in a later step.
        driftTime += Gdx.graphics.getDeltaTime();
        subjectFootPosition.x = driftTime * 0.05f;

        pixelCamera.follow(subjectFootPosition, SUBJECT_WORLD_HEIGHT, SUBJECT_PIXEL_HEIGHT);
        pixelCamera.snapToPixelGrid(lowRes.getWidth(), lowRes.getHeight());

        // Spherical billboard alignment: the box's local axes now match the camera's
        // right/up/forward exactly, so its world-Y height no longer foreshortens under
        // the camera's pitch - this is what the distance formula in PixelCamera assumes.
        billboardCenter.set(subjectFootPosition).add(0f, SUBJECT_WORLD_HEIGHT * 0.5f, 0f);
        billboardForward.set(pixelCamera.camera.direction).scl(-1f).nor();
        billboardUp.set(pixelCamera.camera.up).nor();
        billboardRight.set(billboardUp).crs(billboardForward).nor();

        float[] t = subjectInstance.transform.val;
        t[Matrix4.M00] = billboardRight.x; t[Matrix4.M10] = billboardRight.y; t[Matrix4.M20] = billboardRight.z; t[Matrix4.M30] = 0f;
        t[Matrix4.M01] = billboardUp.x; t[Matrix4.M11] = billboardUp.y; t[Matrix4.M21] = billboardUp.z; t[Matrix4.M31] = 0f;
        t[Matrix4.M02] = billboardForward.x; t[Matrix4.M12] = billboardForward.y; t[Matrix4.M22] = billboardForward.z; t[Matrix4.M32] = 0f;
        t[Matrix4.M03] = billboardCenter.x; t[Matrix4.M13] = billboardCenter.y; t[Matrix4.M23] = billboardCenter.z; t[Matrix4.M33] = 1f;

        lowRes.begin();
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(pixelCamera.camera);
        modelBatch.render(subjectInstance, environment);
        modelBatch.end();

        drawPixelRuler();
        lowRes.end();

        lowRes.blitToScreen(blitBatch);
    }

    // Horizontal ticks every 10px (brighter every 50px), so a screenshot's rendered
    // subject height can be measured against SUBJECT_PIXEL_HEIGHT.
    private void drawPixelRuler() {
        int width = lowRes.getWidth();
        int height = lowRes.getHeight();

        overlayProjection.setToOrtho2D(0, 0, width, height);
        shapes.setProjectionMatrix(overlayProjection);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int y = 0; y <= height; y += 10) {
            shapes.setColor(y % 50 == 0 ? Color.WHITE : Color.GRAY);
            shapes.line(0, y, 6, y);
        }
        shapes.end();
    }

    @Override
    public void dispose() {
        lowRes.dispose();
        blitBatch.dispose();
        shapes.dispose();
        modelBatch.dispose();
        subjectModel.dispose();
    }
}
