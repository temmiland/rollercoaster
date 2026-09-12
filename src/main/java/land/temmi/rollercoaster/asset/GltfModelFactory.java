package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/** Loads direct GLTF and GLB scene assets and owns their resources. */
public final class GltfModelFactory implements ModelCatalog.Factory {
    private final ModelDefinition definition;
    private final String path;
    private final boolean binary;
    private final Array<SceneAsset> assets = new Array<>();

    public GltfModelFactory(ModelDefinition definition) {
        if (definition == null) throw new IllegalArgumentException("Model definition is required");
        this.definition = definition;
        String path = definition.source.substring(definition.source.indexOf(':') + 1);
        if (path.length() == 0) throw new IllegalArgumentException("Model path is required");
        this.path = path;
        binary = path.toLowerCase().endsWith(".glb");
    }

    @Override
    public Model create() {
        SceneAsset asset = binary
            ? new GLBLoader().load(Gdx.files.classpath(path))
            : new GLTFLoader().load(Gdx.files.classpath(path));
        if (asset.scene == null || asset.scene.model == null) {
            asset.dispose();
            throw new IllegalArgumentException("Model has no default scene: " + path);
        }
        BoundingBox bounds = asset.scene.model.calculateBoundingBox(new BoundingBox());
        if (!matches(bounds.min.x, definition.boundsMinX) || !matches(bounds.min.y, definition.boundsMinY)
            || !matches(bounds.min.z, definition.boundsMinZ) || !matches(bounds.max.x, definition.boundsMaxX)
            || !matches(bounds.max.y, definition.boundsMaxY) || !matches(bounds.max.z, definition.boundsMaxZ)
            || !matches((bounds.max.y - bounds.min.y) * definition.scale, definition.height)) {
            asset.dispose();
            throw new IllegalArgumentException("Model bounds do not match manifest: " + definition.id);
        }
        assets.add(asset);
        return asset.scene.model;
    }

    private static boolean matches(float actual, float expected) {
        return Math.abs(actual - expected) <= 0.001f;
    }

    @Override
    public void dispose() {
        for (SceneAsset asset : assets) asset.dispose();
        assets.clear();
    }
}
