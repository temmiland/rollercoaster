package land.temmi.rollercoaster.game;

import land.temmi.rollercoaster.debug.DebugHud;
import land.temmi.rollercoaster.game.state.GameStateManager;
import land.temmi.rollercoaster.platform.Window;
import land.temmi.rollercoaster.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * Fixed-timestep game loop with a per-iteration render pass and adaptive frame pacing.
 *
 * <p>Game logic runs at {@code tickRate} TPS via a fixed-timestep accumulator. Input is
 * polled once per tick so "pressed this tick" edges are consumed in the tick that produces
 * them. Rendering happens once per loop iteration. Frame pacing adapts to the VSync setting:
 * <ul>
 *   <li><b>VSync on:</b> {@code window.swapBuffers()} blocks until the next vertical blank,
 *       so the loop is paced to the monitor refresh rate. Only a small safety floor applies.</li>
 *   <li><b>VSync off:</b> {@code swapBuffers()} returns immediately, so the loop sleeps until
 *       the {@code fpsRate} frame budget using a hybrid park-then-spin sleep for precision.</li>
 * </ul>
 * Both settings are read through suppliers each frame, so they can be toggled at runtime.
 * A spiral-of-death guard limits the maximum number of catch-up logic steps per iteration
 * to {@code maxUpdatesPerFrame}.</p>
 *
 * <p>A JVM shutdown hook ensures {@code onShutdown} is called even when the process is
 * killed externally. An {@link AtomicBoolean} prevents the hook and the normal exit path
 * from both invoking it.</p>
 */
public final class GameLoop {

	/** Logger for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);

	/** Shared empty array returned when no debug lines are active, avoids repeated allocation. */
	private static final String[] NO_DEBUG_LINES = new String[0];

	/** Nanoseconds per second; used to convert the coarse sleep duration. */
	private static final long NANOS_PER_SECOND = 1_000_000_000L;

	/**
	 * Frame-time floor (~1000 fps) used when VSync is on. Guards against a CPU busy-loop
	 * if {@code swapBuffers()} does not block (e.g. the window is minimised or a driver
	 * overrides VSync off).
	 */
	private static final double MIN_FRAME_TIME = 1.0 / 1000.0;

	/**
	 * Busy-spin margin for the hybrid sleep. The loop coarse-sleeps until this many seconds
	 * before the target, then busy-spins the remainder. Absorbs scheduler oversleep
	 * ({@code parkNanos} granularity is several ms on macOS) at the cost of a short spin.
	 */
	private static final double BUSY_SPIN_MARGIN = 0.002;

