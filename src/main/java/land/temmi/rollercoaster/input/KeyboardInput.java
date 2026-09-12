package land.temmi.rollercoaster.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/** Keyboard adapter; gameplay consumes only the intent enum. */
public final class KeyboardInput implements InputSource {
    @Override public MoveIntent pollMove() {
        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) return MoveIntent.UP;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) return MoveIntent.RIGHT;
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) return MoveIntent.DOWN;
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) return MoveIntent.LEFT;
        return MoveIntent.NONE;
    }
}
