package land.temmi.rollercoaster.platform.input;

import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;

/**
 * Registers GLFW callbacks and writes {@link InputEvent} objects into a queue.
 * The queue is drained once per tick by {@link InputProcessor}.
 *
 * <p>GLFW callbacks run on the same thread as {@code glfwPollEvents()}, so no
 * race conditions arise in practice. {@link ConcurrentLinkedQueue} is used
 * nonetheless to keep the contract thread-safe for future extensions.</p>
 */
public class InputHandler {

	/** Thread-safe queue of raw GLFW input events. */
	private final ConcurrentLinkedQueue<InputEvent> eventQueue = new ConcurrentLinkedQueue<>();

	/** GLFW window handle, retained so callbacks can be detached in {@link #free()}. */
	private long windowHandle;

	/** Keyboard callback — reference must be kept alive to prevent GC collection. */
	private GLFWKeyCallback keyCallback;
	/** Mouse-button callback — reference must be kept alive to prevent GC collection. */
	private GLFWMouseButtonCallback mouseButtonCallback;
	/** Cursor-position callback — reference must be kept alive to prevent GC collection. */
	private GLFWCursorPosCallback cursorPosCallback;
	/**
	 * Window-focus callback — enqueues {@link InputEvent.Type#FOCUS_LOST} when the window loses
	 * focus so that {@link InputProcessor} can clear stuck keys.
	 */
	private GLFWWindowFocusCallback windowFocusCallback;
	/**
	 * Char callback — delivers Unicode code points for printable characters (text input widgets).
	 * Not fired for non-printable keys such as Backspace.
	 */
	private GLFWCharCallback charCallback;
	/** Scroll callback — enqueues scroll events when the mouse wheel is scrolled. */
	private GLFWScrollCallback scrollCallback;

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Registers all GLFW callbacks for the given window handle.
	 * Must be called after {@code glfwCreateWindow()}.
	 *
	 * @param handle native GLFW window handle
	 */
	public void registerCallbacks(final long handle) {
		this.windowHandle = handle;

		keyCallback = GLFWKeyCallback.create((win, key, scancode, action, mods) -> {
			if (action == GLFW_PRESS) {
				eventQueue.add(InputEvent.keyDown(key));
			} else if (action == GLFW_RELEASE) {
				eventQueue.add(InputEvent.keyUp(key));
			}
			// GLFW_REPEAT is ignored — held-key state is tracked via the heldKeys set
			// in InputProcessor instead.
		});

		mouseButtonCallback = GLFWMouseButtonCallback.create((win, button, action, mods) -> {
			final double[] xArr = new double[1];
			final double[] yArr = new double[1];
			glfwGetCursorPos(win, xArr, yArr);
			if (action == GLFW_PRESS) {
				eventQueue.add(InputEvent.mousePress(button, xArr[0], yArr[0]));
			} else if (action == GLFW_RELEASE) {
				eventQueue.add(InputEvent.mouseRelease(button, xArr[0], yArr[0]));
			}
		});

		cursorPosCallback = GLFWCursorPosCallback.create((win, x, y) ->
			eventQueue.add(InputEvent.mouseMove(x, y))
		);

		windowFocusCallback = GLFWWindowFocusCallback.create((win, focused) -> {
			if (!focused) {
				eventQueue.add(InputEvent.focusLost());
			}
		});

		charCallback = GLFWCharCallback.create((win, codepoint) ->
			eventQueue.add(InputEvent.charTyped(codepoint))
		);

		scrollCallback = GLFWScrollCallback.create((win, scrollX, scrollY) ->
			eventQueue.add(InputEvent.scroll(scrollX, scrollY))
		);

		glfwSetKeyCallback(handle, keyCallback);
		glfwSetMouseButtonCallback(handle, mouseButtonCallback);
		glfwSetCursorPosCallback(handle, cursorPosCallback);
		glfwSetWindowFocusCallback(handle, windowFocusCallback);
		glfwSetCharCallback(handle, charCallback);
		glfwSetScrollCallback(handle, scrollCallback);
	}

	/**
	 * Detaches all callbacks from GLFW and releases the underlying LWJGL wrapper objects.
	 *
	 * <p>Must be called before {@code glfwFreeCallbacks(window)}, otherwise
	 * {@code glfwFreeCallbacks} would free the same callbacks a second time (double-free).</p>
	 */
	public void free() {
		// Detach from GLFW first so glfwFreeCallbacks() does not free them again.
		glfwSetKeyCallback(windowHandle, null);
		glfwSetMouseButtonCallback(windowHandle, null);
		glfwSetCursorPosCallback(windowHandle, null);
		glfwSetWindowFocusCallback(windowHandle, null);
		glfwSetCharCallback(windowHandle, null);
		glfwSetScrollCallback(windowHandle, null);

		if (keyCallback != null) {
			keyCallback.free();
			keyCallback = null;
		}
		if (mouseButtonCallback != null) {
			mouseButtonCallback.free();
			mouseButtonCallback = null;
		}
		if (cursorPosCallback != null) {
			cursorPosCallback.free();
			cursorPosCallback = null;
		}
		if (windowFocusCallback != null) {
			windowFocusCallback.free();
			windowFocusCallback = null;
		}
		if (charCallback != null) {
			charCallback.free();
			charCallback = null;
		}
		if (scrollCallback != null) {
			scrollCallback.free();
			scrollCallback = null;
		}
	}

	// -------------------------------------------------------------------------
	// Queue access
	// -------------------------------------------------------------------------

	/**
	 * Returns the raw event queue. Drained by {@link InputProcessor#processEvents(Queue)}
	 * once per tick.
	 *
	 * @return the live event queue
	 */
	public Queue<InputEvent> getEventQueue() {
		return eventQueue;
	}
}
