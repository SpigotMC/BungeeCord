package net.md_5.bungee.forge;

import java.util.regex.Pattern;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ForgeConstants
{

    // Forge
    public static final String FORGE_REGISTER = "FORGE";

    // FML
    public static final String FML_TAG = "FML";
    public static final String FML_HANDSHAKE_TAG = "FML|HS";
    public static final String FML_REGISTER = "REGISTER";

    /**
     * The FML 1.8 handshake token.
     */
    public static final String FML_HANDSHAKE_TOKEN = "\0FML\0";

    public static final PluginMessage FML_RESET_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[]
    {
        -2, 0
    }, false );
    public static final PluginMessage FML_ACK = new PluginMessage( FML_HANDSHAKE_TAG, new byte[]
    {
        -1, 0
    }, false );
    public static final PluginMessage FML_START_CLIENT_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[]
    {
        0, 1
    }, false );
    public static final PluginMessage FML_START_SERVER_HANDSHAKE = new PluginMessage( FML_HANDSHAKE_TAG, new byte[]
    {
        1, 1
    }, false );
    public static final PluginMessage FML_EMPTY_MOD_LIST = new PluginMessage( FML_HANDSHAKE_TAG, new byte[]
    {
        2, 0
    }, false );

    /**
     * The minimum Forge version required to use Forge features. TODO: When the
     * FML branch gets pulled, update this number to be the build that includes
     * the change.
     */
    public static final int FML_MIN_BUILD_VERSION = 1209;

    /**
     * Regex to use to scrape the version information from a FML handshake.
     */
    public static final Pattern FML_HANDSHAKE_VERSION_REGEX = Pattern.compile( "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)" );
}
