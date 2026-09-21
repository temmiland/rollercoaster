package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

final class BillboardShader implements Shader {
    private ShaderProgram program;
    private RenderContext context;
    private Camera camera;
    private final LightingEnvironment lighting;
    private final DirectionalShadowMap shadows;
    private final LightingUniforms lightingUniforms = new LightingUniforms();
    private final ShadowUniforms shadowUniforms = new ShadowUniforms();
    private final Matrix4 inverseProjectionView = new Matrix4();

    BillboardShader(LightingEnvironment lighting, DirectionalShadowMap shadows) {
        this.lighting = lighting;
        this.shadows = shadows;
    }

    @Override
    public void init() {
        String path = "land/temmi/rollercoaster/render/";
        String prefix = shadows == null ? "" : "#define directionalShadow\n";
        program = new ShaderProgram(
            prefix + Gdx.files.classpath(path + "billboard.vert").readString("UTF-8"),
            prefix + Gdx.files.classpath(path + "billboard.frag").readString("UTF-8"));
        if (!program.isCompiled()) {
            String log = program.getLog();
            program.dispose();
            throw new GdxRuntimeException("Billboard shader compilation failed: " + log);
        }
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.bind();
        program.setUniformMatrix("u_projViewTrans", camera.combined);
        inverseProjectionView.set(camera.combined).inv();
        program.setUniformf("u_targetSize", camera.viewportWidth, camera.viewportHeight);
        lightingUniforms.apply(program, lighting);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setDepthMask(true);
        context.setCullFace(GL20.GL_NONE);
        context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void render(Renderable renderable) {
        TextureAttribute texture = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        program.setUniformi("u_texture", context.textureBinder.bind(texture.textureDescription));
        program.setUniformf("u_uvTransform", texture.offsetU, texture.offsetV, texture.scaleU, texture.scaleV);
        BillboardDepthAttribute depth = (BillboardDepthAttribute) renderable.material.get(BillboardDepthAttribute.TYPE);
        if (depth == null) {
            program.setUniformf("u_depthPlane", 0f, 0f, 0f, 0f);
        } else {
            // Planes transform by the inverse transpose, including the camera's pixel snap.
            float[] m = inverseProjectionView.val;
            float x = depth.normal.x, y = depth.normal.y, z = depth.normal.z, w = depth.offset;
            program.setUniformf("u_depthPlane",
                m[Matrix4.M00] * x + m[Matrix4.M10] * y + m[Matrix4.M20] * z + m[Matrix4.M30] * w,
                m[Matrix4.M01] * x + m[Matrix4.M11] * y + m[Matrix4.M21] * z + m[Matrix4.M31] * w,
                m[Matrix4.M02] * x + m[Matrix4.M12] * y + m[Matrix4.M22] * z + m[Matrix4.M32] * w,
                m[Matrix4.M03] * x + m[Matrix4.M13] * y + m[Matrix4.M23] * z + m[Matrix4.M33] * w);
        }
        if (shadows != null) {
            // Sprites are lit directly and do not receive the ground shadow map.
            program.setUniformi("u_shadowReceiver", 0);
            shadowUniforms.apply(program, shadows, context.textureBinder);
        }
        renderable.meshPart.render(program);
    }

    @Override
    public boolean canRender(Renderable renderable) {
        return renderable.userData == BillboardRenderer.TAG
            && renderable.material.has(TextureAttribute.Diffuse)
            && renderable.meshPart.mesh.getVertexAttributes().findByUsage(Usage.Position) != null
            && renderable.meshPart.mesh.getVertexAttributes().findByUsage(Usage.TextureCoordinates) != null;
    }

    @Override public int compareTo(Shader other) { return 0; }
    @Override public void end() { context = null; camera = null; }
    @Override public void dispose() { if (program != null) program.dispose(); }
}
