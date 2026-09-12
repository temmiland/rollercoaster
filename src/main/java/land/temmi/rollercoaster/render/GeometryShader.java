package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

final class GeometryShader implements Shader {
    private final boolean colored;
    private final boolean textured;
    private final boolean normals;
    private ShaderProgram program;
    private RenderContext context;
    private final LightingEnvironment lighting;
    private final LightingUniforms lightingUniforms = new LightingUniforms();

    GeometryShader(LightingEnvironment lighting, Renderable renderable) {
        this.lighting = lighting;
        colored = hasColor(renderable);
        textured = renderable.material.has(TextureAttribute.Diffuse);
        normals = (renderable.meshPart.mesh.getVertexAttributes().getMask() & Usage.Normal) != 0;
        if (!canRender(renderable)) {
            throw new GdxRuntimeException("World geometry requires positions, UVs for textures, and an opaque material");
        }
    }

    private static boolean hasColor(Renderable renderable) {
        long mask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        return (mask & (Usage.ColorPacked | Usage.ColorUnpacked)) != 0;
    }

    @Override
    public void init() {
        String prefix = (colored ? "#define vertexColor\n" : "")
            + (textured ? "#define diffuseTexture\n" : "")
            + (normals ? "#define vertexNormal\n" : "");
        String path = "land/temmi/rollercoaster/render/";
        program = new ShaderProgram(prefix + Gdx.files.classpath(path + "geometry.vert").readString("UTF-8"),
            prefix + Gdx.files.classpath(path + "geometry.frag").readString("UTF-8"));
        if (!program.isCompiled()) {
            String log = program.getLog();
            program.dispose();
            throw new GdxRuntimeException("Geometry shader compilation failed: " + log);
        }
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        program.bind();
        program.setUniformMatrix("u_projViewTrans", camera.combined);
        lightingUniforms.apply(program, lighting);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setDepthMask(true);
        context.setCullFace(GL20.GL_BACK);
        context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        ColorAttribute tint = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
        program.setUniformf("u_diffuseColor", tint == null ? Color.WHITE : tint.color);
        if (textured) {
            TextureAttribute texture = (TextureAttribute) renderable.material.get(TextureAttribute.Diffuse);
            program.setUniformi("u_diffuseTexture", context.textureBinder.bind(texture.textureDescription));
            program.setUniformf("u_uvTransform", texture.offsetU, texture.offsetV, texture.scaleU, texture.scaleV);
        }
        renderable.meshPart.render(program);
    }

    @Override
    public boolean canRender(Renderable renderable) {
        long mask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        return (mask & Usage.Position) != 0 && colored == hasColor(renderable)
            && textured == renderable.material.has(TextureAttribute.Diffuse)
            && (!textured || ((TextureAttribute) renderable.material.get(TextureAttribute.Diffuse)).uvIndex == 0
                && renderable.meshPart.mesh.getVertexAttributes().findByUsage(Usage.TextureCoordinates) != null
                && renderable.meshPart.mesh.getVertexAttributes().findByUsage(Usage.TextureCoordinates).unit == 0)
            && !renderable.material.has(BlendingAttribute.Type);
    }

    @Override
    public int compareTo(Shader other) { return 0; }

    @Override
    public void end() { context = null; }

    @Override
    public void dispose() { program.dispose(); }
}
