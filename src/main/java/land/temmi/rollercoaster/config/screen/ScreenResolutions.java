package land.temmi.rollercoaster.config.screen;

/**
 * Enumeration of supported screen resolutions, each carrying a display name, pixel dimensions,
 * and a UI scale factor.
 *
 * <p>Scale factors are height-based relative to 1920×1080 (reference = {@code 1.0}).
 * Larger resolutions have smaller scales so UI elements stay a consistent physical size;
 * smaller resolutions have larger scales so UI elements remain readable.</p>
 */
public enum ScreenResolutions {

	// --- 5K ---
	R_5120_2880("5120x2880", 5120, 2880, 0.5),

	// --- 4K ---
	R_4096_2160("4096x2160", 4096, 2160, 0.5),
	R_3840_2160("3840x2160", 3840, 2160, 0.5),

	// --- UltraWide QHD ---
	R_3440_1440("3440x1440", 3440, 1440, 0.75),

	// --- QHD / 1440p ---
	R_2880_1800("2880x1800", 2880, 1800, 0.75),
	R_2560_1600("2560x1600", 2560, 1600, 0.75),
	R_2560_1440("2560x1440", 2560, 1440, 0.75),

	// --- UltraWide FHD ---
	R_2560_1080("2560x1080", 2560, 1080, 1.0),

	// --- Misc high-res ---
	R_2048_1536("2048x1536", 2048, 1536, 0.75),
	R_1920_1440("1920x1440", 1920, 1440, 0.75),
	R_1920_1200("1920x1200", 1920, 1200, 1.0),

	// --- FHD (reference: scale = 1.0) ---
	R_1920_1080("1920x1080", 1920, 1080, 1.0),

	// --- WSXGA+ / UXGA ---
	R_1680_1050("1680x1050", 1680, 1050, 1.0),
	R_1600_1200("1600x1200", 1600, 1200, 1.0),
	R_1600_1024("1600x1024", 1600, 1024, 1.0),
	R_1600_900("1600x900",   1600,  900, 1.25),
	R_1440_900("1440x900",   1440,  900, 1.25),

	// --- HD / WXGA ---
	R_1366_768("1366x768",   1366,  768, 1.5),
	R_1280_1024("1280x1024", 1280, 1024, 1.0),
	R_1280_800("1280x800",   1280,  800, 1.25),
	R_1280_768("1280x768",   1280,  768, 1.5),
	R_1280_720("1280x720",   1280,  720, 1.5),
	R_1152_864("1152x864",   1152,  864, 1.25),

	// --- Low res ---
	R_1024_768("1024x768",   1024,  768, 1.5),
	R_800_600("800x600",      800,  600, 2.0);

	/** Human-readable label shown in the UI (e.g. {@code "1920x1080"}). */
	private final String displayName;
	/** Horizontal resolution in pixels. */
	private final int width;
	/** Vertical resolution in pixels. */
	private final int height;
	/**
	 * UI scale multiplier relative to 1920×1080 (= {@code 1.0}).
	 * Values below {@code 1.0} shrink the UI for high-res displays;
	 * values above {@code 1.0} enlarge it for low-res displays.
	 */
	private final double scale;

	ScreenResolutions(final String displayName, final int width, final int height, final double scale) {
		this.displayName = displayName;
		this.width       = width;
		this.height      = height;
		this.scale       = scale;
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	/**
	 * Returns the human-readable label for this resolution.
	 *
	 * @return display name (e.g. {@code "1920x1080"})
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the horizontal resolution in pixels.
	 *
	 * @return width in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the vertical resolution in pixels.
	 *
	 * @return height in pixels
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the UI scale multiplier for this resolution.
	 * {@code 1.0} is the reference scale (1920×1080).
	 *
	 * @return UI scale factor
	 */
	public double getScale() {
		return scale;
	}

	// -------------------------------------------------------------------------
	// Lookup
	// -------------------------------------------------------------------------

	/**
	 * Finds the enum constant matching the given pixel dimensions.
	 *
	 * @param width  horizontal resolution to match
	 * @param height vertical resolution to match
	 * @return the matching {@code ScreenResolutions} constant, or {@code null} if none matches
	 */
	public static ScreenResolutions getResolution(final int width, final int height) {
		for (final ScreenResolutions sr : ScreenResolutions.values()) {
			if (width == sr.getWidth() && height == sr.getHeight()) {
				return sr;
			}
		}
		return null;
	}

}
