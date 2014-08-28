package net.md_5.bungee.forge;

import net.md_5.bungee.protocol.packet.PluginMessage;

public interface IForgeServer {
    /**
     * Handles any {@link PluginMessage} that contains a Forge Handshake.
     *
     * @param message The message to handle.
     * @throws IllegalArgumentException If the wrong packet is sent down.
     */
    void handle(PluginMessage message) throws IllegalArgumentException;

    /**
     * Receives a {@link PluginMessage} from ForgeClientData to pass to Server.
     *
     * @param message The message to being received.
     */
    void receive(PluginMessage message) throws IllegalArgumentException;

    /**
     * Returns whether the server handshake has been initiated.
     *
     * @return <code>true</code> if the server has started a Forge handshake.
     */
    boolean isServerForge();
}
