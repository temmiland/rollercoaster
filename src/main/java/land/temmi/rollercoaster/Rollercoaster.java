package land.temmi.rollercoaster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.temmi.rollercoaster.config.ConfigurationStorage;
import land.temmi.rollercoaster.config.JsonConfig;
import land.temmi.rollercoaster.config.screen.ScreenConfiguration;
import land.temmi.rollercoaster.platform.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import land.temmi.rollercoaster.game.Game;

import static org.lwjgl.glfw.GLFW.glfwInit;

/**
 * Central bootstrapping and initialization class for graphical applications.
 *
 * Rollercoaster provides base infrastructure for the orchestrated initialization
 * of subsystems in the correct order. It is not limited to pxWorlds, but serves
 * as a generic foundation for different application types.
 *
 * Typical initialization sequence:
 * <ol>
 *   <li>Configuration management (persistent settings, data management)</li>
 *   <li>GLFW/rendering system (OpenGL context, window)</li>
 *   <li>Main application loop (game, editor, etc.)</li>
 * </ol>
 *
 * Usage notes:
 * - All methods are static; instantiation serves no purpose
 * - Abstracts platform-specific initialization logic
 */
public class Rollercoaster {

    /** Central configuration management for persistent application settings. */
    private static ConfigurationStorage configurationStorage;
    /** GLFW window for rendering, input, and platform integration. */
    private static Window window;

    /**
     * Initializes all subsystems and starts a game.
     *
     * This is the simplified one-shot entry point for applications.
     * This method orchestrates:
     * <ol>
     *   <li>Full subsystem initialization via {@link #initialize(String)}</li>
     *   <li>Game instance creation via constructor with {@link Window} and {@link ConfigurationStorage}</li>
     *   <li>Window creation</li>
     *   <li>Window configuration (VSync, title via {@link Game#getTitle()})</li>
     *   <li>Game execution via {@link Game#run()}</li>
     * </ol>
     *
     * @param gameClass       the concrete {@link Game} implementation to instantiate
     * @param configDirectory the directory from which configuration files are loaded
     * @throws RuntimeException if initialization, instantiation, or game execution fails
     */
    public static void run(Class<? extends Game<?>> gameClass, String configDirectory) {
        initialize(configDirectory);
        try {
            final Game<?> game = gameClass
                    .getDeclaredConstructor(Window.class, ConfigurationStorage.class)
                    .newInstance(window, configurationStorage);
            window.createWindow();
            window.setVSync(configurationStorage.getScreenConfiguration().isVsync());
            window.setTitle(game.getTitle());
            game.run();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Erstellen/Ausführen des Games: " + gameClass.getName(), e);
        }
    }

    /**
     * Orchestrates the full initialization of all critical subsystems.
     *
     * This method controls initialization in the following order:
     * <ol>
     *   <li>Creates and initializes the configuration storage</li>
     *   <li>Initializes GLFW</li>
     *   <li>Prepares the window with stored settings</li>
     * </ol>
     *
     * After this method returns, the application is ready for the main loop.
     *
     * @param configDirectory the directory from which configuration files are loaded
     * @throws RuntimeException if a critical subsystem cannot be initialized (e.g. GLFW failure)
     */
    public static void initialize(String configDirectory) {
        createConfigurationStorage(configDirectory);
        configurationStorage.init();

        initGlfw();
        prepareWindow();
    }

    /**
     * Creates the configuration storage and initializes it with JSON serialization.
     *
     * Configures a {@link Gson} instance with complex map key serialization and pretty-printing,
     * then constructs a {@link ConfigurationStorage} backed by that JSON parser.
     *
     * @param configDirectory the directory from which configuration files are loaded
     */
    private static void createConfigurationStorage(String configDirectory) {
        final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
        final JsonConfig jsonConfig = new JsonConfig(gson, configDirectory);
        configurationStorage = new ConfigurationStorage(jsonConfig);
    }

    /**
     * Initializes GLFW and registers global error callbacks.
     *
     * Must be called BEFORE window creation. It:
     * <ul>
     *   <li>Registers global GLFW error callbacks</li>
     *   <li>Calls {@code glfwInit()} to initialize the GLFW system</li>
     * </ul>
     *
     * @throws RuntimeException if {@code glfwInit()} fails
     */
    private static void initGlfw() {
        final Logger logger = LoggerFactory.getLogger(Rollercoaster.class);
        Window.setGlfwErrorCallbacks();
        if (!glfwInit()) {
            logger.error("GLFW Failed to initialize!");
            throw new RuntimeException("Failed to initialize GLFW");
        }
    }

    /**
     * Creates the window instance and applies stored settings to it.
     *
     * Creates a new {@link Window} and applies the last known window settings:
     * <ul>
     *   <li>Width and height from the previous session</li>
     *   <li>Fullscreen state</li>
     * </ul>
     *
     * Note: the actual OS window is not created here; that happens later via {@code window.createWindow()}.
     *
     * @throws NullPointerException if the configuration storage has not been initialized
     */
    private static void prepareWindow() {
        final ScreenConfiguration screenConfiguration = configurationStorage.getScreenConfiguration();
        window = new Window(screenConfiguration.getLastWidth(), screenConfiguration.getLastHeight());
        window.setFullscreen(screenConfiguration.isFullscreen());
    }

}
