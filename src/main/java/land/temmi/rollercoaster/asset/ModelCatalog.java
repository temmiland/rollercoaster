package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.ObjectMap;

/** Maps stable map model IDs to model factories. */
public final class ModelCatalog {
    public interface Factory { Model create(); }

    public static final class Definition {
        public final float offsetX;
        public final float offsetY;
        public final float offsetZ;
        private final Factory factory;

        private Definition(Factory factory, float offsetX, float offsetY, float offsetZ) {
            this.factory = factory;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        private Model create() { return factory.create(); }
    }

    private final ObjectMap<String, Definition> definitions = new ObjectMap<>();

    public ModelCatalog register(String id, Factory factory) {
        return register(id, factory, 0f, 0f, 0f);
    }

    public ModelCatalog register(String id, Factory factory, float offsetX, float offsetY, float offsetZ) {
        if (id == null || id.length() == 0 || factory == null) {
            throw new IllegalArgumentException("Model ID and factory are required");
        }
        if (definitions.containsKey(id)) throw new IllegalArgumentException("Duplicate model ID: " + id);
        definitions.put(id, new Definition(factory, offsetX, offsetY, offsetZ));
        return this;
    }

    public Model create(String id) {
        return definition(id).create();
    }

    public Definition definition(String id) {
        Definition definition = definitions.get(id);
        if (definition == null) throw new IllegalArgumentException("Unknown model ID: " + id);
        return definition;
    }
}
