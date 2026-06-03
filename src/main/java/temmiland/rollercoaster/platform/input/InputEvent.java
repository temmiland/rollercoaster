package temmiland.rollercoaster.platform.input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

/**
 * Immutable data-transfer object representing a single raw input event.
 * Created by {@link InputHandler} via GLFW callbacks and consumed by
 * {@link InputProcessor} once per tick.
 */
public final class InputEvent {

	// -------------------------------------------------------------------------
	// Event type
	// -------------------------------------------------------------------------

	/** Discriminator for the kind of input event carried by an {@link InputEvent} instance. */
	public enum Type {
		/** A keyboard key was pressed. */
		KEY_DOWN,
		/** A keyboard key was released. */
		KEY_UP,
		/** A mouse button was pressed. */
		MOUSE_PRESS,
		/** A mouse button was released. */
		MOUSE_RELEASE,
		/** The cursor was moved. */
		MOUSE_MOVE,
		/**
		 * The window lost focus. {@link InputProcessor} clears all held keys and mouse buttons
		 * on receipt so that no keys remain stuck in the pressed state.
		 */
		FOCUS_LOST,
		/**
		 * A printable Unicode character was typed (via the GLFW char callback).
		 * Not fired for non-printable keys such as Backspace or arrow keys.
		 */
		CHAR_TYPED,
		/** The mouse scroll wheel was scrolled. */
		SCROLL
	}

	// -------------------------------------------------------------------------
	// Fields
	// -------------------------------------------------------------------------

	/** The type of this event. */
	private final Type type;
	/** GLFW key code, or {@code GLFW_KEY_UNKNOWN} for non-keyboard events. */
	private final int keyCode;
	/** GLFW mouse button index, or {@code -1} for non-mouse-button events. */
	private final int mouseButton;
	/** Cursor X position in screen coordinates (relevant for mouse events only). */
	private final double mouseX;
	/** Cursor Y position in screen coordinates (relevant for mouse events only). */
	private final double mouseY;
	/** Unicode code point of the typed character (relevant for {@link Type#CHAR_TYPED} only). */
	private final int codepoint;
	/** Scroll offset X (relevant for {@link Type#SCROLL} only). */
	private final double scrollX;
	/** Scroll offset Y (relevant for {@link Type#SCROLL} only). */
	private final double scrollY;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	private InputEvent(
			final Type eventType,
			final int eventKeyCode,
			final int eventMouseButton,
			final double eventMouseX,
			final double eventMouseY,
			final int eventCodepoint,
			final double eventScrollX,
			final double eventScrollY) {
		this.type = eventType;
		this.keyCode = eventKeyCode;
		this.mouseButton = eventMouseButton;
		this.mouseX = eventMouseX;
		this.mouseY = eventMouseY;
		this.codepoint = eventCodepoint;
		this.scrollX = eventScrollX;
		this.scrollY = eventScrollY;
	}

	// -------------------------------------------------------------------------
	// Factory methods
	// -------------------------------------------------------------------------

