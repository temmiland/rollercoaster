package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

/**
 * A camera-facing quad anchored at its world-space foot point.
 *
 * <p>Shares its mesh with every other renderer using the same {@link BillboardQuad}, so changing
 * the sprite region costs a uniform update rather than a vertex buffer upload.
 */
public final class BillboardRenderer implements RenderableProvider, Disposable {
    public static final Object TAG = new Object();

    private final BillboardQuad quad;
    private final Material material;
    private final TextureAttribute textureAttribute;
    private final Texture texture;
    private final TextureRegion region;
    private final Vector3 center = new Vector3();
    private final Vector3 up = new Vector3(Vector3.Y);
    private final Vector3 right = new Vector3(Vector3.X);
    private final Vector3 anchor = new Vector3();
    private float worldHeight;
    private float aspect = 0.75f;
    private float bottomPadding;
    private boolean visible = true;

    public BillboardRenderer(BillboardQuad quad, Texture texture, TextureRegion region, float worldHeight) {
        if (quad == null || texture == null || region == null) {
            throw new IllegalArgumentException("Quad, texture and region are required");
        }
        this.quad = quad;
        this.texture = texture;
        this.region = new TextureRegion(region);
        this.worldHeight = worldHeight;
        textureAttribute = TextureAttribute.createDiffuse(texture);
        material = new Material(textureAttribute);
        applyRegion();
    }

    public void setPosition(float x, float y, float z) { center.set(x, y, z); }
    public void setPosition(Vector3 position) { center.set(position); }
    public Vector3 getPosition() { return center; }
    public void setWorldHeight(float worldHeight) { this.worldHeight = worldHeight; }
    public void setAspect(float aspect) { this.aspect = aspect; }

    /**
     * Axes the quad spans, normally the camera's own right and up. A folded map whose walking plane
     * turns passes axes rolled in the image plane instead, which stands the sprite on that plane.
     */
    public void setBasis(Vector3 right, Vector3 up) {
        if (right == null || up == null || right.len2() < 0.000001f || up.len2() < 0.000001f) {
            throw new IllegalArgumentException("Billboard axes are required");
        }
        this.right.set(right).nor();
        this.up.set(up).nor();
    }

    public void setBottomPadding(float fraction) {
        if (fraction < 0f || fraction >= 1f) throw new IllegalArgumentException("Invalid billboard bottom padding");
        bottomPadding = fraction;
    }
    public void setVisible(boolean visible) { this.visible = visible; }

    public void setRegion(TextureRegion next) {
        if (next == null || next.getTexture() != texture) {
            throw new IllegalArgumentException("Region must belong to the billboard texture");
        }
        if (next.getU() == region.getU() && next.getV() == region.getV()
            && next.getU2() == region.getU2() && next.getV2() == region.getV2()) {
            return;
        }
        region.setRegion(next);
        applyRegion();
    }

    /** Feeds the region into the shader's UV transform; the shared quad keeps its 0..1 UVs. */
    private void applyRegion() {
        textureAttribute.offsetU = region.getU();
        textureAttribute.offsetV = region.getV();
        textureAttribute.scaleU = region.getU2() - region.getU();
        textureAttribute.scaleV = region.getV2() - region.getV();
        if (region.getRegionHeight() > 0) {
            aspect = region.getRegionWidth() / (float) region.getRegionHeight();
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (!visible) return;
        Renderable renderable = pool.obtain();
        renderable.meshPart.set(quad.meshPart);
        renderable.material = material;
        renderable.userData = TAG;
        // The quad's axes travel in the transform itself, so each sprite can stand on its own plane.
        anchor.set(center).mulAdd(up, -worldHeight * bottomPadding);
        float[] m = renderable.worldTransform.idt().val;
        m[Matrix4.M00] = right.x * worldHeight * aspect;
        m[Matrix4.M10] = right.y * worldHeight * aspect;
        m[Matrix4.M20] = right.z * worldHeight * aspect;
        m[Matrix4.M01] = up.x * worldHeight;
        m[Matrix4.M11] = up.y * worldHeight;
        m[Matrix4.M21] = up.z * worldHeight;
        m[Matrix4.M03] = anchor.x;
        m[Matrix4.M13] = anchor.y;
        m[Matrix4.M23] = anchor.z;
        renderables.add(renderable);
    }

    public TextureRegion getRegion() { return region; }
    public Texture getTexture() { return texture; }

    /** The shared quad outlives individual renderers and is disposed by its owner. */
    @Override public void dispose() { material.clear(); }
}
