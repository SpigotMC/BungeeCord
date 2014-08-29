package net.md_5.bungee.forge;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ForgeUtils {

    public static Set<String> readRegisteredChannels(PluginMessage pluginMessage)
    {
        String channels = new String(pluginMessage.getData(), Charsets.UTF_8);
        String[] split = channels.split("\0");
        Set<String> channelSet = ImmutableSet.copyOf(split);
        return channelSet;
    }

    /**
     * Get the build number of FML from the packet.
     * 
     * @param modList The modlist, as bytes.
     * @return The build number, or 0 if it failed.
     */
    public static int getBuildNumber(byte[] modList)
    {
        try
        {
            String s = new String(modList, "UTF-8");
            Matcher matcher = ForgeConstants.FML_HANDSHAKE_VERSION_REGEX.matcher( s );

            if ( matcher.find() ) 
            {
                // We know from the regex that we have an int.
                return Integer.parseInt( matcher.group( 4 ));
            }

            return 0;
        } catch ( UnsupportedEncodingException ex )
        {
            Logger.getLogger( ForgeUtils.class.getName() ).log( Level.SEVERE, null, ex );
            return 0;
        }
    }
}
