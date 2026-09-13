package land.temmi.rollercoaster.actor;

import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.input.MoveIntent;

/** Moves one whole tile at a time while exposing smooth world coordinates. */
public final class GridActor {
    /** Optional fixed 3D grid. The actor retains its speed, input cadence and facing animation. */
    public interface MovementSpace {
        boolean tryStep(int x, int y, int z, MoveIntent input, Vector3 target);
        void worldPosition(int x, int y, int z, Vector3 out);

        /**
         * Offset added to the straight line between both tiles while a step runs, which lets a
         * space arc a step that leaves its plane instead of dragging the actor through geometry.
         */
        default void stepArc(int fromX, int fromY, int fromZ, int toX, int toY, int toZ,
                             float progress, Vector3 out) {
            out.setZero();
        }
    }

    private static final float WORLD_OFFSET_X = -0.5f;
    private static final float WORLD_OFFSET_Z = -0.5f;

    /**
     * Terrain the actor walks on. The step rule itself lives with the terrain, so the editor can
     * ask exactly the same question the runtime does; {@code TerrainRules} is its implementation.
     */
    public interface TileAccess {
        /** Whether a single step from the given tile towards {@code dx}/{@code dz} is allowed. */
        boolean canStep(int fromX, int fromZ, int dx, int dz);

        /** World-Y of the walkable surface at that tile's centre. */
        float heightAt(int x, int z);

        /** World-Y of the surface nearest the actor's current level at that tile's centre. */
        default float heightAt(int x, int z, float currentHeight) { return heightAt(x, z); }

        /** Whether a step is allowed while staying near the actor's current level. */
        default boolean canStep(int fromX, int fromZ, int dx, int dz, float currentHeight) {
            return canStep(fromX, fromZ, dx, dz);
        }
    }

    private final int width;
    private final int depth;
    private final float speed;
    private final Vector3 position = new Vector3();
    private Facing facing = Facing.SOUTH;
    private int tileX;
    private int tileZ;
    private int targetX;
    private int targetZ;
    private float progress;
    private boolean moving;
    private TileAccess tileAccess;
    private MovementSpace movementSpace;
    private int tileY;
    private int fromX, fromY, fromZ;
    private final Vector3 gridTarget = new Vector3();
    private final Vector3 stepFrom = new Vector3();
    private final Vector3 stepTo = new Vector3();
    private final Vector3 stepArc = new Vector3();
    private boolean stepped;

    public void setMovementSpace(MovementSpace space) {
        movementSpace = space;
        moving = false;
        stepped = false;
        progress = 0f;
    }

    public void setGridTile(int x, int y, int z) {
        if (movementSpace == null) throw new IllegalStateException("No 3D movement space");
        movementSpace.worldPosition(x, y, z, position);
        tileX = targetX = fromX = x; tileY = fromY = y; tileZ = targetZ = fromZ = z;
        moving = stepped = false;
        progress = 0f;
    }

    /**
     * True only on the update which commits a step. In a {@link MovementSpace} the tile is
     * committed when the step starts, so a listener reacting to it - a camera following the
     * walking plane, say - animates alongside the step rather than after it.
     */
    public boolean didStep() { return stepped; }
    public int getTileY() { return tileY; }

    public GridActor(int width, int depth, float speed) {
        if (width <= 0 || depth <= 0 || speed <= 0f) throw new IllegalArgumentException("Invalid actor bounds or speed");
        this.width = width;
        this.depth = depth;
        this.speed = speed;
    }

    public void setTile(int x, int z) {
        if (!inside(x, z)) throw new IllegalArgumentException("Tile outside actor bounds");
        tileX = targetX = x;
        tileZ = targetZ = z;
        progress = 0f;
        moving = false;
        position.set(x + WORLD_OFFSET_X, heightAt(x, z), z + WORLD_OFFSET_Z);
    }

    public void setTileAccess(TileAccess tileAccess) {
        this.tileAccess = tileAccess;
        if (!moving) position.y = heightAt(tileX, tileZ);
    }

    public void update(float delta, MoveIntent intent) {
        stepped = false;
        if (movementSpace != null) {
            updateGridSpace(delta, intent);
            return;
        }
        if (moving) {
            progress = Math.min(1f, progress + Math.max(0f, delta) * speed);
            float fromY = heightAt(tileX, tileZ, position.y);
            float toY = heightAt(targetX, targetZ, position.y);
            position.set(tileX + (targetX - tileX) * progress + WORLD_OFFSET_X,
                fromY + (toY - fromY) * progress,
                tileZ + (targetZ - tileZ) * progress + WORLD_OFFSET_Z);
            if (progress >= 1f) {
                tileX = targetX;
                tileZ = targetZ;
                position.set(tileX + WORLD_OFFSET_X, heightAt(tileX, tileZ, position.y), tileZ + WORLD_OFFSET_Z);
                moving = false;
                stepped = true;
            }
            return;
        }
        if (intent == null || intent == MoveIntent.NONE) return;
        facing = intent.facing;
        int nextX = tileX + facing.dx;
        int nextZ = tileZ + facing.dz;
        if (!inside(nextX, nextZ)) return;
        if (tileAccess != null && !tileAccess.canStep(tileX, tileZ, facing.dx, facing.dz, position.y)) return;
        targetX = nextX;
        targetZ = nextZ;
        progress = 0f;
        moving = true;
    }

    private void updateGridSpace(float delta, MoveIntent intent) {
        if (moving) {
            progress = Math.min(1f, progress + Math.max(0f, delta) * speed);
            applyStepPosition();
            if (progress < 1f) return;
            moving = false;
        }
        if (intent == null || intent == MoveIntent.NONE) return;
        facing = intent.facing;
        if (!movementSpace.tryStep(tileX, tileY, tileZ, intent, gridTarget)) return;
        fromX = tileX; fromY = tileY; fromZ = tileZ;
        stepFrom.set(position);
        tileX = targetX = (int) gridTarget.x;
        tileY = (int) gridTarget.y;
        tileZ = targetZ = (int) gridTarget.z;
        movementSpace.worldPosition(tileX, tileY, tileZ, stepTo);
        progress = 0f;
        moving = stepped = true;
        applyStepPosition();
    }

    private void applyStepPosition() {
        movementSpace.stepArc(fromX, fromY, fromZ, tileX, tileY, tileZ, progress, stepArc);
        position.set(stepFrom).lerp(stepTo, progress).add(stepArc);
    }

    private float heightAt(int x, int z) { return tileAccess == null ? 0f : tileAccess.heightAt(x, z); }
    private float heightAt(int x, int z, float currentHeight) {
        return tileAccess == null ? 0f : tileAccess.heightAt(x, z, currentHeight);
    }
    private boolean inside(int x, int z) { return x >= 0 && x < width && z >= 0 && z < depth; }
    public Vector3 getPosition() { return position; }
    public int getTileX() { return tileX; }
    public int getTileZ() { return tileZ; }
    public Facing getFacing() { return facing; }
    public boolean isMoving() { return moving; }
}
