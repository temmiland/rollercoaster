package temmiland.rollercoaster.gui;

import temmiland.rollercoaster.gui.widgets.button.ButtonSkin;
import temmiland.rollercoaster.gui.widgets.textfield.TextFieldSkin;

/**
 * Render backend for the immediate-mode GUI: {@link GuiPrimitives} plus lifecycle hooks.
 *
 * <p>The GUI framework is render-API-agnostic: it declares widgets via {@link GuiContext}
 * and delegates drawing to a game-side implementation of this interface and to the skins
 * ({@link ButtonSkin}, {@link TextFieldSkin}). A game implements {@code GuiRenderer} once
 * (e.g. on top of its OpenGL / shader / tilesheet infrastructure).</p>
 */
public interface GuiRenderer extends GuiPrimitives {

	/**
	 * Called once at the start of each render pass, before any widget draw calls.
	 * Use this to bind the GUI shader, reset batch state, etc.
	 */
	void begin();

	/**
	 * Called when the window is resized (e.g. to recompute the GUI projection matrix).
	 *
	 * @param width  new window width in pixels
	 * @param height new window height in pixels
	 */
	void resize(int width, int height);
}
