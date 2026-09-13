package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import land.temmi.rollercoaster.asset.ModelCatalog;
import land.temmi.rollercoaster.asset.ModelDefinition;

/**
 * Runtime scene containing baked terrain and map props.
 *
 * <p>Takes ownership of the chunk models - including when construction fails - and disposes them.
 * The model catalog stays owned by the caller, because props only reference its registered models.
 */
public final class WorldScene implements Disposable {
    /** Marker copied to terrain renderables so only the ground receives the shadow map. */
    public static final Object TERRAIN_TAG = new Object();

    private final LoadedMap map;
    private final Array<Model> chunks;
    private final ModelCatalog modelCatalog;
    private final Array<ModelInstance> instances = new Array<>();
    private final Array<BoundingBox> bounds = new Array<>();
    private final Vector3 cullCenter = new Vector3();
    private final Vector3 cullDimensions = new Vector3();
    private final Vector3 slopeNormal = new Vector3();
    private final Quaternion slopeRotation = new Quaternion();
    private final TerrainSurface surface;

    public WorldScene(LoadedMap map, Array<Model> chunks, ModelCatalog modelCatalog) {
        if (map == null || chunks == null || modelCatalog == null) {
            throw new IllegalArgumentException("Map, chunks, and model catalog are required");
        }
        this.map = map;
        this.chunks = chunks;
        this.modelCatalog = modelCatalog;
        this.surface = new TerrainSurface(map.tiles);
        try {
            for (Model chunk : chunks) {
                ModelInstance terrain = new ModelInstance(chunk);
                terrain.userData = TERRAIN_TAG;
                addInstance(terrain);
            }
            for (MapProp prop : map.props) addProp(prop);
        } catch (RuntimeException failure) {
            disposeChunks();
            throw failure;
        }
    }

    public LoadedMap getMap() { return map; }
    public Array<ModelInstance> getInstances() { return instances; }

    /** Fills {@code visible} with instances intersecting the camera frustum. */
    public Array<ModelInstance> getVisibleInstances(Camera camera, Array<ModelInstance> visible) {
        if (camera == null || visible == null) throw new IllegalArgumentException("Camera and output are required");
        visible.clear();
        for (int i = 0; i < instances.size; i++) {
            BoundingBox instanceBounds = bounds.get(i);
            instanceBounds.getCenter(cullCenter);
            instanceBounds.getDimensions(cullDimensions);
            if (camera.frustum.boundsInFrustum(cullCenter, cullDimensions)) visible.add(instances.get(i));
        }
        return visible;
    }

    private void addInstance(ModelInstance instance) {
        instances.add(instance);
        // calculateBoundingBox only walks node transforms; the instance transform that
        // carries a prop's world placement has to be applied on top of it.
        bounds.add(instance.calculateBoundingBox(new BoundingBox()).mul(instance.transform));
    }

    private void addProp(MapProp prop) {
        ModelDefinition definition = modelCatalog.definition(prop.model);
        float groundY = surface.heightAt(prop.x - 0.5f, prop.z - 0.5f);
        applyCollision(prop, definition, groundY);
        ModelInstance instance = new ModelInstance(modelCatalog.create(prop.model));
        instance.userData = prop;
        float radians = prop.rotation * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(radians);
        float sin = MathUtils.sin(radians);
        float offsetX = definition.offsetX * definition.scale * cos - definition.offsetZ * definition.scale * sin;
        float offsetZ = definition.offsetX * definition.scale * sin + definition.offsetZ * definition.scale * cos;

        // Prop coordinates are grid coordinates, so tile N's centre lies at world N - 0.5.
        // Sampling there lets a prop on a ramp sit at the sloped surface instead of a tile step.
        float worldX = prop.x - 0.5f + offsetX;
        float worldZ = prop.z - 0.5f + offsetZ;
        instance.transform.setToTranslation(worldX, groundY + prop.elevation + definition.offsetY, worldZ);
        if (definition.alignToSlope) alignToSlope(instance, prop);
        instance.transform.scale(definition.scale, definition.scale, definition.scale)
            .rotate(Vector3.Y, prop.rotation);
        addInstance(instance);
    }

    /** Tilts the prop so its up axis matches the terrain normal under the anchor tile. */
    private void alignToSlope(ModelInstance instance, MapProp prop) {
        TileShape shape = map.tiles.getShape(MathUtils.floor(prop.x), MathUtils.floor(prop.z));
        if (!shape.isRamp()) return;
        slopeNormal.set(-shape.slopeX, 1f, -shape.slopeZ).nor();
        slopeRotation.setFromCross(Vector3.Y, slopeNormal);
        instance.transform.rotate(slopeRotation);
    }

    /**
     * Writes the model's footprint into the map's collision layer. The footprint belongs to the
     * model, so a placement blocks its tiles automatically rather than relying on the map author
     * having marked the same tiles by hand.
     */
    private void applyCollision(MapProp prop, ModelDefinition definition, float groundY) {
        int anchorX = MathUtils.floor(prop.x);
        int anchorZ = MathUtils.floor(prop.z);
        float radians = prop.rotation * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(radians);
        float sin = MathUtils.sin(radians);

        // Rotate the footprint corners and take their axis-aligned bounds: exact on 90 degree
        // steps, conservative in between, and never silently skipped like an unrotated check.
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (int corner = 0; corner < 4; corner++) {
            float cx = (corner & 1) == 0 ? definition.collisionMinX : definition.collisionMaxX;
            float cz = (corner & 2) == 0 ? definition.collisionMinZ : definition.collisionMaxZ;
            float rx = cx * cos - cz * sin;
            float rz = cx * sin + cz * cos;
            minX = Math.min(minX, Math.round(rx));
            maxX = Math.max(maxX, Math.round(rx));
            minZ = Math.min(minZ, Math.round(rz));
            maxZ = Math.max(maxZ, Math.round(rz));
        }
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                int mapX = anchorX + x;
                int mapZ = anchorZ + z;
                if (!map.tiles.contains(mapX, mapZ)) {
                    throw new IllegalStateException("Prop footprint leaves the map: " + definition.id
                        + " at " + mapX + "," + mapZ);
                }
                if (definition.walkable) {
                    map.tiles.setWalkableSurface(mapX, mapZ,
                        groundY + prop.elevation + definition.offsetY + definition.walkHeight);
                } else {
                    map.tiles.setBlocked(mapX, mapZ, true);
                }
            }
        }
    }

    @Override
    public void dispose() {
        disposeChunks();
    }

    private void disposeChunks() {
        for (Model chunk : chunks) chunk.dispose();
        chunks.clear();
        instances.clear();
        bounds.clear();
    }
}
