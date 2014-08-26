package net.md_5.bungee.forge;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.forge.delegates.IForgePluginMessageSender;
import net.md_5.bungee.forge.delegates.IVoidAction;
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
    
    /**
     * Provides an interface that allows us to send a Forge packet to the server when
     * the client has transitioned to the DONE state.
     */
    @Setter
    private IVoidAction serverHandshakeCompletion = null;
    
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

        ForgeLogger.logClient( ForgeLogger.LogDirection.RECEIVED, state.name(), message);
        state = state.send( message, con );
    }

    /**
     * Sends a LoginSuccess packet to the Forge client, to reset the handshake state.
     */
    @Override
    public void resetHandshake() {
        state = ForgeClientHandshakeState.START;
        con.unsafe().sendPacket(ForgeConstants.FML_RESET_HANDSHAKE);
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

        this.serverModList = modList;
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

        this.serverIdList = idList;
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
    }
}
