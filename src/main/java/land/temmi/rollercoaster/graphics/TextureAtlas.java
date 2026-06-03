package land.temmi.rollercoaster.graphics;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Runtime texture atlas — loads all registered sprite textures as individual PNGs
 * and packs them into a single OpenGL texture at game startup.
 *
 * <p>Layout: a horizontal strip, each sprite occupies {@code tilePx}×{@code tilePx}
 * pixels. Individual PNGs are scaled to this size via nearest-neighbour, entirely
 * without Graphics2D (direct pixel copy → no JVM-side color conversion artifacts).</p>
 *
 * <p>No UV inset: with {@code GL_NEAREST} the sampler always snaps exactly to the
 * nearest texel center — bleeding between tiles is impossible.</p>
 */
public class TextureAtlas implements AutoCloseable {

    /** Bytes per pixel (RGBA). */
    private static final int BYTES_PER_PIXEL = 4;

    /** Bit shift for the red channel in ARGB format. */
    private static final int RED_SHIFT   = 16;
    /** Bit shift for the green channel in ARGB format. */
    private static final int GREEN_SHIFT = 8;
    /** Bit shift for the alpha channel in ARGB format. */
    private static final int ALPHA_SHIFT = 24;
    /** Mask for extracting an 8-bit color channel. */
    private static final int COLOR_MASK  = 0xFF;

    /** Tile size in pixels used when building this atlas. */
    private final int tilePx;

    /** OpenGL texture id. 0 = not initialized. */
    private int textureId;

    /** UV map: texture name → float[]{u0, v0, u1, v1}. */
    private final Map<String, float[]> uvMap = new LinkedHashMap<>();

    /**
     * Builds the atlas from an array of sprites. Must be called on the GL thread.
     *
     * @param sprites all sprites to include; {@code null} entries are skipped
     * @param tilePx  output tile size in pixels (width and height)
     */
    public TextureAtlas(final Sprite[] sprites, final int tilePx) {
        this.tilePx = tilePx;

        final Map<String, int[]> images = new LinkedHashMap<>();
        for (final Sprite sprite : sprites) {
            if (sprite == null) {
                continue;
            }
            final String name = sprite.getTexture();
            if (images.containsKey(name)) {
                continue;
            }
            final int[] pixels = loadAndScale(name);
            if (pixels != null) {
                images.put(name, pixels);
            }
        }

        if (images.isEmpty()) {
            return;
        }

        final int count  = images.size();
        final int atlasW = count * tilePx;
        final int atlasH = tilePx;

        final int[] atlasPixels = new int[atlasW * atlasH];

        // No UV inset needed: GL_NEAREST always snaps to the nearest texel center,
        // bleeding between atlas tiles is physically impossible.
        int slot = 0;
        for (final Map.Entry<String, int[]> entry : images.entrySet()) {
            final int[] src    = entry.getValue();
            final int   startX = slot * tilePx;

            for (int row = 0; row < tilePx; row++) {
                for (int col = 0; col < tilePx; col++) {
                    atlasPixels[(startX + col) + row * atlasW] = src[col + row * tilePx];
                }
            }

            final float u0 = (float) slot       / count;
            final float u1 = (float) (slot + 1) / count;
            uvMap.put(entry.getKey(), new float[]{u0, 0f, u1, 1f});
            slot++;
        }

        uploadToGpu(atlasPixels, atlasW, atlasH);
    }

