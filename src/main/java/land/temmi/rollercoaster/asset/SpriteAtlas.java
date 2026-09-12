package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/** Loads a texture-packer atlas exported by the editor and exposes named sprite regions. */
public final class SpriteAtlas implements Disposable {
    private final TextureAtlas atlas;

    public SpriteAtlas(String atlasPath) {
        if (atlasPath == null || atlasPath.length() == 0) throw new IllegalArgumentException("Atlas path is required");
        atlas = new TextureAtlas(Gdx.files.classpath(atlasPath));
    }

    public TextureRegion region(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) throw new IllegalArgumentException("Unknown sprite: " + name);
        return region;
    }

    public TextureRegion region(String name, int index) {
        TextureRegion region = atlas.findRegion(name, index);
        if (region == null) throw new IllegalArgumentException("Unknown sprite: " + name + "#" + index);
        return region;
    }

    public TextureAtlas getAtlas() { return atlas; }

    @Override
    public void dispose() { atlas.dispose(); }
}
