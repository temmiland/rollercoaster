package land.temmi.rollercoaster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import land.temmi.rollercoaster.render.LowResTarget;
import land.temmi.rollercoaster.render.ScalePolicy;

public class RollercoasterGame extends ApplicationAdapter {

    private static final int TEST_CELL = 20;

    private LowResTarget lowRes;
    private SpriteBatch blitBatch;
    private ShapeRenderer shapes;
    private final Matrix4 testProjection = new Matrix4();

    @Override
    public void create() {
        lowRes = new LowResTarget(ScalePolicy.FLEX_WIDTH);
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

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int y = 0; y < height; y += TEST_CELL) {
            for (int x = 0; x < width; x += TEST_CELL) {
                boolean even = ((x / TEST_CELL) + (y / TEST_CELL)) % 2 == 0;
                shapes.setColor(even ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                shapes.rect(x, y, TEST_CELL, TEST_CELL);
            }
        }
        // corner markers catch an accidental vertical/horizontal flip
        shapes.setColor(Color.CYAN);
        shapes.rect(0, height - TEST_CELL, TEST_CELL, TEST_CELL);
        shapes.setColor(Color.MAGENTA);
        shapes.rect(width - TEST_CELL, 0, TEST_CELL, TEST_CELL);
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
