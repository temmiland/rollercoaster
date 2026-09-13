package land.temmi.rollercoaster.world;

import com.badlogic.gdx.utils.Array;

public final class LoadedMap {
    public final String name;
    public final TileMap tiles;
    public final Array<MapProp> props;
    public final Array<MapEntity> entities;
    public final Array<MapLight> lights;
    public final Array<MapTransition> transitions;

    public LoadedMap(String name, TileMap tiles, Array<MapProp> props, Array<MapEntity> entities) {
        this(name, tiles, props, entities, new Array<>());
    }

    public LoadedMap(String name, TileMap tiles, Array<MapProp> props, Array<MapEntity> entities,
                     Array<MapLight> lights) {
        this(name, tiles, props, entities, lights, new Array<>());
    }

    public LoadedMap(String name, TileMap tiles, Array<MapProp> props, Array<MapEntity> entities,
                     Array<MapLight> lights, Array<MapTransition> transitions) {
        this.name = name;
        this.tiles = tiles;
        this.props = props;
        this.entities = entities;
        this.lights = lights;
        this.transitions = transitions;
    }
}
