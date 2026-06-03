package temmiland.rollercoaster.game;

import temmiland.rollercoaster.config.ConfigurationStorage;
import temmiland.rollercoaster.config.screen.ScreenConfiguration;
import temmiland.rollercoaster.graphics.Renderer;
import temmiland.rollercoaster.debug.DebugHud;
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
	 *   <li>initialise the rendering system, then {@link #onInitRenderer()}</li>
	 *   <li>{@link #onLoadAssets()} — load game resources</li>
	 *   <li>prepare the game-state system, then {@link #onInitGameState()}</li>
	 *   <li>{@link #runGameLoop()} — execute the main loop</li>
	 *   <li>{@link #onCleanup()} then framework teardown (always runs, even on error)</li>
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

	// -------------------------------------------------------------------------
	// Internal lifecycle (final — framework logic that always runs)
	// -------------------------------------------------------------------------

	/** Initialises the rendering system, then calls {@link #onInitRenderer()}. */
	private void initRenderer() {
		Renderer.initRenderer(window);
		onInitRenderer();
	}

	/** Loads game assets, then calls {@link #onLoadAssets()}. */
	private void loadAssets() {
		onLoadAssets();
	}

	/** Prepares the game-state system, then calls {@link #onInitGameState()}. */
	private void initGameState() {
		gsm.init(window);
		onInitGameState();
	}

	/**
	 * Executes the main game loop.
	 * The default implementation builds a {@link GameLoop} from the values returned by
	 * the other template methods. Override only if the game needs a completely custom
	 * loop structure.
	 */
	protected void runGameLoop() {
		final ScreenConfiguration screen = configurationStorage.getScreenConfiguration();
		new GameLoop(gsm, getTickRate(), getMaxUpdatesPerFrame(),
				isDebugTogglePressed(), screen::isVsync, screen::getFrameRate,
				createDebugHud(), () -> gsm.save()).run(window);
	}

	/**
	 * Runs the subclass cleanup hook, then releases framework resources.
	 * Always called at the end of {@link #run()}, even if an exception was thrown.
	 *
	 * <p>The subclass hook runs first, while the window and GL context are still
	 * valid; framework teardown follows. Both phases are guarded so a failure in
	 * one does not prevent the other.</p>
	 *
	 * <p>Framework teardown:
	 * <ol>
	 *   <li>Save the current window size to the screen configuration</li>
	 *   <li>Persist all configurations to disk</li>
	 *   <li>Release the GLFW window</li>
	 *   <li>Terminate GLFW</li>
	 * </ol>
	 */
	private void cleanup() {
		onCleanup();
		configurationStorage.getScreenConfiguration()
				.setLastWidth(window.getWidth())
				.setLastHeight(window.getHeight());
		configurationStorage.saveAllConfigurations();
		window.cleanUp();
		glfwTerminate();
	}

	// -------------------------------------------------------------------------
	// Template-method hooks
	// -------------------------------------------------------------------------

	/**
	 * Called after the rendering system has been initialised. Override to perform
	 * game-specific render setup. Default is a no-op.
	 */
	protected abstract void onInitRenderer();

	/**
	 * Called by the framework to load game resources (e.g. textures, shaders, audio).
	 * Called after renderer initialisation.
	 */
	protected abstract void onLoadAssets();

	/**
	 * Called after the game-state system has been prepared. Override to perform
	 * game-specific state setup. Default is a no-op.
	 */
	protected abstract void onInitGameState();

	/**
	 * Called during shutdown to release game-specific resources, while the window
	 * and GL context are still valid. Framework teardown runs afterwards.
	 * Default is a no-op.
	 */
	protected abstract void onCleanup();

	/**
	 * Returns the window title for this game. Applied by the bootstrap right after
	 * the window is created.
	 *
	 * @return the window title
	 */
	public abstract String getTitle();

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
	protected abstract BooleanSupplier isDebugTogglePressed();

	/**
	 * Returns the {@link DebugHud} overlay, or {@code null} if no overlay is desired.
	 *
	 * @return debug HUD, or {@code null}
	 */
	protected abstract DebugHud createDebugHud();

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
