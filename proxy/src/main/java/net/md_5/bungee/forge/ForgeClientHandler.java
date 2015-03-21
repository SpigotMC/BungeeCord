package net.md_5.bungee.forge;

import java.util.ArrayDeque;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handles the Forge Client data and handshake procedure.
 */
@RequiredArgsConstructor
public class ForgeClientHandler
{

    @NonNull
    private final UserConnection con;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean forgeOutdated = false;

    /**
     * The users' mod list.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Map<String, String> clientModList = null;

    private final ArrayDeque<PluginMessage> packetQueue = new ArrayDeque<PluginMessage>();

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private ForgeClientHandshakeState state = ForgeClientHandshakeState.HELLO;

    private PluginMessage serverModList = null;
    private PluginMessage serverIdList = null;

    /**
     * Gets or sets a value indicating whether the '\00FML\00' token was found
     * in the handshake.
     */
    @Getter
    @Setter
    private boolean fmlTokenInHandshake = false;

    /**
     * Handles the Forge packet.
     *
     * @param message The Forge Handshake packet to handle.
     */
    public void handle(PluginMessage message) throws IllegalArgumentException
    {
        if ( !message.getTag().equalsIgnoreCase( ForgeConstants.FML_HANDSHAKE_TAG ) )
        {
            throw new IllegalArgumentException( "Expecting a Forge Handshake packet." );
        }

        message.setAllowExtendedPacket( true ); // FML allows extended packets so this must be enabled
        ForgeClientHandshakeState prevState = state;
        packetQueue.add( message );
        state = state.send( message, con );
        if ( state != prevState ) // state finished, send packets
        {
            synchronized ( packetQueue )
            {
                while ( !packetQueue.isEmpty() )
                {
                    ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, prevState.name(), packetQueue.getFirst() );
                    con.getForgeServerHandler().receive( packetQueue.removeFirst() );
                }
            }
        }
    }

    /**
     * Receives a {@link PluginMessage} from ForgeServer to pass to Client.
     *
     * @param message The message to being received.
     */
    public void receive(PluginMessage message) throws IllegalArgumentException
    {
        state = state.handle( message, con );
    }

    /**
     * Resets the client handshake state to HELLO, and, if we know the handshake
     * has been completed before, send the reset packet.
     */
    public void resetHandshake()
    {
        state = ForgeClientHandshakeState.HELLO;
        con.unsafe().sendPacket( ForgeConstants.FML_RESET_HANDSHAKE );
    }

    /**
     * Sends the server mod list to the client, or stores it for sending later.
     *
     * @param modList The {@link PluginMessage} to send to the client containing
     * the mod list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was
     * not as expected.
     */
    public void setServerModList(PluginMessage modList) throws IllegalArgumentException
    {
        if ( !modList.getTag().equalsIgnoreCase( ForgeConstants.FML_HANDSHAKE_TAG ) || modList.getData()[0] != 2 )
        {
            throw new IllegalArgumentException( "modList" );
        }

        this.serverModList = modList;
    }

    /**
     * Sends the server ID list to the client, or stores it for sending later.
     *
     * @param idList The {@link PluginMessage} to send to the client containing
     * the ID list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was
     * not as expected.
     */
    public void setServerIdList(PluginMessage idList) throws IllegalArgumentException
    {
        if ( !idList.getTag().equalsIgnoreCase( ForgeConstants.FML_HANDSHAKE_TAG ) || idList.getData()[0] != 3 )
        {
            throw new IllegalArgumentException( "idList" );
        }

        this.serverIdList = idList;
    }

    /**
     * Returns whether the handshake is complete.
     *
     * @return <code>true</code> if the handshake has been completed.
     */
    public boolean isHandshakeComplete()
    {
        return this.state == ForgeClientHandshakeState.DONE;
    }

    public void setHandshakeComplete()
    {
        this.state = ForgeClientHandshakeState.DONE;
    }

    /**
     * Returns whether we know if the user is a forge user. In FML 1.8, a "FML"
     * token is included in the initial handshake. We can use that to determine
     * if the user is a Forge 1.8 user.
     *
     * @return <code>true</code> if the user is a forge user.
     */
    public boolean isForgeUser()
    {
        return fmlTokenInHandshake || clientModList != null;
    }

    /**
     * Checks to see if a user is using an outdated FML build, and takes
     * appropriate action on the User side. This should only be called during a
     * server connection, by the ServerConnector
     *
     * @return <code>true</code> if the user's FML build is outdated, otherwise
     * <code>false</code>
     */
    public boolean checkUserOutdated()
    {
        if ( forgeOutdated )
        {
            if ( con.isDimensionChange() )
            {
                con.disconnect( BungeeCord.getInstance().getTranslation( "connect_kick_outdated_forge" ) );
            } else
            {
                con.sendMessage( BungeeCord.getInstance().getTranslation( "connect_kick_outdated_forge" ) );
            }
        }

        return forgeOutdated;
    }
}
