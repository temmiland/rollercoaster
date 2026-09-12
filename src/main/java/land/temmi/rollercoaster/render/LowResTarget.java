package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class LowResTarget implements Disposable {

    private static final int TARGET_HEIGHT = 360;
    private static final int FIXED_WIDTH = 640;
    private static final int MIN_FLEX_WIDTH = 512;
    private static final int MAX_FLEX_WIDTH = 768;

    private final ScalePolicy policy;
    private final TextureRegion colorRegion = new TextureRegion();
    private final Matrix4 blitProjection = new Matrix4();

    private FrameBuffer fbo;
    private int internalWidth;
    private final int internalHeight = TARGET_HEIGHT;

    public LowResTarget(ScalePolicy policy) {
        this.policy = policy;
    }

    public void resize(int windowWidth, int windowHeight) {
        int width = policy == ScalePolicy.FLEX_WIDTH
            ? MathUtils.clamp(Math.round(TARGET_HEIGHT * (windowWidth / (float) windowHeight)), MIN_FLEX_WIDTH, MAX_FLEX_WIDTH)
            : FIXED_WIDTH;

        if (fbo == null || width != internalWidth) {
            internalWidth = width;
            rebuild();
        }
    }

    private void rebuild() {
        if (fbo != null) {
            fbo.dispose();
        }

        FrameBuffer.FrameBufferBuilder builder = new FrameBuffer.FrameBufferBuilder(internalWidth, internalHeight);
        builder.addBasicColorTextureAttachment(Pixmap.Format.RGB888);
        // GL_DEPTH_COMPONENT16 is the only depth format GLES2 guarantees; use 24-bit where GL30 is available.
        if (Gdx.graphics.isGL30Available()) {
            builder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
        } else {
            builder.addBasicDepthRenderBuffer();
        }
        fbo = builder.build();

        TextureFilter filter = policy == ScalePolicy.STRETCH_SHARP ? TextureFilter.Linear : TextureFilter.Nearest;
        fbo.getColorBufferTexture().setFilter(filter, filter);

        colorRegion.setRegion(fbo.getColorBufferTexture());
        colorRegion.flip(false, true); // FBO textures are V-flipped relative to the screen
    }

    public void begin() {
        fbo.begin();
    }

    public void end() {
        fbo.end();
    }

    public void blitToScreen(SpriteBatch batch) {
        int screenW = Gdx.graphics.getBackBufferWidth();
        int screenH = Gdx.graphics.getBackBufferHeight();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int vpX;
        int vpY;
        int vpW;
        int vpH;
        if (policy == ScalePolicy.STRETCH_SHARP) {
            vpX = 0;
            vpY = 0;
            vpW = screenW;
            vpH = screenH;
        } else {
            int scale = Math.max(1, Math.min(screenW / internalWidth, screenH / internalHeight));
            vpW = internalWidth * scale;
            vpH = internalHeight * scale;
            vpX = (screenW - vpW) / 2;
            vpY = (screenH - vpH) / 2;
        }

        // fbo.end() already reset the viewport to the full backbuffer; the letterboxed
        // viewport for the blit has to be set by hand.
        Gdx.gl.glViewport(vpX, vpY, vpW, vpH);

        blitProjection.setToOrtho2D(0, 0, internalWidth, internalHeight);
        batch.setProjectionMatrix(blitProjection);
        batch.begin();
        batch.draw(colorRegion, 0, 0, internalWidth, internalHeight);
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
        if (fbo != null) {
            fbo.dispose();
        }
    }
}
