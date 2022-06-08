package net.md_5.bungee.conf;

import com.google.common.base.Preconditions;
import gnu.trove.map.TMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Favicon;
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
    /**
     * Whether to check the authentication server public key.
     */
    private boolean enforceSecureProfile;
    /**
     * Whether we log proxy commands to the proxy log
     */
    private boolean logCommands;
    private boolean logPings = true;
    private int remotePingCache = -1;
    private int playerLimit = -1;
    private Collection<String> disabledCommands;
    private int serverConnectTimeout = 5000;
    private int remotePingTimeout = 5000;
    private int throttle = 4000;
    private int throttleLimit = 3;
    private boolean ipForward;
    private Favicon favicon;
    private int compressionThreshold = 256;
    private boolean preventProxyConnections;
    private boolean forgeSupport;

    public void load()
    {
        ConfigurationAdapter adapter = ProxyServer.getInstance().getConfigurationAdapter();
        adapter.load();

        File fav = new File( "server-icon.png" );
        if ( fav.exists() )
        {
            try
            {
                favicon = Favicon.create( ImageIO.read( fav ) );
            } catch ( IOException | IllegalArgumentException ex )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon", ex );
            }
        }

        listeners = adapter.getListeners();
        timeout = adapter.getInt( "timeout", timeout );
        uuid = adapter.getString( "stats", uuid );
        onlineMode = adapter.getBoolean( "online_mode", onlineMode );
        enforceSecureProfile = adapter.getBoolean( "enforce_secure_profile", enforceSecureProfile );
        logCommands = adapter.getBoolean( "log_commands", logCommands );
        logPings = adapter.getBoolean( "log_pings", logPings );
        remotePingCache = adapter.getInt( "remote_ping_cache", remotePingCache );
        playerLimit = adapter.getInt( "player_limit", playerLimit );
        serverConnectTimeout = adapter.getInt( "server_connect_timeout", serverConnectTimeout );
        remotePingTimeout = adapter.getInt( "remote_ping_timeout", remotePingTimeout );
        throttle = adapter.getInt( "connection_throttle", throttle );
        throttleLimit = adapter.getInt( "connection_throttle_limit", throttleLimit );
        ipForward = adapter.getBoolean( "ip_forward", ipForward );
        compressionThreshold = adapter.getInt( "network_compression_threshold", compressionThreshold );
        preventProxyConnections = adapter.getBoolean( "prevent_proxy_connections", preventProxyConnections );
        forgeSupport = adapter.getBoolean( "forge_support", forgeSupport );

        disabledCommands = new CaseInsensitiveSet( (Collection<String>) adapter.getList( "disabled_commands", Arrays.asList( "disabledcommandhere" ) ) );
        disabledCommands.remove( "bungee" ); //BotFilter
        disabledCommands.remove( "botfilter" ); //BotFilter

        Preconditions.checkArgument( listeners != null && !listeners.isEmpty(), "No listeners defined." );

        loadServers( adapter, false );

        for ( ListenerInfo listener : listeners )
        {
            for ( int i = 0; i < listener.getServerPriority().size(); i++ )
            {
                String server = listener.getServerPriority().get( i );
                Preconditions.checkArgument( servers.containsKey( server ), "Server %s (priority %s) is not defined", server, i );
            }
            for ( String server : listener.getForcedHosts().values() )
            {
                if ( !servers.containsKey( server ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Forced host server {0} is not defined", server );
                }
            }
        }
    }

    //BotFilter start
    public void loadServers(ConfigurationAdapter adapter, boolean load)
    {
        if ( load )
        {
            adapter.load();
        }
        Map<String, ServerInfo> newServers = adapter.getServers();
        Preconditions.checkArgument( newServers != null && !newServers.isEmpty(), "No servers defined" );

        if ( servers == null )
        {
            servers = new CaseInsensitiveMap<>( newServers );
        } else
        {
            HashSet<String> toRemove = new HashSet<>();
            servers.values().stream()
                .filter( oldServer -> ( !newServers.containsKey( oldServer.getName() )
                        || !newServers.get( oldServer.getName() ).equals( oldServer ) ) )
                .forEach( server ->
                {
                    toRemove.add( server.getName() );
                    server.getPlayers().forEach( p -> p.disconnect( BungeeCord.getInstance().getTranslation( "server_went_down" ) ) );
                } );

            toRemove.forEach( s -> servers.remove( s ) );

            for ( Map.Entry<String, ServerInfo> newServer : newServers.entrySet() )
            {
                if ( !servers.containsValue( newServer.getValue() ) )
                {
                    servers.put( newServer.getKey(), newServer.getValue() );
                }
            }
        }
    }
    // BotFilter end

    @Override
    @Deprecated
    public String getFavicon()
    {
        return getFaviconObject().getEncoded();
    }

    @Override
    public Favicon getFaviconObject()
    {
        return favicon;
    }
}
