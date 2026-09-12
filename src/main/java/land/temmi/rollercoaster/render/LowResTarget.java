package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class LowResTarget implements Disposable {

    // Pinned to whichever screen axis is shorter, so pixel density stays
    // consistent across landscape and portrait devices.
    private static final int MINOR_AXIS_PX = 360;

    private final TextureRegion sourceRegion = new TextureRegion();
    private final TextureRegion intermediateRegion = new TextureRegion();
    private final Matrix4 passProjection = new Matrix4();

    private FrameBuffer sourceFbo;
    private int internalWidth;
    private int internalHeight;

    private FrameBuffer intermediateFbo;
    private int lastScreenW = -1;
    private int lastScreenH = -1;

    public void resize(int windowWidth, int windowHeight) {
        if (windowWidth <= 0 || windowHeight <= 0) {
            return; // e.g. a minimized window
        }

        int width;
        int height;
        if (windowWidth >= windowHeight) {
            height = MINOR_AXIS_PX;
            width = Math.round(height * (windowWidth / (float) windowHeight));
        } else {
            width = MINOR_AXIS_PX;
            height = Math.round(width * (windowHeight / (float) windowWidth));
        }

        if (sourceFbo == null || width != internalWidth || height != internalHeight) {
            internalWidth = width;
            internalHeight = height;
            rebuildSource();
            lastScreenW = -1; // intScale depends on internalWidth/Height too; force an intermediate rebuild
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
        } else {
            builder.addBasicDepthRenderBuffer();
        }
        sourceFbo = builder.build();
        sourceFbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        sourceRegion.setRegion(sourceFbo.getColorBufferTexture());
        sourceRegion.flip(false, true); // FBO textures are V-flipped relative to the screen
    }

    private void rebuildIntermediate(int screenW, int screenH) {
        if (intermediateFbo != null) {
            intermediateFbo.dispose();
        }

        // Largest whole-number upscale that still fits the screen: this pass stays
        // pixel-perfect. Only the leftover fractional remainder (see blitToScreen)
        // gets blurred, instead of blurring the whole low-res image directly.
        int intScale = Math.max(1, Math.min(screenW / internalWidth, screenH / internalHeight));
        int width = internalWidth * intScale;
        int height = internalHeight * intScale;

        intermediateFbo = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
        intermediateFbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        intermediateRegion.setRegion(intermediateFbo.getColorBufferTexture());
        intermediateRegion.flip(false, true);
    }

    public void begin() {
        sourceFbo.begin();
    }

    public void end() {
        sourceFbo.end();
    }

    public void blitToScreen(SpriteBatch batch) {
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        if (intermediateFbo == null || screenW != lastScreenW || screenH != lastScreenH) {
            rebuildIntermediate(screenW, screenH);
            lastScreenW = screenW;
            lastScreenH = screenH;
        }

        // Pass 1: nearest-neighbour integer upscale, still pixel-perfect.
        intermediateFbo.begin();
        passProjection.setToOrtho2D(0, 0, internalWidth, internalHeight);
        batch.setProjectionMatrix(passProjection);
        batch.begin();
        batch.draw(sourceRegion, 0, 0, internalWidth, internalHeight);
        batch.end();
        intermediateFbo.end();

        // Pass 2: small fractional stretch to fill the screen exactly; only this step blurs.
        Gdx.gl.glViewport(0, 0, screenW, screenH);
        passProjection.setToOrtho2D(0, 0, intermediateFbo.getWidth(), intermediateFbo.getHeight());
        batch.setProjectionMatrix(passProjection);
        batch.begin();
        batch.draw(intermediateRegion, 0, 0, intermediateFbo.getWidth(), intermediateFbo.getHeight());
        batch.end();
    }

    public int getWidth() {
        return internalWidth;
    }

    public int getHeight() {
        return internalHeight;
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
