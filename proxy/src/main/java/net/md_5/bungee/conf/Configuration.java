package net.md_5.bungee.conf;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import gnu.trove.map.TMap;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import lombok.Getter;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.util.CaseInsensitiveMap;
import net.md_5.bungee.util.CaseInsensitiveSet;

/**
 * Core configuration for the proxy.
 */
@Getter
public class Configuration implements ProxyConfig
{

    /**
     * Time before users are disconnected due to no network activity.
     */
    private int timeout = 30000;
    /**
     * UUID used for metrics.
     */
    private String uuid = UUID.randomUUID().toString();
    /**
     * Set of all listeners.
     */
    private Collection<ListenerInfo> listeners;
    /**
     * Set of all servers.
     */
    private TMap<String, ServerInfo> servers;
    /**
     * Should we check minecraft.net auth.
     */
    private boolean onlineMode = true;
    private int playerLimit = -1;
    private Collection<String> disabledCommands;
    private int throttle = 4000;
    private boolean ipFoward;
    public String favicon;

    public void load()
    {
        ConfigurationAdapter adapter = ProxyServer.getInstance().getConfigurationAdapter();
        adapter.load();

        File fav = new File( "server-icon.png" );
        if ( fav.exists() )
        {
            try
            {
                BufferedImage image = ImageIO.read( fav );
                if ( image != null )
                {
                    if ( image.getHeight() == 64 && image.getWidth() == 64 )
                    {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        ImageIO.write( image, "png", bytes );
                        favicon = "data:image/png;base64," + BaseEncoding.base64().encode( bytes.toByteArray() );
                        if ( favicon.length() > Short.MAX_VALUE )
                        {
                            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Favicon file too large for server to process" );
                            favicon = null;
                        }
                    } else
                    {
                        ProxyServer.getInstance().getLogger().log( Level.WARNING, "Server icon must be exactly 64x64 pixels" );
                    }
                } else
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon for unknown reason. Please double check its format." );
                }
            } catch ( IOException ex )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon", ex );
            }
        }

        listeners = adapter.getListeners();
        timeout = adapter.getInt( "timeout", timeout );
        uuid = adapter.getString( "stats", uuid );
        onlineMode = adapter.getBoolean( "online_mode", onlineMode );
        playerLimit = adapter.getInt( "player_limit", playerLimit );
        throttle = adapter.getInt( "connection_throttle", throttle );
        ipFoward = adapter.getBoolean( "ip_forward", ipFoward );

        disabledCommands = new CaseInsensitiveSet( (Collection<String>) adapter.getList( "disabled_commands", Arrays.asList( "find" ) ) );

        Preconditions.checkArgument( listeners != null && !listeners.isEmpty(), "No listeners defined." );

        Map<String, ServerInfo> newServers = adapter.getServers();
        Preconditions.checkArgument( newServers != null && !newServers.isEmpty(), "No servers defined" );

        if ( servers == null )
        {
            servers = new CaseInsensitiveMap<>( newServers );
        } else
        {
            for ( ServerInfo oldServer : servers.values() )
            {
                // Don't allow servers to be removed
                Preconditions.checkArgument( newServers.containsValue( oldServer ), "Server %s removed on reload!", oldServer.getName() );
            }

            // Add new servers
            for ( Map.Entry<String, ServerInfo> newServer : newServers.entrySet() )
            {
                if ( !servers.containsValue( newServer.getValue() ) )
                {
                    servers.put( newServer.getKey(), newServer.getValue() );
                }
            }
        }

        for ( ListenerInfo listener : listeners )
        {
            Preconditions.checkArgument( servers.containsKey( listener.getDefaultServer() ), "Default server %s is not defined", listener.getDefaultServer() );
            Preconditions.checkArgument( servers.containsKey( listener.getFallbackServer() ), "Fallback server %s is not defined", listener.getFallbackServer() );
            for ( String server : listener.getForcedHosts().values() )
            {
                if ( !servers.containsKey( server ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.SEVERE, "Forced host server {0} is not defined", server );
                }
            }
        }
    }
}
