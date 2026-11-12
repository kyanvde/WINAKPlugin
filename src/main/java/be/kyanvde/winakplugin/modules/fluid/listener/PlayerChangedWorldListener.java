package be.kyanvde.winakplugin.modules.fluid.listener;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import be.kyanvde.winakplugin.modules.fluid.model.PeeStream;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        PeeStream stream = FluidModule.getInstance().getPeeStreams().remove(event.getPlayer().getUniqueId());

        if (stream != null) {
            stream.discard();
        }
    }
}
