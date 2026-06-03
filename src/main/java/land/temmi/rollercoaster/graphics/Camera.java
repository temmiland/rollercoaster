package land.temmi.rollercoaster.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Orthographic 2D camera with a virtual, resolution-independent viewport.
 *
 * <p>The viewport keeps a fixed reference height and derives its width from the
 * current aspect ratio, so the world scales proportionally on window resizes.
 * The camera also stores the previous-tick position to support smooth render
 * interpolation, and reuses scratch matrices/vectors to avoid per-frame
 * allocations.</p>
 */
public class Camera {

	/** The current camera position. */
	private Vector3f position;
	/** Camera position at the start of the last tick — used for render interpolation. */
	private Vector3f prevPosition;
	/** The projection matrix. */
	private Matrix4f projection;
	/** Reusable scratch matrix for getProjection() — avoids per-frame allocation. */
	private final Matrix4f projectionScratch = new Matrix4f();
	/** Reusable scratch matrix for getProjection(alpha) — avoids per-frame allocation. */
	private final Matrix4f projectionAlphaScratch = new Matrix4f();
	/** Reusable scratch vector for the lerp in getProjection(alpha) — avoids per-frame allocation. */
	private final Vector3f interpolatedScratch = new Vector3f();

	/** Reference height for proportional scaling on resize. */
	private final int referenceHeight;
	/** Virtual viewport width (derived from reference height + aspect ratio). */
	private int virtualWidth;
	/** Virtual viewport height (= referenceHeight). */
	private int virtualHeight;

	/**
	 * Creates a new camera with the given viewport size.
	 * @param width  viewport width in pixels
	 * @param height viewport height in pixels
	 */
	public Camera(int width, int height) {
		position = new Vector3f(0, 0, 0);
		prevPosition = new Vector3f(0, 0, 0);
		referenceHeight = height;
		setProjection(width, height);
	}

	/**
	 * Recomputes the projection matrix for the given viewport size.
	 * @param width  viewport width in pixels
	 * @param height viewport height in pixels
	 */
	public void setProjection(int width, int height) {
		final float aspect = (float) width / height;
		virtualHeight = referenceHeight;
		virtualWidth  = (int) (referenceHeight * aspect);
		projection = new Matrix4f().setOrtho2D(
				-virtualWidth / 2f, virtualWidth / 2f,
				-virtualHeight / 2f, virtualHeight / 2f);
	}

	/** @return virtual viewport width (in world-pixel units) */
	public int getVirtualWidth() {
		return virtualWidth;
	}

	/** @return virtual viewport height (in world-pixel units) */
	public int getVirtualHeight() {
		return virtualHeight;
	}

	/** @return the current camera position (mutable, backing instance) */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Sets the camera position from a vector.
	 * @param position the new position
	 */
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}

	/**
	 * Sets the camera position to the given x/y values (z stays unchanged).
	 * @param x new X position
	 * @param y new Y position
	 */
	public void setPosition(final float x, final float y) {
		this.position.set(x, y, this.position.z);
	}

	/**
	 * Adds the given offset to the camera position.
	 * @param position the offset to add
	 */
	public void addPosition(Vector3f position) {
		this.position.add(position);
	}

	/**
	 * Stores the current position as prevPosition.
	 * Must be called at the start of every tick, before an entity updates the camera.
	 */
	public void savePrevPosition() {
		prevPosition.set(position);
	}

	/**
	 * Returns the untransformed projection matrix (without camera translation).
	 * @return the raw projection matrix
	 */
	public Matrix4f getUntransformedProjection() {
		return projection;
	}

	/**
	 * Returns the projection matrix with the current camera position.
	 * Only use where no interpolation is needed (e.g. GUI).
	 * @return the projection matrix
	 */
	public Matrix4f getProjection() {
		return projection.translate(position, projectionScratch);
	}

	/**
	 * Returns the projection matrix with the camera position interpolated between
	 * the last and current tick. Use for all in-world rendering.
	 * @param alpha interpolation factor 0.0–1.0 (from the game loop)
	 * @return the interpolated projection matrix
	 */
	public Matrix4f getProjection(final float alpha) {
		prevPosition.lerp(position, alpha, interpolatedScratch);
		return projection.translate(interpolatedScratch, projectionAlphaScratch);
	}
}
