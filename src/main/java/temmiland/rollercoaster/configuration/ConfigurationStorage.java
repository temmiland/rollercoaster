package temmiland.rollercoaster.configuration;

import temmiland.rollercoaster.configuration.screen.ScreenConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Manages loading, caching, and saving of all game configuration objects.
 *
 * <p>Configurations are persisted as JSON files via {@link JsonConfig}.
 * Call {@link #init()} once at startup to ensure all config files exist and are loaded.
 */
public class ConfigurationStorage {

	/** JSON serialisation backend used to read and write config files. */
	private final JsonConfig jsonConfig;
	/** Cached screen configuration, loaded during {@link #init()}. */
	private ScreenConfiguration screenConfiguration;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code ConfigurationStorage} backed by the given {@link JsonConfig}.
	 *
	 * @param cJsonConfig the JSON config backend to use for all read/write operations
	 */
	public ConfigurationStorage(final JsonConfig cJsonConfig) {
		this.jsonConfig = cJsonConfig;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Ensures all configuration files exist on disk and loads them into memory.
	 * Must be called once before any configuration is read.
	 */
	public void init() {
		createConfigDirectoryIfNotExists();
		createScreenConfigurationIfNotExists();
	}

	// -------------------------------------------------------------------------
	// File initialisation
	// -------------------------------------------------------------------------

	/**
	 * Creates the configuration directory if it does not already exist.
	 */
	public void createConfigDirectoryIfNotExists() {
		final File file = new File(jsonConfig.generateConfigName("").replace("screen_configuration.json", ""));
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * Creates the {@code screen_configuration.json} file with default values if it does not exist,
	 * then loads and caches the configuration.
	 */
	public void createScreenConfigurationIfNotExists() {
		final File file = new File(jsonConfig.generateConfigName("screen_configuration"));
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				jsonConfig.saveConfig(new ScreenConfiguration(), jsonConfig.generateConfigName("screen_configuration"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setScreenConfiguration(jsonConfig.readConfiguration(
				jsonConfig.generateConfigName("screen_configuration"),
				Configuration.ConfigurationType.SCREEN_CONFIGURATION.getType()));
	}

	// -------------------------------------------------------------------------
	// Screen configuration
	// -------------------------------------------------------------------------

	/**
	 * Returns the cached screen configuration.
	 *
	 * @return the current {@link ScreenConfiguration}
	 */
	public ScreenConfiguration getScreenConfiguration() {
		return screenConfiguration;
	}

	/**
	 * Replaces the cached screen configuration.
	 *
	 * @param cScreenConfiguration the new {@link ScreenConfiguration} to cache
	 * @return {@code this} for method chaining
	 */
	public ConfigurationStorage setScreenConfiguration(final ScreenConfiguration cScreenConfiguration) {
		this.screenConfiguration = cScreenConfiguration;
		return this;
	}

	// -------------------------------------------------------------------------
	// Persistence
	// -------------------------------------------------------------------------

	/**
	 * Persists the screen configuration to disk.
	 *
	 * @return {@code this} for method chaining
	 */
	public ConfigurationStorage saveScreenConfiguration() {
		jsonConfig.saveConfig(getScreenConfiguration(), jsonConfig.generateConfigName("screen_configuration"));
		return this;
	}

	/**
	 * Persists all configurations to disk.
	 *
	 * @return {@code this} for method chaining
	 */
	public ConfigurationStorage saveAllConfigurations() {
		return saveScreenConfiguration();
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Returns the underlying {@link JsonConfig} used for serialisation.
	 *
	 * @return the JSON config backend
	 */
	public JsonConfig getJsonConfig() {
		return jsonConfig;
	}
}
