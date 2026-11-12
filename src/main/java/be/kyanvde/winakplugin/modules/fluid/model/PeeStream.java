package be.kyanvde.winakplugin.modules.fluid.model;

import be.kyanvde.winakplugin.WINAKPlugin;
import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import be.kyanvde.winakplugin.modules.fluid.effect.ImpactEffects;
import be.kyanvde.winakplugin.modules.fluid.effect.StreamRenderer;
import be.kyanvde.winakplugin.util.Vectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A purely cosmetic pee stream. Nothing in the world is changed.
 *
 * <p>The stream is simulated like a garden hose: every tick a few droplets
 * leave the player's hips and then fly on their own under gravity. Turning
 * only changes where <em>new</em> droplets go, so the stream already in the
 * air lags behind and swings naturally.
 */
public final class PeeStream {

    /**
     * Streams stop by themselves after this many ticks, in case someone forgets.
     */
    private static final int MAX_TICKS = 20 * 20; // 20 seconds

    /**
     * How far in front of the player's centre the stream starts; the skin's front is ~0.13.
     */
    private static final double NOZZLE_OFFSET = 0.15D;

    /**
     * Start height as a fraction of the player's height; the crotch of a skin is ~0.41.
     */
    private static final double NOZZLE_HEIGHT = 0.40D;

    /**
     * Initial speed of a droplet, in blocks per tick.
     */
    public static final double LAUNCH_SPEED = 0.42D;

    /**
     * Random spread added to each droplet's launch velocity.
     */
    private static final double LAUNCH_JITTER = 0.006D;

    /**
     * Droplets released per tick; more means a smoother curve when turning.
     */
    private static final int DROPLETS_PER_TICK = 2;

    private static final double MIN_PITCH = -60.0D;
    private static final double MAX_PITCH = 70.0D;
    /**
     * Aims a little upwards from where the player looks, so the arc lands where they look.
     */
    private static final double AIM_LIFT = 0.15D;
    private static final double SWAY_SPEED = 0.35D;
    private static final double SWAY_AMOUNT = 0.03D;

    @Getter
    private final Player player;
    private final World world;
    private final FluidModule module;
    private final BukkitTask task;
    private final StreamRenderer renderer;
    private final ImpactEffects impacts;

    /**
     * All droplets still in the air, oldest first.
     */
    private final List<Droplet> droplets = new ArrayList<>();

    /**
     * Whether new droplets are being released. The stream lives on until the last one has landed.
     */
    @Getter
    private boolean emitting;
    private int age;
    private long nextId;

    /**
     * Nozzle position and aim of the previous tick, to spread droplets in between.
     */
    private Vector lastNozzle;
    private Vector lastAim;

    public PeeStream(FluidModule module, Player player) {
        this.module = module;
        this.player = player;
        this.world = player.getWorld();
        this.renderer = new StreamRenderer(world);
        this.impacts = new ImpactEffects(world, player, module.getReactions());
        this.task = Bukkit.getScheduler().runTaskTimer(WINAKPlugin.getInstance(), this::tick, 0L, 1L);
    }

    /**
     * Starts (or restarts) releasing droplets.
     */
    public void open() {
        if (!emitting) {
            playZipper(true);
        }
        emitting = true;
        age = 0;
    }

    /**
     * Stops releasing droplets; those still in the air finish falling.
     */
    public void close() {
        if (emitting) {
            emitting = false;
            playZipper(false);
        }
    }

    /**
     * Stops the stream at once, without letting the droplets land.
     */
    public void cancel() {
        task.cancel();
    }

    private void tick() {
        if (!player.isOnline() || player.getWorld() != world) {
            module.discard(player);
            return;
        }
        impacts.tick();
        if (emitting && age++ >= MAX_TICKS) {
            close();
        }

        Vector nozzle = nozzle();
        Vector aim = aim();

        // Move what is already in the air first, so the fresh droplets start at the nozzle.
        droplets.forEach(this::move);

        if (emitting) {
            emit(nozzle, aim);
            lastNozzle = nozzle;
            lastAim = aim;
        } else {
            lastNozzle = null;
            lastAim = null;
        }

        renderer.render(droplets, emitting ? nozzle : null, aim.clone().multiply(LAUNCH_SPEED));

        droplets.removeIf(Droplet::isDone);
        if (!emitting && droplets.isEmpty()) {
            module.remove(this);
            cancel();
        }
    }

