package be.kyanvde.winakplugin.modules.fluid.effect;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Particles and sounds for where a stream lands. Purely cosmetic: nothing in the world is changed.
 *
 * <p>One instance belongs to one stream, so the sound throttling is per stream.
 */
public final class ImpactEffects {

    private static final Particle.DustOptions SPLASH_DUST = new Particle.DustOptions(StreamRenderer.STREAM_COLOR, 0.55F);

    /**
     * How far splash particles are pushed out of the surface they hit.
     */
    private static final double SURFACE_OFFSET = 0.06D;

    private static final Set<Material> WATER = Set.of(Material.WATER, Material.BUBBLE_COLUMN);
    private static final Set<Material> HOT = Set.of(
            Material.LAVA, Material.MAGMA_BLOCK, Material.CAMPFIRE, Material.SOUL_CAMPFIRE);

    private final World world;
    private final Player peer;
    private final Reactions reactions;

    /**
     * Ticks until a throttled impact sound may play again.
     */
    private int soundCooldown;

    public ImpactEffects(World world, Player peer, Reactions reactions) {
        this.world = world;
        this.peer = peer;
        this.reactions = reactions;
    }

    /**
     * Must be called once per tick.
     */
    public void tick() {
        if (soundCooldown > 0) {
            soundCooldown--;
        }
    }

    /**
     * Splat against a mob, animal, NPC or player: a burst that runs down their body.
     */
    public void splatEntity(Entity entity, Vector at) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        spawn(Particle.DUST, at, 3, 0.08D, 0.08D, 0.08D, 0.0D, SPLASH_DUST);
        if (random.nextInt(3) == 0) {
            spawn(Particle.FALLING_HONEY, at.clone().add(new Vector(0, -0.05D, 0)), 1, 0.1D, 0.05D, 0.1D, 0.0D, null);
        }
        if (random.nextInt(4) == 0) {
            spawn(Particle.SPLASH, at, 2, 0.1D, 0.05D, 0.1D, 0.0D, null);
        }
        playThrottled(at, Sound.ENTITY_SLIME_SQUISH_SMALL, 0.25F, 1.3F, 1.7F, 3);
        reactions.react(peer, entity);
    }

    /**
     * Splash against a block: a puddle splash, a splash in water, or a sizzle on something hot.
     */
    public void splashBlock(RayTraceResult hit) {
        BlockFace face = hit.getHitBlockFace();
        Vector normal = face != null ? face.getDirection() : new Vector(0, 1, 0);
        Vector out = hit.getHitPosition().clone().add(normal.multiply(SURFACE_OFFSET));
        Block block = hit.getHitBlock();
        Material type = block != null ? block.getType() : Material.AIR;

        if (WATER.contains(type)) {
            splashWater(out);
        } else if (HOT.contains(type)) {
            sizzle(out);
        } else {
            splashSurface(out, face);
        }
    }

    /**
     * Into water: a little splash instead of a puddle.
     */
    private void splashWater(Vector at) {
        spawn(Particle.SPLASH, at.clone().add(new Vector(0, 0.05D, 0)), 3, 0.1D, 0.0D, 0.1D, 0.0D, null);
        if (ThreadLocalRandom.current().nextInt(4) == 0) {
            spawn(Particle.BUBBLE_POP, at, 1, 0.1D, 0.0D, 0.1D, 0.0D, null);
        }
        playThrottled(at, Sound.ENTITY_PLAYER_SPLASH, 0.12F, 1.6F, 2.0F, 5);
    }

    /**
     * Onto something hot: it sizzles (but nothing is actually put out).
     */
    private void sizzle(Vector at) {
        spawn(Particle.SMOKE, at.clone().add(new Vector(0, 0.1D, 0)), 2, 0.1D, 0.05D, 0.1D, 0.01D, null);
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            spawn(Particle.CLOUD, at.clone().add(new Vector(0, 0.2D, 0)), 1, 0.1D, 0.05D, 0.1D, 0.02D, null);
        }
        playThrottled(at, Sound.BLOCK_FIRE_EXTINGUISH, 0.25F, 1.4F, 2.0F, 8);
    }

    /**
     * Onto any other block; against a wall it also runs down.
     */
    private void splashSurface(Vector at, BlockFace face) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        playThrottled(at, Sound.BLOCK_POINTED_DRIPSTONE_DRIP_WATER, 0.45F, 0.8F, 1.4F, 2);
        if (random.nextInt(5) == 0) {
            play(at, Sound.BLOCK_WET_GRASS_STEP, 0.12F, 1.4F, 1.8F);
        }

        if (random.nextBoolean()) {
            spawn(Particle.LANDING_HONEY, at, 1, 0.12D, 0.04D, 0.12D, 0.0D, null);
        }
        spawn(Particle.DUST, at, 2, 0.1D, 0.06D, 0.1D, 0.0D, SPLASH_DUST);

        boolean wall = face != null && face != BlockFace.UP && face != BlockFace.DOWN;
        if (wall && random.nextInt(6) == 0) {
            spawn(Particle.FALLING_HONEY, at.clone().add(new Vector(0, -0.1D, 0)), 1, 0.08D, 0.05D, 0.08D, 0.0D, null);
        }
    }

    /**
     * Plays an impact sound at most once per {@code cooldown} ticks, so a
     * steady stream trickles instead of machine-gunning.
     */
    private void playThrottled(Vector at, Sound sound, float volume, float minPitch, float maxPitch, int cooldown) {
        if (soundCooldown > 0) {
            return;
        }
        soundCooldown = cooldown;
        play(at, sound, volume, minPitch, maxPitch);
    }

    private void play(Vector at, Sound sound, float volume, float minPitch, float maxPitch) {
        float pitch = minPitch + ThreadLocalRandom.current().nextFloat() * (maxPitch - minPitch);
        world.playSound(at.toLocation(world), sound, SoundCategory.PLAYERS, volume, pitch);
    }

    private <T> void spawn(Particle particle, Vector at, int count,
                           double spreadX, double spreadY, double spreadZ, double speed, T data) {
        world.spawnParticle(particle, at.getX(), at.getY(), at.getZ(), count, spreadX, spreadY, spreadZ, speed, data);
    }
}
