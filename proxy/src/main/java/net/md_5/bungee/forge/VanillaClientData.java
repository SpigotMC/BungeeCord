package net.md_5.bungee.forge;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * An implementation of {@link IForgeClientData} for when the system is being run
 * without Forge support enabled.
 */
@RequiredArgsConstructor
public class VanillaClientData implements IForgeClientData {

    @NonNull
    private final UserConnection con;

    @Override
    public byte[] getClientModList()
    {
        return null;
    }

    @Override
    public void handle(PluginMessage message) throws IllegalArgumentException
    {
    }

    @Override
    public boolean isForgeUser()
    {
        return false;
    }

    @Override
    public boolean isHandshakeComplete()
    {
        return false;
    }

    @Override
    public void resetHandshake()
    {
        con.setState(ForgeClientHandshakeState.START);
        con.unsafe().sendPacket(ForgeConstants.FML_RESET_HANDSHAKE); // ignored by vanilla clients
    }

    @Override
    public void setClientModList(byte[] value)
    {
    }

    @Override
    public void setServerIdList(PluginMessage idList) throws IllegalArgumentException
    {
    }

    @Override
    public void setServerModList(PluginMessage modList) throws IllegalArgumentException
    {
    }

    @Override
    public void receive(PluginMessage message) throws IllegalArgumentException
    {
    }
}
