package land.temmi.rollercoaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.render.LowResTarget;
import land.temmi.rollercoaster.render.PixelCamera;
import land.temmi.rollercoaster.render.WorldShaderProvider;
import land.temmi.rollercoaster.render.BillboardRenderer;
import land.temmi.rollercoaster.world.TestMap;
import com.badlogic.gdx.utils.Array;

public class RollercoasterGame extends ApplicationAdapter {

    private static final float SUBJECT_WORLD_HEIGHT = 1.8f;
    private static final float SUBJECT_PIXEL_HEIGHT = 48f;

    private LowResTarget lowRes;
    private PixelCamera pixelCamera;
    private SpriteBatch blitBatch;
    private ShapeRenderer shapes;
    private final Matrix4 overlayProjection = new Matrix4();

    private ModelBatch modelBatch;
    private Array<Model> chunks;
    private final Array<ModelInstance> world = new Array<>();
    private Texture spriteTexture;
    private BillboardRenderer playerSprite;
    private final Vector3 subjectFootPosition = new Vector3(0f, 0f, 0f);

    @Override
    public void create() {
        lowRes = new LowResTarget();
        lowRes.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        pixelCamera = new PixelCamera();
        pixelCamera.resize(lowRes.getWidth(), lowRes.getHeight());

        blitBatch = new SpriteBatch();
        shapes = new ShapeRenderer();

        modelBatch = new ModelBatch(new WorldShaderProvider());
        chunks = TestMap.create();
        for (Model chunk : chunks) world.add(new ModelInstance(chunk));

        Pixmap sprite = new Pixmap(16, 24, Pixmap.Format.RGBA8888);
        sprite.setColor(0f, 0f, 0f, 0f);
        sprite.fill();
        sprite.setColor(0.95f, 0.55f, 0.15f, 1f);
        sprite.fillRectangle(5, 15, 6, 7);
        sprite.setColor(0.20f, 0.42f, 0.85f, 1f);
        sprite.fillRectangle(4, 8, 8, 7);
        sprite.setColor(0.15f, 0.20f, 0.32f, 1f);
        sprite.fillRectangle(4, 3, 3, 5);
        sprite.fillRectangle(9, 3, 3, 5);
        spriteTexture = new Texture(sprite);
        sprite.dispose();
        spriteTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        playerSprite = new BillboardRenderer(spriteTexture, new TextureRegion(spriteTexture), SUBJECT_WORLD_HEIGHT);
    }

    @Override
    public void resize(int width, int height) {
        lowRes.resize(width, height);
        pixelCamera.resize(lowRes.getWidth(), lowRes.getHeight());
    }

    private float driftTime;

    @Override
    public void render() {
        // Bounded drift keeps the map visible while exercising camera snapping.
        driftTime += Gdx.graphics.getDeltaTime();
        subjectFootPosition.set(12f + (float) Math.sin(driftTime * 0.15f) * 2f, 0f, 14f);

        pixelCamera.follow(subjectFootPosition, SUBJECT_WORLD_HEIGHT, SUBJECT_PIXEL_HEIGHT);
        pixelCamera.snapToPixelGrid(lowRes.getWidth(), lowRes.getHeight());

        playerSprite.setPosition(subjectFootPosition.x, SUBJECT_WORLD_HEIGHT * 0.5f, subjectFootPosition.z);

        lowRes.begin();
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(pixelCamera.camera);
        modelBatch.render(world);
        modelBatch.render(playerSprite);
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
        playerSprite.dispose();
        spriteTexture.dispose();
        for (Model chunk : chunks) chunk.dispose();
    }
}
