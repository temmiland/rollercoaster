package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public final class WorldShaderProvider extends BaseShaderProvider {
    private final LightingEnvironment lighting;
    private final DirectionalShadowMap shadows;

    public WorldShaderProvider() {
        this(new LightingEnvironment(), null);
    }

    public WorldShaderProvider(LightingEnvironment lighting) {
        this(lighting, null);
    }

    public WorldShaderProvider(LightingEnvironment lighting, DirectionalShadowMap shadows) {
        if (lighting == null) throw new IllegalArgumentException("Lighting environment is required");
        this.lighting = lighting;
        this.shadows = shadows;
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        if (renderable.userData == BillboardRenderer.TAG) return new BillboardShader(lighting, shadows);
        return new GeometryShader(lighting, shadows, renderable);
    }
}
