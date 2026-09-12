package land.temmi.rollercoaster.world;

import com.badlogic.gdx.utils.Array;

public final class LoadedMap {
    public final String name;
    public final TileMap tiles;
    public final Array<MapProp> props;
    public final Array<MapEntity> entities;

    LoadedMap(String name, TileMap tiles, Array<MapProp> props, Array<MapEntity> entities) {
        this.name = name;
        this.tiles = tiles;
        this.props = props;
        this.entities = entities;
    }
}
