package temmiland.rollercoaster.gui;

import org.joml.Vector2f;

import java.util.List;

/**
 * Input backend for the immediate-mode GUI of the Rollercoaster engine.
 *
 * <p>Abstracts the game-side input processing so that the GUI framework has no dependency
 * on the concrete input implementation. A game implements this interface once (as an adapter
 * over its input processor) and passes it to {@link GuiContext#interact} and
 * {@link GuiContext#render} each frame.</p>
 *
 * <p>The tracked mouse button is the left button. "Pressed" is <b>edge-triggered</b>
 * (only {@code true} in the first tick the button is down); "down" reflects the held state.</p>
 */
public interface GuiInput {

	/**
	 * Returns the current mouse position in GUI coordinates (origin at window centre,
	 * Y-axis pointing up), already scaled to the virtual GUI space.
	 *
	 * @return mouse position in GUI space
	 */
	Vector2f mousePosition();

	/**
	 * Returns {@code true} only in the tick the left mouse button was first pressed.
	 *
	 * @return {@code true} on the leading edge of a left-click
	 */
	boolean mousePressed();

	/**
	 * Returns {@code true} for as long as the left mouse button is held down.
	 *
	 * @return {@code true} while the left mouse button is held
	 */
	boolean mouseDown();

	/**
	 * Returns the Unicode code points typed this tick (for text input).
	 *
	 * @return list of typed code points; empty if none
	 */
	List<Integer> typedCodepoints();

	/**
	 * Returns {@code true} only in the tick Backspace was first pressed.
	 *
	 * @return {@code true} on the leading edge of a Backspace press
	 */
	boolean backspacePressed();

	/**
	 * Returns {@code true} only in the tick the confirm key (Enter) was first pressed.
	 *
	 * @return {@code true} on the leading edge of an Enter press
	 */
	boolean confirmPressed();
}
