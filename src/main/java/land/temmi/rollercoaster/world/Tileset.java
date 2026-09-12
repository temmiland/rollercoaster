package land.temmi.rollercoaster.world;

import com.badlogic.gdx.utils.ObjectMap;

/** Named tile surfaces used by map documents. */
public final class Tileset {
    private final ObjectMap<String, TileSurface> surfaces = new ObjectMap<>();

    public Tileset add(TileSurface surface) {
        if (surface == null) throw new IllegalArgumentException("Tile surface is required");
        surfaces.put(surface.id, surface);
        return this;
    }

    public TileSurface get(String name) {
        TileSurface surface = surfaces.get(name);
        if (surface == null) throw new IllegalArgumentException("Unknown tile: " + name);
        return surface;
    }

    public boolean has(String name) { return surfaces.containsKey(name); }
}
