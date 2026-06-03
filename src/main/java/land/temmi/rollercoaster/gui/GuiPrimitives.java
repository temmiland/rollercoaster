package land.temmi.rollercoaster.gui;

import land.temmi.rollercoaster.gui.widgets.button.ButtonSkin;
import land.temmi.rollercoaster.gui.widgets.textfield.TextFieldSkin;

/**
 * Lowest drawing layer of the immediate-mode GUI: individual, style-free render operations.
 *
 * <p>Skins ({@link ButtonSkin}, {@link TextFieldSkin}) compose widget appearances exclusively
 * from these primitives — they deliberately cannot see the lifecycle methods of a
 * {@link GuiRenderer} ({@code begin} / {@code resize}).</p>
 *
 * <p>All coordinates are in <b>GUI space</b>: origin at the window centre, X pointing right,
 * Y pointing up. Sizes are given as half-extents (half-width / half-height).
 * Implementations are responsible for converting to their own render space and for
 * centring text around the given midpoint.</p>
 */
public interface GuiPrimitives {

	/**
	 * Draws a single tile by texture name, centred on {@code (cx, cy)}.
	 *
	 * @param textureName the registered texture name (as in {@code Tile.getTexture()})
	 * @param cx          centre X in GUI coordinates
	 * @param cy          centre Y in GUI coordinates
	 * @param halfWidth   half the tile width
	 * @param halfHeight  half the tile height
	 */
	void drawTile(String textureName, float cx, float cy, float halfWidth, float halfHeight);

	/**
	 * Draws text horizontally and vertically centred on {@code (cx, cy)}.
	 *
	 * @param text   the text to draw
	 * @param cx     centre X in GUI coordinates
	 * @param cy     centre Y in GUI coordinates
	 * @param scale  font scale factor
	 * @param r      red channel 0..1
	 * @param g      green channel 0..1
	 * @param b      blue channel 0..1
	 * @param shadow {@code true} to draw a dark drop shadow
	 */
	void drawText(String text, float cx, float cy, float scale,
	              float r, float g, float b, boolean shadow);

	/**
	 * Draws a solid-colour rectangle centred on {@code (cx, cy)}.
	 *
	 * @param cx         centre X in GUI coordinates
	 * @param cy         centre Y in GUI coordinates
	 * @param halfWidth  half the rectangle width
	 * @param halfHeight half the rectangle height
	 * @param r          red channel 0..1
	 * @param g          green channel 0..1
	 * @param b          blue channel 0..1
	 */
	void drawQuad(float cx, float cy, float halfWidth, float halfHeight,
	              float r, float g, float b);
}
