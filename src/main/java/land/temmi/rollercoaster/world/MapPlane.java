package land.temmi.rollercoaster.world;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * An ordinary tile map placed as one walking plane of a folded map.
 *
 * <p>The map keeps its own local X/Z coordinates, its terrain heights and its collision, so the
 * same authoring and the same {@link ChunkMesher} geometry serve a wall exactly as they serve the
 * ground. Only the rigid placement differs: local X follows the plane's right axis and local Z its
 * backward axis, which is a pure rotation for each {@link GravityState} and therefore never
 * mirrors a map.
 */
public final class MapPlane {
    public final String id;
    public final GravityState gravity;
    public final LoadedMap map;

    private final Vector3 origin = new Vector3();
    private final Vector3 axisX = new Vector3();
    private final Vector3 axisZ = new Vector3();
    private final Vector3 normal = new Vector3();
    private final TerrainRules rules;
    private final Vector3 offset = new Vector3();

    /** {@code x}, {@code y} and {@code z} are the world tile occupied by local tile (0, 0). */
    public MapPlane(String id, GravityState gravity, LoadedMap map, int x, int y, int z) {
        if (id == null || gravity == null || map == null) throw new IllegalArgumentException("Plane data required");
        this.id = id;
        this.gravity = gravity;
        this.map = map;
        rules = new TerrainRules(map.tiles);
        origin.set(x, y, z);
        gravity.normal(normal);
        gravity.right(axisX);
        gravity.forward(axisZ).scl(-1f);
    }

    public Vector3 getNormal(Vector3 out) { return out.set(normal); }

    /** World tile holding local tile {@code (u, v)}. */
    public Vector3 tile(int u, int v, Vector3 out) {
        return out.set(origin).mulAdd(axisX, u).mulAdd(axisZ, v);
    }

    public int localX(int x, int y, int z) {
        return Math.round(offset.set(x, y, z).sub(origin).dot(axisX));
    }

    public int localZ(int x, int y, int z) {
        return Math.round(offset.set(x, y, z).sub(origin).dot(axisZ));
    }

    public boolean contains(int x, int y, int z) {
        offset.set(x, y, z).sub(origin);
        if (Math.abs(offset.dot(normal)) > 0.0001f) return false;
        return map.tiles.contains(Math.round(offset.dot(axisX)), Math.round(offset.dot(axisZ)));
    }

    public boolean isWalkable(int x, int y, int z) {
        return contains(x, y, z) && map.tiles.isWalkable(localX(x, y, z), localZ(x, y, z));
    }

    /** Whether the plane's own terrain rules allow a step between two of its world tiles. */
    public boolean canStep(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        if (!contains(fromX, fromY, fromZ) || !isWalkable(toX, toY, toZ)) return false;
        int u = localX(fromX, fromY, fromZ);
        int v = localZ(fromX, fromY, fromZ);
        return rules.canStep(u, v, localX(toX, toY, toZ) - u, localZ(toX, toY, toZ) - v);
    }

    /** Centre of the walkable surface of a world tile, lifted out of the plane by its terrain height. */
    public Vector3 worldPosition(int x, int y, int z, Vector3 out) {
        float height = rules.heightAt(localX(x, y, z), localZ(x, y, z));
        return out.set(x, y, z).mulAdd(normal, height);
    }

    /** Rigid placement shared by the plane's baked terrain and its props. */
    public Matrix4 transform(Matrix4 out) {
        float[] m = out.idt().val;
        m[Matrix4.M00] = axisX.x; m[Matrix4.M10] = axisX.y; m[Matrix4.M20] = axisX.z;
        m[Matrix4.M01] = normal.x; m[Matrix4.M11] = normal.y; m[Matrix4.M21] = normal.z;
        m[Matrix4.M02] = axisZ.x; m[Matrix4.M12] = axisZ.y; m[Matrix4.M22] = axisZ.z;
        // A tile map's tile (u, v) spans local [u-1, u]; its centre has to land on the world tile.
        offset.set(origin).mulAdd(axisX, 0.5f).mulAdd(axisZ, 0.5f);
        m[Matrix4.M03] = offset.x; m[Matrix4.M13] = offset.y; m[Matrix4.M23] = offset.z;
        return out;
    }
}
