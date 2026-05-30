package temmiland.rollercoaster.game;

import temmiland.rollercoaster.configuration.ConfigurationStorage;
import temmiland.rollercoaster.game.rendering.Renderer;
import temmiland.rollercoaster.game.debug.DebugHud;
import temmiland.rollercoaster.game.state.GameStateManager;
import temmiland.rollercoaster.platform.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BooleanSupplier;

import static org.lwjgl.glfw.GLFW.glfwTerminate;

/**
 * Abstract base class for graphical games built on the Rollercoaster framework.
 *
 * <p>Defines the standard game-loop pattern and provides template methods for
 * subclasses to customise. Handles:
 * <ul>
 *   <li>Renderer initialisation (e.g. OpenGL)</li>
 *   <li>Asset and resource loading</li>
 *   <li>Game-state initialisation</li>
 *   <li>Main game loop</li>
 *   <li>Cleanup and resource release</li>
 * </ul>
 *
 * <p>Subclasses implement game-specific logic via template methods.
 * {@link #run()} controls the entire lifetime of the game.
 */
public abstract class Game<T extends GameStateManager<T>> {

	private static final Logger logger = LoggerFactory.getLogger(Game.class);

	/** Window reference for rendering and input. */
	protected final Window window;
	/** Configuration storage for persistent settings. */
	protected final ConfigurationStorage configurationStorage;
	/** Game-state manager for state transitions. */
	protected T gsm;
	/** Target frame rate read from the screen configuration. */
	protected final int fpsRate;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new game instance.
	 *
	 * @param window               the initialised GLFW window
	 * @param configurationStorage the central configuration storage
	 * @param gsm                  the game-specific state manager
	 */
	protected Game(final Window window, final ConfigurationStorage configurationStorage, final T gsm) {
		this.window = window;
		this.configurationStorage = configurationStorage;
		this.fpsRate = configurationStorage.getScreenConfiguration().getFrameRate();
		this.gsm = gsm;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Starts the game and orchestrates its full lifetime.
	 *
	 * <p>This method is {@code final} and runs the standard sequence:
	 * <ol>
	 *   <li>{@link #initRenderer()} — initialise the rendering system</li>
	 *   <li>{@link #loadAssets()} — load game resources</li>
	 *   <li>{@link #initGameState()} — prepare the game-state system</li>
	 *   <li>{@link #runGameLoop()} — execute the main loop</li>
	 *   <li>{@link #cleanup()} — release resources (always runs, even on error)</li>
	 * </ol>
	 */
	public final void run() {
		try {
			initRenderer();
			loadAssets();
			initGameState();
			runGameLoop();
		} catch (Exception e) {
			logger.error("Uncaught exception during game execution", e);
			throw new RuntimeException("Game error", e);
		} finally {
			cleanup();
		}
	}

	/**
	 * Lifecycle hook called after the game is instantiated but before
	 * {@code glfwCreateWindow} runs.
	 *
	 * <p>Override to configure window properties (title, hints, …).
	 * The default implementation is a no-op.
	 *
	 * @param window the configurable, not-yet-created window
	 */
	public void afterWindowCreation(final Window window) { }

	// -------------------------------------------------------------------------
	// Template methods
	// -------------------------------------------------------------------------

	/**
	 * Initialises the rendering system (e.g. OpenGL).
	 * The default implementation calls {@link Renderer#initRenderer(Window)}.
	 */
	protected void initRenderer() {
		Renderer.initRenderer(window);
	}

	/**
	 * Loads game resources (e.g. textures, shaders, audio).
	 * Called after renderer initialisation.
	 */
	protected abstract void loadAssets();

	/**
	 * Prepares the game-state system.
	 * Called after asset loading. The default implementation calls {@link GameStateManager#init}.
	 */
	protected void initGameState() {
		gsm.init(window);
	}

	/**
	 * Executes the main game loop.
	 * The default implementation builds a {@link GameLoop} from the values returned by
	 * the other template methods. Override only if the game needs a completely custom
	 * loop structure.
	 */
	protected void runGameLoop() {
		new GameLoop(gsm, fpsRate, getTickRate(), getMaxUpdatesPerFrame(),
				getDebugToggle(), createDebugHud(),
				() -> gsm.save()).run(window);
	}

	/**
	 * Returns the logic update rate in ticks per second.
	 *
	 * @return tick rate in TPS
	 */
	protected abstract int getTickRate();

	/**
	 * Returns the maximum number of catch-up steps allowed per frame.
	 * Limits the spiral-of-death when the game runs too slowly.
	 *
	 * @return max logic updates per rendered frame
	 */
	protected abstract int getMaxUpdatesPerFrame();

	/**
	 * Returns a supplier that indicates whether the debug-toggle key was pressed this frame.
	 * The default implementation always returns {@code false} (no debug HUD).
	 *
	 * @return debug toggle supplier
	 */
	protected BooleanSupplier getDebugToggle() {
		return () -> false;
	}

	/**
	 * Returns the {@link DebugHud} overlay, or {@code null} if no overlay is desired.
	 *
	 * @return debug HUD, or {@code null}
	 */
	protected DebugHud createDebugHud() {
		return null;
	}

	/**
	 * Releases resources and performs cleanup.
	 * Always called at the end of {@link #run()}, even if an exception was thrown.
	 *
	 * <p>Default behaviour:
	 * <ol>
	 *   <li>Save the current window size to the screen configuration</li>
	 *   <li>Persist all configurations to disk</li>
	 *   <li>Release the GLFW window</li>
	 *   <li>Terminate GLFW</li>
	 * </ol>
	 */
	protected void cleanup() {
		try {
			configurationStorage.getScreenConfiguration()
					.setLastWidth(window.getWidth())
					.setLastHeight(window.getHeight());
			configurationStorage.saveAllConfigurations();
			window.cleanUp();
			glfwTerminate();
		} catch (Exception e) {
			logger.error("Error during cleanup", e);
		}
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Returns the GLFW window.
	 *
	 * @return the window
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Returns the central configuration storage.
	 *
	 * @return the configuration storage
	 */
	public ConfigurationStorage getConfigurationStorage() {
		return configurationStorage;
	}
}
