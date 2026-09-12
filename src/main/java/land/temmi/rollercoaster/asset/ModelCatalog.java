package land.temmi.rollercoaster.asset;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.ObjectMap;

/** Maps stable map model IDs to model factories. */
public final class ModelCatalog {
    public interface Factory {
        Model create();
        default void dispose() { }
    }

    private static final class Entry {
        public final ModelDefinition definition;
        private final Factory factory;

        private Entry(ModelDefinition definition, Factory factory) {
            this.definition = definition;
            this.factory = factory;
        }

        private Model create() { return factory.create(); }
    }

    private final ObjectMap<String, Entry> entries = new ObjectMap<>();

    public ModelCatalog register(ModelDefinition definition, Factory factory) {
        if (definition == null || factory == null) throw new IllegalArgumentException("Model definition and factory are required");
        String id = definition.id;
        if (entries.containsKey(id)) throw new IllegalArgumentException("Duplicate model ID: " + id);
        entries.put(id, new Entry(definition, factory));
        return this;
    }

    public Model create(String id) {
        return entry(id).create();
    }

    public ModelDefinition definition(String id) {
        return entry(id).definition;
    }

    private Entry entry(String id) {
        Entry entry = entries.get(id);
        if (entry == null) throw new IllegalArgumentException("Unknown model ID: " + id);
        return entry;
    }

    public void dispose() {
        for (Entry entry : entries.values()) entry.factory.dispose();
        entries.clear();
    }
}
