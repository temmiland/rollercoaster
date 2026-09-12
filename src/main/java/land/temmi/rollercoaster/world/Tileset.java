package land.temmi.rollercoaster.world;

import com.badlogic.gdx.utils.ObjectMap;

/** Named tile prototypes used by map documents. */
public final class Tileset {
    private final ObjectMap<String, TilePrototype> prototypes = new ObjectMap<>();

    public Tileset add(String name, TilePrototype prototype) {
        if (name == null || name.length() == 0 || prototype == null) throw new IllegalArgumentException("Invalid tile prototype");
        prototypes.put(name, prototype);
        return this;
    }

    public TilePrototype get(String name) {
        TilePrototype prototype = prototypes.get(name);
        if (prototype == null) throw new IllegalArgumentException("Unknown tile: " + name);
        return prototype;
    }
}
