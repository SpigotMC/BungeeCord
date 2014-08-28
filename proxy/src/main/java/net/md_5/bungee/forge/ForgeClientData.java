package net.md_5.bungee.forge;

import java.util.ArrayDeque;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PluginMessage;

/**
 * Handles the Forge Client data and handshake procedure.
 */
@RequiredArgsConstructor
public class ForgeClientData implements IForgeClientData
{
    @NonNull
    private final UserConnection con;

    /**
     * The users' mod list.
     */
    @Getter
    private byte[] clientModList = null;

    @Getter
    @Setter
    private ArrayDeque<PluginMessage> packetQueue = new ArrayDeque<PluginMessage>();

    private PluginMessage serverModList = null;
    private PluginMessage serverIdList = null;

    /**
     * Handles the Forge packet.
     * @param message The Forge Handshake packet to handle.
     */
    @Override
    public void handle(PluginMessage message) throws IllegalArgumentException {
        if (!message.getTag().equalsIgnoreCase(ForgeConstants.FML_HANDSHAKE_TAG)) {
            throw new IllegalArgumentException("Expecting a Forge Handshake packet.");
        }

        message.setAllowExtendedPacket(true); // FML allows extended packets so this must be enabled
        ForgeClientHandshakeState prevState = con.getState();
        packetQueue.add(message);
        con.setState(con.getState().send( message, con ));
        if (con.getState() != prevState) // state finished, send packets
        {
            synchronized (packetQueue)
            {
                while (!packetQueue.isEmpty())
                {
                    ForgeLogger.logClient( ForgeLogger.LogDirection.SENDING, prevState.name(), packetQueue.getFirst());
                    con.getForgeServer().receive(packetQueue.removeFirst());
                }
            }
        }
    }

    /**
     * Receives a {@link PluginMessage} from ForgeServer to pass to Client.
     *
     * @param message The message to being received.
     */
    @Override
    public void receive(PluginMessage message) throws IllegalArgumentException {
        con.setState(con.getState().handle(message, con));
    }

    /**
     * Sends a LoginSuccess packet to the Forge client, to reset the handshake state.
     */
    @Override
    public void resetHandshake() {
        con.setState(ForgeClientHandshakeState.HELLO);
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
        if (!modList.getTag().equalsIgnoreCase( ForgeConstants.FML_HANDSHAKE_TAG ) || modList.getData()[0] != 2) {
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
        if (!idList.getTag().equalsIgnoreCase( ForgeConstants.FML_HANDSHAKE_TAG ) || idList.getData()[0] != 3) {
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
        return con.getState() == ForgeClientHandshakeState.DONE;
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
