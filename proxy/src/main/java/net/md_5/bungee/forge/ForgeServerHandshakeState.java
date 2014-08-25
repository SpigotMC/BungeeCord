package net.md_5.bungee.forge;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handshake sequence manager for the Bungee - Forge Server (Downstream/Server Connector) link. Modelled after the Forge implementation.
 */
public enum ForgeServerHandshakeState implements IForgeServerPacketHandler<ForgeServerHandshakeState> {
    /**
     * Start the handshake. 
     * 
     * If the user is already known to be a Forge user, then send the mod list - transition to WAITINGFORSERVERDATA state.
     * If not, hold on, transition to PENDINGUSER state.
     */
    START {
        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            // If the user is a Forge user already, return the state from the
            // PENDINGUSER send method.
            if (con.getForgeClientData().isForgeUser()) {
                return PENDINGUSER.send( new PluginMessage ( ForgeConstants.FORGE_HANDSHAKE_TAG, con.getForgeClientData().getClientModList(), true), con, ch );
            }

            // Otherwise, we have to wait for the user to be ready. 
            return PENDINGUSER;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }
        
    },
    
    /**
     * A special state, indicating that we are waiting for a user to send their Mod list. 
     * 
     * Generally only seen during a User's initial connection. Will transition to the WAITINGFORSERVERDATA state.
     */
    PENDINGUSER {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            // Send custom channel registration. Send Hello. Send Server Mod List.
            ch.write( ForgeConstants.FML_START_SERVER_HANDSHAKE );
            ch.write( message );

            return WAITINGFORSERVERDATA;
        }
    },
    
    /**
     * Waiting for the server to accept the mod list, and send their mod list. 
     * 
     * Will send the server an "ack" packet, and transition to the WAITINGFORIDLIST state.
     */
    WAITINGFORSERVERDATA {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            // Mod List
            if (message.getData()[0] == 2) {
                // Send ACK
                ch.write( ForgeConstants.FML_ACK );
                con.getForgeClientData().setServerModList( message );
                return WAITFORIDLIST;
            }

            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }
    },
    
    /**
     * Accepts the server item ID list, and sends an "Ack" back. Transitions to the PRECOMPLETION state.
     */
    WAITFORIDLIST {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            if (message.getData()[0] == 3) {
                // We don't send back "ACK" just yet. We hold the server at this state in order to
                // wait for the user to recieve the ID list and accept it, and complete their handshake.
                // Once their handshake is complete, then we finish this and complete server connection.
                con.getForgeClientData().setServerIdList( message );
                return PRECOMPLETION;
            }
            
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }
        
    },
    
    /**
     * Responds to the server's final "Ack". Transitions to the "DONE" state.
     */
    PRECOMPLETION {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            if (message.getData()[0] == -1) {
                ch.write( ForgeConstants.FML_ACK );
                return DONE;
            }

            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            // Send ACK
            ch.write( ForgeConstants.FML_ACK );
            return this;
        }
        
    },
    
    /**
     * Handshake has been completed. Do not respond to any more handshake packets.
     */
    DONE {

        @Override
        public ForgeServerHandshakeState handle(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }

        @Override
        public ForgeServerHandshakeState send(PluginMessage message, UserConnection con, ChannelWrapper ch)
        {
            return this;
        }
    }
}
