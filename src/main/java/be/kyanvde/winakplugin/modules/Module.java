package be.kyanvde.winakplugin.modules;

import be.kyanvde.winakplugin.WINAKPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * An interface for WINAK Plugin modules.
 */
public abstract class Module {

    /**
     * The display name of the module.
     */
    @Getter
    private final String name;

    /**
     * A list of modules this module depends on.
     */
    @Getter
    private final List<Module> dependencies = new ArrayList<>();

    /**
     * A list of all listeners that belong to this module.
     */
    @Getter
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * A map of all commands that belong to this module.
     */
    @Getter
    private final Map<PluginCommand, CommandExecutor> commands = new HashMap<>();

    /**
     * Initializes a new module.
     * @param name The display name of the module.
     * @param dependencies The modules this module depends on.
     */
    protected Module(String name, Module... dependencies) {
        this.name = name;
        this.dependencies.addAll(Arrays.asList(dependencies));

        registerCommands();
        registerListeners();
    }

    public void enable() {

    }

    public void disable() {

    }

    protected abstract void addCommands();

    protected void addCommand(String command, CommandExecutor executor) {
        this.commands.put(WINAKPlugin.getInstance().getCommand(command), executor);
    }

    protected abstract void addListeners();

    protected void addListener(Listener listener) {
        this.listeners.add(listener);
    }



    private void registerCommands() {
        for (Map.Entry<PluginCommand, CommandExecutor> command : commands.entrySet())
            command.getKey().setExecutor(command.getValue());
    }

    private void registerListeners() {
        for (Listener listener : this.listeners) {
            Bukkit.getPluginManager().registerEvents(listener, WINAKPlugin.getInstance());
        }
    }
}
