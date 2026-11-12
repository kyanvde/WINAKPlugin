package be.kyanvde.winakplugin.modules;

import be.kyanvde.winakplugin.WINAKPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A self-contained feature of the WINAK plugin with its own commands and listeners.
 *
 * <p>Subclasses declare their commands and listeners in {@link #addCommands()} and
 * {@link #addListeners()}; this class registers them on {@link #enable()} and
 * unregisters them again on {@link #disable()}.
 */
public abstract class Module {

    /**
     * The display name of the module.
     */
    @Getter
    private final String name;

    /**
     * The modules this module depends on. They are enabled before this one.
     */
    @Getter
    private final List<Module> dependencies;

    /**
     * Whether the module is currently enabled.
     */
    @Getter
    private boolean enabled;

    private final List<Listener> listeners = new ArrayList<>();
    private final Map<String, CommandExecutor> commands = new HashMap<>();

    /**
     * Initializes a new module.
     * @param name The display name of the module.
     * @param dependencies The modules this module depends on.
     */
    protected Module(String name, Module... dependencies) {
        this.name = name;
        this.dependencies = List.of(dependencies);
    }

    /**
     * Enables this module (and its dependencies), registering all of its commands and listeners.
     */
    public final void enable() {
        if (enabled) {
            return;
        }
        dependencies.forEach(Module::enable);

        WINAKPlugin plugin = WINAKPlugin.getInstance();
        addCommands();
        addListeners();

        commands.forEach((label, executor) -> getCommand(label).setExecutor(executor));
        listeners.forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, plugin));

        onEnable();
        enabled = true;
        plugin.getLogger().info("Enabled module " + name + ".");
    }

    /**
     * Disables this module, unregistering all of its commands and listeners.
     */
    public final void disable() {
        if (!enabled) {
            return;
        }
        onDisable();

        listeners.forEach(HandlerList::unregisterAll);
        commands.keySet().forEach(label -> getCommand(label).setExecutor(null));
        listeners.clear();
        commands.clear();

        enabled = false;
        WINAKPlugin.getInstance().getLogger().info("Disabled module " + name + ".");
    }

    /**
     * Called after the commands and listeners are registered.
     */
    protected void onEnable() {
    }

    /**
     * Called before the commands and listeners are unregistered.
     */
    protected void onDisable() {
    }

    /**
     * Declares the commands of this module using {@link #addCommand(String, CommandExecutor)}.
     */
    protected abstract void addCommands();

    /**
     * Binds a command to an executor.
     * @param label The command's name, as declared in {@code plugin.yml}.
     * @param executor The executor that handles the command.
     */
    protected void addCommand(String label, CommandExecutor executor) {
        commands.put(label, executor);
    }

    /**
     * Declares the listeners of this module using {@link #addListener(Listener)}.
     */
    protected abstract void addListeners();

    /**
     * Adds an event listener to this module.
     * @param listener The listener to register.
     */
    protected void addListener(Listener listener) {
        listeners.add(listener);
    }

    private static PluginCommand getCommand(String label) {
        PluginCommand command = WINAKPlugin.getInstance().getCommand(label);
        if (command == null) {
            throw new IllegalStateException("Command /" + label + " is not declared in plugin.yml.");
        }
        return command;
    }
}
