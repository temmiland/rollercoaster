package land.temmi.rollercoaster.actor;

import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.input.MoveIntent;

/** Moves one whole tile at a time while exposing smooth world coordinates. */
public final class GridActor {
    private static final float WORLD_OFFSET_X = -0.5f;
    private static final float WORLD_OFFSET_Z = -0.5f;
    /** Half a tile level, so a ramp tile is climbable but the cliff beside it is not. */
    private static final float DEFAULT_MAX_STEP_HEIGHT = 0.5f;

    public interface TileAccess {
        boolean canEnter(int x, int z);

        /** World-Y of the walkable surface on that tile. */
        default float heightAt(int x, int z) { return 0f; }
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
    private float maxStepHeight = DEFAULT_MAX_STEP_HEIGHT;
    private boolean moving;
    private TileAccess tileAccess;

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

    /** Largest height difference a single step may cross; above it the step is refused. */
    public void setMaxStepHeight(float maxStepHeight) {
        if (maxStepHeight < 0f) throw new IllegalArgumentException("Max step height must not be negative");
        this.maxStepHeight = maxStepHeight;
    }

    public void update(float delta, MoveIntent intent) {
        if (moving) {
            progress = Math.min(1f, progress + Math.max(0f, delta) * speed);
            float fromY = heightAt(tileX, tileZ);
            float toY = heightAt(targetX, targetZ);
            position.set(tileX + (targetX - tileX) * progress + WORLD_OFFSET_X,
                fromY + (toY - fromY) * progress,
                tileZ + (targetZ - tileZ) * progress + WORLD_OFFSET_Z);
            if (progress >= 1f) {
                tileX = targetX;
                tileZ = targetZ;
                position.set(tileX + WORLD_OFFSET_X, heightAt(tileX, tileZ), tileZ + WORLD_OFFSET_Z);
                moving = false;
            }
            return;
        }
        if (intent == null || intent == MoveIntent.NONE) return;
        facing = intent.facing;
        int nextX = tileX + facing.dx;
        int nextZ = tileZ + facing.dz;
        if (!inside(nextX, nextZ) || tileAccess != null && !tileAccess.canEnter(nextX, nextZ)) return;
        if (Math.abs(heightAt(nextX, nextZ) - heightAt(tileX, tileZ)) > maxStepHeight) return;
        targetX = nextX;
        targetZ = nextZ;
        progress = 0f;
        moving = true;
    }

    private float heightAt(int x, int z) { return tileAccess == null ? 0f : tileAccess.heightAt(x, z); }
    private boolean inside(int x, int z) { return x >= 0 && x < width && z >= 0 && z < depth; }
    public Vector3 getPosition() { return position; }
    public int getTileX() { return tileX; }
    public int getTileZ() { return tileZ; }
    public Facing getFacing() { return facing; }
    public boolean isMoving() { return moving; }
}
