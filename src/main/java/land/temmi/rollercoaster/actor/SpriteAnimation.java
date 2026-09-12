package land.temmi.rollercoaster.actor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Small deterministic frame animation driven by elapsed seconds. */
public final class SpriteAnimation {
    private final TextureRegion[] frames;
    private final float frameDuration;
    private float elapsed;
    private boolean playing;

    public SpriteAnimation(float frameDuration, TextureRegion... frames) {
        if (frameDuration <= 0f || frames == null || frames.length == 0) throw new IllegalArgumentException("Animation needs frames");
        this.frameDuration = frameDuration;
        this.frames = new TextureRegion[frames.length];
        for (int i = 0; i < frames.length; i++) this.frames[i] = new TextureRegion(frames[i]);
    }

    public void setPlaying(boolean playing) {
        if (this.playing && !playing) elapsed = 0f;
        this.playing = playing;
    }

    public void update(float delta) { if (playing) elapsed += Math.max(0f, delta); }
    public TextureRegion getFrame() { return frames[playing ? (int) (elapsed / frameDuration) % frames.length : 0]; }
}