    /**
     * Releases this tick's droplets, spread between last tick's and this tick's nozzle and aim.
     */
    private void emit(Vector nozzle, Vector aim) {
        Vector fromNozzle = lastNozzle != null ? lastNozzle : nozzle;
        Vector fromAim = lastAim != null ? lastAim : aim;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 1; i <= DROPLETS_PER_TICK; i++) {
            double t = (double) i / DROPLETS_PER_TICK;
            Vector start = Vectors.lerp(fromNozzle, nozzle, t);
            Vector jitter = new Vector(random.nextGaussian(), random.nextGaussian(), random.nextGaussian())
                    .multiply(LAUNCH_JITTER);
            Vector velocity = Vectors.lerp(fromAim, aim, t).normalize().multiply(LAUNCH_SPEED).add(jitter);

            // Droplets released earlier in the tick have already travelled a little.
            Vector position = start.clone().add(velocity.clone().multiply(1.0D - t));
            droplets.add(new Droplet(nextId++, position, velocity));
        }
    }

    /**
     * Moves a droplet one tick, landing it on whatever block or entity is in the way.
     */
    private void move(Droplet droplet) {
        droplet.incrementAge();
        Vector velocity = droplet.getVelocity();
        double speed = velocity.length();
        if (speed < 1.0E-6) {
            return;
        }

        RayTraceResult hit = world.rayTrace(droplet.getPosition().toLocation(world),
                velocity.clone().multiply(1.0D / speed), speed,
                FluidCollisionMode.ALWAYS, true, 0.1D, this::canBeHit);
        if (hit == null) {
            droplet.advance();
            return;
        }

        droplet.land(hit.getHitPosition());
        if (hit.getHitEntity() != null) {
            impacts.splatEntity(hit.getHitEntity(), hit.getHitPosition());
        } else {
            impacts.splashBlock(hit);
        }
    }

    /**
     * Living things the stream can splash against: not the peer, nor invisible helpers.
     */
    private boolean canBeHit(Entity entity) {
        return entity != player
                && entity instanceof LivingEntity
                && !(entity instanceof ArmorStand)
                && !entity.isInvisible()
                && !(entity instanceof Player other && other.getGameMode() == GameMode.SPECTATOR);
    }

    /**
     * Hips: a fraction of the (sneak- and scale-aware) hitbox height, slightly
     * in front of where the torso faces (the body lags behind the head).
     */
    private Vector nozzle() {
        return player.getLocation().toVector()
                .add(new Vector(0, player.getHeight() * NOZZLE_HEIGHT, 0))
                .add(Vectors.horizontal(player.getBodyYaw()).multiply(NOZZLE_OFFSET));
    }

    /**
     * Follows where the head looks, but never straight up or down, with a gentle sway.
     */
    private Vector aim() {
        Location eye = player.getEyeLocation();
        Vector forward = Vectors.horizontal(eye.getYaw());
        double pitch = Math.toRadians(Math.clamp(eye.getPitch(), MIN_PITCH, MAX_PITCH));

        Vector aim = forward.clone().multiply(Math.cos(pitch)).setY(-Math.sin(pitch) + AIM_LIFT).normalize();
        Vector side = new Vector(-forward.getZ(), 0, forward.getX());
        return aim.add(side.multiply(Math.sin(age * SWAY_SPEED) * SWAY_AMOUNT)).normalize();
    }

    /**
     * Unzip / zip up.
     */
    private void playZipper(boolean open) {
        world.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER,
                SoundCategory.PLAYERS, 0.6F, open ? 1.5F : 1.2F);
    }
}
