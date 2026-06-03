package land.temmi.rollercoaster.graphics;

import org.lwjgl.opengl.GL;
import land.temmi.rollercoaster.platform.Window;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Utility class for common OpenGL initialisation steps.
 */
public class Renderer {

	/**
	 * Initialises OpenGL capabilities, enables alpha blending, and sets the viewport
	 * to match the window's framebuffer dimensions.
	 *
	 * @param window the GLFW window whose framebuffer size is used for the viewport
	 */
	public static void initRenderer(final Window window) {
		GL.createCapabilities();
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glViewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
	}
}
