package land.temmi.rollercoaster.gui;

import org.joml.Vector2f;
import land.temmi.rollercoaster.gui.widgets.button.ButtonState;
import land.temmi.rollercoaster.gui.widgets.button.ButtonSkin;
import land.temmi.rollercoaster.gui.widgets.textfield.TextFieldSkin;
import land.temmi.rollercoaster.gui.widgets.textfield.TextField;

import java.util.function.Consumer;

/**
 * Immediate-mode GUI context for the Rollercoaster engine.
 *
 * <p>A screen is written as a single layout method ({@code Consumer<GuiContext>}) that
 * declares widgets once per frame. It is executed in two distinct phases:</p>
 * <ul>
 *   <li>{@link #interact} — called during the logic tick: hit-tests and edge-triggered
 *       clicks are evaluated; {@link #button} returns {@code true} on click.</li>
 *   <li>{@link #render} — called during the render frame: widgets are drawn;
 *       {@link #button} always returns {@code false} so action blocks do not fire twice.</li>
 * </ul>
 *
 * <p>This separation respects the independent tick and render rates of the game loop:
 * mouse "pressed" edges are cleared each tick and must therefore only be evaluated in the
 * interact pass. Widgets carry no cross-frame state (except the caller-owned
 * {@link TextField}), so no widget-ID management is needed.</p>
 */
public final class GuiContext {

	/** Execution phase of the current layout pass. */
	private enum Phase {
		/** Logic tick: evaluate input. */
		INTERACT,
		/** Render frame: draw widgets. */
		RENDER
	}

	/** Game-side draw backend. */
	private final GuiRenderer renderer;
	/** Visual style for buttons. */
	private final ButtonSkin buttonSkin;
	/** Visual style for text fields. */
	private final TextFieldSkin textFieldSkin;

	/** Current execution phase. */
	private Phase phase;
	/** Input backend for the current layout pass. */
	private GuiInput input;
	/** Tick delta for the current interact pass. */
	private double delta;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new {@code GuiContext}.
	 *
	 * @param renderer      the game-side draw backend
	 * @param buttonSkin    the visual style applied to buttons
	 * @param textFieldSkin the visual style applied to text fields
	 */
	public GuiContext(final GuiRenderer renderer, final ButtonSkin buttonSkin,
	                  final TextFieldSkin textFieldSkin) {
		this.renderer      = renderer;
		this.buttonSkin    = buttonSkin;
		this.textFieldSkin = textFieldSkin;
	}

	// -------------------------------------------------------------------------
	// Lifecycle
	// -------------------------------------------------------------------------

	/**
	 * Forwards a window resize to the render backend.
	 *
	 * @param width  new window width in pixels
	 * @param height new window height in pixels
	 */
	public void resize(final int width, final int height) {
		renderer.resize(width, height);
	}

	// -------------------------------------------------------------------------
	// Phase drivers
	// -------------------------------------------------------------------------

	/**
	 * Executes the layout in the interact pass (call from the logic tick).
	 *
	 * @param input  input backend for this tick
	 * @param delta  tick delta in seconds
	 * @param layout  the screen layout method
	 */
	public void interact(final GuiInput input, final double delta, final Consumer<GuiContext> layout) {
		this.phase = Phase.INTERACT;
		this.input = input;
		this.delta = delta;
		layout.accept(this);
	}

	/**
	 * Executes the layout in the render pass (call from the render frame).
	 *
	 * @param input  input backend for this frame (used for hover and pressed visuals)
	 * @param layout  the screen layout method
	 */
	public void render(final GuiInput input, final Consumer<GuiContext> layout) {
		this.phase = Phase.RENDER;
		this.input = input;
		renderer.begin();
		layout.accept(this);
	}

	// -------------------------------------------------------------------------
	// Widgets
	// -------------------------------------------------------------------------

	/**
	 * Declares a button centred on {@code (cx, cy)}.
	 *
	 * @param label button label; may be empty
	 * @param cx    centre X in GUI coordinates
	 * @param cy    centre Y in GUI coordinates
	 * @param hw    half-width
	 * @param hh    half-height
	 * @return {@code true} in the interact pass when the button was clicked this tick;
	 *         always {@code false} in the render pass
	 */
	public boolean button(final String label, final float cx, final float cy,
	                      final float hw, final float hh) {
		final boolean hover = contains(cx, cy, hw, hh, input.mousePosition());

		if (phase == Phase.INTERACT) {
			return hover && input.mousePressed();
		}

		final ButtonState state;
		if (!hover) {
			state = ButtonState.IDLE;
		} else if (input.mouseDown()) {
			state = ButtonState.CLICKED;
		} else {
			state = ButtonState.SELECTED;
		}
		buttonSkin.draw(renderer, label, cx, cy, hw, hh, state);
		return false;
	}

	/**
	 * Declares a full-screen tiled background (visible in the render pass only).
	 * Call first in a layout so it sits behind all other widgets.
	 *
	 * @param textureName the registered texture name (as in {@code Tile.getTexture()})
	 */
	public void background(final String textureName) {
		if (phase == Phase.RENDER) {
			renderer.drawBackground(textureName);
		}
	}

	/**
	 * Declares a centred text label (visible in the render pass only).
	 *
	 * @param text  the text to display
	 * @param cx    centre X in GUI coordinates
	 * @param cy    centre Y in GUI coordinates
	 * @param scale font scale factor
	 * @param r     red channel 0..1
	 * @param g     green channel 0..1
	 * @param b     blue channel 0..1
	 */
	public void label(final String text, final float cx, final float cy, final float scale,
	                  final float r, final float g, final float b) {
		if (phase == Phase.RENDER) {
			renderer.drawText(text, cx, cy, scale, r, g, b, false);
		}
	}

	/**
	 * Declares a single tilesheet tile (visible in the render pass only).
	 *
	 * @param textureName the registered texture name (as in {@code Tile.getTexture()})
	 * @param cx          centre X in GUI coordinates
	 * @param cy          centre Y in GUI coordinates
	 * @param hw          half-width
	 * @param hh          half-height
	 */
	public void sprite(final String textureName, final float cx, final float cy,
	                   final float hw, final float hh) {
		if (phase == Phase.RENDER) {
			renderer.drawTile(textureName, cx, cy, hw, hh);
		}
	}

	/**
	 * Declares a solid-colour rectangle (visible in the render pass only).
	 *
	 * @param cx centre X in GUI coordinates
	 * @param cy centre Y in GUI coordinates
	 * @param hw half-width
	 * @param hh half-height
	 * @param r  red channel 0..1
	 * @param g  green channel 0..1
	 * @param b  blue channel 0..1
	 */
	public void quad(final float cx, final float cy, final float hw, final float hh,
	                 final float r, final float g, final float b) {
		if (phase == Phase.RENDER) {
			renderer.drawQuad(cx, cy, hw, hh, r, g, b);
		}
	}

	/**
	 * Declares a caller-owned text input field.
	 *
	 * @param field the text field instance (owned by the calling state)
	 * @return {@code true} in the interact pass when the confirm key was pressed this tick;
	 *         always {@code false} in the render pass
	 */
	public boolean textField(final TextField field) {
		if (phase == Phase.INTERACT) {
			return field.update(input, delta);
		}
		field.render(renderer, textFieldSkin);
		return false;
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	private static boolean contains(final float cx, final float cy, final float hw, final float hh,
	                                final Vector2f point) {
		return point.x >= cx - hw && point.x <= cx + hw
				&& point.y >= cy - hh && point.y <= cy + hh;
	}
}
