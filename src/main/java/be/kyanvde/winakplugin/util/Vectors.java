package be.kyanvde.winakplugin.util;

import org.bukkit.util.Vector;

/**
 * Small vector helpers that Bukkit's {@link Vector} does not provide.
 */
public final class Vectors {

    private Vectors() {
    }

    /**
     * Linearly interpolates between two vectors without modifying either.
     * @param a The vector at {@code t = 0}.
     * @param b The vector at {@code t = 1}.
     * @param t The interpolation factor.
     * @return A new vector between {@code a} and {@code b}.
     */
    public static Vector lerp(Vector a, Vector b, double t) {
        return a.clone().multiply(1.0D - t).add(b.clone().multiply(t));
    }

    /**
     * @param yawDegrees A Minecraft yaw in degrees.
     * @return The horizontal unit vector pointing in that direction.
     */
    public static Vector horizontal(float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
    }
}
