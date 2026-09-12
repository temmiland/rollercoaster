package land.temmi.rollercoaster.world;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

/** Local tile geometry; copied into chunks before disposal. */
public final class TilePrototype implements Disposable {
    final Model model;

    public TilePrototype(Model model) {
        this.model = model;
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}
