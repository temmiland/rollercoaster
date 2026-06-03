package temmiland.rollercoaster.graphics;

/**
 * Minimal contract for anything the engine renders via the texture atlas:
 * a named texture that can be looked up in a {@link TextureAtlas}.
 */
public interface Sprite {
	String getTexture();
}
