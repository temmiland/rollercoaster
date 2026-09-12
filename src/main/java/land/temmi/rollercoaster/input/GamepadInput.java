package land.temmi.rollercoaster.input;

/** Converts a platform gamepad's left stick into grid movement intents. */
public final class GamepadInput implements InputSource {
    public interface State {
        float horizontal();
        float vertical();
    }

    private final State state;
    private final float deadZone;

    public GamepadInput(State state) {
        this(state, 0.35f);
    }

    public GamepadInput(State state, float deadZone) {
        if (state == null || deadZone < 0f || deadZone >= 1f) throw new IllegalArgumentException("Invalid gamepad input");
        this.state = state;
        this.deadZone = deadZone;
    }

    @Override
    public MoveIntent pollMove() {
        float horizontal = state.horizontal();
        float vertical = state.vertical();
        if (Math.abs(horizontal) < deadZone && Math.abs(vertical) < deadZone) return MoveIntent.NONE;
        if (Math.abs(horizontal) >= Math.abs(vertical)) return horizontal < 0f ? MoveIntent.LEFT : MoveIntent.RIGHT;
        return vertical < 0f ? MoveIntent.UP : MoveIntent.DOWN;
    }
}
