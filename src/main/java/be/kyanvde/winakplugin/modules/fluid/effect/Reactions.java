package be.kyanvde.winakplugin.modules.fluid.effect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One-off reactions from whatever gets hit by a stream. Purely cosmetic.
 *
 * <p>Shared by all streams, so a target reacts at most once per
 * {@link #COOLDOWN} ticks no matter how many players are aiming at it.
 */
public final class Reactions {

    /**
     * Ticks before the same mob, NPC or player reacts to being hit again.
     */
    private static final int COOLDOWN = 60;

    /**
     * Once this many targets are remembered, expired entries are cleaned up.
     */
    private static final int CLEANUP_THRESHOLD = 256;

    /**
     * Metadata key Citizens (and most other NPC plugins) put on their NPCs.
     */
    private static final String NPC_METADATA = "NPC";

    /**
     * Entity to the server tick it last reacted on.
     */
    private final Map<UUID, Integer> lastReaction = new HashMap<>();

    /**
     * Makes {@code target} react to being peed on by {@code peer}, unless it reacted recently.
     */
    public void react(Player peer, Entity target) {
        if (!tryStartCooldown(target)) {
            return;
        }

        if (target instanceof Player victim) {
            if (!victim.hasMetadata(NPC_METADATA)) {
                victim.sendActionBar(Component.text(peer.getName() + " is peeing on you!", NamedTextColor.YELLOW));
            }
            return;
        }

        if (target instanceof Mob mob) {
            mob.lookAt(peer);
        }
        Sound sound = reactionSound(target);
        if (sound != null) {
            target.getWorld().playSound(target.getLocation(), sound, SoundCategory.NEUTRAL, 0.8F, 1.0F);
        }
    }

    public void clear() {
        lastReaction.clear();
    }

    /**
     * @return Whether the target may react now; if so, its cooldown starts.
     */
    private boolean tryStartCooldown(Entity target) {
        int now = Bukkit.getCurrentTick();
        Integer last = lastReaction.get(target.getUniqueId());
        if (last != null && now - last < COOLDOWN) {
            return false;
        }
        if (lastReaction.size() > CLEANUP_THRESHOLD) {
            lastReaction.values().removeIf(tick -> now - tick >= COOLDOWN);
        }
        lastReaction.put(target.getUniqueId(), now);
        return true;
    }

    private static Sound reactionSound(Entity target) {
        return switch (target) {
            case Cat ignored -> Sound.ENTITY_CAT_HISS;
            case Ocelot ignored -> Sound.ENTITY_CAT_HISS;
            case AbstractVillager ignored -> Sound.ENTITY_VILLAGER_NO;
            case Wolf ignored -> Sound.ENTITY_WOLF_GROWL;
            case Mob mob -> mob.getAmbientSound();
            default -> null;
        };
    }
}
