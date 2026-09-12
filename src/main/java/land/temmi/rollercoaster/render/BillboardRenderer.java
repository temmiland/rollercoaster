package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/** A reusable camera-facing quad anchored at its world-space foot point. */
public final class BillboardRenderer implements RenderableProvider, Disposable {
    public static final Object TAG = new Object();

    private final Mesh mesh;
    private final MeshPart meshPart;
    private final Material material;
    private final Texture texture;
    private final TextureRegion region;
    private final Matrix4 transform = new Matrix4();
    private final Vector3 center = new Vector3();
    private float worldHeight = 1.8f;
    private float aspect = 0.75f;
    private boolean visible = true;

    public BillboardRenderer(Texture texture, TextureRegion region, float worldHeight) {
        if (texture == null || region == null) throw new IllegalArgumentException("Texture and region are required");
        this.texture = texture;
        this.region = new TextureRegion(region);
        this.worldHeight = worldHeight;
        if (region.getRegionHeight() > 0) aspect = region.getRegionWidth() / (float) region.getRegionHeight();
        mesh = new Mesh(true, 4, 6, com.badlogic.gdx.graphics.VertexAttribute.Position(),
            com.badlogic.gdx.graphics.VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[] {-0.5f, -0.5f, 0f, region.getU(), region.getV(),
            0.5f, -0.5f, 0f, region.getU2(), region.getV(),
            0.5f, 0.5f, 0f, region.getU2(), region.getV2(),
            -0.5f, 0.5f, 0f, region.getU(), region.getV2()});
        mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
        meshPart = new MeshPart("billboard", mesh, 0, 6, GL20.GL_TRIANGLES);
        material = new Material(com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute.createDiffuse(texture));
    }

    public void setPosition(float x, float y, float z) { center.set(x, y, z); }
    public void setPosition(Vector3 position) { center.set(position); }
    public Vector3 getPosition() { return center; }
    public void setWorldHeight(float worldHeight) { this.worldHeight = worldHeight; }
    public void setAspect(float aspect) { this.aspect = aspect; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setRegion(TextureRegion next) {
        if (next == null || next.getTexture() != texture) throw new IllegalArgumentException("Region must belong to the billboard texture");
        region.setRegion(next);
        if (next.getRegionHeight() > 0) aspect = next.getRegionWidth() / (float) next.getRegionHeight();
        mesh.setVertices(new float[] {-0.5f, -0.5f, 0f, region.getU(), region.getV(),
            0.5f, -0.5f, 0f, region.getU2(), region.getV(),
            0.5f, 0.5f, 0f, region.getU2(), region.getV2(),
            -0.5f, 0.5f, 0f, region.getU(), region.getV2()});
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (!visible) return;
        Renderable renderable = pool.obtain();
        renderable.meshPart.set(meshPart);
        renderable.material = material;
        renderable.userData = TAG;
        renderable.worldTransform.setToTranslation(center);
        renderable.worldTransform.scl(worldHeight * aspect, worldHeight, 1f);
        renderables.add(renderable);
    }

    public TextureRegion getRegion() { return region; }
    public Texture getTexture() { return texture; }

    @Override public void dispose() { mesh.dispose(); material.clear(); }
}
