package be.kyanvde.winakplugin.modules.fluid.listener;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final FluidModule module;

    public PlayerQuitListener(FluidModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        module.discard(event.getPlayer());
    }
}
