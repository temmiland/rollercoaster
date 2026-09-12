package land.temmi.rollercoaster.world;

import land.temmi.rollercoaster.actor.GridActor;

/**
 * The single movement rule shared by the runtime and the editor.
 *
 * <p>A step is judged at the edge the two tiles share, not between their centres: two ramps that
 * meet seamlessly differ by zero there, while a cliff of a full level differs by one. That keeps
 * {@code maxStepHeight} a statement about climbing rather than about tile spacing.
 */
public final class TerrainRules implements GridActor.TileAccess {
    /** Numerical tolerance for flush terrain edges; ramps provide the climb between complete levels. */
    public static final float DEFAULT_MAX_STEP_HEIGHT = 0.0001f;

    public enum Step {
        ALLOWED,
        /** Target lies outside the map. */
        OUTSIDE_MAP,
        /** Target is blocked by the collision layer or is a non-walkable tile type. */
        BLOCKED,
        /** The two surfaces meet with a height difference larger than {@code maxStepHeight}. */
        TOO_STEEP
    }

    private final TileMap map;
    private final TerrainSurface surface;
    private float maxStepHeight = DEFAULT_MAX_STEP_HEIGHT;

    public TerrainRules(TileMap map) {
        this(new TerrainSurface(map));
    }

    public TerrainRules(TerrainSurface surface) {
        if (surface == null) throw new IllegalArgumentException("Terrain surface is required");
        this.surface = surface;
        this.map = surface.getMap();
    }

    public TerrainRules setMaxStepHeight(float maxStepHeight) {
        if (maxStepHeight < 0f) throw new IllegalArgumentException("Max step height must not be negative");
        this.maxStepHeight = maxStepHeight;
        return this;
    }

    public float getMaxStepHeight() { return maxStepHeight; }
    public TerrainSurface getSurface() { return surface; }

    /** Full diagnosis of a one-tile step, for editor overlays and error messages. */
    public Step step(int fromX, int fromZ, int dx, int dz) {
        int toX = fromX + dx;
        int toZ = fromZ + dz;
        if (!map.contains(fromX, fromZ) || !map.contains(toX, toZ)) return Step.OUTSIDE_MAP;
        if (!map.isWalkable(toX, toZ)) return Step.BLOCKED;
        float exit = surface.heightAtEdge(fromX, fromZ, dx, dz);
        float entry = surface.heightAtEdge(toX, toZ, -dx, -dz);
        if (Math.abs(entry - exit) > maxStepHeight) return Step.TOO_STEEP;
        return Step.ALLOWED;
    }

    @Override
    public boolean canStep(int fromX, int fromZ, int dx, int dz) {
        return step(fromX, fromZ, dx, dz) == Step.ALLOWED;
    }

    @Override
    public boolean canStep(int fromX, int fromZ, int dx, int dz, float currentHeight) {
        return step(fromX, fromZ, dx, dz, currentHeight) == Step.ALLOWED;
    }

    @Override
    public float heightAt(int x, int z) {
        return map.contains(x, z) ? surface.heightAtCenter(x, z) : 0f;
    }

    @Override
    public float heightAt(int x, int z, float currentHeight) {
        return map.contains(x, z) ? surface.heightAtCenter(x, z, currentHeight) : 0f;
    }

    private Step step(int fromX, int fromZ, int dx, int dz, float currentHeight) {
        int toX = fromX + dx;
        int toZ = fromZ + dz;
        if (!map.contains(fromX, fromZ) || !map.contains(toX, toZ)) return Step.OUTSIDE_MAP;
        if (!map.isWalkable(toX, toZ)) return Step.BLOCKED;
        float exit = surface.heightAtEdge(fromX, fromZ, dx, dz, currentHeight);
        float entry = surface.heightAtEdge(toX, toZ, -dx, -dz, currentHeight);
        if (Math.abs(entry - exit) > maxStepHeight) return Step.TOO_STEEP;
        return Step.ALLOWED;
    }
}
