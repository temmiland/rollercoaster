package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

final class ShadowShaderProvider extends BaseShaderProvider {
    @Override
    public Shader getShader(Renderable renderable) {
        Shader suggested = renderable.shader;
        renderable.shader = null;
        try { return super.getShader(renderable); }
        finally { renderable.shader = suggested; }
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        return new ShadowShader();
    }
}
