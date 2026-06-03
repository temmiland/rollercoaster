package land.temmi.rollercoaster.game.state;

import land.temmi.rollercoaster.platform.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Manages the set of available {@link GameState}s and drives the active one each tick
 * and frame.
 *
 * <p>Subclasses declare their states via {@link #getStates()} and the initial state via
 * {@link #getInitialState()}. All states are instantiated during {@link #init(Window)}
 * and stored by name in an internal map.</p>
 *
 * @param <M> the concrete manager type (self-referential for type-safe state creation)
 */
public abstract class GameStateManager<M extends GameStateManager<M>> {

	/** All registered states, keyed by their descriptor name. */
	protected Map<String, GameState<?>> gameStates;
	/** Name of the currently active state. */
	protected String currentState;
	/** The GLFW window, available after {@link #init(Window)}. */
	protected Window window;

	// -------------------------------------------------------------------------
	// Abstract descriptors
	// -------------------------------------------------------------------------

	/**
	 * Returns the list of all state descriptors to register during {@link #init}.
	 *
	 * @return ordered list of state descriptors
	 */
	protected abstract List<GameStateDescriptor<M>> getStates();

	/**
	 * Returns the descriptor of the state to activate on startup.
	 *
	 * @return initial state descriptor
	 */
	protected abstract GameStateDescriptor<M> getInitialState();

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/** Creates a new manager with an empty state map and no active state. */
	protected GameStateManager() {
		this.gameStates   = new HashMap<>();
		this.currentState = null;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Registers all states returned by {@link #getStates()} and activates the initial state.
	 * Must be called once before any tick or frame methods.
	 *
	 * @param window the GLFW window passed to each state on activation
	 */
	@SuppressWarnings("unchecked")
	public void init(final Window window) {
		this.window = window;
		for (final GameStateDescriptor<M> descriptor : getStates()) {
			registerState(descriptor.name(), descriptor.create((M) this));
		}
		setCurrentState(getInitialState().name());
	}

	// -------------------------------------------------------------------------
	// State management
	// -------------------------------------------------------------------------

	/**
	 * Registers a state under the given key.
	 *
	 * @param key   the state name
	 * @param state the state instance
	 */
	protected void registerState(final String key, final GameState<?> state) {
		gameStates.put(key, state);
	}

	/**
	 * Switches to the state registered under the given key and calls its {@code init}.
	 * Has no effect if the key is not registered.
	 *
	 * @param key the target state name
	 */
	public void setCurrentState(final String key) {
		if (gameStates.containsKey(key)) {
			currentState = key;
			gameStates.get(currentState).init(window);
		}
	}

	/**
	 * Switches to the state identified by the given descriptor.
	 *
	 * @param descriptor the target state descriptor
	 */
	public void setCurrentState(final GameStateDescriptor<?> descriptor) {
		setCurrentState(descriptor.name());
	}

	/**
	 * Returns the state identified by the given descriptor, or {@code null} if not registered.
	 *
	 * @param descriptor the state descriptor
	 * @return the corresponding {@link GameState}, or {@code null}
	 */
	protected GameState<?> getState(final GameStateDescriptor<?> descriptor) {
		return gameStates.get(descriptor.name());
	}

	/**
	 * Returns the currently active {@link GameState}.
	 *
	 * @return the active state
	 */
	public GameState<?> getCurrentState() {
		return gameStates.get(currentState);
	}

	// -------------------------------------------------------------------------
	// Per-tick / per-frame operations
	// -------------------------------------------------------------------------

	/**
	 * Updates the OpenGL viewport and notifies the active state of the resize.
	 * Called by the game loop when the window is resized.
	 */
	public void resize() {
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
		gameStates.get(currentState).resize();
	}

	/**
	 * Updates the active state for the current logic tick.
	 *
	 * @param delta time elapsed since the last tick in seconds
	 */
	public void update(final double delta) {
		gameStates.get(currentState).update(delta);
	}

	/**
	 * Clears the colour buffer and renders the active state's game world.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public void render(final float alpha) {
		glClear(GL_COLOR_BUFFER_BIT);
		gameStates.get(currentState).render(alpha);
	}

	/**
	 * Renders the active state's debug layer.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public void renderDebug(final float alpha) {
		gameStates.get(currentState).renderDebug(alpha);
	}

	/**
	 * Renders the active state's GUI layer.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public void renderGui(final float alpha) {
		gameStates.get(currentState).renderGui(alpha);
	}

	// -------------------------------------------------------------------------
	// Debug
	// -------------------------------------------------------------------------

	/**
	 * Returns the debug text lines from the active state.
	 *
	 * @return array of debug lines; never {@code null}
	 */
	public String[] getDebugLines() {
		return gameStates.get(currentState).getDebugLines();
	}

	/**
	 * Returns whether the debug overlay is enabled for the active state.
	 *
	 * @return {@code true} if the debug overlay should be shown
	 */
	public boolean isDebugOverlayEnabled() {
		return gameStates.get(currentState).isDebugOverlayEnabled();
	}

	// -------------------------------------------------------------------------
	// Persistence
	// -------------------------------------------------------------------------

	/**
	 * Calls {@link GameState#save()} on all registered states.
	 * Invoked by the game loop when it exits.
	 */
	public void save() {
		for (final GameState<?> state : gameStates.values()) {
			state.save();
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
}
