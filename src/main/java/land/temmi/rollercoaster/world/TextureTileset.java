package land.temmi.rollercoaster.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.Disposable;

/** Loads an editor atlas and exposes its tile surfaces for terrain meshing. */
public final class TextureTileset implements Disposable {
    private final Texture atlas;
    private final Tileset tileset = new Tileset();
    private boolean disposed;

    public TextureTileset(TilesetManifest manifest) {
        if (manifest == null) throw new IllegalArgumentException("Tileset manifest is required");
        atlas = new Texture(Gdx.files.classpath(manifest.texture));
        atlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        try {
            for (TileDefinition definition : manifest.tiles) {
                checkInside(definition.id, definition.atlasX, definition.atlasY,
                    definition.atlasWidth, definition.atlasHeight);
                checkInside(definition.id, definition.sideX, definition.sideY,
                    definition.sideWidth, definition.sideHeight);
                tileset.add(new TileSurface(definition.id)
                    .setRegion(new TextureRegion(atlas, definition.atlasX, definition.atlasY,
                            definition.atlasWidth, definition.atlasHeight),
                        new TextureRegion(atlas, definition.sideX, definition.sideY,
                            definition.sideWidth, definition.sideHeight))
                    .setWalkable(definition.walkable));
            }
        } catch (RuntimeException failure) {
            dispose();
            throw failure;
        }
    }

    public TextureTileset(FileHandle manifestFile) {
        this(TilesetManifest.load(manifestFile));
    }

    public Tileset getTileset() { return tileset; }
    public Texture getAtlas() { return atlas; }
    public Material createMaterial() { return new Material(TextureAttribute.createDiffuse(atlas)); }

    private void checkInside(String id, int x, int y, int width, int height) {
        if (x + width > atlas.getWidth() || y + height > atlas.getHeight()) {
            throw new IllegalArgumentException("Tile region outside atlas: " + id);
        }
    }

    /** Idempotent, so a failed construction can clean up without double-disposing the atlas. */
    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        atlas.dispose();
    }
}
