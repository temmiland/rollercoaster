package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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

    /** Lets editor previews load an exported atlas from a project directory rather than the classpath. */
    public SpriteAtlas(FileHandle atlasFile) {
        if (atlasFile == null) throw new IllegalArgumentException("Atlas file is required");
        atlas = new TextureAtlas(atlasFile);
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

    /** Returns the page texture for single-page billboard renderers. */
    public Texture getTexture() {
        if (atlas.getTextures().size != 1) throw new IllegalStateException("Sprite atlas must have one page");
        return atlas.getTextures().first();
    }

    @Override
    public void dispose() { atlas.dispose(); }
}
