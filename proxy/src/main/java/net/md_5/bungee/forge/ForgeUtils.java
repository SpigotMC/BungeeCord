package net.md_5.bungee.forge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ForgeUtils
{

    /**
     * Gets the registered FML channels from the packet.
     *
     * @param pluginMessage The packet representing FMLProxyPacket.
     * @return The registered channels.
     */
    public static Set<String> readRegisteredChannels(PluginMessage pluginMessage)
    {
        String channels = new String( pluginMessage.getData(), Charsets.UTF_8 );
        String[] split = channels.split( "\0" );
        Set<String> channelSet = ImmutableSet.copyOf( split );
        return channelSet;
    }

    /**
     * Gets the modlist from the packet.
     *
     * @param pluginMessage The packet representing FMLProxyPacket.
     * @return The modlist.
     */
    public static Map<String, String> readModList(PluginMessage pluginMessage)
    {
        Map<String, String> modTags = Maps.newHashMap();
        ByteBuf payload = Unpooled.wrappedBuffer( pluginMessage.getData() );
        byte discriminator = payload.readByte();
        if ( discriminator == 2 ) // ModList
        {
            ByteBuf buffer = payload.slice();
            int modCount = DefinedPacket.readVarInt( buffer, 2 );
            for ( int i = 0; i < modCount; i++ )
            {
                modTags.put( DefinedPacket.readString( buffer ), DefinedPacket.readString( buffer ) );
            }
        }
        return modTags;
    }

    /**
     * Get the build number of FML from the packet.
     *
     * @param modList The modlist, as a Map.
     * @return The build number, or 0 if it failed.
     */
    public static int getFmlBuildNumber(Map<String, String> modList)
    {
        if ( modList.containsKey( "FML" ) )
        {
            String fmlVersion  = modList.get( "FML" );
            
            // FML's version is hardcoded to this for builds beyond 1405 for 1.7.10 - if we see this, return Forge's build number.
            if ( fmlVersion.equals( "7.10.99.99" ) )
            {
                Matcher matcher = ForgeConstants.FML_HANDSHAKE_VERSION_REGEX.matcher( modList.get( "Forge" ) );
                if ( matcher.find() )
                {
                    // We know from the regex that we have an int.
                    return Integer.parseInt( matcher.group( 4 ) );
                }
            }
            else 
            {
                Matcher matcher = ForgeConstants.FML_HANDSHAKE_VERSION_REGEX.matcher( fmlVersion );
                if ( matcher.find() )
                {
                    // We know from the regex that we have an int.
                    return Integer.parseInt( matcher.group( 4 ) );
                }
            }
        }

        return 0;
    }
}
