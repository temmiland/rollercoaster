package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Matrix4;

/** Procedural render fixture with four chunks, a path, a plateau and a house. */
public final class TestMap {
    private static LoadedMap loadedMap;
    private static TileMap loadedTiles;
    private TestMap() { }

    public static Array<Model> create() {
        Array<TilePrototype> prototypes = new Array<>();
        try {
            TilePrototype grass = ground(new Color(0.34f, 0.58f, 0.25f, 1f), 0.35f);
            prototypes.add(grass);
            TilePrototype lightGrass = ground(new Color(0.38f, 0.63f, 0.28f, 1f), 0.35f);
            prototypes.add(lightGrass);
            TilePrototype path = ground(new Color(0.76f, 0.65f, 0.43f, 1f), 0.35f);
            prototypes.add(path);
            TilePrototype plateau = ground(new Color(0.47f, 0.64f, 0.30f, 1f), 1.35f, 5f, 6f);
            prototypes.add(plateau);
            Tileset tileset = new Tileset()
                .add("grass", grass)
                .add("lightGrass", lightGrass)
                .add("path", path)
                .add("plateau", plateau);
            LoadedMap loaded = new MapLoader().load(
                Gdx.files.classpath("maps/testfield.json"), tileset);
            TileMap map = loaded.tiles;
            loadedMap = loaded;
            loadedTiles = map;
            return new ChunkMesher().build(map, new Material());
        } finally {
            for (TilePrototype prototype : prototypes) prototype.dispose();
        }
    }

    public static boolean isBlocked(int x, int z) {
        return loadedTiles != null && loadedTiles.isBlocked(x, z);
    }

    public static LoadedMap getLoadedMap() {
        if (loadedMap == null) throw new IllegalStateException("Test map has not been created");
        return loadedMap;
    }

    public static Array<Model> createPropModels() {
        Array<Model> models = new Array<>();
        for (MapProp prop : getLoadedMap().props) {
            if (!"house".equals(prop.model)) {
                for (Model model : models) model.dispose();
                throw new IllegalArgumentException("Unknown test map prop: " + prop.model);
            }
            models.add(house());
        }
        return models;
    }

    private static TilePrototype ground(Color color, float thickness) {
        return ground(color, thickness, 1f, 1f);
    }

    private static TilePrototype ground(Color color, float thickness, float width, float depth) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder mesh = builder.part("ground", GL20.GL_TRIANGLES, ChunkMesher.ATTRIBUTES, new Material());
        mesh.setVertexTransform(new Matrix4().setToScaling(width, 1f, depth));
        float sideTint = thickness > 1f ? 0.72f : 1f;
        mesh.setColor(color.r * sideTint, color.g * sideTint, color.b * sideTint, 1f);
        mesh.rect(0, -thickness, 1, 1, -thickness, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1);
        mesh.rect(1, -thickness, 0, 0, -thickness, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1);
        mesh.rect(0, -thickness, 0, 0, -thickness, 1, 0, 0, 1, 0, 0, 0, -1, 0, 0);
        mesh.rect(1, -thickness, 1, 1, -thickness, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0);
        mesh.setColor(color);
        mesh.rect(0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0);
        return new TilePrototype(builder.end());
    }

    private static Model house() {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder mesh = builder.part("house", GL20.GL_TRIANGLES, ChunkMesher.ATTRIBUTES, new Material());
        mesh.setColor(0.84f, 0.78f, 0.59f, 1f);
        mesh.box(0.5f, 1.4f, 0.5f, 4f, 2.8f, 3f);
        mesh.setColor(0.55f, 0.22f, 0.18f, 1f);
        mesh.rect(-1.8f, 2.8f, 2.3f, 2.8f, 2.8f, 2.3f, 2.8f, 4f, 0.5f, -1.8f, 4f, 0.5f, 0f, 1f, 1f);
        mesh.setColor(0.42f, 0.16f, 0.14f, 1f);
        mesh.rect(2.8f, 2.8f, -1.3f, -1.8f, 2.8f, -1.3f, -1.8f, 4f, 0.5f, 2.8f, 4f, 0.5f, 0f, 1f, -1f);
        mesh.setColor(0.68f, 0.58f, 0.40f, 1f);
        mesh.triangle(new Vector3(-1.5f, 2.8f, -1f),
            new Vector3(-1.5f, 2.8f, 2f), new Vector3(-1.5f, 4f, 0.5f));
        mesh.triangle(new Vector3(2.5f, 2.8f, 2f),
            new Vector3(2.5f, 2.8f, -1f), new Vector3(2.5f, 4f, 0.5f));
        mesh.setColor(0.27f, 0.17f, 0.12f, 1f);
        mesh.box(0.5f, 0.8f, 2.02f, 0.8f, 1.6f, 0.04f);
        mesh.setColor(0.40f, 0.70f, 0.78f, 1f);
        mesh.box(-0.7f, 1.65f, 2.02f, 0.7f, 0.8f, 0.04f);
        mesh.box(1.7f, 1.65f, 2.02f, 0.7f, 0.8f, 0.04f);
        return builder.end();
    }
}
