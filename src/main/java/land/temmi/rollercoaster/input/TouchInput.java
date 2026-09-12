package land.temmi.rollercoaster.input;

import com.badlogic.gdx.Gdx;

/** Reads a normalized virtual D-pad from the lower-left touch area. */
public final class TouchInput implements InputSource {
    private float centerX = 0.18f;
    private float centerY = 0.20f;
    private float radius = 0.16f;
    private float deadZone = 0.30f;

    public TouchInput setDpad(float centerX, float centerY, float radius) {
        if (centerX < 0f || centerX > 1f || centerY < 0f || centerY > 1f || radius <= 0f || radius > 1f) {
            throw new IllegalArgumentException("Invalid virtual D-pad bounds");
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        return this;
    }

    public TouchInput setDeadZone(float deadZone) {
        if (deadZone < 0f || deadZone >= 1f) throw new IllegalArgumentException("Invalid touch dead zone");
        this.deadZone = deadZone;
        return this;
    }

    @Override
    public MoveIntent pollMove() {
        if (!Gdx.input.isTouched()) return MoveIntent.NONE;
        float width = Math.max(1f, Gdx.graphics.getWidth());
        float height = Math.max(1f, Gdx.graphics.getHeight());
        float x = Gdx.input.getX() / width;
        float y = 1f - Gdx.input.getY() / height;
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > radius || distance < radius * deadZone) return MoveIntent.NONE;
        if (Math.abs(dx) >= Math.abs(dy)) return dx < 0f ? MoveIntent.LEFT : MoveIntent.RIGHT;
        return dy < 0f ? MoveIntent.DOWN : MoveIntent.UP;
    }
}
