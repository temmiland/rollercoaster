package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import land.temmi.rollercoaster.asset.ModelCatalog;

/** Ordinary terrain/prop scenes rigidly placed on the folded map's authored walking planes. */
public final class FoldedMapScene implements Disposable {
    private final Array<WorldScene> scenes = new Array<>();
    private final Array<ModelInstance> visible = new Array<>();
    private final Array<ModelInstance> all = new Array<>();

    public FoldedMapScene(FoldedMap map, Material material, ModelCatalog catalog, float borderDepth) {
        if (map == null || material == null) throw new IllegalArgumentException("Map and material are required");
        Matrix4 placement = new Matrix4();
        try {
            for (MapPlane plane : map.planes()) {
                WorldScene scene = new WorldScene(plane.map,
                    new ChunkMesher().setBorderDepth(borderDepth).build(plane.map.tiles, material), catalog);
                scenes.add(scene);
                scene.applyTransform(plane.transform(placement));
                all.addAll(scene.getInstances());
            }
        } catch (RuntimeException error) {
            dispose();
            throw error;
        }
    }

    public Array<ModelInstance> getInstances() { return all; }

    public Array<ModelInstance> getVisibleInstances(Camera camera, Array<ModelInstance> out) {
        out.clear();
        for (WorldScene scene : scenes) out.addAll(scene.getVisibleInstances(camera, visible));
        return out;
    }

    @Override public void dispose() {
        for (WorldScene scene : scenes) scene.dispose();
        scenes.clear(); all.clear(); visible.clear();
    }
}
