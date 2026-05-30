package temmiland.rollercoaster.configuration;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Thin wrapper around {@link Gson} for reading and writing {@link Configuration} objects as JSON
 * files on disk.
 *
 * <p>All file paths are resolved relative to the {@code configDirectory} supplied at construction
 * time via {@link #generateConfigName(String)}.
 */
public class JsonConfig {

	/** Gson instance used for all serialisation and deserialisation. */
	private final Gson gson;
	/** Base directory for all configuration files, including a trailing path separator. */
	private final String configDirectory;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code JsonConfig}.
	 *
	 * @param cGson            Gson instance to use for JSON serialisation
	 * @param cConfigDirectory base directory for config files (must include trailing separator)
	 */
	public JsonConfig(final Gson cGson, final String cConfigDirectory) {
		this.gson = cGson;
		this.configDirectory = cConfigDirectory;
	}

	// -------------------------------------------------------------------------
	// Path helper
	// -------------------------------------------------------------------------

	/**
	 * Builds the full file path for a named configuration file.
	 *
	 * @param configName the base name of the config file (without extension)
	 * @return the full path including directory and {@code .json} suffix
	 */
	public String generateConfigName(final String configName) {
		return configDirectory + configName + ".json";
	}

	// -------------------------------------------------------------------------
	// Persistence
	// -------------------------------------------------------------------------

	/**
	 * Serialises a {@link Configuration} object to a JSON file.
	 * Silently prints the stack trace on {@link IOException}.
	 *
	 * @param configuration the configuration object to save
	 * @param fileName      full path of the target file
	 */
	public void saveConfig(final Configuration configuration, final String fileName) {
		try {
			final Writer writer = new OutputStreamWriter(new FileOutputStream(fileName));
			writer.write(gson.toJson(configuration, configuration.getClass()));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deserialises a JSON file into a {@link Configuration} subtype.
	 * If the file cannot be read or the JSON is invalid, a default instance is returned
	 * and an empty JSON object is saved to the file.
	 *
	 * @param <T>      the configuration subtype
	 * @param fileName full path of the source file
	 * @param type     Gson {@link Type} token for deserialisation
	 * @return the deserialised configuration, or a default instance on error
	 */
	public <T extends Configuration> T readConfiguration(final String fileName, final Type type) {
		final StringBuilder stringBuilder = new StringBuilder();
		final File file = new File(fileName);
		try {
			final FileReader     fileReader     = new FileReader(file);
			final BufferedReader bufferedReader = new BufferedReader(fileReader);
			String currentLine;
			while ((currentLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(currentLine);
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return gson.fromJson(stringBuilder.toString(), type);
		} catch (Exception e) {
			saveConfig(new Configuration(), fileName);
			return gson.fromJson("{}", type);
		}
	}

}
