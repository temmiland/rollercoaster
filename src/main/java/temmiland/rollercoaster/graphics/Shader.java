package temmiland.rollercoaster.graphics;

import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compiles, links and binds a GLSL shader program (vertex + fragment).
 *
 * <p>The shader sources are loaded from the classpath under
 * {@code /shaders/<name>.vs} and {@code /shaders/<name>.fs}. Compilation, linking
 * and validation failures are logged and raised as runtime exceptions. Provides
 * typed uniform setters and implements {@link AutoCloseable} to release the GL
 * objects.</p>
 */
public class Shader implements AutoCloseable {

	/** Logger of this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Shader.class);

	// Shader constants
	/** OpenGL success code. */
	private static final int GL_SUCCESS = 1;
	/** Attribute location for vertices. */
	private static final int VERTICES_ATTRIB_LOCATION = 0;
	/** Attribute location for texture coordinates. */
	private static final int TEXTURES_ATTRIB_LOCATION = 1;
	/** Invalid uniform location value. */
	private static final int INVALID_UNIFORM_LOCATION = -1;
	/** Buffer size for matrices (4×4 = 16 floats). */
	private static final int MATRIX_BUFFER_SIZE = 16;

	/** OpenGL program object id. */
	private int programObject;
	/** OpenGL vertex shader object id. */
	private int vertexShaderObject;
	/** OpenGL fragment shader object id. */
	private int fragmentShaderObject;

	/**
	 * Loads, compiles, links and validates the shader program.
	 *
	 * @param filename the shader base name (resolved to {@code .vs}/{@code .fs})
	 * @throws RuntimeException if compilation, linking or validation fails
	 */
	public Shader(String filename) {
		programObject = glCreateProgram();

		vertexShaderObject = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderObject, readFile(filename + ".vs"));
		glCompileShader(vertexShaderObject);
		if (glGetShaderi(vertexShaderObject, GL_COMPILE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetShaderInfoLog(vertexShaderObject);
			LOGGER.error("Vertex shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile vertex shader: " + errorLog);
		}

		fragmentShaderObject = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderObject, readFile(filename + ".fs"));
		glCompileShader(fragmentShaderObject);
		if (glGetShaderi(fragmentShaderObject, GL_COMPILE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetShaderInfoLog(fragmentShaderObject);
			LOGGER.error("Fragment shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile fragment shader: " + errorLog);
		}
		glAttachShader(programObject, vertexShaderObject);
		glAttachShader(programObject, fragmentShaderObject);

		glBindAttribLocation(programObject, VERTICES_ATTRIB_LOCATION, "vertices");
		glBindAttribLocation(programObject, TEXTURES_ATTRIB_LOCATION, "textures");

		glLinkProgram(programObject);
		if (glGetProgrami(programObject, GL_LINK_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetProgramInfoLog(programObject);
			LOGGER.error("Shader program linking failed: {}", errorLog);
			throw new RuntimeException("Failed to link shader program: " + errorLog);
		}
		glValidateProgram(programObject);
		if (glGetProgrami(programObject, GL_VALIDATE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetProgramInfoLog(programObject);
			LOGGER.error("Shader program validation failed: {}", errorLog);
			throw new RuntimeException("Failed to validate shader program: " + errorLog);
		}
	}

	/** Binds this shader program as the active one. */
	public void bind() {
		glUseProgram(programObject);
	}

	/**
	 * Sets an integer uniform (e.g. a sampler unit). No-op if the uniform is absent.
	 *
	 * @param uniformName the uniform name
	 * @param value       the integer value
	 */
	public void setUniform(String uniformName, int value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniform1i(location, value);
		}
	}

	/**
	 * Sets a vec4 uniform. No-op if the uniform is absent.
	 *
	 * @param uniformName the uniform name
	 * @param value       the vector value
	 */
	public void setUniform(String uniformName, Vector4f value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniform4f(location, value.x, value.y, value.z, value.w);
		}
	}

	/**
	 * Sets a mat4 uniform. No-op if the uniform is absent.
	 *
	 * @param uniformName the uniform name
	 * @param value       the matrix value
	 */
	public void setUniform(String uniformName, Matrix4f value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		final FloatBuffer matrixData = BufferUtils.createFloatBuffer(MATRIX_BUFFER_SIZE);
		value.get(matrixData);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniformMatrix4fv(location, false, matrixData);
		}
	}

	/** Detaches, deletes the shaders and deletes the program object. */
	@Override
	public void close() {
		glDetachShader(programObject, vertexShaderObject);
		glDetachShader(programObject, fragmentShaderObject);
		glDeleteShader(vertexShaderObject);
		glDeleteShader(fragmentShaderObject);
		glDeleteProgram(programObject);
	}

	/**
	 * Reads a shader source file from the classpath under {@code /shaders/}.
	 *
	 * @param filename the file name including extension
	 * @return the file contents as a string
	 */
	private String readFile(String filename) {
		final StringBuilder outputString = new StringBuilder();
		try {
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/shaders/" + filename)));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				outputString.append(line);
				outputString.append("\n");
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputString.toString();
	}
}
