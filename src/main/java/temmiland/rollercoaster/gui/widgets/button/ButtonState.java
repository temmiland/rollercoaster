package temmiland.rollercoaster.gui.widgets.button;

/**
 * Visual interaction state of a button widget.
 *
 * <p>The ordinal corresponds to the column offset in the button tilesheet:
 * {@link #IDLE} is the base column; {@link #SELECTED} and {@link #CLICKED} shift by a
 * multiple of the state width.</p>
 */
public enum ButtonState {
	/** No cursor interaction — default appearance. */
	IDLE,
	/** Cursor is hovering over the button. */
	SELECTED,
	/** Mouse button is held down over the button. */
	CLICKED
}
