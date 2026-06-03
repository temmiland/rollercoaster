package land.temmi.rollercoaster.gui.widgets.textfield;

import land.temmi.rollercoaster.gui.GuiContext;
import land.temmi.rollercoaster.gui.GuiPrimitives;

/**
 * Contract for the game-side visual appearance of a text input field.
 *
 * <p>The engine framework provides only geometry, the current content, and the cursor
 * visibility of the current blink phase. How the field looks — border, background,
 * font style, cursor rendering — is determined by the implementation, which must use
 * only the {@link GuiPrimitives} API.</p>
 *
 * <p>Implementations are stateless and are supplied once when the {@link GuiContext} is built.
 * All coordinates are in GUI space (origin at window centre, Y-axis pointing up).</p>
 */
public interface TextFieldSkin {

	/**
	 * Draws a text input field centred on {@code (cx, cy)}.
	 *
	 * @param primitives    the allowed draw operations
	 * @param text          current field content
	 * @param cx            centre X in GUI coordinates
	 * @param cy            centre Y in GUI coordinates
	 * @param halfWidth     half the field width
	 * @param halfHeight    half the field height
	 * @param cursorVisible {@code true} if the cursor should be shown in this blink phase
	 */
	void draw(GuiPrimitives primitives, String text, float cx, float cy,
	          float halfWidth, float halfHeight, boolean cursorVisible);
}
