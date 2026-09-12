package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.world.ChunkMesher;

/** Temporary model factories used until external GLTF assets are connected. */
public final class ProceduralModels {
    public static final float HOUSE_OFFSET_X = -1f;
    public static final float HOUSE_OFFSET_Z = -1f;

    private ProceduralModels() { }

    public static Model house() {
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
