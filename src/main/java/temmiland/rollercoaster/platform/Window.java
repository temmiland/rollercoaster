package temmiland.rollercoaster.platform;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

import temmiland.rollercoaster.platform.io.InputHandler;
import temmiland.rollercoaster.platform.io.InputProcessor;
import org.joml.Vector2f;
import org.lwjgl.glfw.*;

/**
 * Manages the GLFW window lifecycle, input handling, and framebuffer state.
 *
 * <p>Typical usage:
 * <ol>
 *   <li>Call {@link #setGlfwErrorCallbacks()} once before {@code glfwInit()}.</li>
 *   <li>Construct a {@code Window} and call {@link #createWindow()} after GLFW is initialised.</li>
 *   <li>Drive the main loop with {@link #update()} and {@link #swapBuffers()}.</li>
 *   <li>Call {@link #cleanUp()} before {@code glfwTerminate()}.</li>
 * </ol>
 */
public class Window {

	/** Default window width in pixels. */
	private static final int DEFAULT_WIDTH = 640;
	/** Default window height in pixels. */
	private static final int DEFAULT_HEIGHT = 480;
	/** Native GLFW window handle. */
	private long window;

	/** Current window width and height in pixels. */
	private int width, height;
	/** Whether the window is in fullscreen mode. */
	private boolean fullscreen;
	/** Whether the window was resized since the last {@link #update()} call. */
	private boolean hasResized;
	/** Callback that keeps {@link #width} and {@link #height} in sync with GLFW resize events. */
	private GLFWWindowSizeCallback windowSizeCallback;

	/** Event producer: translates raw GLFW callbacks into an {@code InputEvent} queue. */
	private InputHandler inputHandler;
	/** Event consumer: translates queued {@code InputEvent}s into GLFW-independent game actions. */
	private InputProcessor inputProcessor;

	// -------------------------------------------------------------------------
	// Static setup
	// -------------------------------------------------------------------------

