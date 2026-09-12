package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

/** Local tile geometry plus its terrain semantics; copied into chunks before disposal. */
public final class TilePrototype implements Disposable {
    final Model model;
    private final TileShape shape;
    private final boolean walkable;

    public TilePrototype(Model model) {
        this(model, TileShape.FLAT, true);
    }

    /** A non-walkable ramp is scenery: it still slopes, but no actor may step onto it. */
    public TilePrototype(Model model, TileShape shape, boolean walkable) {
        if (shape == null) throw new IllegalArgumentException("Tile shape is required");
        this.model = model;
        this.shape = shape;
        this.walkable = walkable;
    }

    public Model getModel() { return model; }
    public TileShape getShape() { return shape; }
    public boolean isWalkable() { return walkable; }

    @Override
    public void dispose() {
        model.dispose();
    }
}
