package temmiland.rollercoaster.game.state;

import java.util.function.Function;

/**
 * Immutable descriptor that pairs a state name with a factory function for creating
 * the corresponding {@link GameState}.
 *
 * <p>Descriptors are registered with a {@link GameStateManager} at startup and are used
 * as type-safe handles when switching between states at runtime.</p>
 */
public final class GameStateDescriptor<M extends GameStateManager<M>> {

	/** Unique name identifying this state within the manager. */
	private final String name;
	/** Factory that creates a new instance of the state for a given manager. */
	private final Function<M, GameState<?>> factory;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Creates a new descriptor.
	 *
	 * @param name    unique state name
	 * @param factory factory function producing a {@link GameState} for a given manager
	 */
	public GameStateDescriptor(final String name, final Function<M, GameState<?>> factory) {
		this.name = name;
		this.factory = factory;
	}

	// -------------------------------------------------------------------------
	// Accessors
	// -------------------------------------------------------------------------

	/**
	 * Returns the unique name of this state.
	 *
	 * @return state name
	 */
	public String name() {
		return name;
	}

	/**
	 * Creates a new {@link GameState} instance for the given manager.
	 *
	 * @param manager the owning game-state manager
	 * @return a new state instance
	 */
	public GameState<?> create(final M manager) {
		return factory.apply(manager);
	}
}
