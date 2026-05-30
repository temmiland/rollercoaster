package temmiland.rollercoaster.configuration;

import com.google.gson.reflect.TypeToken;
import temmiland.rollercoaster.configuration.screen.ScreenConfiguration;

import java.lang.reflect.Type;

/**
 * Base class for all configuration objects and registry of known configuration types.
 *
 * <p>Subclasses are serialised to and deserialised from JSON by {@link JsonConfig}.
 * Each concrete configuration type is registered as a {@link ConfigurationType} constant
 * so that {@link JsonConfig#readConfiguration} can resolve the correct Gson {@link Type}
 * at runtime.
 */
public class Configuration {

	/**
	 * Registry of known configuration types, each carrying the Gson {@link Type} token
	 * needed for deserialisation.
	 */
	public enum ConfigurationType {
		/** Configuration type for screen/display settings. */
		SCREEN_CONFIGURATION(new TypeToken<ScreenConfiguration>() {
		}.getType());

		/** Gson {@link Type} token used for deserialising this configuration. */
		private final Type type;

		ConfigurationType(final Type cType) {
			this.type = cType;
		}

		/**
		 * Returns the Gson {@link Type} token for this configuration type.
		 *
		 * @return the Gson type token
		 */
		public Type getType() {
			return type;
		}
	}

}
