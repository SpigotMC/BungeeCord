package net.md_5.bungee.forge;

import java.util.Map;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.forge.ForgeLogger.LogDirection;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handshake sequence manager for the Bungee - Forge Client (Upstream) link.
 * Modelled after the Forge implementation. See
 * https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/handshake/FMLHandshakeClientState.java
 */
enum ForgeClientHandshakeState implements IForgeClientPacketHandler<ForgeClientHandshakeState>
{

    /**
     * Initiated at the start of a client handshake. This is a special case
     * where we don't want to use a {@link PluginMessage}, we're just sending
     * stuff out here.
     *
     * Transitions into the HELLO state upon completion.
     *
     * Requires: {@link UserConnection}.
     */
    START
            {
                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    ForgeLogger.logClient( LogDirection.RECEIVED, this.name(), message );
                    con.unsafe().sendPacket( message );
                    con.getForgeClientHandler().setState( HELLO );
                    return HELLO;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    return HELLO;
                }
            },
    /**
     * Waiting to receive a client HELLO and the mod list. Upon receiving the
     * mod list, return the mod list of the server.
     *
     * We will be stuck in this state if we don't have a forge client. This is
     * OK.
     *
     * Transitions to the WAITINGCACK state upon completion.
     *
     * Requires:
     * {@link PluginMessage}, {@link UserConnection}, {@link ServerConnector}
     */
    HELLO
            {
                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    ForgeLogger.logClient( LogDirection.RECEIVED, this.name(), message );
                    // Server Hello.
                    if ( message.getData()[0] == 0 )
                    {
                        con.unsafe().sendPacket( message );
                    }

                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    // Client Hello.
                    if ( message.getData()[0] == 1 )
                    {
                        return this;
                    }

                    // Mod list.
                    if ( message.getData()[0] == 2 )
                    {
                        if ( con.getForgeClientHandler().getClientModList() == null )
                        {
                            // This is the first Forge connection - so get the mods now.
                            // Once we've done it, no point doing it again.
                            Map<String, String> clientModList = ForgeUtils.readModList( message );
                            con.getForgeClientHandler().setClientModList( clientModList );
                        }

                        return WAITINGSERVERDATA;
                    }

                    return this;
                }

            },
    WAITINGSERVERDATA
            {

                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                    // Mod list.
                    if ( message.getData()[0] == 2 )
                    {
                        con.unsafe().sendPacket( message );
                    }

                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    // ACK
                    return WAITINGSERVERCOMPLETE;
                }
            },
    WAITINGSERVERCOMPLETE
            {

                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                    // Mod ID's.
                    if ( message.getData()[0] == 3 )
                    {
                        con.unsafe().sendPacket( message );
                        return this;
                    }

                    con.unsafe().sendPacket( message ); // pass everything else
                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    // Send ACK.
                    return PENDINGCOMPLETE;
                }
            },
    PENDINGCOMPLETE
            {

                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    // Ack.
                    if ( message.getData()[0] == -1 )
                    {
                        ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                        con.unsafe().sendPacket( message );
                    }

                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    // Send an ACK
                    return COMPLETE;
                }
            },
    COMPLETE
            {

                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    // Ack.
                    if ( message.getData()[0] == -1 )
                    {
                        ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                        con.unsafe().sendPacket( message );
                    }

                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    return DONE;
                }
            },
    /**
     * Handshake has been completed. Ignores any future handshake packets.
     */
    DONE
            {

                @Override
                public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
                {
                    ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                    return this;
                }

                @Override
                public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
                {
                    return this;
                }
            }
}
