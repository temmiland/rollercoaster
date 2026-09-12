package land.temmi.rollercoaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import land.temmi.rollercoaster.render.LowResTarget;

public class RollercoasterGame extends ApplicationAdapter {

    private static final float TARGET_TEST_CELL = 20f;

    private LowResTarget lowRes;
    private SpriteBatch blitBatch;
    private ShapeRenderer shapes;
    private final Matrix4 testProjection = new Matrix4();

    @Override
    public void create() {
        lowRes = new LowResTarget();
        lowRes.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        blitBatch = new SpriteBatch();
        shapes = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {
        lowRes.resize(width, height);
    }

    @Override
    public void render() {
        lowRes.begin();
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        drawTestPattern();
        lowRes.end();

        lowRes.blitToScreen(blitBatch);
    }

    private void drawTestPattern() {
        int width = lowRes.getWidth();
        int height = lowRes.getHeight();

        testProjection.setToOrtho2D(0, 0, width, height);
        shapes.setProjectionMatrix(testProjection);

        // Cell size is derived from a column/row count, not a fixed pixel size, so
        // cells always tile exactly - internalWidth/Height vary with device aspect
        // and are rarely a multiple of any fixed cell size.
        int cols = Math.max(1, Math.round(width / TARGET_TEST_CELL));
        int rows = Math.max(1, Math.round(height / TARGET_TEST_CELL));
        float cellW = width / (float) cols;
        float cellH = height / (float) rows;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                boolean even = (col + row) % 2 == 0;
                shapes.setColor(even ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                shapes.rect(col * cellW, row * cellH, cellW, cellH);
            }
        }
        // corner markers catch an accidental vertical/horizontal flip
        shapes.setColor(Color.CYAN);
        shapes.rect(0, height - cellH, cellW, cellH);
        shapes.setColor(Color.MAGENTA);
        shapes.rect(width - cellW, 0, cellW, cellH);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.RED);
        shapes.rect(0.5f, 0.5f, width - 1, height - 1);
        shapes.end();
    }

    @Override
    public void dispose() {
        lowRes.dispose();
        blitBatch.dispose();
        shapes.dispose();
    }
}
