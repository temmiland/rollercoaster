package temmiland.rollercoaster.config.screen;

import temmiland.rollercoaster.config.Configuration;

/**
 * Persisted screen/display settings for the game window.
 *
 * <p>Default values are applied when no config file exists yet. The class is serialised
 * to and deserialised from JSON by {@link temmiland.rollercoaster.config.JsonConfig}.
 * Setters return {@code this} to support method chaining.
 */
public class ScreenConfiguration extends Configuration {

	/** Default window width in pixels. */
	private static final int DEFAULT_WIDTH = 1280;
	/** Default window height in pixels. */
	private static final int DEFAULT_HEIGHT = 720;
	/** Default target frame rate in frames per second. */
	private static final int DEFAULT_FRAME_RATE = 60;
	/** Whether VSync is enabled by default. */
	private static final boolean DEFAULT_VSYNC = true;

	// Initial values for the configuration fields. These will be overridden by values from the config file if it exists.
	/** Whether the window should run in fullscreen mode. */
	private boolean fullscreen = false;
	/** Window width from the last session, used to restore the window size on startup. */
	private int lastWidth = DEFAULT_WIDTH;
	/** Window height from the last session, used to restore the window size on startup. */
	private int lastHeight = DEFAULT_HEIGHT;
	/** Target frame rate in frames per second. Only applied when VSync is disabled. */
	private int frameRate = DEFAULT_FRAME_RATE;
	/** Whether hardware VSync paces the render loop to the monitor refresh rate. */
	private boolean vsync = DEFAULT_VSYNC;

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
	 * @param fullscreen {@code true} to enable fullscreen
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setFullscreen(final boolean fullscreen) {
		this.fullscreen = fullscreen;
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
	 * @param lastWidth window width in pixels
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setLastWidth(final int lastWidth) {
		this.lastWidth = lastWidth;
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
	 * @param lastHeight window height in pixels
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setLastHeight(final int lastHeight) {
		this.lastHeight = lastHeight;
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
	 * @param frameRate target frame rate in frames per second
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setFrameRate(final int frameRate) {
		this.frameRate = frameRate;
		return this;
	}

	// -------------------------------------------------------------------------
	// VSync
	// -------------------------------------------------------------------------

	/**
	 * Returns whether hardware VSync is enabled.
	 *
	 * @return {@code true} if VSync paces the render loop to the monitor refresh rate
	 */
	public boolean isVsync() {
		return vsync;
	}

	/**
	 * Sets whether hardware VSync is enabled.
	 *
	 * @param vsync {@code true} to pace rendering to the monitor refresh rate
	 * @return {@code this} for method chaining
	 */
	public ScreenConfiguration setVsync(final boolean vsync) {
		this.vsync = vsync;
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
				+ ", frameRate=" + frameRate
				+ ", vsync=" + vsync + '}';
	}
}
