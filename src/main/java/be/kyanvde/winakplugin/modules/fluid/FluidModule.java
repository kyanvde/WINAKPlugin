package be.kyanvde.winakplugin.modules.fluid;

import be.kyanvde.winakplugin.modules.Module;
import be.kyanvde.winakplugin.modules.fluid.command.PeeCommand;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerChangedWorldListener;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerDeathListener;
import be.kyanvde.winakplugin.modules.fluid.listener.PlayerQuitListener;
import be.kyanvde.winakplugin.modules.fluid.model.PeeStream;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FluidModule extends Module {

    @Getter
    private static FluidModule instance;

    @Getter
    private final Map<UUID, PeeStream> peeStreams = new HashMap<>();

    protected FluidModule() {
        super("Fluid");
        instance = this;
    }

    @Override
    protected void addCommands() {
        addCommand("pee", new PeeCommand());
    }

    @Override
    protected void addListeners() {
        addListener(new PlayerChangedWorldListener());
        addListener(new PlayerDeathListener());
        addListener(new PlayerQuitListener());
    }
}
