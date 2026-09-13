package land.temmi.rollercoaster.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.actor.GridActor;
import land.temmi.rollercoaster.input.MoveIntent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A room folded out of several walking planes, sharing one fixed X/Y/Z tile grid.
 *
 * <p>Walking within a plane is the plane's own business. Crossing to another one happens where the
 * planes touch, or across an authored seam where they do not: a step whose direction leaves the
 * current plane is the only case a seam is consulted for, because the way back off a plane always
 * points along the normal it just gained, which no input maps to.
 */
public final class SurfaceRoom implements GridActor.MovementSpace {
    private final List<SurfacePlatform> platforms = new ArrayList<>();
    private final List<Seam> seams = new ArrayList<>();
    private final Vector3 step = new Vector3();
    private final Vector3 fromNormal = new Vector3();
    private final Vector3 toNormal = new Vector3();
    private final Vector3 scratch = new Vector3();
    private float seamArcHeight = 0.35f;

    private static final class Seam {
        final int fromX, fromY, fromZ, toX, toY, toZ;
        final MoveIntent input;

        Seam(int fromX, int fromY, int fromZ, MoveIntent input, int toX, int toY, int toZ) {
            this.fromX = fromX; this.fromY = fromY; this.fromZ = fromZ;
            this.toX = toX; this.toY = toY; this.toZ = toZ;
            this.input = input;
        }

        boolean starts(int x, int y, int z, MoveIntent from) {
            return input == from && fromX == x && fromY == y && fromZ == z;
        }
    }

    /** How far a step that changes plane bulges away from both planes on its way over. */
    public SurfaceRoom setSeamArcHeight(float seamArcHeight) {
        if (seamArcHeight < 0f) throw new IllegalArgumentException("Seam arc height must not be negative");
        this.seamArcHeight = seamArcHeight;
        return this;
    }

    public SurfaceRoom add(SurfacePlatform platform) {
        if (platform == null) throw new IllegalArgumentException("Platform required");
        for (int u = 0; u < platform.map.tiles.getWidth(); u++) {
            for (int v = 0; v < platform.map.tiles.getDepth(); v++) {
                platform.tile(u, v, scratch);
                if (platformAt(round(scratch.x), round(scratch.y), round(scratch.z)) != null) {
                    throw new IllegalArgumentException("Overlapping platform footprints at " + scratch);
                }
            }
        }
        platforms.add(platform);
        return this;
    }

    public List<SurfacePlatform> platforms() { return Collections.unmodifiableList(platforms); }

    /**
     * Room tile of the first entity of that type on any plane, or null. Planes carry the room's
     * markers - where the player arrives, where it leads out - as ordinary map entities.
     */
    public Vector3 entityTile(String type, Vector3 out) {
        if (type == null || out == null) throw new IllegalArgumentException("Entity type and output are required");
        for (int i = 0; i < platforms.size(); i++) {
            SurfacePlatform platform = platforms.get(i);
            for (MapEntity entity : platform.map.entities) {
                if (type.equals(entity.type)) return platform.tile(entity.x, entity.z, out);
            }
        }
        return null;
    }

    public SurfacePlatform platformAt(int x, int y, int z) {
        for (int i = 0; i < platforms.size(); i++) {
            if (platforms.get(i).contains(x, y, z)) return platforms.get(i);
        }
        return null;
    }

    /**
     * Links two room tiles in both directions. Each direction carries its own input, because the
     * step that leaves a plane and the step that returns are expressed in different planes.
     */
    public SurfaceRoom seam(Vector3 a, MoveIntent fromA, Vector3 b, MoveIntent fromB) {
        addSeam(a, fromA, b);
        addSeam(b, fromB, a);
        return this;
    }

    private void addSeam(Vector3 from, MoveIntent input, Vector3 to) {
        requireTile(from);
        requireTile(to);
        if (input == null || input == MoveIntent.NONE) throw new IllegalArgumentException("Seam needs a direction");
        int x = round(from.x), y = round(from.y), z = round(from.z);
        for (int i = 0; i < seams.size(); i++) {
            if (seams.get(i).starts(x, y, z, input)) {
                throw new IllegalArgumentException("Duplicate seam at " + from + " towards " + input);
            }
        }
        seams.add(new Seam(x, y, z, input, round(to.x), round(to.y), round(to.z)));
    }

    private void requireTile(Vector3 tile) {
        if (tile == null) throw new IllegalArgumentException("Seam tile required");
        int x = round(tile.x), y = round(tile.y), z = round(tile.z);
        if (tile.x != x || tile.y != y || tile.z != z || platformAt(x, y, z) == null) {
            throw new IllegalArgumentException("Seam tile is not a room tile: " + tile);
        }
    }

    @Override
    public boolean tryStep(int x, int y, int z, MoveIntent input, Vector3 target) {
        SurfacePlatform current = platformAt(x, y, z);
        if (current == null || input == null || input == MoveIntent.NONE) return false;
        current.gravity.step(input, step);
        target.set(x, y, z).add(step);
        int toX = round(target.x), toY = round(target.y), toZ = round(target.z);
        if (current.contains(toX, toY, toZ)) return current.canStep(x, y, z, toX, toY, toZ);

        Seam seam = seamAt(x, y, z, input);
        if (seam != null) {
            toX = seam.toX; toY = seam.toY; toZ = seam.toZ;
            target.set(toX, toY, toZ);
        }
        SurfacePlatform destination = platformAt(toX, toY, toZ);
        return destination != null && destination.isWalkable(toX, toY, toZ);
    }

    @Override
    public void worldPosition(int x, int y, int z, Vector3 out) {
        SurfacePlatform platform = platformAt(x, y, z);
        if (platform == null) throw new IllegalArgumentException("No platform at tile " + x + "," + y + "," + z);
        platform.worldPosition(x, y, z, out);
    }

    @Override
    public void stepArc(int fromX, int fromY, int fromZ, int toX, int toY, int toZ,
                        float progress, Vector3 out) {
        out.setZero();
        if (seamArcHeight <= 0f) return;
        SurfacePlatform from = platformAt(fromX, fromY, fromZ);
        SurfacePlatform to = platformAt(toX, toY, toZ);
        if (from == null || to == null || from == to) return;
        // Both normals point out of the room, so their sum clears the corner the step crosses.
        from.getNormal(fromNormal);
        to.getNormal(toNormal);
        out.set(fromNormal).add(toNormal);
        if (out.len2() < 0.000001f) return;
        out.nor().scl(seamArcHeight * MathUtils.sin(MathUtils.PI * MathUtils.clamp(progress, 0f, 1f)));
    }

    private Seam seamAt(int x, int y, int z, MoveIntent input) {
        for (int i = 0; i < seams.size(); i++) {
            if (seams.get(i).starts(x, y, z, input)) return seams.get(i);
        }
        return null;
    }

    private static int round(float value) { return Math.round(value); }
}
