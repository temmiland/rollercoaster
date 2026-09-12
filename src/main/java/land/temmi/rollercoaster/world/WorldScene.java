package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import land.temmi.rollercoaster.asset.ModelCatalog;
import land.temmi.rollercoaster.asset.ModelDefinition;

/** Runtime scene containing baked terrain and map props. */
public final class WorldScene implements Disposable {
    private final LoadedMap map;
    private final Array<Model> chunks;
    private final ModelCatalog modelCatalog;
    private final Array<ModelInstance> instances = new Array<>();

    public WorldScene(LoadedMap map, Array<Model> chunks, ModelCatalog modelCatalog) {
        if (map == null || chunks == null || modelCatalog == null) {
            throw new IllegalArgumentException("Map, chunks, and model catalog are required");
        }
        this.map = map;
        this.chunks = chunks;
        this.modelCatalog = modelCatalog;
        try {
            for (Model chunk : chunks) instances.add(new ModelInstance(chunk));
            for (MapProp prop : map.props) addProp(prop);
        } catch (RuntimeException failure) {
            dispose();
            throw failure;
        }
    }

    public LoadedMap getMap() { return map; }
    public Array<ModelInstance> getInstances() { return instances; }

    private void addProp(MapProp prop) {
        ModelDefinition definition = modelCatalog.definition(prop.model);
        validateCollision(prop, definition);
        ModelInstance instance = new ModelInstance(modelCatalog.create(prop.model));
        float radians = prop.rotation * MathUtils.degreesToRadians;
        float offsetX = definition.offsetX * definition.scale * MathUtils.cos(radians)
            - definition.offsetZ * definition.scale * MathUtils.sin(radians);
        float offsetZ = definition.offsetX * definition.scale * MathUtils.sin(radians)
            + definition.offsetZ * definition.scale * MathUtils.cos(radians);
        instance.transform.setToTranslation(prop.x + offsetX,
            map.tiles.getHeight(MathUtils.floor(prop.x), MathUtils.floor(prop.z)) + definition.offsetY,
            prop.z + offsetZ)
            .scale(definition.scale, definition.scale, definition.scale)
            .rotate(Vector3.Y, prop.rotation);
        instances.add(instance);
    }

    private void validateCollision(MapProp prop, ModelDefinition definition) {
        if (Math.abs(prop.rotation % 360f) > 0.001f) return;
        for (int z = definition.collisionMinZ; z <= definition.collisionMaxZ; z++) {
            for (int x = definition.collisionMinX; x <= definition.collisionMaxX; x++) {
                int mapX = MathUtils.floor(prop.x) + x;
                int mapZ = MathUtils.floor(prop.z) + z;
                if (!map.tiles.isBlocked(mapX, mapZ)) {
                    throw new IllegalStateException("Prop collision footprint is not blocked: " + definition.id
                        + " at " + mapX + "," + mapZ);
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (Model chunk : chunks) chunk.dispose();
        modelCatalog.dispose();
        instances.clear();
    }
}
