package net.md_5.bungee.forge;

import net.md_5.bungee.protocol.packet.PluginMessage;

public interface IForgeClientData {

    byte[] getClientModList();

    /**
     * Handles the Forge packet.
     * @param message The Forge Handshake packet to handle.
     */
    void handle(PluginMessage message) throws IllegalArgumentException;

    /**
     * Returns whether we know if the user is a forge user.
     * @return <code>true</code> if the user is a forge user.
     */
    boolean isForgeUser();

    /**
     * Returns whether the handshake is complete.
     * @return <code>true</code> if the handshake has been completed.
     */
    boolean isHandshakeComplete();

    /**
     * Sends a LoginSuccess packet to the Forge client, to reset the handshake state.
     */
    void resetHandshake();

    void setClientModList(byte[] value);

    void setDelayedPacketSender(IForgePacketSender sender);

    void setVanilla();

    /**
     * Sends the server ID list to the client, or stores it for sending later.
     *
     * @param idList The {@link PluginMessage} to send to the client containing the ID list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was not as expected.
     */
    void setServerIdList(PluginMessage idList) throws IllegalArgumentException;

    /**
     * Sends the server mod list to the client, or stores it for sending later.
     *
     * @param modList The {@link PluginMessage} to send to the client containing the mod list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was not as expected.
     */
    void setServerModList(PluginMessage modList) throws IllegalArgumentException;

    /**
     * Starts a Forge handshake.
     */
    void startHandshake();
}
