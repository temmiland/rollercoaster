package temmiland.rollercoaster.graphics;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Dynamic sprite batcher.
 *
 * <p>Collects sprite geometry in a CPU-side {@code float[]} buffer and sends
 * everything to the GPU in <strong>a single</strong> {@code glDrawArrays} call.
 * Ideal for entities, particles and other dynamic sprites that have to be
 * rebuilt every frame.</p>
 *
 * <p>Vertex format (interleaved): {@code x, y, u, v} — 4 floats per vertex.<br>
 * Per sprite: 6 vertices (2 triangles, no index buffer).</p>
 *
 * <p>Uses the {@link TextureAtlas} for texture-switch-free rendering.</p>
 *
 * <pre>
 * batch.begin();
 * batch.draw(cx, cy, hw, hh, u0, v0, u1, v1);
 * ...
 * batch.end(shader, projectionMatrix);
 * </pre>
 */
public class SpriteBatch implements AutoCloseable {

    /** Floats per vertex: x, y, u, v. */
    private static final int FLOATS_PER_VERTEX  = 4;
    /** Vertices per sprite (2 triangles). */
    private static final int VERTICES_PER_SPRITE = 6;
    /** Total floats per sprite. */
    private static final int FLOATS_PER_SPRITE   = FLOATS_PER_VERTEX * VERTICES_PER_SPRITE;
    /** Maximum number of sprites per batch run. */
    private static final int MAX_SPRITES         = 8192;

    /** Attribute location: position (x, y). Must match batch.vs. */
    private static final int ATTRIB_POSITION = 0;
    /** Attribute location: texture coordinate (u, v). */
    private static final int ATTRIB_TEXCOORD = 1;
    /** Stride in bytes (4 floats × 4 bytes). */
    private static final int STRIDE          = FLOATS_PER_VERTEX * Float.BYTES;
    /** Offset of the UV attribute in bytes (after x, y). */
    private static final long UV_OFFSET      = 2L * Float.BYTES;

    /** CPU-side geometry buffer. */
    private final float[]      buffer       = new float[MAX_SPRITES * FLOATS_PER_SPRITE];
    /** Reusable FloatBuffer for the GPU upload (avoids per-frame allocation). */
    private final FloatBuffer  uploadBuffer = BufferUtils.createFloatBuffer(MAX_SPRITES * FLOATS_PER_SPRITE);

    /** Number of sprites accumulated since the last begin(). */
    private int spriteCount;
    /** OpenGL VBO id. */
    private final int vboId;

    /** Creates the batcher and pre-allocates the dynamic GPU buffer. */
    public SpriteBatch() {
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER,
                     (long) MAX_SPRITES * FLOATS_PER_SPRITE * Float.BYTES,
                     GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /** Begins a new batch. Must be called before the first {@link #draw}. */
    public void begin() {
        spriteCount = 0;
    }

    /**
     * Adds a sprite to the buffer.
     *
     * @param cx  center X in model coordinates
     * @param cy  center Y in model coordinates
     * @param hw  half width
     * @param hh  half height
     * @param u0  left UV edge
     * @param v0  top UV edge
     * @param u1  right UV edge
     * @param v1  bottom UV edge
     */
    public void draw(final float cx, final float cy,
                     final float hw, final float hh,
                     final float u0, final float v0,
                     final float u1, final float v1) {
        if (spriteCount >= MAX_SPRITES) {
            return;
        }
        int i = spriteCount * FLOATS_PER_SPRITE;

        final float x0 = cx - hw;
        final float x1 = cx + hw;
        final float y0 = cy - hh;
        final float y1 = cy + hh;

        // Triangle 1: TL → TR → BR
        buffer[i++] = x0; buffer[i++] = y1; buffer[i++] = u0; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y1; buffer[i++] = u1; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y0; buffer[i++] = u1; buffer[i++] = v1;
        // Triangle 2: TL → BR → BL
        buffer[i++] = x0; buffer[i++] = y1; buffer[i++] = u0; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y0; buffer[i++] = u1; buffer[i++] = v1;
        buffer[i]   = x0; buffer[i + 1] = y0; buffer[i + 2] = u0; buffer[i + FLOATS_PER_VERTEX - 1] = v1;

        spriteCount++;
    }

    /**
     * Finishes the batch: uploads the buffer to the GPU and draws everything in
     * a single {@code glDrawArrays} call.
     *
     * @param shader      the active batch shader (must know "projection" and "sampler")
     * @param projection  combined view-projection matrix
     */
    public void end(final Shader shader, final Matrix4f projection) {
        if (spriteCount == 0) {
            return;
        }

        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", projection);

        uploadBuffer.clear();
        uploadBuffer.put(buffer, 0, spriteCount * FLOATS_PER_SPRITE);
        uploadBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, uploadBuffer);

        glEnableVertexAttribArray(ATTRIB_POSITION);
        glEnableVertexAttribArray(ATTRIB_TEXCOORD);

        glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, false, STRIDE, 0L);
        glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, false, STRIDE, UV_OFFSET);

        glDrawArrays(GL_TRIANGLES, 0, spriteCount * VERTICES_PER_SPRITE);

        glDisableVertexAttribArray(ATTRIB_POSITION);
        glDisableVertexAttribArray(ATTRIB_TEXCOORD);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        spriteCount = 0;
    }

    /** Releases the GPU buffer owned by this batcher. */
    @Override
    public void close() {
        glDeleteBuffers(vboId);
    }
}
