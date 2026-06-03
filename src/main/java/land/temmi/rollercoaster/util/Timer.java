package land.temmi.rollercoaster.util;

/**
 * Utility class providing high-resolution time access.
 *
 * Wraps {@link System#nanoTime()} and converts the result to seconds,
 * suitable for measuring elapsed time in a game loop.
 *
 * Not instantiable; all methods are static.
 */
public final class Timer {

    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;

    private Timer() { }

    /**
     * Returns the current value of the JVM high-resolution time source in seconds.
     *
     * The returned value is only meaningful as a difference between two calls;
     * it has no absolute reference point.
     *
     * @return current time in seconds as a double
     */
    public static double getTime() {
        return (double) System.nanoTime() / (double) NANOSECONDS_PER_SECOND;
    }
}
