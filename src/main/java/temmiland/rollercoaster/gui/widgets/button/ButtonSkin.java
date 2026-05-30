package temmiland.rollercoaster.gui.widgets.button;

import temmiland.rollercoaster.gui.GuiContext;
import temmiland.rollercoaster.gui.GuiPrimitives;

/**
 * Contract for the game-side visual appearance of a button.
 *
 * <p>The engine framework provides only geometry (centre point + half-extents), a label,
 * and the semantic {@link ButtonState} (IDLE / SELECTED / CLICKED). How a button looks —
 * shape, tiles, colours, label style — is determined by the implementation, which must use
 * only the {@link GuiPrimitives} API.</p>
 *
 * <p>Implementations are stateless and are supplied once when the {@link GuiContext} is built.
 * All coordinates are in GUI space (origin at window centre, Y-axis pointing up).</p>
 */
public interface ButtonSkin {

	/**
	 * Draws a button centred on {@code (cx, cy)}.
	 *
	 * @param primitives the allowed draw operations
	 * @param label      button label; may be {@code null} or empty
	 * @param cx         centre X in GUI coordinates
	 * @param cy         centre Y in GUI coordinates
	 * @param halfWidth  half the button width
	 * @param halfHeight half the button height
	 * @param state      current interaction state
	 */
	void draw(GuiPrimitives primitives, String label, float cx, float cy,
	          float halfWidth, float halfHeight, ButtonState state);
}
