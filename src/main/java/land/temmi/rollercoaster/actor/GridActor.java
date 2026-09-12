package land.temmi.rollercoaster.actor;

import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.input.MoveIntent;

/** Moves one whole tile at a time while exposing smooth world coordinates. */
public final class GridActor {
    private static final float WORLD_OFFSET_X = -0.5f;
    private static final float WORLD_OFFSET_Z = -0.5f;
    public interface TileAccess { boolean canEnter(int x, int z); }

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
        position.set(x + WORLD_OFFSET_X, 0f, z + WORLD_OFFSET_Z);
    }

    public void setTileAccess(TileAccess tileAccess) { this.tileAccess = tileAccess; }

    public void update(float delta, MoveIntent intent) {
        if (moving) {
            progress = Math.min(1f, progress + Math.max(0f, delta) * speed);
            position.set(tileX + (targetX - tileX) * progress + WORLD_OFFSET_X, 0f,
                tileZ + (targetZ - tileZ) * progress + WORLD_OFFSET_Z);
            if (progress >= 1f) {
                tileX = targetX;
                tileZ = targetZ;
                position.set(tileX + WORLD_OFFSET_X, 0f, tileZ + WORLD_OFFSET_Z);
                moving = false;
            }
            return;
        }
        if (intent == null || intent == MoveIntent.NONE) return;
        facing = intent.facing;
        int nextX = tileX + facing.dx;
        int nextZ = tileZ + facing.dz;
        if (!inside(nextX, nextZ) || tileAccess != null && !tileAccess.canEnter(nextX, nextZ)) return;
        targetX = nextX;
        targetZ = nextZ;
        progress = 0f;
        moving = true;
    }

    private boolean inside(int x, int z) { return x >= 0 && x < width && z >= 0 && z < depth; }
    public Vector3 getPosition() { return position; }
    public int getTileX() { return tileX; }
    public int getTileZ() { return tileZ; }
    public Facing getFacing() { return facing; }
    public boolean isMoving() { return moving; }
}
