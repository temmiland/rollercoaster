package land.temmi.rollercoaster.input;

import land.temmi.rollercoaster.actor.Facing;

public enum MoveIntent {
    NONE(null), UP(Facing.NORTH), RIGHT(Facing.EAST), DOWN(Facing.SOUTH), LEFT(Facing.WEST);
    public final Facing facing;
    MoveIntent(Facing facing) { this.facing = facing; }
}
