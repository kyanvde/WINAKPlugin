package be.kyanvde.winakplugin.modules.fluid.listener;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final FluidModule module;

    public PlayerDeathListener(FluidModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        module.discard(event.getPlayer());
    }
}
