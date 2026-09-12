package land.temmi.rollercoaster.actor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import land.temmi.rollercoaster.asset.SpriteAtlas;
import land.temmi.rollercoaster.asset.SpriteDefinition;

/** Selects idle or walking frames from a four-direction sprite definition. */
public final class DirectionalSpriteAnimation {
    private final SpriteAnimation[] idle;
    private final SpriteAnimation[] walk;
    private final float worldHeight;
    private Facing facing = Facing.SOUTH;
    private boolean moving;

    public DirectionalSpriteAnimation(SpriteAtlas atlas, SpriteDefinition definition) {
        if (atlas == null || definition == null) throw new IllegalArgumentException("Atlas and definition are required");
        worldHeight = definition.worldHeight;
        idle = new SpriteAnimation[Facing.values().length];
        walk = new SpriteAnimation[Facing.values().length];
        for (Facing direction : Facing.values()) {
            idle[direction.ordinal()] = new SpriteAnimation(definition.frameDuration,
                atlas.region(definition.idleRegion(direction)));
            String[] names = definition.walkRegions(direction);
            TextureRegion[] frames = new TextureRegion[names.length];
            for (int i = 0; i < names.length; i++) frames[i] = atlas.region(names[i]);
            walk[direction.ordinal()] = new SpriteAnimation(definition.frameDuration, frames);
        }
    }

    public void setFacing(Facing next) {
        if (next == null || next == facing) return;
        SpriteAnimation previous = active();
        previous.setPlaying(false);
        facing = next;
        active().setPlaying(moving);
    }

    public Facing getFacing() { return facing; }

    public void setMoving(boolean moving) {
        if (this.moving == moving) return;
        this.moving = moving;
        active().setPlaying(moving);
    }

    public boolean isMoving() { return moving; }

    public void update(float delta) { active().update(delta); }

    public TextureRegion getFrame() { return active().getFrame(); }

    public float getWorldHeight() { return worldHeight; }

    private SpriteAnimation active() { return moving ? walk[facing.ordinal()] : idle[facing.ordinal()]; }
}
