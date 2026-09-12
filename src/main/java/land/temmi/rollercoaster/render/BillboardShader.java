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
import com.badlogic.gdx.utils.GdxRuntimeException;

final class BillboardShader implements Shader {
    private ShaderProgram program;
    private RenderContext context;
    private Camera camera;
    private final LightingEnvironment lighting;
    private final LightingUniforms lightingUniforms = new LightingUniforms();

    BillboardShader(LightingEnvironment lighting) {
        this.lighting = lighting;
    }

    @Override
    public void init() {
        String path = "land/temmi/rollercoaster/render/";
        program = new ShaderProgram(
            Gdx.files.classpath(path + "billboard.vert").readString("UTF-8"),
            Gdx.files.classpath(path + "billboard.frag").readString("UTF-8"));
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
        program.setUniformf("u_targetSize", camera.viewportWidth, camera.viewportHeight);
        float rightLength = (float) Math.sqrt(camera.direction.x * camera.direction.x + camera.direction.z * camera.direction.z);
        program.setUniformf("u_billboardRight", -camera.direction.z / rightLength, 0f, camera.direction.x / rightLength);
        program.setUniformf("u_heightScale", 1f / Math.max(0.001f, camera.up.y));
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
