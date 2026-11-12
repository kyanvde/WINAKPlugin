package be.kyanvde.winakplugin;

import be.kyanvde.winakplugin.modules.Module;
import be.kyanvde.winakplugin.modules.fluid.FluidModule;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class WINAKPlugin extends JavaPlugin {

    @Getter
    private static WINAKPlugin instance;

    /**
     * All modules of the plugin, in the order they are enabled.
     */
    @Getter
    private List<Module> modules = List.of();

    @Override
    public void onEnable() {
        instance = this;

        modules = List.of(
                new FluidModule()
        );
        modules.forEach(Module::enable);
    }

    @Override
    public void onDisable() {
        modules.reversed().forEach(Module::disable);
        modules = List.of();
        instance = null;
    }
}
