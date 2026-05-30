package temmiland.rollercoaster.game.state;

import temmiland.rollercoaster.platform.Window;

/**
 * Abstract base class for a single game state (e.g. menu, gameplay, pause screen).
 *
 * <p>The public {@code final} methods delegate to protected {@code on*} template methods,
 * ensuring the framework can inject cross-cutting behaviour (e.g. storing the window
 * reference in {@link #init}) without subclasses bypassing it.</p>
 *
 * <p>Required template methods ({@link #onInit}, {@link #onUpdate}, {@link #onRender})
 * must be implemented by every subclass. All other {@code on*} methods have no-op
 * defaults and may be overridden as needed.</p>
 */
public abstract class GameState<M extends GameStateManager<M>> {

	/** The owning game-state manager. */
	protected M gsm;
	/** The GLFW window; set by {@link #init(Window)} before {@link #onInit} is called. */
	protected Window window;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new game state attached to the given manager.
	 *
	 * @param gsm the owning game-state manager
	 */
	protected GameState(final M gsm) {
		this.gsm = gsm;
	}

	// -------------------------------------------------------------------------
	// Public API (final — delegates to template methods)
	// -------------------------------------------------------------------------

	/**
	 * Stores the window reference and calls {@link #onInit(Window)}.
	 * Called by the manager when this state becomes active.
	 *
	 * @param window the GLFW window
	 */
	public final void init(final Window window) {
		this.window = window;
		onInit(window);
	}

	/** Delegates to {@link #onResize()}. Called when the window is resized. */
	public final void resize()                       { onResize(); }

	/**
	 * Delegates to {@link #onUpdate(double)}.
	 *
	 * @param delta time elapsed since the last tick in seconds
	 */
	public final void update(final double delta)     { onUpdate(delta); }

	/**
	 * Delegates to {@link #onRender(float)}.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public final void render(final float alpha)      { onRender(alpha); }

	/**
	 * Delegates to {@link #onRenderGui(float)}.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public final void renderGui(final float alpha)   { onRenderGui(alpha); }

	/**
	 * Delegates to {@link #onRenderDebug(float)}.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	public final void renderDebug(final float alpha) { onRenderDebug(alpha); }

	/** Delegates to {@link #onSave()}. Called when the game loop exits. */
	public final void save()                         { onSave(); }

	/**
	 * Returns the debug text lines provided by {@link #onGetDebugLines()}.
	 *
	 * @return array of debug lines; never {@code null}
	 */
	public final String[] getDebugLines()            { return onGetDebugLines(); }

	/**
	 * Returns whether the debug overlay should be shown, as reported by {@link #showDebugMenu()}.
	 *
	 * @return {@code true} if the debug overlay is enabled for this state
	 */
	public final boolean isDebugOverlayEnabled()     { return showDebugMenu(); }

	// -------------------------------------------------------------------------
	// Required template methods
	// -------------------------------------------------------------------------

	/**
	 * Called once when the state becomes active.
	 *
	 * @param window the GLFW window
	 */
	protected abstract void onInit(Window window);

	/**
	 * Called each logic tick to update game state.
	 *
	 * @param delta time elapsed since the last tick in seconds
	 */
	protected abstract void onUpdate(double delta);

	/**
	 * Called each rendered frame to draw the game world.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	protected abstract void onRender(float alpha);

	// -------------------------------------------------------------------------
	// Optional template methods
	// -------------------------------------------------------------------------

	/** Called when the window is resized. Default is a no-op. */
	protected void onResize() {}

	/**
	 * Called each rendered frame to draw the GUI layer. Default is a no-op.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	protected void onRenderGui(final float alpha) {}

	/**
	 * Called each rendered frame to draw the debug layer. Default is a no-op.
	 *
	 * @param alpha interpolation factor between the last two ticks (0..1)
	 */
	protected void onRenderDebug(final float alpha) {}

	/** Called when the game loop exits to persist state. Default is a no-op. */
	protected void onSave() {}

	/**
	 * Returns debug text lines for the debug HUD. Default returns an empty array.
	 *
	 * @return array of debug lines
	 */
	protected String[] onGetDebugLines() { return new String[0]; }

	/**
	 * Returns whether the debug overlay should be shown for this state.
	 * Default returns {@code true}.
	 *
	 * @return {@code true} to show the debug overlay
	 */
	protected boolean showDebugMenu() { return true; }
}
