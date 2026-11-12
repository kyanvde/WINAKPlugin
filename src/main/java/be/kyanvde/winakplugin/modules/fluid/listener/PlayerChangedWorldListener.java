package be.kyanvde.winakplugin.modules.fluid.listener;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    private final FluidModule module;

    public PlayerChangedWorldListener(FluidModule module) {
        this.module = module;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        module.discard(event.getPlayer());
    }
}
