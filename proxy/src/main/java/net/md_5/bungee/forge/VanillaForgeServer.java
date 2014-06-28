package net.md_5.bungee.forge;

import net.md_5.bungee.protocol.packet.PluginMessage;

public class VanillaForgeServer implements IForgeServer {

    public final static VanillaForgeServer vanilla = new VanillaForgeServer();

    private VanillaForgeServer() { }

    @Override
    public void handle(PluginMessage message) throws IllegalArgumentException {
    }

    @Override
    public boolean isServerForge() {
        return false;
    }
}
