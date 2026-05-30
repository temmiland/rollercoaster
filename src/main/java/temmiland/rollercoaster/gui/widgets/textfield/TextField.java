package temmiland.rollercoaster.gui.widgets.textfield;

import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.gui.GuiInput;
import temmiland.rollercoaster.gui.GuiPrimitives;

/**
 * Stateful text-input widget for the immediate-mode GUI.
 *
 * <p>{@code TextField} encapsulates only <b>behaviour and state</b> (content, input
 * processing, cursor blink timing) — the visual appearance (border, colours, font,
 * cursor rendering) is delegated to a game-supplied {@link TextFieldSkin}. The widget
 * retains its content across frames and is therefore owned by the calling state, which
 * passes it to {@link GuiContext#textField(TextField)} each frame.</p>
 *
 * <p>Characters arrive as Unicode code points from {@link GuiInput#typedCodepoints()};
 * only {@code [A-Za-z0-9_-]} is accepted. Backspace deletes the last character;
 * the confirm key signals completion.</p>
 */
public final class TextField {

	/** Maximum number of characters allowed. */
	public static final int MAX_LENGTH = 18;

	/** Cursor blink interval in seconds. */
	private static final float BLINK_INTERVAL = 0.5f;

	/** Centre X in GUI coordinates. */
	private final float cx;
	/** Centre Y in GUI coordinates. */
	private final float cy;
	/** Half-width. */
	private final float hw;
	/** Half-height. */
	private final float hh;

	/** Current input text. */
	private final StringBuilder value = new StringBuilder();
	/** Accumulated time for cursor blinking. */
	private float blinkTimer;
	/** Whether the cursor is currently visible. */
	private boolean cursorVisible = true;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code TextField}.
	 *
	 * @param cCx centre X in GUI coordinates
	 * @param cCy centre Y in GUI coordinates
	 * @param cHw half-width
	 * @param cHh half-height
	 */
	public TextField(final float cCx, final float cCy, final float cHw, final float cHh) {
		this.cx = cCx;
		this.cy = cCy;
		this.hw = cHw;
		this.hh = cHh;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Processes input for the current tick and advances the cursor blink timer.
	 *
	 * @param input the input backend
	 * @param delta tick delta in seconds (used for cursor blink timing)
	 * @return {@code true} if the confirm key was pressed this tick
	 */
	public boolean update(final GuiInput input, final double delta) {
		blinkTimer += (float) delta;
		if (blinkTimer >= BLINK_INTERVAL) {
			blinkTimer -= BLINK_INTERVAL;
			cursorVisible = !cursorVisible;
		}

		for (final int cp : input.typedCodepoints()) {
			if (value.length() >= MAX_LENGTH) {
				break;
			}
			if (isAllowed(cp)) {
				value.appendCodePoint(cp);
				resetCursor();
			}
		}

		if (input.backspacePressed() && value.length() > 0) {
			value.deleteCharAt(value.length() - 1);
			resetCursor();
		}

		return input.confirmPressed();
	}

	/**
	 * Passes the current content and cursor state to the skin for rendering.
	 *
	 * @param primitives the allowed draw operations
	 * @param skin       the game-side visual style
	 */
	public void render(final GuiPrimitives primitives, final TextFieldSkin skin) {
		skin.draw(primitives, value.toString(), cx, cy, hw, hh, cursorVisible);
	}

	/**
	 * Returns the current input text.
	 *
	 * @return current text content
	 */
	public String getValue() {
		return value.toString();
	}

	/** Clears the content and resets the cursor. */
	public void clear() {
		value.setLength(0);
		resetCursor();
	}

	/**
	 * Replaces the content with the given text, filtering out disallowed characters
	 * and truncating to {@link #MAX_LENGTH}.
	 *
	 * @param text the new content
	 */
	public void setValue(final String text) {
		value.setLength(0);
		text.codePoints()
				.filter(TextField::isAllowed)
				.limit(MAX_LENGTH)
				.forEach(value::appendCodePoint);
		resetCursor();
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	private void resetCursor() {
		blinkTimer    = 0f;
		cursorVisible = true;
	}

	private static boolean isAllowed(final int cp) {
		return (cp >= 'a' && cp <= 'z')
				|| (cp >= 'A' && cp <= 'Z')
				|| (cp >= '0' && cp <= '9')
				|| cp == '_'
				|| cp == '-';
	}
}
