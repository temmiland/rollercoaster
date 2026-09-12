package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

public final class ChunkMesher {
    public static final int CHUNK_SIZE = 16;
    public static final long ATTRIBUTES = Usage.Position | Usage.ColorUnpacked | Usage.TextureCoordinates;

    /** One mesh/material per nonempty chunk. Caller owns the returned models and the material's textures. */
    public Array<Model> build(TileMap map, Material material) {
        Array<Model> chunks = new Array<>();
        Matrix4 placement = new Matrix4();
        Matrix4 transform = new Matrix4();
        try {
            for (int z0 = 0; z0 < map.getDepth(); z0 += CHUNK_SIZE) {
                for (int x0 = 0; x0 < map.getWidth(); x0 += CHUNK_SIZE) {
                    ModelBuilder builder = new ModelBuilder();
                    builder.begin();
                    MeshPartBuilder mesh = null;
                    for (int z = z0; z < Math.min(z0 + CHUNK_SIZE, map.getDepth()); z++) {
                        for (int x = x0; x < Math.min(x0 + CHUNK_SIZE, map.getWidth()); x++) {
                            TilePrototype tile = map.getTile(x, z);
                            if (tile == null) continue;
                            if (mesh == null) mesh = builder.part("chunk-" + x0 + "-" + z0,
                                GL20.GL_TRIANGLES, ATTRIBUTES, material);
                            placement.setToTranslation(x - 1f, map.getHeight(x, z), z - 1f);
                            for (Node node : tile.model.nodes) append(mesh, node, placement, transform);
                        }
                    }
                    Model chunk = builder.end();
                    if (mesh == null) chunk.dispose();
                    else chunks.add(chunk);
                }
            }
            return chunks;
        } catch (RuntimeException failure) {
            for (Model chunk : chunks) chunk.dispose();
            throw failure;
        }
    }

    private void append(MeshPartBuilder mesh, Node node, Matrix4 placement, Matrix4 transform) {
        transform.set(placement).mul(node.globalTransform);
        mesh.setVertexTransform(transform);
        for (NodePart part : node.parts) {
            if (part.enabled) mesh.addMesh(part.meshPart);
        }
        for (Node child : node.getChildren()) append(mesh, child, placement, transform);
    }
}
