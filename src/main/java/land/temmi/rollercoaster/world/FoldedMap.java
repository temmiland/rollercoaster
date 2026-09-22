package land.temmi.rollercoaster.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.actor.GridActor;
import land.temmi.rollercoaster.input.MoveIntent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A map folded out of several walking planes, sharing one fixed X/Y/Z tile grid.
 *
 * <p>Walking within a plane is the plane's own business. Crossing to another one happens where the
 * planes touch, or across an authored crossing where they do not: a step whose direction leaves the
 * current plane is the only case a crossing is consulted for, because the way back off a plane always
 * points along the normal it just gained, which no input maps to.
 */
public final class FoldedMap implements GridActor.MovementSpace {
    private final List<MapPlane> planes = new ArrayList<>();
    private final List<Crossing> crossings = new ArrayList<>();
    private final Vector3 step = new Vector3();
    private final Vector3 fromNormal = new Vector3();
    private final Vector3 toNormal = new Vector3();
    private final Vector3 scratch = new Vector3();
    private float crossingArcHeight = 0.35f;

    private static final class Crossing {
        final int fromX, fromY, fromZ, toX, toY, toZ;
        final MoveIntent input;

        Crossing(int fromX, int fromY, int fromZ, MoveIntent input, int toX, int toY, int toZ) {
            this.fromX = fromX; this.fromY = fromY; this.fromZ = fromZ;
            this.toX = toX; this.toY = toY; this.toZ = toZ;
            this.input = input;
        }

        boolean starts(int x, int y, int z, MoveIntent from) {
            return input == from && fromX == x && fromY == y && fromZ == z;
        }
    }

    /** How far a step that changes plane bulges away from both planes on its way over. */
    public FoldedMap setCrossingArcHeight(float crossingArcHeight) {
        if (crossingArcHeight < 0f) throw new IllegalArgumentException("Crossing arc height must not be negative");
        this.crossingArcHeight = crossingArcHeight;
        return this;
    }

    public FoldedMap add(MapPlane plane) {
        if (plane == null) throw new IllegalArgumentException("Plane required");
        for (int u = 0; u < plane.map.tiles.getWidth(); u++) {
            for (int v = 0; v < plane.map.tiles.getDepth(); v++) {
                plane.tile(u, v, scratch);
                if (planeAt(round(scratch.x), round(scratch.y), round(scratch.z)) != null) {
                    throw new IllegalArgumentException("Overlapping plane footprints at " + scratch);
                }
            }
        }
        planes.add(plane);
        return this;
    }

    public List<MapPlane> planes() { return Collections.unmodifiableList(planes); }

    /**
     * World tile of the first entity of that type on any plane, or null. Planes carry the map's
     * markers - where the player arrives, where it leads out - as ordinary map entities.
     */
    public Vector3 entityTile(String type, Vector3 out) {
        if (type == null || out == null) throw new IllegalArgumentException("Entity type and output are required");
        for (int i = 0; i < planes.size(); i++) {
            MapPlane plane = planes.get(i);
            for (MapEntity entity : plane.map.entities) {
                if (type.equals(entity.type)) return plane.tile(entity.x, entity.z, out);
            }
        }
        return null;
    }

    public MapPlane planeAt(int x, int y, int z) {
        for (int i = 0; i < planes.size(); i++) {
            if (planes.get(i).contains(x, y, z)) return planes.get(i);
        }
        return null;
    }

    /**
     * The plane reached by an allowed step only when that step leaves the current plane.
     * Useful for showing an impending crossing before the actor takes it.
     */
    public MapPlane crossingDestination(int x, int y, int z, MoveIntent input) {
        MapPlane current = planeAt(x, y, z);
        if (current == null || !tryStep(x, y, z, input, scratch)) return null;
        MapPlane destination = planeAt(round(scratch.x), round(scratch.y), round(scratch.z));
        return destination == current ? null : destination;
    }

    /**
     * Links two world tiles in both directions. Each direction carries its own input, because the
     * step that leaves a plane and the step that returns are expressed in different planes.
     */
    public FoldedMap crossing(Vector3 a, MoveIntent fromA, Vector3 b, MoveIntent fromB) {
        addCrossing(a, fromA, b);
        addCrossing(b, fromB, a);
        return this;
    }

    private void addCrossing(Vector3 from, MoveIntent input, Vector3 to) {
        requireTile(from);
        requireTile(to);
        if (input == null || input == MoveIntent.NONE) throw new IllegalArgumentException("A crossing needs a direction");
        int x = round(from.x), y = round(from.y), z = round(from.z);
        for (int i = 0; i < crossings.size(); i++) {
            if (crossings.get(i).starts(x, y, z, input)) {
                throw new IllegalArgumentException("Duplicate crossing at " + from + " towards " + input);
            }
        }
        crossings.add(new Crossing(x, y, z, input, round(to.x), round(to.y), round(to.z)));
    }

    private void requireTile(Vector3 tile) {
        if (tile == null) throw new IllegalArgumentException("Crossing tile required");
        int x = round(tile.x), y = round(tile.y), z = round(tile.z);
        if (tile.x != x || tile.y != y || tile.z != z || planeAt(x, y, z) == null) {
            throw new IllegalArgumentException("Crossing tile is not a world tile: " + tile);
        }
    }

    @Override
    public boolean tryStep(int x, int y, int z, MoveIntent input, Vector3 target) {
        MapPlane current = planeAt(x, y, z);
        if (current == null || input == null || input == MoveIntent.NONE) return false;
        current.gravity.step(input, step);
        target.set(x, y, z).add(step);
        int toX = round(target.x), toY = round(target.y), toZ = round(target.z);
        if (current.contains(toX, toY, toZ)) return current.canStep(x, y, z, toX, toY, toZ);

        Crossing crossing = crossingAt(x, y, z, input);
        if (crossing != null) {
            toX = crossing.toX; toY = crossing.toY; toZ = crossing.toZ;
            target.set(toX, toY, toZ);
        }
        MapPlane destination = planeAt(toX, toY, toZ);
        return destination != null && destination.isWalkable(toX, toY, toZ);
    }

    @Override
    public void worldPosition(int x, int y, int z, Vector3 out) {
        MapPlane plane = planeAt(x, y, z);
        if (plane == null) throw new IllegalArgumentException("No plane at tile " + x + "," + y + "," + z);
        plane.worldPosition(x, y, z, out);
    }

    @Override
    public void stepArc(int fromX, int fromY, int fromZ, int toX, int toY, int toZ,
                        float progress, Vector3 out) {
        out.setZero();
        if (crossingArcHeight <= 0f) return;
        MapPlane from = planeAt(fromX, fromY, fromZ);
        MapPlane to = planeAt(toX, toY, toZ);
        if (from == null || to == null || from == to) return;
        // Both normals point out of the walking planes, so their sum clears the corner the step crosses.
        from.getNormal(fromNormal);
        to.getNormal(toNormal);
        out.set(fromNormal).add(toNormal);
        if (out.len2() < 0.000001f) return;
        out.nor().scl(crossingArcHeight * MathUtils.sin(MathUtils.PI * MathUtils.clamp(progress, 0f, 1f)));
    }

    private Crossing crossingAt(int x, int y, int z, MoveIntent input) {
        for (int i = 0; i < crossings.size(); i++) {
            if (crossings.get(i).starts(x, y, z, input)) return crossings.get(i);
        }
        return null;
    }

    private static int round(float value) { return Math.round(value); }
}
