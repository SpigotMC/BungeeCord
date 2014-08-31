package net.md_5.bungee.forge;

import net.md_5.bungee.forge.delegates.IForgePluginMessageSender;
import net.md_5.bungee.forge.delegates.IVoidAction;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * An implementation of {@link IForgeClientData} for when the system is being run
 * without Forge support enabled.
 */
public class VanillaForgeClientData implements IForgeClientData {

    /**
     * Static variable to hold the vanilla instance.
     */
    public final static VanillaForgeClientData vanilla = new VanillaForgeClientData();

    private VanillaForgeClientData() { }

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
    }

    @Override
    public void setClientModList(byte[] value)
    {
    }

    @Override
    public void setDelayedPacketSender(IForgePluginMessageSender sender)
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
    public void startHandshake()
    {
    }

    @Override
    public void setVanilla()
    {
    }

    @Override
    public void loginSuccessPacketInterception(LoginSuccess login, ServerConnector sc) throws Exception
    {
    }

    @Override
    public void setServerHandshakeCompletion(IVoidAction sender)
    {
    }
}