    /**
     * Builds a GUI atlas from a spritesheet, extracting every cell as a named entry.
     *
     * <p>Each cell is stored under the key {@code col + "_" + row} (e.g. {@code "3_2"}).
     * Cell size is {@code sheetPx / gridSize} pixels; cells are scaled to {@code tilePx}
     * in the atlas the same way regular sprites are.</p>
     *
     * @param sheetName name of the sheet PNG (without path/extension), loaded from {@code /textures/sheets/}
     * @param gridSize  number of cells per axis
     * @param tilePx    output tile size in pixels (width and height)
     */
    public TextureAtlas(final String sheetName, final int gridSize, final int tilePx) {
        this.tilePx = tilePx;

        final int[] sheetPixels;
        final int sheetW;
        final int sheetH;
        try (final InputStream is = getClass().getResourceAsStream("/textures/sheets/" + sheetName + ".png")) {
            if (is == null) {
                return;
            }
            final BufferedImage src = ImageIO.read(is);
            if (src == null) {
                return;
            }
            sheetW = src.getWidth();
            sheetH = src.getHeight();
            sheetPixels = src.getRGB(0, 0, sheetW, sheetH, null, 0, sheetW);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final int cellPxW = sheetW / gridSize;
        final int cellPxH = sheetH / gridSize;
        final int count   = gridSize * gridSize;
        final int atlasW  = count * tilePx;

        final int[] atlasPixels = new int[atlasW * tilePx];

        int slot = 0;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                final int startX = slot * tilePx;
                for (int py = 0; py < tilePx; py++) {
                    for (int px = 0; px < tilePx; px++) {
                        final int srcCol = col * cellPxW + px * cellPxW / tilePx;
                        final int srcRow = row * cellPxH + py * cellPxH / tilePx;
                        atlasPixels[(startX + px) + py * atlasW] = sheetPixels[srcCol + srcRow * sheetW];
                    }
                }
                final float u0 = (float) slot       / count;
                final float u1 = (float) (slot + 1) / count;
                uvMap.put(col + "_" + row, new float[]{u0, 0f, u1, 1f});
                slot++;
            }
        }

        uploadToGpu(atlasPixels, atlasW, tilePx);
    }

    /**
     * Binds the atlas to the given sampler slot.
     * @param sampler OpenGL sampler slot (0-based)
     */
    public void bind(final int sampler) {
        glActiveTexture(GL_TEXTURE0 + sampler);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    /**
     * Returns the UV coordinates for the given texture name.
     * @param textureName name of the texture (without path/extension)
     * @return float[]{u0, v0, u1, v1} or {@code null} if not in the atlas
     */
    public float[] getUV(final String textureName) {
        return uvMap.get(textureName);
    }

    /** Releases the atlas GL texture if it was created. */
    @Override
    public void close() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Uploads the given pixel array as a GL_RGBA texture.
     *
     * @param pixels ARGB pixel array (row-major)
     * @param width  atlas width in pixels
     * @param height atlas height in pixels
     */
    private void uploadToGpu(final int[] pixels, final int width, final int height) {
        final ByteBuffer buf = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                final int px = pixels[col + row * width];
                buf.put((byte) ((px >> RED_SHIFT)   & COLOR_MASK));
                buf.put((byte) ((px >> GREEN_SHIFT)  & COLOR_MASK));
                buf.put((byte) (px                   & COLOR_MASK));
                buf.put((byte) ((px >> ALPHA_SHIFT)  & COLOR_MASK));
            }
        }
        buf.flip();

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Loads a PNG and scales it via nearest-neighbour to {@code tilePx}×{@code tilePx}.
     *
     * @param name texture name (without path and extension)
     * @return an ARGB int[tilePx*tilePx] array, or null on error
     */
    private int[] loadAndScale(final String name) {
        final BufferedImage src;
        try (InputStream is = getClass().getResourceAsStream("/textures/" + name + ".png")) {
            if (is == null) {
                return null;
            }
            src = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (src == null) {
            return null;
        }

        final int srcW = src.getWidth();
        final int srcH = src.getHeight();

        final int[] srcPixels = src.getRGB(0, 0, srcW, srcH, null, 0, srcW);
        final int[] out       = new int[tilePx * tilePx];

        for (int row = 0; row < tilePx; row++) {
            for (int col = 0; col < tilePx; col++) {
                final int srcCol = col * srcW / tilePx;
                final int srcRow = row * srcH / tilePx;
                out[col + row * tilePx] = srcPixels[srcCol + srcRow * srcW];
            }
        }
        return out;
    }
}