	/**
	 * Creates a {@link Type#KEY_DOWN} event for the given GLFW key code.
	 *
	 * @param eventKeyCode GLFW key code of the pressed key
	 * @return a new {@code KEY_DOWN} event
	 */
	public static InputEvent keyDown(final int eventKeyCode) {
		return new InputEvent(Type.KEY_DOWN, eventKeyCode, -1, 0, 0, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#KEY_UP} event for the given GLFW key code.
	 *
	 * @param eventKeyCode GLFW key code of the released key
	 * @return a new {@code KEY_UP} event
	 */
	public static InputEvent keyUp(final int eventKeyCode) {
		return new InputEvent(Type.KEY_UP, eventKeyCode, -1, 0, 0, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#MOUSE_PRESS} event.
	 *
	 * @param button       GLFW mouse button index
	 * @param eventMouseX  cursor X at the time of the press
	 * @param eventMouseY  cursor Y at the time of the press
	 * @return a new {@code MOUSE_PRESS} event
	 */
	public static InputEvent mousePress(
			final int button,
			final double eventMouseX,
			final double eventMouseY) {
		return new InputEvent(Type.MOUSE_PRESS, GLFW_KEY_UNKNOWN, button, eventMouseX, eventMouseY, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#MOUSE_RELEASE} event.
	 *
	 * @param button       GLFW mouse button index
	 * @param eventMouseX  cursor X at the time of the release
	 * @param eventMouseY  cursor Y at the time of the release
	 * @return a new {@code MOUSE_RELEASE} event
	 */
	public static InputEvent mouseRelease(
			final int button,
			final double eventMouseX,
			final double eventMouseY) {
		return new InputEvent(Type.MOUSE_RELEASE, GLFW_KEY_UNKNOWN, button, eventMouseX, eventMouseY, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#MOUSE_MOVE} event.
	 *
	 * @param eventMouseX new cursor X position
	 * @param eventMouseY new cursor Y position
	 * @return a new {@code MOUSE_MOVE} event
	 */
	public static InputEvent mouseMove(final double eventMouseX, final double eventMouseY) {
		return new InputEvent(Type.MOUSE_MOVE, GLFW_KEY_UNKNOWN, -1, eventMouseX, eventMouseY, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#FOCUS_LOST} event.
	 *
	 * @return a new {@code FOCUS_LOST} event
	 */
	public static InputEvent focusLost() {
		return new InputEvent(Type.FOCUS_LOST, GLFW_KEY_UNKNOWN, -1, 0, 0, 0, 0, 0);
	}

	/**
	 * Creates a {@link Type#CHAR_TYPED} event for the given Unicode code point.
	 *
	 * @param codepoint Unicode code point of the typed character
	 * @return a new {@code CHAR_TYPED} event
	 */
	public static InputEvent charTyped(final int codepoint) {
		return new InputEvent(Type.CHAR_TYPED, GLFW_KEY_UNKNOWN, -1, 0, 0, codepoint, 0, 0);
	}

	/**
	 * Creates a {@link Type#SCROLL} event.
	 *
	 * @param eventScrollX scroll offset on the X axis
	 * @param eventScrollY scroll offset on the Y axis
	 * @return a new {@code SCROLL} event
	 */
	public static InputEvent scroll(final double eventScrollX, final double eventScrollY) {
		return new InputEvent(Type.SCROLL, GLFW_KEY_UNKNOWN, -1, 0, 0, 0, eventScrollX, eventScrollY);
	}

	// -------------------------------------------------------------------------
	// Getters
	// -------------------------------------------------------------------------

	/**
	 * Returns the type of this event.
	 *
	 * @return the event type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the GLFW key code, or {@code GLFW_KEY_UNKNOWN} for non-keyboard events.
	 *
	 * @return GLFW key code
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * Returns the GLFW mouse button index, or {@code -1} for non-mouse-button events.
	 *
	 * @return GLFW mouse button index
	 */
	public int getMouseButton() {
		return mouseButton;
	}

	/**
	 * Returns the cursor X position in screen coordinates at the time of the event.
	 *
	 * @return cursor X in screen coordinates
	 */
	public double getMouseX() {
		return mouseX;
	}

	/**
	 * Returns the cursor Y position in screen coordinates at the time of the event.
	 *
	 * @return cursor Y in screen coordinates
	 */
	public double getMouseY() {
		return mouseY;
	}

	/**
	 * Returns the Unicode code point of the typed character.
	 * Only meaningful for {@link Type#CHAR_TYPED} events.
	 *
	 * @return Unicode code point
	 */
	public int getCodepoint() {
		return codepoint;
	}

	/**
	 * Returns the scroll offset on the X axis.
	 * Only meaningful for {@link Type#SCROLL} events.
	 *
	 * @return scroll offset X
	 */
	public double getScrollX() {
		return scrollX;
	}

	/**
	 * Returns the scroll offset on the Y axis.
	 * Only meaningful for {@link Type#SCROLL} events.
	 *
	 * @return scroll offset Y
	 */
	public double getScrollY() {
		return scrollY;
	}

	// -------------------------------------------------------------------------
	// Object
	// -------------------------------------------------------------------------

	@Override
	public String toString() {
		return "InputEvent{type=" + type
				+ ", keyCode=" + keyCode
				+ ", mouseButton=" + mouseButton
				+ ", mouseX=" + mouseX
				+ ", mouseY=" + mouseY
				+ ", scrollX=" + scrollX
				+ ", scrollY=" + scrollY + "}";
	}
}
