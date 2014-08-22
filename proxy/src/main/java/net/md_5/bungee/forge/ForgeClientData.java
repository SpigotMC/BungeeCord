package net.md_5.bungee.forge;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handles the Forge Client data and handshake procedure.
 */
@RequiredArgsConstructor
public class ForgeClientData implements IForgeClientData
{
    @NonNull
    private final UserConnection con;
    
    @NonNull
    private ForgeClientHandshakeState state = ForgeClientHandshakeState.START;

    private LoginSuccess loginPacket;

    private ServerConnector serverConnector;

    /**
     * The users' mod list.
     */
    @Getter
    private byte[] clientModList = null;
    
    /**
     * Provides an interface that allows us to send a Forge packet to the server when
     * the client has returned a mod list.
     */
    @Setter
    private IForgePluginMessageSender delayedPacketSender = null;
    
    private PluginMessage serverModList = null;
    private PluginMessage serverIdList = null;
    
    /**
     * Handles the Forge packet.
     * @param message The Forge Handshake packet to handle.
     */
    @Override
    public void handle(PluginMessage message) throws IllegalArgumentException {
        if (!message.getTag().equalsIgnoreCase( ForgeConstants.FORGE_HANDSHAKE_TAG )) {
            throw new IllegalArgumentException("Expecting a Forge Handshake packet.");
        }
        
        state = state.handle( message, con );
        
        if (state == ForgeClientHandshakeState.SENDMODLIST && serverModList != null) {
            setServerModList(serverModList);

            // Null the server mod list now, we don't need to keep it in memory.
            serverModList = null;
        } else if (state == ForgeClientHandshakeState.COMPLETEHANDSHAKE && serverIdList != null) {
            setServerIdList(serverIdList);

            // Null the server mod list now, we don't need to keep it in memory.
            serverIdList = null;
        } else if (state == ForgeClientHandshakeState.DONE && loginPacket != null && serverConnector != null) {
            // Fire login packet handler now!
            try {
                serverConnector.handle( loginPacket );
            } catch (Exception e) {
                // Swallow it, we're outside of the Netty workflow.
            }
        }
    }

    /**
     * Starts a Forge handshake.
     */
    @Override
    public void startHandshake() {
        if (state == ForgeClientHandshakeState.START) {
            // For the START state, it's already part of the call. No need to provide the
            // plugin message here.
            state = state.send( null, con);
        }
    }

    /**
     * Sends a LoginSuccess packet to the Forge client, to reset the handshake state.
     */
    @Override
    public void resetHandshake() {
        state = ForgeClientHandshakeState.START;

        // Send a LoginSuccess packet to reset the handshake.
        if (con.getPendingConnection().getVersion() >= ProtocolConstants.MINECRAFT_1_7_6) {
            con.unsafe().sendPacket(new LoginSuccess(con.getUniqueId().toString(), con.getName())); // With dashes in between
        } else {
            con.unsafe().sendPacket(new LoginSuccess(con.getUUID(), con.getName())); // Without dashes, for older clients.
        }

        // Now start the handshake again
        startHandshake();
    }

    /**
     * Sends the server mod list to the client, or stores it for sending later.
     * 
     * @param modList The {@link PluginMessage} to send to the client containing the mod list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was not as expected.
     */
    @Override
    public void setServerModList(PluginMessage modList) throws IllegalArgumentException {
        if (!modList.getTag().equalsIgnoreCase( ForgeConstants.FORGE_HANDSHAKE_TAG ) || modList.getData()[0] != 2) {
            throw new IllegalArgumentException("modList");
        }
        
        if (state == ForgeClientHandshakeState.SENDMODLIST) {
            // Directly send it.
            state = state.send( modList, con );
        } else {
            // Store it for use later.
            this.serverModList = modList;
        }
    }

    /**
     * Sends the server ID list to the client, or stores it for sending later.
     * 
     * @param idList The {@link PluginMessage} to send to the client containing the ID list.
     * @throws IllegalArgumentException Thrown if the {@link PluginMessage} was not as expected.
     */
    @Override
    public void setServerIdList(PluginMessage idList) throws IllegalArgumentException {
        if (!idList.getTag().equalsIgnoreCase( ForgeConstants.FORGE_HANDSHAKE_TAG ) || idList.getData()[0] != 3) {
            throw new IllegalArgumentException("idList");
        }
        
        if (state == ForgeClientHandshakeState.COMPLETEHANDSHAKE) {
            // Directly send it.
            state = state.send( idList, con );
        } else {
            // Store it for use later.
            this.serverIdList = idList;
        }
    }
    
    /**
     * Returns whether the handshake is complete.
     * @return <code>true</code> if the handshake has been completed.
     */
    @Override
    public boolean isHandshakeComplete() {
        return state == ForgeClientHandshakeState.DONE;
    }

    /**
     * Returns whether we know if the user is a forge user.
     * @return <code>true</code> if the user is a forge user.
     */
    @Override
    public boolean isForgeUser() {
        return clientModList != null;
    }
    
    @Override
    public void setClientModList(byte[] value) {
        this.clientModList = value;
        
        // If we have a delayed packet, process it again.
        if ( delayedPacketSender != null ) {
            delayedPacketSender.send( new PluginMessage( ForgeConstants.FORGE_HANDSHAKE_TAG, value, true ) );
        }
    }

    /**
     * Sets the client to the vanilla experience. If the user has a completed handshake, reset it.
     */
    @Override
    public void setVanilla() {
        if (isHandshakeComplete()) {
            // TODO: When we get to resend the handshake - then we reset it. For now, though, we have
            // to ignore the request here and just return. Remove this line when we can.
            return;
            
            // If we already have a completed handshake, we need to reset the handshake now (if we can). We then set the
            // vanilla forge data. This should be handled automatically by the handshake handler.
            // resetHandshake();
        }

        setServerModList(ForgeConstants.FML_EMPTY_MOD_LIST);
        
        // TODO: Minecraft version.
        setServerIdList(ForgeConstants.FML_DEFAULT_IDS_17);
    }

    @Override
    public void loginSuccessPacketInterception(LoginSuccess login, ServerConnector sc) throws Exception {
        loginPacket = login;
        serverConnector = sc;
        throw CancelSendSignal.INSTANCE;
    }
}
