package land.temmi.rollercoaster.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

final class ShadowShader implements Shader {
    private ShaderProgram program;

    @Override
    public void init() {
        String path = "land/temmi/rollercoaster/render/";
        program = new ShaderProgram(
            Gdx.files.classpath(path + "shadow.vert").readString("UTF-8"),
            Gdx.files.classpath(path + "shadow.frag").readString("UTF-8"));
        if (!program.isCompiled()) {
            String log = program.getLog();
            program.dispose();
            throw new GdxRuntimeException("Shadow shader compilation failed: " + log);
        }
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        program.bind();
        program.setUniformMatrix("u_shadowTrans", camera.combined);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setDepthMask(true);
        context.setCullFace(GL20.GL_BACK);
        context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        renderable.meshPart.render(program);
    }

    @Override
    public boolean canRender(Renderable renderable) {
        long mask = renderable.meshPart.mesh.getVertexAttributes().getMask();
        return renderable.userData != BillboardRenderer.TAG
            && (mask & Usage.Position) != 0
            && !renderable.material.has(BlendingAttribute.Type);
    }

    @Override public int compareTo(Shader other) { return 0; }
    @Override public void end() { }
    @Override public void dispose() { if (program != null) program.dispose(); }
}
