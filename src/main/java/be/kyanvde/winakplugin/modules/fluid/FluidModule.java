package be.kyanvde.winakplugin.modules.fluid;

import be.kyanvde.winakplugin.modules.Module;
import be.kyanvde.winakplugin.modules.fluid.command.PeeCommand;
import be.kyanvde.winakplugin.modules.fluid.effect.Reactions;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerChangedWorldListener;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerDeathListener;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerQuitListener;
import be.kyanvde.winakplugin.modules.fluid.model.PeeStream;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FluidModule extends Module {

    /**
     * Every player's stream, including streams that stopped emitting but still have droplets in the air.
     */
    private final Map<UUID, PeeStream> streams = new HashMap<>();

    @Getter
    private final Reactions reactions = new Reactions();

    public FluidModule() {
        super("Fluid");
    }

    @Override
    protected void addCommands() {
        addCommand("pee", new PeeCommand(this));
    }

    @Override
    protected void addListeners() {
        addListener(new PlayerChangedWorldListener(this));
        addListener(new PlayerDeathListener(this));
        addListener(new PlayerQuitListener(this));
    }

    @Override
    protected void onDisable() {
        streams.values().forEach(PeeStream::cancel);
        streams.clear();
        reactions.clear();
    }

    public boolean isPeeing(Player player) {
        PeeStream stream = streams.get(player.getUniqueId());
        return stream != null && stream.isEmitting();
    }

    /**
     * Starts or stops the player's stream.
     */
    public void toggle(Player player) {
        if (isPeeing(player)) {
            stop(player);
            return;
        }
        start(player);
    }

    public void start(Player player) {
        streams.computeIfAbsent(player.getUniqueId(), id -> new PeeStream(this, player)).open();
    }

    /**
     * Stops peeing; droplets still in the air finish falling.
     */
    public void stop(Player player) {
        PeeStream stream = streams.get(player.getUniqueId());
        if (stream != null) {
            stream.close();
        }
    }

    /**
     * Removes the player's stream at once, without letting the droplets land.
     */
    public void discard(Player player) {
        PeeStream stream = streams.remove(player.getUniqueId());
        if (stream != null) {
            stream.cancel();
        }
    }

    /**
     * Forgets a stream that has finished by itself.
     */
    public void remove(PeeStream stream) {
        streams.remove(stream.getPlayer().getUniqueId(), stream);
    }
}
