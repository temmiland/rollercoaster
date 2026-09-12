package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/** Loads direct GLTF and GLB scene assets and owns their resources. */
public final class GltfModelFactory implements ModelCatalog.Factory {
    private final String path;
    private final boolean binary;
    private final Array<SceneAsset> assets = new Array<>();

    public GltfModelFactory(String path) {
        if (path == null || path.length() == 0) throw new IllegalArgumentException("Model path is required");
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
        assets.add(asset);
        return asset.scene.model;
    }

    @Override
    public void dispose() {
        for (SceneAsset asset : assets) asset.dispose();
        assets.clear();
    }
}
