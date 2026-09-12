package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public final class WorldShaderProvider extends BaseShaderProvider {
    private final LightingEnvironment lighting;

    public WorldShaderProvider() {
        this(new LightingEnvironment());
    }

    public WorldShaderProvider(LightingEnvironment lighting) {
        if (lighting == null) throw new IllegalArgumentException("Lighting environment is required");
        this.lighting = lighting;
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        if (renderable.userData == BillboardRenderer.TAG) return new BillboardShader(lighting);
        return new GeometryShader(lighting, renderable);
    }
}
