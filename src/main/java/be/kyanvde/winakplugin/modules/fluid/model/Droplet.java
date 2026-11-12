package be.kyanvde.winakplugin.modules.fluid.model;

import lombok.Getter;
import org.bukkit.util.Vector;

/**
 * A single droplet of a {@link PeeStream}, flying on its own under gravity.
 */
@Getter
public final class Droplet {

    private static final double GRAVITY = 0.035D;
    private static final double DRAG = 0.99D;
    private static final int MAX_AGE = 80;

    /**
     * Sequence number within its stream; consecutive ids were released right after each other.
     */
    private final long id;
    private final Vector velocity;
    private Vector position;
    private int age;
    private boolean landed;

    Droplet(long id, Vector position, Vector velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
    }

    /**
     * @param other Another droplet of the same stream.
     * @return Whether {@code other} was released right after this droplet.
     */
    public boolean isFollowedBy(Droplet other) {
        return other.id == id + 1;
    }

    /**
     * @return Whether this droplet has landed or has been flying for too long.
     */
    boolean isDone() {
        return landed || age > MAX_AGE;
    }

    void moveTo(Vector position) {
        this.position = position;
    }

    void land(Vector at) {
        position = at;
        landed = true;
    }

    /**
     * Moves the droplet by its velocity, then applies drag and gravity.
     */
    void advance() {
        position = position.clone().add(velocity);
        velocity.multiply(DRAG).setY(velocity.getY() - GRAVITY);
    }

    void incrementAge() {
        age++;
    }
}
