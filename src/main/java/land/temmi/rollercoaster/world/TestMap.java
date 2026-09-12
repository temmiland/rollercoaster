package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.Color;
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
            TilePrototype house = house();
            prototypes.add(house);
            TileMap map = new TileMap(24, 24);
            for (int z = 0; z < map.getDepth(); z++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    TilePrototype tile = (x >= 11 && x <= 12 || z >= 14 && z <= 15)
                        ? path : ((x + z) % 2 == 0 ? grass : lightGrass);
                    map.set(x, z, tile, 0f);
                }
            }
            map.set(8, 10, house, 0f);
            map.set(17, 7, plateau, 1f);
            return new ChunkMesher().build(map, new Material());
        } finally {
            for (TilePrototype prototype : prototypes) prototype.dispose();
        }
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

    private static TilePrototype house() {
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
        return new TilePrototype(builder.end());
    }
}
