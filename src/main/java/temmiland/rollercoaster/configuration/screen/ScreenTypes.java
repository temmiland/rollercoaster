package temmiland.rollercoaster.configuration.screen;

/**
 * Enumeration of supported window display modes.
 */
public enum ScreenTypes {

	/** Normal windowed mode. */
	WINDOWED("Normal windowed mode", 0),
	/** Borderless windowed fullscreen mode. */
	WINDOWED_FULLSCREEN("Borderless windowed fullscreen mode", 1),
	/** Exclusive fullscreen mode. */
	FULLSCREEN("Fullscreen mode", 2);

	/** Human-readable label for this display mode. */
	private final String displayName;
	/** Integer representation used for serialisation. */
	private final int screenType;

	ScreenTypes(final String displayName, final int screenType) {
		this.displayName = displayName;
		this.screenType  = screenType;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	/**
	 * Returns the human-readable label for this display mode.
	 *
	 * @return display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the integer representation of this display mode.
	 *
	 * @return integer code
	 */
	public int getScreenType() {
		return screenType;
	}

}
