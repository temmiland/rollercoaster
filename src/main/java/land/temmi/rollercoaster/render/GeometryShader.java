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
import com.badlogic.gdx.math.Matrix3;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import land.temmi.rollercoaster.world.WorldScene;

final class GeometryShader implements Shader {
    private final boolean colored;
    private final boolean textured;
    private final boolean emissiveTextured;
    private final boolean normals;
    private ShaderProgram program;
    private final Matrix3 normalMatrix = new Matrix3();
    private RenderContext context;
    private final LightingEnvironment lighting;
    private final DirectionalShadowMap shadows;
    private final LightingUniforms lightingUniforms = new LightingUniforms();
    private final ShadowUniforms shadowUniforms = new ShadowUniforms();

    GeometryShader(LightingEnvironment lighting, DirectionalShadowMap shadows, Renderable renderable) {
        this.lighting = lighting;
        this.shadows = shadows;
        colored = hasColor(renderable);
        textured = diffuse(renderable) != null;
        emissiveTextured = emissive(renderable) != null;
        normals = (renderable.meshPart.mesh.getVertexAttributes().getMask() & Usage.Normal) != 0;
        if (!canRender(renderable)) {
            throw new GdxRuntimeException("World geometry requires positions, UVs for textures, and an opaque material");
        }
    }

    private static TextureAttribute diffuse(Renderable r) {
        TextureAttribute pbr = (TextureAttribute) r.material.get(PBRTextureAttribute.BaseColorTexture);
        return pbr != null ? pbr : (TextureAttribute) r.material.get(TextureAttribute.Diffuse);
    }

    private static TextureAttribute emissive(Renderable r) {
        TextureAttribute pbr = (TextureAttribute) r.material.get(PBRTextureAttribute.EmissiveTexture);
        return pbr != null ? pbr : (TextureAttribute) r.material.get(TextureAttribute.Emissive);
    }

    private static boolean validUv(Renderable r, TextureAttribute texture) {
        return texture == null || texture.uvIndex == 0
            && r.meshPart.mesh.getVertexAttributes().findByUsage(Usage.TextureCoordinates) != null
            && r.meshPart.mesh.getVertexAttributes().findByUsage(Usage.TextureCoordinates).unit == 0;
    }

    private static boolean hasColor(Renderable renderable) {
        long mask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        return (mask & (Usage.ColorPacked | Usage.ColorUnpacked)) != 0;
    }

    @Override
    public void init() {
        String prefix = (colored ? "#define vertexColor\n" : "")
            + (textured ? "#define diffuseTexture\n" : "")
            + (emissiveTextured ? "#define emissiveTexture\n" : "")
            + (normals ? "#define vertexNormal\n" : "")
            + (shadows != null ? "#define directionalShadow\n" : "");
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
        if (normals) program.setUniformMatrix("u_normalMatrix", normalMatrix.set(renderable.worldTransform).inv().transpose());
        ColorAttribute tint = (ColorAttribute) renderable.material.get(PBRColorAttribute.BaseColorFactor);
        if (tint == null) tint = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
        ColorAttribute glow = (ColorAttribute) renderable.material.get(ColorAttribute.Emissive);
        PBRFloatAttribute intensity = (PBRFloatAttribute) renderable.material.get(PBRFloatAttribute.EmissiveIntensity);
        float strength = intensity == null ? 1f : intensity.value;
        Color color = glow == null ? Color.BLACK : glow.color;
        program.setUniformf("u_emissiveColor", color.r * strength, color.g * strength, color.b * strength);
        if (emissiveTextured) {
            TextureAttribute texture = emissive(renderable);
            program.setUniformi("u_emissiveTexture", context.textureBinder.bind(texture.textureDescription));
            program.setUniformf("u_emissiveUvTransform", texture.offsetU, texture.offsetV, texture.scaleU, texture.scaleV);
        }
        program.setUniformf("u_diffuseColor", tint == null ? Color.WHITE : tint.color);
        if (textured) {
            TextureAttribute texture = diffuse(renderable);
            program.setUniformi("u_diffuseTexture", context.textureBinder.bind(texture.textureDescription));
            program.setUniformf("u_uvTransform", texture.offsetU, texture.offsetV, texture.scaleU, texture.scaleV);
        }
        if (shadows != null) {
            // The shadow map is a ground effect. Props and other vertical geometry still receive
            // direct sun/point/ambient light, but are never darkened by the map themselves.
            program.setUniformi("u_shadowReceiver", renderable.userData == WorldScene.TERRAIN_TAG ? 1 : 0);
            shadowUniforms.apply(program, shadows, context.textureBinder);
        }
        renderable.meshPart.render(program);
    }

    @Override
    public boolean canRender(Renderable renderable) {
        long mask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        return renderable.userData != BillboardRenderer.TAG
            && normals == ((mask & Usage.Normal) != 0)
            && (mask & Usage.Position) != 0 && colored == hasColor(renderable)
            && textured == (diffuse(renderable) != null)
            && emissiveTextured == (emissive(renderable) != null)
            && validUv(renderable, diffuse(renderable)) && validUv(renderable, emissive(renderable))
            && !renderable.material.has(BlendingAttribute.Type);
    }

    @Override
    public int compareTo(Shader other) { return 0; }

    @Override
    public void end() { context = null; }

    @Override
    public void dispose() { program.dispose(); }
}
