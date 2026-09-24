package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.collision.BoundingBox;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

/**
 * Loads a direct GLTF or GLB scene asset and owns its resources.
 *
 * <p>The model is parsed once and shared by every {@code ModelInstance} placed from it, so a map
 * with many copies of the same prop costs one parse and one set of buffers.
 */
public final class GltfModelFactory implements ModelCatalog.Factory {
    private final ModelDefinition definition;
    private final String path;
    private final boolean binary;
    private final FileHandle sourceFile;
    private SceneAsset asset;
    private Model model;

    /** {@code sourceFile} is the definition's source resolved against its manifest's directory. */
    public GltfModelFactory(ModelDefinition definition, FileHandle sourceFile) {
        if (definition == null) throw new IllegalArgumentException("Model definition is required");
        if (sourceFile == null) throw new IllegalArgumentException("Model source file is required: " + definition.id);
        this.definition = definition;
        int separator = definition.source.indexOf(':');
        if (separator < 0) throw new IllegalArgumentException("Model source needs a gltf: or glb: prefix: " + definition.source);
        String format = definition.source.substring(0, separator);
        path = definition.source.substring(separator + 1);
        if (path.length() == 0) throw new IllegalArgumentException("Model path is required");
        if ("glb".equals(format)) binary = true;
        else if ("gltf".equals(format)) binary = false;
        else throw new IllegalArgumentException("Unsupported model format: " + format);
        // A mismatch here means the manifest would be parsed by the wrong loader.
        String extension = binary ? ".glb" : ".gltf";
        if (!path.toLowerCase().endsWith(extension)) {
            throw new IllegalArgumentException("Model source declares " + format + " but is not a " + extension + " file: " + path);
        }
        this.sourceFile = sourceFile;
    }

    @Override
    public Model create() {
        if (model != null) return model;
        SceneAsset loaded = binary
            ? new GLBLoader().load(sourceFile)
            : new GLTFLoader().load(sourceFile);
        if (loaded.scene == null || loaded.scene.model == null) {
            loaded.dispose();
            throw new IllegalArgumentException("Model has no default scene: " + path);
        }
        BoundingBox bounds = loaded.scene.model.calculateBoundingBox(new BoundingBox());
        if (!matches(bounds.min.x, definition.boundsMinX) || !matches(bounds.min.y, definition.boundsMinY)
            || !matches(bounds.min.z, definition.boundsMinZ) || !matches(bounds.max.x, definition.boundsMaxX)
            || !matches(bounds.max.y, definition.boundsMaxY) || !matches(bounds.max.z, definition.boundsMaxZ)
            || !matches((bounds.max.y - bounds.min.y) * definition.scale, definition.height)) {
            loaded.dispose();
            throw new IllegalArgumentException("Model bounds do not match manifest: " + definition.id);
        }
        asset = loaded;
        model = loaded.scene.model;
        return model;
    }

    private static boolean matches(float actual, float expected) {
        return Math.abs(actual - expected) <= 0.001f;
    }

    @Override
    public void dispose() {
        if (asset != null) asset.dispose();
        asset = null;
        model = null;
    }
}
