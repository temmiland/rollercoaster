package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.ObjectMap;

/** Maps stable map model IDs to model factories. */
public final class ModelCatalog {
    public interface Factory { Model create(); }

    private final ObjectMap<String, Factory> factories = new ObjectMap<>();

    public ModelCatalog register(String id, Factory factory) {
        if (id == null || id.length() == 0 || factory == null) {
            throw new IllegalArgumentException("Model ID and factory are required");
        }
        if (factories.containsKey(id)) throw new IllegalArgumentException("Duplicate model ID: " + id);
        factories.put(id, factory);
        return this;
    }

    public Model create(String id) {
        Factory factory = factories.get(id);
        if (factory == null) throw new IllegalArgumentException("Unknown model ID: " + id);
        return factory.create();
    }
}
