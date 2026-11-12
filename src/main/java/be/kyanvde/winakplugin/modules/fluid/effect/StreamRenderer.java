package be.kyanvde.winakplugin.modules.fluid.effect;

import be.kyanvde.winakplugin.modules.fluid.model.Droplet;
import be.kyanvde.winakplugin.util.Vectors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Draws a stream of droplets as one continuous line of particles.
 *
 * <p>Neighbouring droplets are joined by a line of short-lived particles that
 * glide along with the flow, so it reads as a single stream rather than dots.
 */
public final class StreamRenderer {

    public static final Color STREAM_COLOR = Color.fromRGB(235, 200, 40);
    private static final Color CORE_COLOR = Color.fromRGB(255, 225, 90);

    /**
     * Neighbouring droplets further apart than this are not joined: the stream breaks up.
     */
    private static final double MAX_JOIN_DISTANCE = 0.9D;

    /**
     * Distance between particles along the stream; small enough to read as a line.
     */
    private static final double PARTICLE_SPACING = 0.06D;

    /**
     * Ticks a stream particle stays visible. Lower = crisper, but may flicker.
     */
    private static final int PARTICLE_LIFETIME = 2;

    private final World world;

    public StreamRenderer(World world) {
        this.world = world;
    }

    /**
     * Draws the stream for this tick.
     * @param droplets The droplets in the air, oldest first.
     * @param nozzle Where the stream leaves the player, or {@code null} if it is not emitting.
     * @param nozzleVelocity The velocity droplets leave the nozzle with.
     */
    public void render(List<Droplet> droplets, Vector nozzle, Vector nozzleVelocity) {
        if (droplets.isEmpty()) {
            return;
        }

        double carry = 0.0D;
        for (int i = 0; i + 1 < droplets.size(); i++) {
            Droplet older = droplets.get(i);
            Droplet newer = droplets.get(i + 1);
            if (canJoin(older, newer)) {
                carry = line(newer.getPosition(), newer.getVelocity(), older.getPosition(), older.getVelocity(), carry);
            } else {
                dot(older.getPosition(), older.getVelocity());
                carry = 0.0D;
            }
        }

        Droplet newest = droplets.getLast();
        if (nozzle != null && newest.getPosition().distance(nozzle) <= MAX_JOIN_DISTANCE) {
            line(nozzle, nozzleVelocity, newest.getPosition(), newest.getVelocity(), 0.0D);
        } else {
            dot(newest.getPosition(), newest.getVelocity());
        }
    }

    private static boolean canJoin(Droplet older, Droplet newer) {
        return older.isFollowedBy(newer)
                && older.getPosition().distance(newer.getPosition()) <= MAX_JOIN_DISTANCE;
    }

    /**
     * Places particles evenly along a segment, each drifting with the
     * interpolated droplet velocity.
     * @return The spacing carried into the next segment, so the gaps stay even across joints.
     */
    private double line(Vector from, Vector fromVelocity, Vector to, Vector toVelocity, double offset) {
        double length = to.clone().subtract(from).length();
        if (length < 1.0E-6) {
            return offset;
        }

        double distance = offset;
        for (; distance < length; distance += PARTICLE_SPACING) {
            double t = distance / length;
            dot(Vectors.lerp(from, to, t), Vectors.lerp(fromVelocity, toVelocity, t));
        }
        return distance - length;
    }

    /**
     * One short-lived particle that glides to where the stream will be
     * next tick and then disappears, so nothing lingers behind the flow.
     */
    private void dot(Vector at, Vector velocity) {
        Location start = at.toLocation(world);
        Location target = at.clone().add(velocity.clone().multiply(PARTICLE_LIFETIME)).toLocation(world);
        trail(start, target, STREAM_COLOR);
        trail(start, target, CORE_COLOR);
    }

    private void trail(Location start, Location target, Color color) {
        world.spawnParticle(Particle.TRAIL, start, 1, 0.0D, 0.0D, 0.0D, 0.0D,
                new Particle.Trail(target, color, PARTICLE_LIFETIME));
    }
}
