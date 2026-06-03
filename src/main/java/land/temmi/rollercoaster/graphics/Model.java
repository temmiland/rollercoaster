package land.temmi.rollercoaster.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

/**
 * An indexed, textured mesh uploaded to the GPU as OpenGL buffer objects.
 *
 * <p>Owns a vertex, texture-coordinate and index buffer and renders them as
 * triangles. Implements {@link AutoCloseable} so the GPU buffers are released
 * deterministically.</p>
 */
public class Model implements AutoCloseable {

	// OpenGL attribute constants
	/** Attribute index for vertices. */
	private static final int VERTICES_ATTRIB_INDEX = 0;
	/** Attribute index for texture coordinates. */
	private static final int TEXTURES_ATTRIB_INDEX = 1;
	/** Size of the vertex data (x, y, z). */
	private static final int VERTEX_SIZE = 3;
	/** Size of the texture coordinate data (u, v). */
	private static final int TEXTURE_COORD_SIZE = 2;
	/** Whether the data should be normalized. */
	private static final boolean NORMALIZED = false;
	/** Stride between two consecutive vertex entries. */
	private static final int STRIDE = 0;
	/** Offset to the first vertex entry. */
	private static final int OFFSET = 0;

	/** Number of elements to draw. */
	private int drawCount;
	/** OpenGL vertex buffer object. */
	private int vertexObject;
	/** OpenGL texture coordinate buffer object. */
	private int textureCoordObject;
	/** OpenGL index buffer object. */
	private int indexObject;

	/**
	 * Uploads the given geometry to the GPU.
	 *
	 * @param vertices      flat array of vertex positions (x, y, z per vertex)
	 * @param textureCoords flat array of texture coordinates (u, v per vertex)
	 * @param indices       triangle indices into the vertex array
	 */
	public Model(float[] vertices, float[] textureCoords, int[] indices) {
		drawCount = indices.length;

		vertexObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexObject);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(vertices), GL_STATIC_DRAW);

		textureCoordObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, textureCoordObject);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(textureCoords), GL_STATIC_DRAW);

		indexObject = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexObject);

		final IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
		buffer.put(indices);
		buffer.flip();

		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	/** Renders the mesh as triangles using its buffers. */
	public void render() {
		glEnableVertexAttribArray(VERTICES_ATTRIB_INDEX);
		glEnableVertexAttribArray(TEXTURES_ATTRIB_INDEX);

		glBindBuffer(GL_ARRAY_BUFFER, vertexObject);
		glVertexAttribPointer(VERTICES_ATTRIB_INDEX, VERTEX_SIZE, GL_FLOAT, NORMALIZED, STRIDE, OFFSET);

		glBindBuffer(GL_ARRAY_BUFFER, textureCoordObject);
		glVertexAttribPointer(TEXTURES_ATTRIB_INDEX, TEXTURE_COORD_SIZE, GL_FLOAT, NORMALIZED, STRIDE, OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexObject);
		glDrawElements(GL_TRIANGLES, drawCount, GL_UNSIGNED_INT, OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glDisableVertexAttribArray(VERTICES_ATTRIB_INDEX);
		glDisableVertexAttribArray(TEXTURES_ATTRIB_INDEX);
	}

	/** Releases the GPU buffer objects owned by this model. */
	@Override
	public void close() {
		glDeleteBuffers(vertexObject);
		glDeleteBuffers(textureCoordObject);
		glDeleteBuffers(indexObject);
	}

	/**
	 * Wraps a float array in a flipped, ready-to-upload {@link FloatBuffer}.
	 *
	 * @param data the source data
	 * @return a flipped float buffer containing {@code data}
	 */
	private FloatBuffer createBuffer(float[] data) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
}
