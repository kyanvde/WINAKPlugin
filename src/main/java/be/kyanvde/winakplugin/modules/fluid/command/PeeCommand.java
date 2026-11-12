package be.kyanvde.winakplugin.modules.fluid.command;

import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * {@code /pee}: starts or stops the sender's stream.
 */
public class PeeCommand implements CommandExecutor {

    private final FluidModule module;

    public PeeCommand(FluidModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        module.toggle(player);
        return true;
    }
}
