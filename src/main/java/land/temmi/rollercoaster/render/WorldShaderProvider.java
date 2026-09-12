package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public final class WorldShaderProvider extends BaseShaderProvider {
    @Override
    protected Shader createShader(Renderable renderable) {
        return new GeometryShader(renderable);
    }
}
