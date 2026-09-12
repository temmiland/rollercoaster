package land.temmi.rollercoaster.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Loads an editor atlas and exposes flat, textured tile prototypes for terrain meshing. */
public final class TextureTileset implements Disposable {
    private final Texture atlas;
    private final Tileset tileset = new Tileset();
    private final Array<TilePrototype> prototypes = new Array<>();
    private boolean disposed;

    public TextureTileset(TilesetManifest manifest) {
        if (manifest == null) throw new IllegalArgumentException("Tileset manifest is required");
        atlas = new Texture(Gdx.files.classpath(manifest.texture));
        atlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        try {
            for (TileDefinition definition : manifest.tiles) {
                if (definition.atlasX + definition.atlasWidth > atlas.getWidth()
                    || definition.atlasY + definition.atlasHeight > atlas.getHeight()) {
                    throw new IllegalArgumentException("Tile region outside atlas: " + definition.id);
                }
                TilePrototype prototype = new TilePrototype(createPrototype(definition));
                prototypes.add(prototype);
                tileset.add(definition.id, prototype);
            }
        } catch (RuntimeException failure) {
            dispose();
            throw failure;
        }
    }

    public TextureTileset(com.badlogic.gdx.files.FileHandle manifestFile) {
        this(TilesetManifest.load(manifestFile));
    }

    public Tileset getTileset() { return tileset; }
    public Texture getAtlas() { return atlas; }
    public Material createMaterial() { return new Material(TextureAttribute.createDiffuse(atlas)); }

    private Model createPrototype(TileDefinition definition) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        Material material = new Material(TextureAttribute.createDiffuse(atlas));
        MeshPartBuilder mesh = builder.part("tile-" + definition.id, GL20.GL_TRIANGLES,
            ChunkMesher.ATTRIBUTES, material);
        mesh.setColor(Color.WHITE);
        mesh.setUVRange(new TextureRegion(atlas, definition.atlasX, definition.atlasY,
            definition.atlasWidth, definition.atlasHeight));
        mesh.rect(0f, 0f, 1f, 1f, 0f, 1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f);
        return builder.end();
    }

    /** Idempotent, so a failed construction can clean up without double-disposing the atlas. */
    @Override
    public void dispose() {
        for (TilePrototype prototype : prototypes) prototype.dispose();
        prototypes.clear();
        if (!disposed) {
            disposed = true;
            atlas.dispose();
        }
    }
}