	/** Game-state manager that drives logic, rendering, and GUI each tick/frame. */
	private final GameStateManager<?> gsm;
	/** Optional debug HUD overlay; {@code null} when no overlay is active. */
	private final DebugHud            debugHud;
	/** Supplies {@code true} when the debug-toggle key is pressed this frame. */
	private final BooleanSupplier     debugToggle;
	/** Supplies whether VSync is currently enabled (read each frame for runtime toggling). */
	private final BooleanSupplier     vsyncEnabled;
	/** Supplies the target frame rate used when VSync is off (read each frame). */
	private final IntSupplier         fpsRate;
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
	 * @param tickRate           logic update rate in TPS
	 * @param maxUpdatesPerFrame max catch-up steps per frame (spiral-of-death guard)
	 * @param debugToggle        supplies {@code true} when the debug key was pressed
	 * @param vsyncEnabled       supplies whether VSync is enabled (read each frame)
	 * @param fpsRate            supplies the target frame rate used when VSync is off
	 * @param debugHud           HUD overlay, or {@code null} for no overlay
	 * @param onShutdown         called once when the loop ends (normally or via hook)
	 */
	public GameLoop(final GameStateManager<?> gsm,
	                final int tickRate,
	                final int maxUpdatesPerFrame,
	                final BooleanSupplier debugToggle,
	                final BooleanSupplier vsyncEnabled,
	                final IntSupplier fpsRate,
	                final DebugHud debugHud,
	                final Runnable onShutdown) {
		this.gsm                = gsm;
		this.tickRate           = tickRate;
		this.maxUpdatesPerFrame = maxUpdatesPerFrame;
		this.debugToggle        = debugToggle;
		this.vsyncEnabled       = vsyncEnabled;
		this.fpsRate            = fpsRate;
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
		final double maxAccumulator = tickDuration * maxUpdatesPerFrame;

		// Software backstop for VSync. Hardware VSync (glfwSwapInterval) does not reliably
		// block on every platform — notably on macOS, where swapBuffers() returns
		// immediately and the loop would otherwise run unbounded. Capping at the monitor
		// refresh rate makes the software sleep a no-op where hardware VSync works, and a
		// correct frame limiter where it does not.
		final int refreshRate = window.getRefreshRate();
		final double vsyncFrameTime;
		if (refreshRate > 0) {
			vsyncFrameTime = 1.0 / refreshRate;
		} else {
			vsyncFrameTime = MIN_FRAME_TIME;
		}

		double tickAccumulator = 0;
		double frameTime       = 0;
		int    frames          = 0;
		double time            = Timer.getTime();

		// Last window size for which the projection has been applied. Compared every frame
		// (level-triggered) instead of relying solely on the one-shot resize flag.
		int appliedWidth  = window.getWidth();
		int appliedHeight = window.getHeight();

		while (!window.shouldClose()) {
			final double now    = Timer.getTime();
			final double passed = now - time;
			time = now;

			tickAccumulator += passed;
			frameTime       += passed;

			// Clamp accumulator to prevent spiral-of-death on lag spikes.
			if (tickAccumulator > maxAccumulator) {
				tickAccumulator = maxAccumulator;
			}

			// --- Logic: fixed timestep ---
			// Input is polled once per tick, so "pressed this tick" edges (mouse clicks,
			// key presses) are consumed in the same tick they are produced. Polling per
			// frame instead would clear those edges before the slower tick reads them.
			while (tickAccumulator >= tickDuration) {
				tickAccumulator -= tickDuration;

				window.update();

				if (debugHud != null && debugToggle.getAsBoolean()) {
					debugHud.toggleDetailed();
				}

				gsm.update(tickDuration);
			}

			// Apply a pending resize before rendering. Level-triggered (compares the current
			// window size to the last applied size) rather than edge-triggered, so a missed
			// resize flag cannot leave a stale projection — notably on macOS, where the live
			// resize blocks glfwPollEvents() inside a modal loop and the GUI would otherwise
			// only rescale after the next state switch.
			if (window.getWidth() != appliedWidth || window.getHeight() != appliedHeight) {
				appliedWidth  = window.getWidth();
				appliedHeight = window.getHeight();
				gsm.resize();
			}

			// --- Rendering: every iteration; VSync paces the loop via swapBuffers() ---
			final float alpha = (float) (tickAccumulator / tickDuration);

			gsm.render(alpha);

			if (debugHud != null && gsm.isDebugOverlayEnabled() && debugHud.isShowDetailed()) {
				gsm.renderDebug(alpha);
			}

			gsm.renderGui(alpha);

			if (debugHud != null && gsm.isDebugOverlayEnabled()) {
				final String[] debugLines;
				if (debugHud.isShowDetailed()) {
					debugLines = gsm.getDebugLines();
				} else {
					debugLines = NO_DEBUG_LINES;
				}
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

			// Frame pacing via a hybrid park-then-spin sleep. With VSync, the target is the
			// monitor refresh interval: where hardware VSync already blocked in swapBuffers()
			// the sleep is a no-op, where it did not (e.g. macOS) it caps to the refresh rate.
			// Without VSync, the target is the configured fpsRate budget; an fpsRate of 0 means
			// uncapped, so the loop renders as fast as the hardware allows (no sleep).
			final int fps = fpsRate.getAsInt();
			final double targetFrameTime;
			if (vsyncEnabled.getAsBoolean()) {
				targetFrameTime = vsyncFrameTime;
			} else if (fps > 0) {
				targetFrameTime = 1.0 / fps;
			} else {
				targetFrameTime = 0.0;
			}
			sleepUntilFrameTime(now, targetFrameTime);
		}

		if (saveGuard.compareAndSet(false, true)) {
			onShutdown.run();
		}
	}

	/**
	 * Sleeps until {@code targetFrameTime} seconds have elapsed since {@code frameStart}.
	 * Coarse-sleeps with {@code parkNanos} up to {@link #BUSY_SPIN_MARGIN} before the target,
	 * then busy-spins the remainder to absorb scheduler oversleep. Returns immediately when
	 * the target has already passed (e.g. a heavy frame, or a VSync frame that already blocked).
	 *
	 * @param frameStart      loop time at the start of this iteration, in seconds
	 * @param targetFrameTime desired total frame duration, in seconds
	 */
	private static void sleepUntilFrameTime(final double frameStart, final double targetFrameTime) {
		final double coarseRemaining = targetFrameTime - (Timer.getTime() - frameStart) - BUSY_SPIN_MARGIN;
		if (coarseRemaining > 0) {
			LockSupport.parkNanos((long) (coarseRemaining * NANOS_PER_SECOND));
		}
		while (Timer.getTime() - frameStart < targetFrameTime) {
			Thread.onSpinWait();
		}
	}
}
