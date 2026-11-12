package be.kyanvde.winakplugin.modules.fluid.listener;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import be.kyanvde.winakplugin.modules.fluid.model.PeeStream;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PeeStream stream = FluidModule.getInstance().getPeeStreams().remove(event.getEntity().getUniqueId());

        if (stream != null) {
            stream.discard();
        }
    }
}
