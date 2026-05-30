package temmiland.rollercoaster.game;

import temmiland.rollercoaster.game.debug.DebugHud;
import temmiland.rollercoaster.game.state.GameStateManager;
import temmiland.rollercoaster.platform.Window;
import temmiland.rollercoaster.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/**
 * Fixed-timestep game loop with a separate, FPS-capped render pass.
 *
 * <p>Game logic runs at {@code tickRate} TPS. Rendering is capped at the configured
 * {@code fpsRate}. A spiral-of-death guard limits the maximum number of catch-up
 * logic steps per iteration to {@code maxUpdatesPerFrame}.</p>
 *
 * <p>A JVM shutdown hook ensures {@code onShutdown} is called even when the process is
 * killed externally. An {@link AtomicBoolean} prevents the hook and the normal exit path
 * from both invoking it.</p>
 */
public final class GameLoop {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

	private static final String[] NO_DEBUG_LINES = new String[0];

	/** Game-state manager that drives logic, rendering, and GUI each tick/frame. */
	private final GameStateManager<?> gsm;
	/** Optional debug HUD overlay; {@code null} when no overlay is active. */
	private final DebugHud            debugHud;
	/** Supplies {@code true} when the debug-toggle key is pressed this frame. */
	private final BooleanSupplier     debugToggle;
	/** Target frame rate in frames per second. */
	private final int                 fpsRate;
	/** Logic update rate in ticks per second. */
	private final int                 tickRate;
	/** Maximum catch-up steps per frame (spiral-of-death guard). */
	private final int                 maxUpdatesPerFrame;
	/** Callback invoked once when the loop exits (normal or via shutdown hook). */
	private final Runnable            onShutdown;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code GameLoop}.
	 *
	 * @param gsm                game-state manager
	 * @param fpsRate            target render frame rate
	 * @param tickRate           logic update rate in TPS
	 * @param maxUpdatesPerFrame max catch-up steps per frame (spiral-of-death guard)
	 * @param debugToggle        supplies {@code true} when the debug key was pressed
	 * @param debugHud           HUD overlay, or {@code null} for no overlay
	 * @param onShutdown         called once when the loop ends (normally or via hook)
	 */
	public GameLoop(final GameStateManager<?> gsm,
	                final int fpsRate,
	                final int tickRate,
	                final int maxUpdatesPerFrame,
	                final BooleanSupplier debugToggle,
	                final DebugHud debugHud,
	                final Runnable onShutdown) {
		this.gsm                = gsm;
		this.fpsRate            = fpsRate;
		this.tickRate           = tickRate;
		this.maxUpdatesPerFrame = maxUpdatesPerFrame;
		this.debugToggle        = debugToggle;
		this.debugHud           = debugHud;
		this.onShutdown         = onShutdown;
	}

	// -------------------------------------------------------------------------
	// Loop
	// -------------------------------------------------------------------------

	/**
	 * Runs the game loop until the window requests a close.
	 * Blocks the calling thread for the lifetime of the game.
	 *
	 * @param window the GLFW window driving input and buffer swaps
	 */
	public void run(final Window window) {
		final AtomicBoolean saveGuard = new AtomicBoolean(false);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (saveGuard.compareAndSet(false, true)) {
				LOGGER.info("Shutdown hook: running cleanup...");
				onShutdown.run();
			}
		}, "game-shutdown-hook"));

		final double tickDuration   = 1.0 / tickRate;
		final double renderCap      = 1.0 / fpsRate;
		final double maxAccumulator = tickDuration * maxUpdatesPerFrame;

		double tickAccumulator   = 0;
		double renderAccumulator = 0;
		double frameTime         = 0;
		int    frames            = 0;
		double time              = Timer.getTime();

		while (!window.shouldClose()) {
			final double now    = Timer.getTime();
			final double passed = now - time;
			time = now;

			tickAccumulator   += passed;
			renderAccumulator += passed;
			frameTime         += passed;

			// Clamp accumulator to prevent spiral-of-death on lag spikes.
			if (tickAccumulator > maxAccumulator) {
				tickAccumulator = maxAccumulator;
			}

			// --- Logic: fixed timestep ---
			while (tickAccumulator >= tickDuration) {
				tickAccumulator -= tickDuration;

				window.update();

				if (window.hasResized()) {
					gsm.resize();
				}

				if (debugHud != null && debugToggle.getAsBoolean()) {
					debugHud.toggleDetailed();
				}

				gsm.update(tickDuration);
			}

			// --- Rendering: fpsRate cap ---
			if (renderAccumulator >= renderCap) {
				renderAccumulator -= renderCap;

				final float alpha = (float) (tickAccumulator / tickDuration);

				gsm.render(alpha);

				if (debugHud != null && gsm.isDebugOverlayEnabled() && debugHud.isShowDetailed()) {
					gsm.renderDebug(alpha);
				}

				gsm.renderGui(alpha);

				if (debugHud != null && gsm.isDebugOverlayEnabled()) {
					final String[] debugLines = debugHud.isShowDetailed()
							? gsm.getDebugLines()
							: NO_DEBUG_LINES;
					debugHud.render(window, alpha, debugLines);
				}

				window.swapBuffers();
				frames++;

				if (frameTime >= 1.0) {
					frameTime = 0;
					if (debugHud != null) {
						debugHud.updateFps(frames);
					}
					frames = 0;
				}
			}

			// Sleep for the shortest remaining interval minus a small margin.
			final double sleepSec = Math.min(tickDuration - tickAccumulator, renderCap - renderAccumulator) - 0.001;
			if (sleepSec > 0.001) {
				LockSupport.parkNanos((long) (sleepSec * 1_000_000_000L));
			}
		}

		if (saveGuard.compareAndSet(false, true)) {
			onShutdown.run();
		}
	}
}
