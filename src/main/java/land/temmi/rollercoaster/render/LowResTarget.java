package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class LowResTarget implements Disposable {

    // Pinned to whichever screen axis is shorter, so pixel density stays
    // consistent across landscape and portrait devices.
    private static final int MINOR_AXIS_PX = 360;

    // Beyond this long:short ratio (in either direction), stop exposing more world
    // and letterbox instead - true ultrawide/super-ultrawide monitors and extreme
    // elongated-portrait phones, not the normal device variance this fills for.
    private static final float MAX_ASPECT = 16f / 9f;

    private final TextureRegion sourceRegion = new TextureRegion();
    private final TextureRegion intermediateRegion = new TextureRegion();
    private final Matrix4 passProjection = new Matrix4();

    private FrameBuffer sourceFbo;
    private int internalWidth;
    private int internalHeight;
    private int depthBits;
    private boolean letterboxed;

    private FrameBuffer intermediateFbo;
    private int lastTargetW = -1;
    private int lastTargetH = -1;

    public void resize(int windowWidth, int windowHeight) {
        if (windowWidth <= 0 || windowHeight <= 0) {
            return; // e.g. a minimized window
        }

        float rawAspect = windowWidth / (float) windowHeight;
        float aspect = MathUtils.clamp(rawAspect, 1f / MAX_ASPECT, MAX_ASPECT);
        letterboxed = aspect != rawAspect;

        int width;
        int height;
        if (aspect >= 1f) {
            height = MINOR_AXIS_PX;
            width = Math.round(height * aspect);
        } else {
            width = MINOR_AXIS_PX;
            height = Math.round(width / aspect);
        }

        if (sourceFbo == null || width != internalWidth || height != internalHeight) {
            internalWidth = width;
            internalHeight = height;
            rebuildSource();
            lastTargetW = -1; // intScale depends on internalWidth/Height too; force an intermediate rebuild
        }
    }

    private void rebuildSource() {
        if (sourceFbo != null) {
            sourceFbo.dispose();
        }

        FrameBuffer.FrameBufferBuilder builder = new FrameBuffer.FrameBufferBuilder(internalWidth, internalHeight);
        builder.addBasicColorTextureAttachment(Pixmap.Format.RGB888);
        // GL_DEPTH_COMPONENT16 is the only depth format GLES2 guarantees; use 24-bit where GL30 is available.
        if (Gdx.graphics.isGL30Available()) {
            builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
            depthBits = 24;
        } else {
            depthBits = 16;
            builder.addBasicDepthRenderBuffer();
        }
        sourceFbo = builder.build();
        bindSourceTexture();
    }

    /**
     * Re-points the cached region at the FBO's current colour texture and restores Nearest
     * filtering. After a context loss libGDX rebuilds managed framebuffers with a brand new,
     * Linear-filtered texture, which would otherwise leave the region pointing at a dead
     * texture and blur the pixel look.
     */
    private void bindSourceTexture() {
        Texture color = sourceFbo.getColorBufferTexture();
        color.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        sourceRegion.setRegion(color);
        sourceRegion.flip(false, true); // FBO textures are V-flipped relative to the screen
    }

    private void bindIntermediateTexture() {
        Texture color = intermediateFbo.getColorBufferTexture();
        color.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        intermediateRegion.setRegion(color);
        intermediateRegion.flip(false, true);
    }

    /** Largest whole-number upscale of the internal buffer that still fits the target area. */
    private int integerScale(int targetW, int targetH) {
        return Math.max(1, Math.min(targetW / internalWidth, targetH / internalHeight));
    }

    private void rebuildIntermediate(int width, int height) {
        if (intermediateFbo != null) {
            intermediateFbo.dispose();
        }
        intermediateFbo = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
        bindIntermediateTexture();
    }

    private void releaseIntermediate() {
        if (intermediateFbo == null) return;
        intermediateFbo.dispose();
        intermediateFbo = null;
        intermediateRegion.setTexture(null);
        lastTargetW = -1;
        lastTargetH = -1;
    }

    public void begin() {
        sourceFbo.begin();
    }

    public void end() {
        sourceFbo.end();
    }

    public void blitToScreen(SpriteBatch batch) {
        if (sourceRegion.getTexture() != sourceFbo.getColorBufferTexture()) bindSourceTexture();

        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        int targetW = screenW;
        int targetH = screenH;
        int vpX = 0;
        int vpY = 0;
        if (letterboxed) {
            // Aspect was capped in resize(); fit it instead of stretching to fill,
            // and letterbox the remainder in black.
            float scale = Math.min(screenW / (float) internalWidth, screenH / (float) internalHeight);
            targetW = Math.round(internalWidth * scale);
            targetH = Math.round(internalHeight * scale);
            vpX = (screenW - targetW) / 2;
            vpY = (screenH - targetH) / 2;
        }

        int intScale = integerScale(targetW, targetH);
        int interW = internalWidth * intScale;
        int interH = internalHeight * intScale;

        if (letterboxed) {
            Gdx.gl.glViewport(0, 0, screenW, screenH);
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        if (interW == targetW && interH == targetH) {
            // The integer upscale already fills the target exactly - the usual case on
            // 720p/1080p/1440p/4K. A fractional pass would be an identity copy, so go
            // straight to the back buffer and keep the intermediate buffer unallocated.
            releaseIntermediate();
            Gdx.gl.glViewport(vpX, vpY, targetW, targetH);
            drawRegion(batch, sourceRegion, internalWidth, internalHeight);
            return;
        }

        if (intermediateFbo == null || targetW != lastTargetW || targetH != lastTargetH) {
            rebuildIntermediate(interW, interH);
            lastTargetW = targetW;
            lastTargetH = targetH;
        } else if (intermediateRegion.getTexture() != intermediateFbo.getColorBufferTexture()) {
            bindIntermediateTexture();
        }

        // Pass 1: nearest-neighbour integer upscale, still pixel-perfect.
        intermediateFbo.begin();
        drawRegion(batch, sourceRegion, internalWidth, internalHeight);
        intermediateFbo.end();

        // Pass 2: small fractional stretch to fill the target area; only this step blurs.
        Gdx.gl.glViewport(vpX, vpY, targetW, targetH);
        drawRegion(batch, intermediateRegion, interW, interH);
    }

    private void drawRegion(SpriteBatch batch, TextureRegion region, int width, int height) {
        passProjection.setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(passProjection);
        batch.begin();
        batch.draw(region, 0, 0, width, height);
        batch.end();
    }

    public int getWidth() {
        return internalWidth;
    }

    public int getHeight() {
        return internalHeight;
    }

    public int getDepthBits() {
        return depthBits;
    }

    @Override
    public void dispose() {
        if (sourceFbo != null) {
            sourceFbo.dispose();
        }
        if (intermediateFbo != null) {
            intermediateFbo.dispose();
        }
    }
}
