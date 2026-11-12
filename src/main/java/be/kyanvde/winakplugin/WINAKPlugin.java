package be.kyanvde.winakplugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class WINAKPlugin extends JavaPlugin {

    @Getter
    private static WINAKPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
