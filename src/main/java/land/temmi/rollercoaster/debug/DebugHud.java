package land.temmi.rollercoaster.debug;

import land.temmi.rollercoaster.game.GameLoop;
import land.temmi.rollercoaster.platform.Window;

/**
 * Contract for an in-game debug HUD overlay.
 *
 * <p>Implementations are driven by {@link GameLoop} each
 * rendered frame when the debug overlay is active.</p>
 */
public interface DebugHud {

	/**
	 * Renders the debug overlay for the current frame.
	 *
	 * @param window     the GLFW window (used for dimensions)
	 * @param alpha      interpolation factor between the last two logic ticks (0..1)
	 * @param debugLines lines of text to display; may be empty but never {@code null}
	 */
	void render(Window window, float alpha, String[] debugLines);

	/**
	 * Updates the displayed FPS counter.
	 * Called once per second by the game loop.
	 *
	 * @param fps the number of frames rendered in the last second
	 */
	void updateFps(int fps);

	/**
	 * Returns whether the detailed debug view is currently shown.
	 *
	 * @return {@code true} if the detailed overlay is visible
	 */
	boolean isShowDetailed();

	/**
	 * Toggles the detailed debug view on or off.
	 */
	void toggleDetailed();
}