	/**
	 * Registers a GLFW error callback that throws an {@link IllegalStateException} on any GLFW
	 * error. Must be called once before {@code glfwInit()}.
	 */
	public static void setGlfwErrorCallbacks() {
		glfwSetErrorCallback(new GLFWErrorCallbackI() {
			@Override
			public void invoke(int error, long description) {
				throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
			}
		});
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Creates a window with the default resolution ({@value DEFAULT_WIDTH}×{@value DEFAULT_HEIGHT}).
	 */
	public Window() {
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Creates a window with the given initial resolution.
	 *
	 * @param cWidth  desired window width in pixels
	 * @param cHeight desired window height in pixels
	 */
	public Window(int cWidth, int cHeight) {
		width = cWidth;
		height = cHeight;
		setFullscreen(false);
		hasResized = false;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Creates the native GLFW window, registers all callbacks, and makes the OpenGL context
	 * current on the calling thread.
	 *
	 * <p>If {@link #fullscreen} is {@code true} the window is created on the primary monitor;
	 * otherwise it is centred on the primary monitor. After creation, {@link #width} and
	 * {@link #height} are updated with the actual values reported by GLFW, which may differ from
	 * the requested size on platforms such as macOS.
	 *
	 * @throws IllegalStateException if GLFW fails to create the window
	 */
	public void createWindow() {
		final long monitor;
		if (fullscreen) {
			monitor = glfwGetPrimaryMonitor();
		} else {
			monitor = 0;
		}

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		window = glfwCreateWindow(width, height, "A rollercoaster game", monitor, 0);

		if (window == 0) {
			throw new IllegalStateException("Failed to create window!");
		}

		if (!fullscreen) {
			final GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(window, (vid.width() - width) / 2, (vid.height() - height) / 2);
		}

		// Register callbacks before glfwShowWindow so that any initial resize events
		// dispatched by the OS (e.g. the macOS window server) are not lost.
		inputHandler = new InputHandler();
		inputHandler.registerCallbacks(window);
		inputProcessor = new InputProcessor();
		setLocalCallbacks();

		glfwShowWindow(window);
		glfwMakeContextCurrent(window);

		// Query the actual window size from GLFW — on macOS this may differ from the
		// requested size due to system constraints.
		final int[] w = new int[1];
		final int[] h = new int[1];
		glfwGetWindowSize(window, w, h);
		this.width  = w[0];
		this.height = h[0];
	}

	/**
	 * Releases all callbacks and native window resources.
	 * Must be called before {@code glfwTerminate()}.
	 */
	public void cleanUp() {
		// Detach and free the InputHandler callbacks first to avoid a double-free
		// when glfwFreeCallbacks() runs afterwards.
		inputHandler.free();
		glfwFreeCallbacks(window);
	}

	// -------------------------------------------------------------------------
	// Main loop
	// -------------------------------------------------------------------------

	/**
	 * Resets the {@link #hasResized} flag and polls GLFW for pending events, which fires
	 * registered callbacks and populates the input-event queue. The queue is then drained
	 * by the {@link InputProcessor} to derive game-level actions.
	 */
	public void update() {
		hasResized = false;
		glfwPollEvents();
		inputProcessor.processEvents(inputHandler.getEventQueue());
	}

	/**
	 * Swaps the front and back buffers to present the rendered frame.
	 * Call once per frame after all rendering commands have been issued.
	 */
	public void swapBuffers() {
		glfwSwapBuffers(window);
	}

	// -------------------------------------------------------------------------
	// Window control
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the user or OS has requested the window to close.
	 *
	 * @return {@code true} when the close flag is set
	 */
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}

	/**
	 * Signals GLFW that the window should close on the next {@link #shouldClose()} check.
	 */
	public void requestClose() {
		glfwSetWindowShouldClose(window, true);
	}

	/**
	 * Updates the window title. Has no effect if the native window has not been created yet.
	 *
	 * @param cTitle the new window title
	 */
	public void setTitle(String cTitle) {
		if (window != 0) {
			glfwSetWindowTitle(window, cTitle);
		}
	}

	/**
	 * Sets the fullscreen mode. Takes effect the next time {@link #createWindow()} is called.
	 *
	 * @param cFullscreen {@code true} to request fullscreen, {@code false} for windowed mode
	 */
	public void setFullscreen(boolean cFullscreen) {
		this.fullscreen = cFullscreen;
	}

	/**
	 * Returns whether the window is in fullscreen mode.
	 *
	 * @return {@code true} if the window is in fullscreen mode
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}

	// -------------------------------------------------------------------------
	// Geometry
	// -------------------------------------------------------------------------

	/**
	 * Returns the current window width in pixels.
	 *
	 * @return window width in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the current window height in pixels.
	 *
	 * @return window height in pixels
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns {@code true} if the window was resized since the last {@link #update()} call.
	 *
	 * @return {@code true} if a resize occurred in the current frame
	 */
	public boolean hasResized() {
		return hasResized;
	}

	/**
	 * Returns the framebuffer width in pixels.
	 * On HiDPI/Retina displays this is typically a multiple of {@link #getWidth()}.
	 *
	 * @return framebuffer width in pixels
	 */
	public int getFramebufferWidth() {
		final int[] w = new int[1];
		glfwGetFramebufferSize(window, w, null);
		return w[0];
	}

	/**
	 * Returns the framebuffer height in pixels.
	 * On HiDPI/Retina displays this is typically a multiple of {@link #getHeight()}.
	 *
	 * @return framebuffer height in pixels
	 */
	public int getFramebufferHeight() {
		final int[] h = new int[1];
		glfwGetFramebufferSize(window, null, h);
		return h[0];
	}

	// -------------------------------------------------------------------------
	// Input
	// -------------------------------------------------------------------------

	/**
	 * Returns the current mouse position in GUI coordinates, where the window centre is the
	 * origin and the Y-axis points upward.
	 *
	 * @return mouse position transformed into GUI space
	 */
	public Vector2f getMousePosition() {
		return inputProcessor.getMousePosition(width, height);
	}

	/**
	 * Returns the {@link InputProcessor} that translates raw input events into GLFW-independent
	 * game actions.
	 *
	 * @return the active {@link InputProcessor}
	 */
	public InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	/**
	 * Registers the window-size callback that updates {@link #width}, {@link #height}, and
	 * {@link #hasResized} whenever GLFW reports a resize event.
	 */
	private void setLocalCallbacks() {
		windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long argWindow, int argWidth, int argHeight) {
				width = argWidth;
				height = argHeight;
				hasResized = true;
			}
		};

		glfwSetWindowSizeCallback(window, windowSizeCallback);
	}

}
