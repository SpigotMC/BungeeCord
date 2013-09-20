package net.md_5.bungee.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.config.TexturePackInfo;
import net.md_5.bungee.api.tab.TabListHandler;
import net.md_5.bungee.tab.Global;
import net.md_5.bungee.tab.GlobalPing;
import net.md_5.bungee.tab.ServerUnique;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YamlConfig implements ConfigurationAdapter
{

    /**
     * The default tab list options available for picking.
     */
    @RequiredArgsConstructor
    private enum DefaultTabList
    {

        GLOBAL( Global.class ), GLOBAL_PING( GlobalPing.class ), SERVER( ServerUnique.class );
        private final Class<? extends TabListHandler> clazz;
    }
    private Yaml yaml;
    private Map config;
    private final File file = new File( "config.yml" );

    @Override
    public void load()
    {
        try
        {
            file.createNewFile();
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
            yaml = new Yaml( options );

            try ( InputStream is = new FileInputStream( file ) )
            {
                config = (Map) yaml.load( is );
            }

            if ( config == null )
            {
                config = new CaseInsensitiveMap();
            } else
            {
                config = new CaseInsensitiveMap( config );
            }
        } catch ( IOException ex )
        {
            throw new RuntimeException( "Could not load configuration!", ex );
        }

        Map<String, Object> permissions = get( "permissions", new HashMap<String, Object>() );
        if ( permissions.isEmpty() )
        {
            permissions.put( "default", Arrays.asList( new String[]
            {
                "bungeecord.command.server", "bungeecord.command.list"
            } ) );
            permissions.put( "admin", Arrays.asList( new String[]
            {
                "bungeecord.command.alert", "bungeecord.command.end", "bungeecord.command.ip", "bungeecord.command.reload"
            } ) );
        }

        Map<String, Object> groups = get( "groups", new HashMap<String, Object>() );
        if ( groups.isEmpty() )
        {
            groups.put( "md_5", Collections.singletonList( "admin" ) );
        }
    }

    private <T> T get(String path, T def)
    {
        return get( path, def, config );
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String path, T def, Map submap)
    {
        int index = path.indexOf( '.' );
        if ( index == -1 )
        {
            Object val = submap.get( path );
            if ( val == null && def != null )
            {
                val = def;
                submap.put( path, def );
                save();
            }
            return (T) val;
        } else
        {
            String first = path.substring( 0, index );
            String second = path.substring( index + 1, path.length() );
            Map sub = (Map) submap.get( first );
            if ( sub == null )
            {
                sub = new LinkedHashMap();
                submap.put( first, sub );
            }
            return get( second, def, sub );
        }
    }

    private void save()
    {
        try
        {
            try ( FileWriter wr = new FileWriter( file ) )
            {
                yaml.dump( config, wr );
            }
        } catch ( IOException ex )
        {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not save config", ex );
        }
    }

    @Override
    public int getInt(String path, int def)
    {
        return get( path, def );
    }

    @Override
    public String getString(String path, String def)
    {
        return get( path, def );
    }

    @Override
    public boolean getBoolean(String path, boolean def)
    {
        return get( path, def );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ServerInfo> getServers()
    {
        Map<String, Map<String, Object>> base = get( "servers", (Map) Collections.singletonMap( "lobby", new HashMap<>() ) );
        Map<String, ServerInfo> ret = new HashMap<>();

        for ( Map.Entry<String, Map<String, Object>> entry : base.entrySet() )
        {
            Map<String, Object> val = entry.getValue();
            String name = entry.getKey();
            String addr = get( "address", "localhost:25565", val );
            String motd = ChatColor.translateAlternateColorCodes( '&', get( "motd", "Just another BungeeCord - Forced Host", val ) );
            boolean restricted = get( "restricted", false, val );
            InetSocketAddress address = Util.getAddr( addr );
            ServerInfo info = ProxyServer.getInstance().constructServerInfo( name, address, motd, restricted );
            ret.put( name, info );
        }

        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ListenerInfo> getListeners()
    {
        Collection<Map<String, Object>> base = get( "listeners", (Collection) Arrays.asList( new Map[]
        {
            new HashMap()
        } ) );
        Map<String, String> forcedDef = new HashMap<>();
        forcedDef.put( "pvp.md-5.net", "pvp" );

        Collection<ListenerInfo> ret = new HashSet<>();

        for ( Map<String, Object> val : base )
        {
            String motd = get( "motd", "Another Bungee server", val );
            motd = ChatColor.translateAlternateColorCodes( '&', motd );

            int maxPlayers = get( "max_players", 1, val );
            String defaultServer = get( "default_server", "lobby", val );
            String fallbackServer = get( "fallback_server", defaultServer, val );
            boolean forceDefault = get( "force_default_server", false, val );
            String host = get( "host", "0.0.0.0:25577", val );
            int tabListSize = get( "tab_size", 60, val );
            InetSocketAddress address = Util.getAddr( host );
            Map<String, String> forced = new CaseInsensitiveMap<>( get( "forced_hosts", forcedDef, val ) );
            String textureURL = get( "texture_url", null, val );
            int textureSize = get( "texture_size", 16, val );
            TexturePackInfo texture = ( textureURL == null ) ? null : new TexturePackInfo( textureURL, textureSize );
            String tabListName = get( "tab_list", "GLOBAL_PING", val );
            DefaultTabList value = DefaultTabList.valueOf( tabListName.toUpperCase() );
            if ( value == null )
            {
                value = DefaultTabList.GLOBAL_PING;
            }

            ListenerInfo info = new ListenerInfo( address, motd, maxPlayers, tabListSize, defaultServer, fallbackServer, forceDefault, forced, texture, value.clazz );
            ret.add( info );
        }

        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getGroups(String player)
    {
        Collection<String> groups = get( "groups." + player, null );
        Collection<String> ret = ( groups == null ) ? new HashSet<String>() : new HashSet<>( groups );
        ret.add( "default" );
        return ret;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getDownstreamProxies() {
    	Collection<String> proxies = get( "downstream_proxies", null );
    	return (proxies == null) ? new HashSet<String>() : new HashSet<>( proxies );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> getPermissions(String group)
    {
        return get( "permissions." + group, Collections.EMPTY_LIST );
    }
}
