package net.md_5.bungee.forge;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Login;
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

    private Login loginPacket;

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
     * Sets the client to the vanilla experience!
     */
    @Override
    public void setVanilla() {
        setServerModList(ForgeConstants.FML_EMPTY_MOD_LIST);
        
        // TODO: Minecraft version.
        setServerIdList(ForgeConstants.FML_DEFAULT_IDS_17);
    }

    @Override
    public void loginPacketInterception(Login login, ServerConnector sc, boolean firstServer) throws Exception {

        // If we have a stored login packet, use that as an indicator as we don't need
        // to intercept again.
        if (loginPacket != null) {
            loginPacket = null;
            serverConnector = null;
            return;
        }

        loginPacket = login;
        serverConnector = sc;

        if (firstServer) {
            onFirstServer();
        } else {
            onServerSwitch();
        }
        
        // If we get here, there is no need for a callback, so re-null the variables we set.
        loginPacket = null;
        serverConnector = null;
    }
    
    private void onFirstServer() throws Exception {
        // Set the mod and ID list data for the forge handshake. If we are
        // logging onto a Vanilla server, we can't assume that the user isn't Forge,
        // and that the handshake will have completed by now, so set it for everyone.
        //
        // If the user is forge, then we have to do the handshake much earlier, hence the final flag.
        // See the plugin message handler.
        setVanilla();
        throw CancelSendSignal.INSTANCE;
    }
    
    private void onServerSwitch() {
        if (isHandshakeComplete()) {
            // If we already have a completed handshake, we need to reset the handshake now (if we can). We then set the
            // vanilla forge data. This should be handled automatically by the handshake handler.
            resetHandshake();
        }

        // Set the mod and ID list data for the handshake. By this point, we know that the user is a Forge user,
        // so we just set it for them in these cases.
        setVanilla();
        throw CancelSendSignal.INSTANCE;
    }
}
