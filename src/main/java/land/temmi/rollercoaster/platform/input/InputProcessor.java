package land.temmi.rollercoaster.platform.input;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Processes raw {@link InputEvent}s from the queue and exposes the input state for the current
 * tick. Must be called once per tick, before game states read any input.
 */
public class InputProcessor {

	/** Currently held keyboard keys as GLFW key codes. */
	private final Set<Integer> heldKeys = new HashSet<>();
	/** Keys pressed for the first time in the current tick (KEY_DOWN events). */
	private final Set<Integer> pressedThisTick = new HashSet<>();
	/** Currently held mouse buttons as GLFW mouse-button indices. */
	private final Set<Integer> heldMouseButtons = new HashSet<>();
	/** Mouse buttons pressed for the first time in the current tick. */
	private final Set<Integer> pressedMouseButtonsThisTick = new HashSet<>();
	/** Unicode code points typed in the current tick (for text-input widgets). */
	private final List<Integer> typedCodepoints = new ArrayList<>();

	/** Last known cursor X position in GLFW screen coordinates (origin at top-left). */
	private double rawMouseX;
	/** Last known cursor Y position in GLFW screen coordinates (origin at top-left). */
	private double rawMouseY;
	/** Scroll offset X accumulated in the current tick. */
	private double scrollX;
	/** Scroll offset Y accumulated in the current tick. */
	private double scrollY;

	// -------------------------------------------------------------------------
	// Tick driver
	// -------------------------------------------------------------------------

	/**
	 * Drains the event queue and updates the internal input state.
	 * Must be called exactly once per tick before any entity reads input state.
	 *
	 * @param eventQueue the queue provided by {@link InputHandler}
	 */
	public void processEvents(final Queue<InputEvent> eventQueue) {
		pressedThisTick.clear();
		pressedMouseButtonsThisTick.clear();
		typedCodepoints.clear();
		scrollX = 0;
		scrollY = 0;

		InputEvent event;
		while ((event = eventQueue.poll()) != null) {
			switch (event.getType()) {
				case KEY_DOWN:
					heldKeys.add(event.getKeyCode());
					pressedThisTick.add(event.getKeyCode());
					break;
				case KEY_UP:
					heldKeys.remove(event.getKeyCode());
					break;
				case MOUSE_PRESS:
					heldMouseButtons.add(event.getMouseButton());
					pressedMouseButtonsThisTick.add(event.getMouseButton());
					break;
				case MOUSE_RELEASE:
					heldMouseButtons.remove(event.getMouseButton());
					break;
				case MOUSE_MOVE:
					rawMouseX = event.getMouseX();
					rawMouseY = event.getMouseY();
					break;
				case FOCUS_LOST:
					heldKeys.clear();
					heldMouseButtons.clear();
					pressedMouseButtonsThisTick.clear();
					break;
				case CHAR_TYPED:
					typedCodepoints.add(event.getCodepoint());
					break;
				case SCROLL:
					scrollX += event.getScrollX();
					scrollY += event.getScrollY();
					break;
				default:
					break;
			}
		}
	}

	// -------------------------------------------------------------------------
	// "Was pressed this tick" queries
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the given GLFW key code was pressed for the first time this tick.
	 *
	 * @param keyCode GLFW key code
	 * @return {@code true} only in the tick the key was first pressed
	 */
	public boolean wasPressed(final int keyCode) {
		return pressedThisTick.contains(keyCode);
	}

	/**
	 * Returns {@code true} if the given key was pressed for the first time this tick.
	 * Mouse buttons are correctly routed to the mouse-button state.
	 *
	 * @param key the key to query
	 * @return {@code true} only in the tick the key was first pressed
	 */
	public boolean wasPressed(final Key key) {
		return key.isMouseButton()
				? pressedMouseButtonsThisTick.contains(key.code())
				: pressedThisTick.contains(key.code());
	}

	/**
	 * Returns {@code true} if the given GLFW mouse-button index was pressed for the first time
	 * this tick.
	 *
	 * @param button GLFW mouse-button index (e.g. {@code GLFW_MOUSE_BUTTON_LEFT = 0})
	 * @return {@code true} only in the tick the button was first clicked
	 */
	public boolean wasMouseButtonPressed(final int button) {
		return pressedMouseButtonsThisTick.contains(button);
	}

	// -------------------------------------------------------------------------
	// "Is currently held" queries
	// -------------------------------------------------------------------------

	/**
	 * Returns {@code true} if the given GLFW key code is currently held down.
	 *
	 * @param keyCode GLFW key code
	 * @return {@code true} for as long as the key is held
	 */
	public boolean isHeld(final int keyCode) {
		return heldKeys.contains(keyCode);
	}

	/**
	 * Returns {@code true} if the given key is currently held down.
	 * Mouse buttons are correctly routed to the mouse-button state.
	 *
	 * @param key the key to query
	 * @return {@code true} for as long as the key is held
	 */
	public boolean isHeld(final Key key) {
		return key.isMouseButton()
				? heldMouseButtons.contains(key.code())
				: heldKeys.contains(key.code());
	}

	/**
	 * Returns {@code true} if the given GLFW mouse button is currently held down.
	 *
	 * @param button GLFW mouse-button index (e.g. {@code GLFW_MOUSE_BUTTON_LEFT = 0})
	 * @return {@code true} for as long as the button is held
	 */
	public boolean isMouseButtonDown(final int button) {
		return heldMouseButtons.contains(button);
	}

	// -------------------------------------------------------------------------
	// Mouse position
	// -------------------------------------------------------------------------

	/**
	 * Returns the current mouse position in GUI coordinates, where the window centre is the
	 * origin and the Y-axis points upward.
	 *
	 * @param windowWidth  current window width in pixels
	 * @param windowHeight current window height in pixels
	 * @return mouse position in GUI space
	 */
	public Vector2f getMousePosition(final int windowWidth, final int windowHeight) {
		final float centeredX = (float) rawMouseX - (windowWidth / 2.0f);
		final float centeredY = -((float) rawMouseY - (windowHeight / 2.0f));
		return new Vector2f(centeredX, centeredY);
	}

	// -------------------------------------------------------------------------
	// Text input
	// -------------------------------------------------------------------------

	/**
	 * Returns the list of Unicode code points typed this tick.
	 * Cleared and repopulated by {@link #processEvents} each tick.
	 *
	 * @return code points typed in the current tick
	 */
	public List<Integer> getTypedCodepoints() {
		return typedCodepoints;
	}

	// -------------------------------------------------------------------------
	// Scroll input
	// -------------------------------------------------------------------------

	/**
	 * Returns the accumulated scroll offset on the X axis for the current tick.
	 * Cleared and repopulated by {@link #processEvents} each tick.
	 *
	 * @return scroll offset X in the current tick
	 */
	public double getScrollX() {
		return scrollX;
	}

	/**
	 * Returns the accumulated scroll offset on the Y axis for the current tick.
	 * Cleared and repopulated by {@link #processEvents} each tick.
	 *
	 * @return scroll offset Y in the current tick
	 */
	public double getScrollY() {
		return scrollY;
	}
}
