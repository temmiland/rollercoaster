package temmiland.rollercoaster.platform.io;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_APOSTROPHE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSLASH;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_COMMA;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F10;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DECIMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_DIVIDE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_EQUAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_MULTIPLY;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_BRACKET;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_MENU;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_NUM_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAUSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PERIOD;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PRINT_SCREEN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_BRACKET;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SCROLL_LOCK;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SEMICOLON;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * Type-safe abstraction over GLFW key codes and mouse buttons.
 *
 * <p>Game states and GUI components can use {@code Key} enum values instead of
 * raw GLFW integer constants:
 * <pre>
 *   if (input.wasPressed(Key.ESCAPE)) { ... }
 *   if (input.isHeld(Key.W))          { ... }
 * </pre>
 *
 * <p>{@link InputProcessor#wasPressed(Key)}, {@link InputProcessor#isHeld(Key)} and related
 * methods accept a {@code Key} parameter and delegate internally to the corresponding
 * GLFW value ({@link #code()}).
 */
public enum Key {

	// --- Unknown ---
	UNKNOWN(GLFW_KEY_UNKNOWN),

	// --- Letters ---
	A(GLFW_KEY_A), B(GLFW_KEY_B), C(GLFW_KEY_C), D(GLFW_KEY_D),
	E(GLFW_KEY_E), F(GLFW_KEY_F), G(GLFW_KEY_G), H(GLFW_KEY_H),
	I(GLFW_KEY_I), J(GLFW_KEY_J), K(GLFW_KEY_K), L(GLFW_KEY_L),
	M(GLFW_KEY_M), N(GLFW_KEY_N), O(GLFW_KEY_O), P(GLFW_KEY_P),
	Q(GLFW_KEY_Q), R(GLFW_KEY_R), S(GLFW_KEY_S), T(GLFW_KEY_T),
	U(GLFW_KEY_U), V(GLFW_KEY_V), W(GLFW_KEY_W), X(GLFW_KEY_X),
	Y(GLFW_KEY_Y), Z(GLFW_KEY_Z),

	// --- Digits (main keyboard) ---
	NUM_0(GLFW_KEY_0), NUM_1(GLFW_KEY_1), NUM_2(GLFW_KEY_2), NUM_3(GLFW_KEY_3),
	NUM_4(GLFW_KEY_4), NUM_5(GLFW_KEY_5), NUM_6(GLFW_KEY_6), NUM_7(GLFW_KEY_7),
	NUM_8(GLFW_KEY_8), NUM_9(GLFW_KEY_9),

	// --- Function keys ---
	F1(GLFW_KEY_F1),  F2(GLFW_KEY_F2),  F3(GLFW_KEY_F3),  F4(GLFW_KEY_F4),
	F5(GLFW_KEY_F5),  F6(GLFW_KEY_F6),  F7(GLFW_KEY_F7),  F8(GLFW_KEY_F8),
	F9(GLFW_KEY_F9),  F10(GLFW_KEY_F10), F11(GLFW_KEY_F11), F12(GLFW_KEY_F12),

	// --- Control / Navigation ---
	ESCAPE(GLFW_KEY_ESCAPE),
	ENTER(GLFW_KEY_ENTER),
	TAB(GLFW_KEY_TAB),
	BACKSPACE(GLFW_KEY_BACKSPACE),
	INSERT(GLFW_KEY_INSERT),
	DELETE(GLFW_KEY_DELETE),
	HOME(GLFW_KEY_HOME),
	END(GLFW_KEY_END),
	PAGE_UP(GLFW_KEY_PAGE_UP),
	PAGE_DOWN(GLFW_KEY_PAGE_DOWN),

	// --- Arrow keys ---
	UP(GLFW_KEY_UP),
	DOWN(GLFW_KEY_DOWN),
	LEFT(GLFW_KEY_LEFT),
	RIGHT(GLFW_KEY_RIGHT),

	// --- Modifiers ---
	LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
	RIGHT_SHIFT(GLFW_KEY_RIGHT_SHIFT),
	LEFT_CONTROL(GLFW_KEY_LEFT_CONTROL),
	RIGHT_CONTROL(GLFW_KEY_RIGHT_CONTROL),
	LEFT_ALT(GLFW_KEY_LEFT_ALT),
	RIGHT_ALT(GLFW_KEY_RIGHT_ALT),
	LEFT_SUPER(GLFW_KEY_LEFT_SUPER),
	RIGHT_SUPER(GLFW_KEY_RIGHT_SUPER),

	// --- Special keys ---
	SPACE(GLFW_KEY_SPACE),
	CAPS_LOCK(GLFW_KEY_CAPS_LOCK),
	SCROLL_LOCK(GLFW_KEY_SCROLL_LOCK),
	NUM_LOCK(GLFW_KEY_NUM_LOCK),
	PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN),
	PAUSE(GLFW_KEY_PAUSE),
	MENU(GLFW_KEY_MENU),

	// --- Punctuation ---
	APOSTROPHE(GLFW_KEY_APOSTROPHE),
	COMMA(GLFW_KEY_COMMA),
	MINUS(GLFW_KEY_MINUS),
	PERIOD(GLFW_KEY_PERIOD),
	SLASH(GLFW_KEY_SLASH),
	SEMICOLON(GLFW_KEY_SEMICOLON),
	EQUAL(GLFW_KEY_EQUAL),
	LEFT_BRACKET(GLFW_KEY_LEFT_BRACKET),
	BACKSLASH(GLFW_KEY_BACKSLASH),
	RIGHT_BRACKET(GLFW_KEY_RIGHT_BRACKET),
	GRAVE_ACCENT(GLFW_KEY_GRAVE_ACCENT),

	// --- Numpad ---
	KP_0(GLFW_KEY_KP_0), KP_1(GLFW_KEY_KP_1), KP_2(GLFW_KEY_KP_2), KP_3(GLFW_KEY_KP_3),
	KP_4(GLFW_KEY_KP_4), KP_5(GLFW_KEY_KP_5), KP_6(GLFW_KEY_KP_6), KP_7(GLFW_KEY_KP_7),
	KP_8(GLFW_KEY_KP_8), KP_9(GLFW_KEY_KP_9),
	KP_DECIMAL(GLFW_KEY_KP_DECIMAL),
	KP_DIVIDE(GLFW_KEY_KP_DIVIDE),
	KP_MULTIPLY(GLFW_KEY_KP_MULTIPLY),
	KP_SUBTRACT(GLFW_KEY_KP_SUBTRACT),
	KP_ADD(GLFW_KEY_KP_ADD),
	KP_ENTER(GLFW_KEY_KP_ENTER),
	KP_EQUAL(GLFW_KEY_KP_EQUAL),

	// --- Mouse buttons ---
	MOUSE_LEFT(GLFW_MOUSE_BUTTON_LEFT),
	MOUSE_RIGHT(GLFW_MOUSE_BUTTON_RIGHT),
	MOUSE_MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE);

	/** Underlying GLFW integer code for this key or mouse button. */
	private final int glfwCode;

	Key(final int glfwCode) {
		this.glfwCode = glfwCode;
	}

	/**
	 * Returns the underlying GLFW code for this key or mouse button.
	 *
	 * @return GLFW key or mouse-button code
	 */
	public int code() {
		return glfwCode;
	}

	/**
	 * Returns {@code true} if this constant represents a mouse button.
	 * Used internally by {@link InputProcessor} to route queries to the correct
	 * state store ({@code heldKeys} vs. {@code heldMouseButtons}).
	 *
	 * @return {@code true} for {@link #MOUSE_LEFT}, {@link #MOUSE_RIGHT}, and {@link #MOUSE_MIDDLE}
	 */
	public boolean isMouseButton() {
		return this == MOUSE_LEFT || this == MOUSE_RIGHT || this == MOUSE_MIDDLE;
	}
}
