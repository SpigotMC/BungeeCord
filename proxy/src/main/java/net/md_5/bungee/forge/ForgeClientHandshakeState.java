package net.md_5.bungee.forge;

import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handshake sequence manager for the Bungee - Forge Client (Upstream) link. Modelled after the Forge implementation.
 * See https://github.com/MinecraftForge/FML/blob/master/src/main/java/cpw/mods/fml/common/network/handshake/FMLHandshakeServerState.java
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
            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.unsafe().sendPacket( message );
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
            // Client Hello.
            if (message.getData()[0] == 0) {
                // We actually ignore this, and wait for the mod list.
                return this;
            }
            
            // Mod list.
            if (message.getData()[0] == 2) {
                // Mod List. The server will actually pass the mod list down to the client
                // (from the FML server, or the empty mod list from Bungee), so no need to send it
                // here. The "setFmlModData" method will set it all in motion.
                con.getForgeClientData().setClientModList(message.getData());
               
                // We are yet to get a mod list. Once we get one, it'll get fired by the send method
                // of the next state.
                return WAITINGCACK;
            }
            
            // Ignore anything else.
            return this;
        }

        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            return this;
        }

    },
    
    /**
     * Sends mod list, and waits for ACK to signify acceptance of the mod list.
     * 
     * Transitions to COMPLETE
     */
    WAITINGCACK {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if (message.getData()[0] == -1) {
                // We got acceptance - so complete the handshake.
                return COMPLETE;
            }

            return this;
        }
        
        /**
         * Sends, or stores for sending, the Server Mod List.
         * 
         * @param message The {@link PluginMessage} containing the mod list.
         * @param con The {@link UserConnection} to send the mod list to.
         * @return The state to enter.
         */
        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send the message, and wait for ACK.
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.unsafe().sendPacket( message );
            return this;
        }
    },

    /**
     * Completes the handshake by sending the ID list, and waiting for acceptance. Transitions to the DONE state
     * upon acceptance.
     */
    COMPLETE {

        @Override
        public ForgeClientHandshakeState handle(PluginMessage message, UserConnection con)
        {
            // Ack.
            if (message.getData()[0] == -1) {
                // We got acceptance - so send an "ack" to put the client into the COMPLETE state, and we are done!
                ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), ForgeConstants.FML_ACK );
                con.unsafe().sendPacket( ForgeConstants.FML_ACK );
                return DONE;
            }
            
            return this;
        }

        /**
         * Sends the Server ID List.
         * 
         * @param message The {@link PluginMessage} containing the ID list.
         * @param con The {@link UserConnection} to send the ID list to.
         * @return The state to enter.
         */
        @Override
        public ForgeClientHandshakeState send(PluginMessage message, UserConnection con)
        {
            // Send the ID list and an ACK, and wait for ACK.
            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), message );
            con.unsafe().sendPacket( message );

            ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, this.name(), ForgeConstants.FML_ACK );
            con.unsafe().sendPacket( ForgeConstants.FML_ACK );
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
