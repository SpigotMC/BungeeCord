package net.md_5.bungee.forge;

import com.google.common.io.BaseEncoding;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ForgeConstants
{
    // Forge
    public static final String FORGE_REGISTER = "FORGE";

    // FML
    public static final String FML_TAG = "FML";
    public static final String FML_HANDSHAKE_TAG = "FML|HS";
    public static final String FML_REGISTER = "REGISTER";

    public static final PluginMessage FML_RESET_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[] { -2, 0 }, false );
    public static final PluginMessage FML_ACK = new PluginMessage( FML_HANDSHAKE_TAG, new byte[] { -1, 0 }, false );
    public static final PluginMessage FML_START_CLIENT_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[] { 0, 1 }, false );
    public static final PluginMessage FML_START_SERVER_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[] { 1, 1 }, false );
    public static final PluginMessage FML_EMPTY_MOD_LIST = new PluginMessage( FML_HANDSHAKE_TAG, new byte[] { 2, 0 }, false );
}
