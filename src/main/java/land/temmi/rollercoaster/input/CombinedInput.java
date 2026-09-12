package land.temmi.rollercoaster.input;

/** Composes keyboard, gamepad, touch, or other intent sources by priority. */
public final class CombinedInput implements InputSource {
    private final InputSource[] sources;

    public CombinedInput(InputSource... sources) {
        if (sources == null || sources.length == 0) throw new IllegalArgumentException("At least one input source is required");
        this.sources = sources.clone();
        for (InputSource source : this.sources) if (source == null) throw new IllegalArgumentException("Input source is required");
    }

    @Override
    public MoveIntent pollMove() {
        for (InputSource source : sources) {
            MoveIntent intent = source.pollMove();
            if (intent != null && intent != MoveIntent.NONE) return intent;
        }
        return MoveIntent.NONE;
    }
}
