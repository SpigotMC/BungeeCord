package net.md_5.bungee.forge;

import java.util.logging.Level;

import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.forge.ForgeLogger.LogDirection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handshake sequence manager for the Bungee - Forge Client (Upstream) link. Modelled after the Forge implementation.
 * See https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/handshake/FMLHandshakClientState.java
 */
enum ForgeClientHandshakeState implements IForgeClientPacketHandler<ForgeClientHandshakeState>
{
    /**
     * Initiated at the start of a client handshake. This is a special case where we don't want to use a
     * {@link PluginMessage}, we're just sending stuff out here.
     * 
     * Transitions into the HELLO state upon completion.
     * 
     * Requires: {@link UserConnection}.
     */
    START {
        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient(LogDirection.RECEIVED, this.name(), message);
            con.unsafe().sendPacket( message );
            return HELLO;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.getServerConnection().getCh().write(message);
            return HELLO;
        }
    },

    /**
     * Waiting to receive a client HELLO and the mod list. Upon receiving the mod list, return the mod list of the
     * server.
     * 
     * We will be stuck in this state if we don't have a forge client. This is OK.
     * 
     * Transitions to the WAITINGCACK state upon completion.
     * 
     * Requires: {@link PluginMessage}, {@link UserConnection}, {@link ServerConnector}
     */
    HELLO {
        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient(LogDirection.RECEIVED, this.name(), message);
            // Server Hello.
            if (message.getData()[0] == 0) {
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient(LogDirection.SENDING, this.name(), message);
            // Client Hello.
            if (message.getData()[0] == 1) {
                con.getServerConnection().getCh().write(message);
                return this;
            }

            // Mod list.
            if (message.getData()[0] == 2) {
                con.getForgeClientData().setClientModList(message.getData()); // cache used for switching servers
                con.getServerConnection().getCh().write(message);;
                return WAITINGSERVERDATA;
            }

            return this;
        }

    },

    WAITINGSERVERDATA {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Mod list.
            if (message.getData()[0] == 2) {
                // Mod List. The server will actually pass the mod list down to the client
                // (from the FML server, or the empty mod list from Bungee), so no need to send it
                // here. The "setFmlModData" method will set it all in motion.
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send the message, and wait for ACK.
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.getServerConnection().getCh().write(message);
            return WAITINGSERVERCOMPLETE;
        }
    },

    WAITINGSERVERCOMPLETE {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
            // Mod ID's.
            if (message.getData()[0] == 3) {
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send ACK.
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.getServerConnection().getCh().write(message);
            return PENDINGCOMPLETE;
        }
    },

    PENDINGCOMPLETE {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if (message.getData()[0] == -1) {
                ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                con.unsafe().sendPacket( message );
            }

            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send an ACK
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.getServerConnection().getCh().write(message);
            return COMPLETE;
        }
    },

    COMPLETE {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if (message.getData()[0] == -1) {
                ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, this.name(), message );
                con.unsafe().sendPacket( ForgeConstants.FML_ACK );
                return DONE;
            }
            
            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.getServerConnection().getCh().write(message);
            return this;
        }
        
    },

    /**
     * Handshake has been completed. Ignores any future handshake packets.
     */
    DONE {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            return this;
        }
        
    }
}
