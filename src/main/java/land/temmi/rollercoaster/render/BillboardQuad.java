package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.utils.Disposable;

/**
 * The unit quad every {@link BillboardRenderer} draws, shared so a scene full of actors costs one
 * vertex buffer. UVs span the full 0..1 range; the atlas region is applied per renderable through
 * the material's texture offset and scale.
 */
public final class BillboardQuad implements Disposable {
    private final Mesh mesh;
    final MeshPart meshPart;

    public BillboardQuad() {
        mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[] {
            -0.5f, -0.5f, 0f, 0f, 1f,
            0.5f, -0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, 0f, 1f, 0f,
            -0.5f, 0.5f, 0f, 0f, 0f});
        mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        meshPart = new MeshPart("billboard", mesh, 0, 6, GL20.GL_TRIANGLES);
    }

    @Override
    public void dispose() { mesh.dispose(); }
}
