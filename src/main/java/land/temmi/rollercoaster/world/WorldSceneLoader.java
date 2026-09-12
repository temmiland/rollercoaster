package land.temmi.rollercoaster.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import land.temmi.rollercoaster.asset.ModelCatalog;

/** Builds a runtime scene from editor-exported map data and registered models. */
public final class WorldSceneLoader {
    private final MapLoader mapLoader;
    private final ChunkMesher chunkMesher;

    public WorldSceneLoader() {
        this(new MapLoader(), new ChunkMesher());
    }

    public WorldSceneLoader(MapLoader mapLoader, ChunkMesher chunkMesher) {
        if (mapLoader == null || chunkMesher == null) throw new IllegalArgumentException("World loaders are required");
        this.mapLoader = mapLoader;
        this.chunkMesher = chunkMesher;
    }

    public WorldScene load(FileHandle mapFile, Tileset tileset, Material terrainMaterial,
                           ModelCatalog modelCatalog) {
        if (terrainMaterial == null || modelCatalog == null) {
            throw new IllegalArgumentException("Terrain material and model catalog are required");
        }
        LoadedMap map = mapLoader.load(mapFile, tileset);
        Array<Model> chunks = chunkMesher.build(map.tiles, terrainMaterial);
        try {
            return new WorldScene(map, chunks, modelCatalog);
        } catch (RuntimeException failure) {
            for (Model chunk : chunks) chunk.dispose();
            throw failure;
        }
    }
}
