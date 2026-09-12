package land.temmi.rollercoaster.actor;

public enum Facing {
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);

    public final int dx;
    public final int dz;

    Facing(int dx, int dz) {
        this.dx = dx;
        this.dz = dz;
    }
}
