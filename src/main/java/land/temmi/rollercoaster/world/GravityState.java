package land.temmi.rollercoaster.world;

import com.badlogic.gdx.math.Vector3;
import land.temmi.rollercoaster.input.MoveIntent;

/**
 * One of the four walking planes of a folded map.
 *
 * <p>A folded map keeps a single fixed X/Y/Z grid; the state only decides which world axes the four
 * screen-relative inputs move along. Walls are the two states that leave X fixed and make Y
 * walkable. There is no force, no fall and no simulation.
 */
public enum GravityState {
    FLOOR(0, 1, 0, 0, 0, -1),
    WEST_WALL(1, 0, 0, 0, 1, 0),
    EAST_WALL(-1, 0, 0, 0, 1, 0),
    CEILING(0, -1, 0, 0, 0, 1);

    private final int nx, ny, nz;
    private final int fx, fy, fz;
    private final int rx, ry, rz;

    GravityState(int nx, int ny, int nz, int fx, int fy, int fz) {
        this.nx = nx; this.ny = ny; this.nz = nz;
        this.fx = fx; this.fy = fy; this.fz = fz;
        // Right is forward x normal, which keeps every state a proper rotation of the floor.
        rx = fy * nz - fz * ny;
        ry = fz * nx - fx * nz;
        rz = fx * ny - fy * nx;
    }

    /** Points out of the surface, away from the walking planes: the direction the camera treats as up. */
    public Vector3 normal(Vector3 out) { return out.set(nx, ny, nz); }

    /** World step of {@link MoveIntent#UP}, which is always away from the viewer on screen. */
    public Vector3 forward(Vector3 out) { return out.set(fx, fy, fz); }

    /** World step of {@link MoveIntent#RIGHT}. */
    public Vector3 right(Vector3 out) { return out.set(rx, ry, rz); }

    public Vector3 step(MoveIntent input, Vector3 out) {
        if (input == null) return out.setZero();
        switch (input) {
            case UP: return out.set(fx, fy, fz);
            case DOWN: return out.set(-fx, -fy, -fz);
            case RIGHT: return out.set(rx, ry, rz);
            case LEFT: return out.set(-rx, -ry, -rz);
            default: return out.setZero();
        }
    }
}
