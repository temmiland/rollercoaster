package land.temmi.rollercoaster.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws {@link TouchInput}'s virtual D-pad zone directly onto the screen (not the low-res pixel
 * target, since TouchInput itself reads raw {@code Gdx.graphics} screen coordinates), so a mobile
 * player can actually see where to touch instead of guessing at an invisible zone.
 */
public final class TouchpadRenderer implements Disposable {
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final Matrix4 projection = new Matrix4();
    private final TouchInput input;

    public TouchpadRenderer(TouchInput input) {
        if (input == null) throw new IllegalArgumentException("Touch input is required");
        this.input = input;
    }

    /** Call after the game world has been drawn, with no other batch/renderer active. */
    public void render() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        projection.setToOrtho2D(0, 0, width, height);
        shapes.setProjectionMatrix(projection);

        float centerX = input.getCenterX() * width;
        float centerY = input.getCenterY() * height;
        float radius = input.getRadius() * Math.min(width, height);
        float deadZoneRadius = radius * input.getDeadZone();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 1f, 1f, 0.12f);
        shapes.circle(centerX, centerY, radius, 32);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(1f, 1f, 1f, 0.5f);
        shapes.circle(centerX, centerY, radius, 32);
        shapes.setColor(1f, 1f, 1f, 0.3f);
        shapes.circle(centerX, centerY, deadZoneRadius, 24);
        shapes.end();

        float knobX = centerX;
        float knobY = centerY;
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();
            float touchY = height - Gdx.input.getY();
            float dx = touchX - centerX;
            float dy = touchY - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > radius && distance > 0f) {
                float scale = radius / distance;
                dx *= scale;
                dy *= scale;
            }
            knobX = centerX + dx;
            knobY = centerY + dy;
        }
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 1f, 1f, 0.55f);
        shapes.circle(knobX, knobY, radius * 0.35f, 24);
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
