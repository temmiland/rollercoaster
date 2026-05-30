package temmiland.rollercoaster.configuration.screen;

import temmiland.rollercoaster.configuration.Configuration;

/**
 * Persisted screen/display settings for the game window.
 *
 * <p>Default values are applied when no config file exists yet. The class is serialised
 * to and deserialised from JSON by {@link temmiland.rollercoaster.configuration.JsonConfig}.
 * Setters return {@code this} to support method chaining.
 */
public class ScreenConfiguration extends Configuration {

	/** Default window width in pixels. */
	private static final int DEFAULT_WIDTH = 1280;
	/** Default window height in pixels. */
	private static final int DEFAULT_HEIGHT = 720;
	/** Default target frame rate in frames per second. */
	private static final int DEFAULT_FRAME_RATE = 60;

	/** Whether the window should run in fullscreen mode. */
	private boolean fullscreen = false;
	/** Window width from the last session, used to restore the window size on startup. */
	private int lastWidth = DEFAULT_WIDTH;
	/** Window height from the last session, used to restore the window size on startup. */
	private int lastHeight = DEFAULT_HEIGHT;
	/** Target frame rate in frames per second. */
	private int frameRate = DEFAULT_FRAME_RATE;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/** Creates a {@code ScreenConfiguration} with default values. */
	public ScreenConfiguration() {
	}

	// -------------------------------------------------------------------------
	// Fullscreen
	// -------------------------------------------------------------------------

	/**
	 * Returns whether the window should run in fullscreen mode.
	 *
	 * @return {@code true} if fullscreen is enabled
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	/**
	 * Sets whether the window should run in fullscreen mode.
	 *
	 * @param cFullscreen {@code true} to enable fullscreen
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setFullscreen(final boolean cFullscreen) {
		this.fullscreen = cFullscreen;
		return this;
	}

	// -------------------------------------------------------------------------
	// Window size
	// -------------------------------------------------------------------------

	/**
	 * Returns the window width from the last session.
	 *
	 * @return last known window width in pixels
	 */
	public int getLastWidth() {
		return lastWidth;
	}

	/**
	 * Sets the window width to persist for the next session.
	 *
	 * @param cLastWidth window width in pixels
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setLastWidth(final int cLastWidth) {
		this.lastWidth = cLastWidth;
		return this;
	}

	/**
	 * Returns the window height from the last session.
	 *
	 * @return last known window height in pixels
	 */
	public int getLastHeight() {
		return lastHeight;
	}

	/**
	 * Sets the window height to persist for the next session.
	 *
	 * @param cLastHeight window height in pixels
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setLastHeight(final int cLastHeight) {
		this.lastHeight = cLastHeight;
		return this;
	}

	// -------------------------------------------------------------------------
	// Frame rate
	// -------------------------------------------------------------------------

	/**
	 * Returns the target frame rate.
	 *
	 * @return target frame rate in frames per second
	 */
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * Sets the target frame rate.
	 *
	 * @param cFrameRate target frame rate in frames per second
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setFrameRate(final int cFrameRate) {
		this.frameRate = cFrameRate;
		return this;
	}

	// -------------------------------------------------------------------------
	// Object
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		return "ScreenConfiguration{"
				+ "fullscreen=" + fullscreen
				+ ", lastWidth=" + lastWidth
				+ ", lastHeight=" + lastHeight
				+ ", frameRate=" + frameRate + '}';
	}
}
