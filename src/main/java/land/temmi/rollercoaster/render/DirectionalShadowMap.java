package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/** A camera-centred shadow map for the directional sun in a lighting environment. */
public final class DirectionalShadowMap implements Disposable {
    /** Default shadow texture edge length. Override with the size constructor on memory-constrained devices. */
    public static final int DEFAULT_SIZE = 2048;

    private final LightingEnvironment lighting;
    private final FrameBuffer target;
    private final ModelBatch batch;
    private Texture texture;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Matrix4 bias = new Matrix4();
    private final Matrix4 shadowMatrix = new Matrix4();
    private final Vector3 center = new Vector3();
    private final Vector3 lightDirection = new Vector3();
    private final Vector3 right = new Vector3();
    private float worldSize = 32f;
    private float lightDistance = 64f;
    private float depthBias = 0.00035f;
    private float strength = 0.65f;
    private boolean rendered;

    public DirectionalShadowMap(LightingEnvironment lighting) {
        this(lighting, DEFAULT_SIZE);
    }

    public DirectionalShadowMap(LightingEnvironment lighting, int size) {
        if (lighting == null || size <= 0) throw new IllegalArgumentException("Lighting and size are required");
        this.lighting = lighting;
        target = new FrameBuffer(Pixmap.Format.RGBA8888, size, size, true);
        ensureTextureState();
        batch = new ModelBatch(new ShadowShaderProvider());
        bias.val[Matrix4.M00] = 0.5f;
        bias.val[Matrix4.M11] = 0.5f;
        bias.val[Matrix4.M22] = 0.5f;
        bias.val[Matrix4.M03] = 0.5f;
        bias.val[Matrix4.M13] = 0.5f;
        bias.val[Matrix4.M23] = 0.5f;
    }

    /** Renders opaque world geometry around {@code worldCenter} into the sun shadow map. */
    public void render(Vector3 worldCenter, Iterable<? extends RenderableProvider> casters) {
        if (worldCenter == null || casters == null) throw new IllegalArgumentException("Center and casters are required");
        rendered = false;
        if (lighting.getSunIntensity() <= 0f) return;

        center.set(worldCenter);
        lightDirection.set(lighting.getSunDirection());
        camera.viewportWidth = worldSize;
        camera.viewportHeight = worldSize;
        camera.near = 0.1f;
        camera.far = lightDistance * 2f + worldSize;
        camera.position.set(center).mulAdd(lightDirection, lightDistance);
        camera.direction.set(lightDirection).scl(-1f).nor();
        camera.up.set(Math.abs(camera.direction.y) > 0.95f ? Vector3.Z : Vector3.Y);
        camera.normalizeUp();
        camera.update();
        right.set(camera.direction).crs(camera.up).nor();
        float texel = worldSize / target.getWidth();
        float x = camera.position.dot(right);
        float y = camera.position.dot(camera.up);
        camera.position.mulAdd(right, Math.round(x / texel) * texel - x);
        camera.position.mulAdd(camera.up, Math.round(y / texel) * texel - y);
        camera.update();
        shadowMatrix.set(bias).mul(camera.combined);

        boolean dither = Gdx.gl.glIsEnabled(GL20.GL_DITHER);
        target.begin();
        try {
            Gdx.gl.glDisable(GL20.GL_DITHER);
            Gdx.gl.glDepthMask(true);
            Gdx.gl.glClearDepthf(1f);
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            batch.begin(camera);
            batch.render(casters);
            batch.end();
            rendered = true;
        } finally {
            target.end();
            if (dither) Gdx.gl.glEnable(GL20.GL_DITHER);
        }
    }

    public Texture getTexture() {
        ensureTextureState();
        return texture;
    }
    public Matrix4 getShadowMatrix() { return shadowMatrix; }
    public float getTexelSize() { return 1f / target.getWidth(); }
    public float getDepthBias() { return depthBias; }
    public float getStrength() { return strength; }
    public boolean isReady() { return rendered; }

    private void ensureTextureState() {
        Texture current = target.getTextureAttachments().peek();
        if (current == texture) return;
        texture = current;
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
    }

    public DirectionalShadowMap setWorldSize(float worldSize) {
        if (worldSize <= 0f) throw new IllegalArgumentException("Shadow world size must be positive");
        this.worldSize = worldSize;
        return this;
    }

    public DirectionalShadowMap setLightDistance(float lightDistance) {
        if (lightDistance <= 0f) throw new IllegalArgumentException("Shadow light distance must be positive");
        this.lightDistance = lightDistance;
        return this;
    }

    public DirectionalShadowMap setDepthBias(float depthBias) {
        if (depthBias < 0f) throw new IllegalArgumentException("Shadow depth bias must be nonnegative");
        this.depthBias = depthBias;
        return this;
    }

    public DirectionalShadowMap setStrength(float strength) {
        if (strength < 0f || strength > 1f) throw new IllegalArgumentException("Shadow strength must be in [0, 1]");
        this.strength = strength;
        return this;
    }

    @Override
    public void dispose() {
        batch.dispose();
        target.dispose();
    }
}
