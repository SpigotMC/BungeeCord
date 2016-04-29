package net.md_5.bungee.forge;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.forge.ForgeLogger.LogDirection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handshake sequence manager for the Bungee - Forge Server (Downstream/Server
 * Connector) link. Modelled after the Forge implementation.
 */
public enum ForgeServerHandshakeState implements IForgeServerPacketHandler<ForgeServerHandshakeState>
{

    /**
     * Start the handshake.
     *
     */
    START
    {
        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, ChannelWrapper ch)
        {
            ForgeLogger.logServer( LogDirection.RECEIVED, this.name(), message );
            ch.write( message );
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send custom channel registration. Send Hello.
            return HELLO;
        }
    },
    HELLO
    {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, ChannelWrapper ch)
        {
            ForgeLogger.logServer( LogDirection.RECEIVED, this.name(), message );
            if ( message.getData()[0] == 1 ) // Client Hello
            {
                ch.write( message );
            }

            if ( message.getData()[0] == 2 ) // Client ModList
            {
                ch.write( message );
            }

            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send Server Mod List.
            return WAITINGCACK;
        }
    },
    WAITINGCACK
    {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, ChannelWrapper ch)
        {
            ForgeLogger.logServer( LogDirection.RECEIVED, this.name(), message );
            ch.write( message );
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con)
        {
            if ( message.getData()[0] == 3 && message.getTag().equals( ForgeConstants.FML_HANDSHAKE_TAG ) )
            {
                con.getForgeClientHandler().setServerIdList( message );
                return this;
            }

            if ( message.getData()[0] == -1 && message.getTag().equals( ForgeConstants.FML_HANDSHAKE_TAG ) ) // transition to COMPLETE after sending ACK
            {
                return this;
            }

            if ( message.getTag().equals( ForgeConstants.FORGE_REGISTER ) ) // wait for Forge channel registration
            {
                return COMPLETE;
            }

            return this;
        }
    },
    COMPLETE
    {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, ChannelWrapper ch)
        {
            // Wait for ACK
            ForgeLogger.logServer( LogDirection.RECEIVED, this.name(), message );
            ch.write( message );
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send ACK
            return DONE;
        }

    },
    /**
     * Handshake has been completed. Do not respond to any more handshake
     * packets.
     */
    DONE
    {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, ChannelWrapper ch)
        {
            // RECEIVE 2 ACKS
            ForgeLogger.logServer( LogDirection.RECEIVED, this.name(), message );
            ch.write( message );
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con)
        {
            return this;
        }
    }
}
