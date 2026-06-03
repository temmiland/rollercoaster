package land.temmi.rollercoaster.graphics;

import land.temmi.rollercoaster.util.Timer;

/**
 * A frame-based sprite animation.
 *
 * <p>Cycles through an ordered sequence of {@link Sprite}s at a fixed frame rate.
 * The current frame's UV is read from the shared {@link TextureAtlas} at render
 * time via {@link #getUV(TextureAtlas)}. Can be paused and resumed.</p>
 */
public class Animation {

	/** The ordered sprite frames. */
	private final Sprite[] frames;
	/** Current frame index. */
	private int texturePointer;

	/** Accumulated time since the last frame advance. */
	private double elapsedTime;
	/** Timestamp of the last tick. */
	private double lastTime;
	/** Frame interval in seconds (1 / fps). */
	private final double frameInterval;
	/** Whether this animation is paused. */
	private boolean paused;

	/**
	 * Creates an animation from an explicit frame sequence.
	 *
	 * @param frames the sprites to cycle through, in order
	 * @param fps    playback rate in frames per second
	 */
	public Animation(final Sprite[] frames, final int fps) {
		this.frames        = frames;
		this.frameInterval = 1.0 / fps;
		this.lastTime      = Timer.getTime();
	}

	/** Pauses playback; the current frame stays on screen. */
	public void pause() {
		this.paused = true;
	}

	/** Resumes playback and resets the frame timing baseline. */
	public void resume() {
		this.paused = false;
		this.lastTime = Timer.getTime();
	}

	/**
	 * Advances the animation (unless paused) and returns the UV coordinates
	 * for the current frame.
	 *
	 * @param atlas the shared texture atlas
	 * @return {@code float[]{u0, v0, u1, v1}} for the current frame, or {@code null}
	 *         if the sprite is not in the atlas
	 */
	public float[] getUV(final TextureAtlas atlas) {
		if (!paused) {
			final double now = Timer.getTime();
			elapsedTime += now - lastTime;
			lastTime = now;

			if (elapsedTime >= frameInterval) {
				elapsedTime = 0;
				texturePointer = (texturePointer + 1) % frames.length;
			}
		}
		return atlas.getUV(frames[texturePointer].getTexture());
	}
}
