package net.md_5.bungee.forge;

import net.md_5.bungee.forge.delegates.IForgePluginMessageSender;
import net.md_5.bungee.forge.delegates.IVoidAction;
import java.util.Timer;
import java.util.TimerTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Contains data about the Forge server, and handles the handshake.
 */
@RequiredArgsConstructor
public class ForgeServer extends AbstractPacketHandler implements IForgeServer {
    private final UserConnection con;
    @Getter
    private final ChannelWrapper ch;

    @Getter(AccessLevel.PACKAGE)
    private final ServerInfo serverInfo;
    private final ProxyServer bungee;
    @Getter
    private ForgeServerHandshakeState state = ForgeServerHandshakeState.START;

    /**
     * Handles any {@link PluginMessage} that contains a Forge Handshake.
     *
     * @param message The message to handle.
     * @throws IllegalArgumentException If the wrong packet is sent down.
     */
    @Override
    public void handle(PluginMessage message) throws IllegalArgumentException
    {
        if ( !message.getTag().equalsIgnoreCase( ForgeConstants.FORGE_HANDSHAKE_TAG ) ) {
            throw new IllegalArgumentException( "Expecting a Forge Handshake packet." );
        }

        state = state.send( message, con );
    }

    /**
     * Returns whether the server handshake has been initiated.
     *
     * @return <code>true</code> if the server has started a Forge handshake.
     */
    @Override
    public boolean isServerForge() {
        return state != ForgeServerHandshakeState.START;
    }
}
