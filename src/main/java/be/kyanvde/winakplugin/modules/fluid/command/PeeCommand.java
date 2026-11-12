package be.kyanvde.winakplugin.modules.fluid.command;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import be.kyanvde.winakplugin.modules.fluid.model.PeeStream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PeeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Map<UUID, PeeStream> streams = FluidModule.getInstance().getPeeStreams();

        if (!streams.containsKey(player.getUniqueId())) {
            streams.put(player.getUniqueId(), new PeeStream());
        } else {
            streams.remove(player.getUniqueId());
        }
        return true;
    }
}
