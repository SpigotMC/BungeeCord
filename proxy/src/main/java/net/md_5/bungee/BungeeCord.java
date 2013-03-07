package net.md_5.bungee;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.md_5.bungee.config.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.TabListHandler;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.command.*;
import net.md_5.bungee.config.YamlConfig;
import net.md_5.bungee.netty.ChannelBootstrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketDecoder;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.PacketFAPluginMessage;

/**
 * Main BungeeCord proxy class.
 */
public class BungeeCord extends ProxyServer
{

    /**
     * Server protocol version.
     */
    public static final byte PROTOCOL_VERSION = 51;
    /**
     * Server game version.
     */
    public static final String GAME_VERSION = "1.4.6";
    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    public final Configuration config = new Configuration();
    /**
     * Thread pool.
     */
    public final MultithreadEventLoopGroup eventLoops = new NioEventLoopGroup( 8, new ThreadFactoryBuilder().setNameFormat( "Netty IO Thread - %1$d" ).build() );
    /**
     * locations.yml save thread.
     */
    private final Timer saveThread = new Timer( "Reconnect Saver" );
    /**
     * Server socket listener.
     */
    private Collection<Channel> listeners = new HashSet<>();
    /**
     * Fully qualified connections.
     */
    public Map<String, UserConnection> connections = new ConcurrentHashMap<>();
    /**
     * Tab list handler
     */
    @Getter
    @Setter
    public TabListHandler tabListHandler;
    /**
     * Plugin manager.
     */
    @Getter
    public final PluginManager pluginManager = new PluginManager();
    @Getter
    @Setter
    private ReconnectHandler reconnectHandler;
    @Getter
    @Setter
    private ConfigurationAdapter configurationAdapter = new YamlConfig();
    private final Collection<String> pluginChannels = new HashSet<>();

    
    {
        getPluginManager().registerCommand( new CommandReload() );
        getPluginManager().registerCommand( new CommandEnd() );
        getPluginManager().registerCommand( new CommandList() );
        getPluginManager().registerCommand( new CommandServer() );
        getPluginManager().registerCommand( new CommandIP() );
        getPluginManager().registerCommand( new CommandAlert() );
        getPluginManager().registerCommand( new CommandBungee() );
        getPluginManager().registerCommand( new CommandPerms() );

        registerChannel( "BungeeCord" );
    }

    public static BungeeCord getInstance()
    {
        return (BungeeCord) ProxyServer.getInstance();
    }

    /**
     * Starts a new instance of BungeeCord.
     *
     * @param args command line arguments, currently none are used
     * @throws IOException when the server cannot be started
     */
    public static void main(String[] args) throws IOException
    {
        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        $().info( "Enabled BungeeCord version " + bungee.getVersion() );
        bungee.start();

        BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
        while ( bungee.isRunning )
        {
            String line = br.readLine();
            if ( line != null )
            {
                boolean handled = getInstance().getPluginManager().dispatchCommand( ConsoleCommandSender.getInstance(), line );
                if ( !handled )
                {
                    System.err.println( "Command not found" );
                }
            }
        }
    }

    /**
     * Start this proxy instance by loading the configuration, plugins and
     * starting the connect thread.
     *
     * @throws IOException
     */
    @Override
    public void start() throws IOException
    {
        File plugins = new File( "plugins" );
        plugins.mkdir();
        pluginManager.loadPlugins( plugins );
        config.load();
        if ( reconnectHandler == null )
        {
            reconnectHandler = new YamlReconnectHandler();
        }
        isRunning = true;

        pluginManager.enablePlugins();

        startListeners();

        saveThread.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                getReconnectHandler().save();
            }
        }, 0, TimeUnit.MINUTES.toMillis( 5 ) );

        new Metrics().start();
    }

    public void startListeners()
    {
        for ( ListenerInfo info : config.getListeners() )
        {
            Channel server = new ServerBootstrap()
                    .childHandler( ChannelBootstrapper.SERVER )
                    .localAddress( info.getHost() )
                    .group( eventLoops )
                    .bind().channel();
            listeners.add( server );

            $().info( "Listening on " + info.getHost() );
        }
    }

    public void stopListeners()
    {
        for ( Channel listener : listeners )
        {
            $().log( Level.INFO, "Closing listener {0}", listener );
            try
            {
                listener.close().syncUninterruptibly();
            } catch ( ChannelException ex )
            {
                $().severe( "Could not close listen thread" );
            }
        }
        listeners.clear();
    }

    @Override
    public void stop()
    {
        this.isRunning = false;

        stopListeners();
        $().info( "Closing pending connections" );

        $().info( "Disconnecting " + connections.size() + " connections" );
        for ( UserConnection user : connections.values() )
        {
            user.disconnect( "Proxy restarting, brb." );
        }

        $().info( "Closing IO threads" );
        eventLoops.shutdown();

        $().info( "Saving reconnect locations" );
        reconnectHandler.save();
        saveThread.cancel();

        $().info( "Disabling plugins" );
        for ( Plugin plugin : pluginManager.getPlugins() )
        {
            plugin.onDisable();
        }

        $().info( "Thank you and goodbye" );
        System.exit( 0 );
    }

    /**
     * Broadcasts a packet to all clients that is connected to this instance.
     *
     * @param packet the packet to send
     */
    public void broadcast(DefinedPacket packet)
    {
        for ( UserConnection con : connections.values() )
        {
            con.packetQueue.add( packet );
        }
    }

    @Override
    public String getName()
    {
        return "BungeeCord";
    }

    @Override
    public String getVersion()
    {
        return ( BungeeCord.class.getPackage().getImplementationVersion() == null ) ? "unknown" : BungeeCord.class.getPackage().getImplementationVersion();
    }

    @Override
    public Logger getLogger()
    {
        return $();
    }

    @Override
    @SuppressWarnings("unchecked") // TODO: Abstract more
    public Collection<ProxiedPlayer> getPlayers()
    {
        return (Collection) connections.values();
    }

    @Override
    public ProxiedPlayer getPlayer(String name)
    {
        return connections.get( name );
    }

    @Override
    public Server getServer(String name)
    {
        Collection<ProxiedPlayer> users = getServers().get( name ).getPlayers();
        return ( users != null && !users.isEmpty() ) ? users.iterator().next().getServer() : null;
    }

    @Override
    public Map<String, ServerInfo> getServers()
    {
        return config.getServers();
    }

    @Override
    public ServerInfo getServerInfo(String name)
    {
        return getServers().get( name );
    }

    @Override
    @Synchronized("pluginChannels")
    public void registerChannel(String channel)
    {
        pluginChannels.add( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public void unregisterChannel(String channel)
    {
        pluginChannels.remove( channel );
    }

    @Override
    @Synchronized("pluginChannels")
    public Collection<String> getChannels()
    {
        return Collections.unmodifiableCollection( pluginChannels );
    }

    public PacketFAPluginMessage registerChannels()
    {
        StringBuilder sb = new StringBuilder();
        for ( String s : getChannels() )
        {
            sb.append( s );
            sb.append( '\00' );
        }
        byte[] payload = sb.substring( 0, sb.length() - 1 ).getBytes();
        return new PacketFAPluginMessage( "REGISTER", payload );
    }

    @Override
    public byte getProtocolVersion()
    {
        return PROTOCOL_VERSION;
    }

    @Override
    public String getGameVersion()
    {
        return GAME_VERSION;
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address)
    {
        return new BungeeServerInfo( name, address );
    }
}
