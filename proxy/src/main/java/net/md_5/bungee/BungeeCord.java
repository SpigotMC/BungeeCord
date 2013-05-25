package net.md_5.bungee;

import net.md_5.bungee.scheduler.BungeeScheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import gnu.trove.map.TMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.md_5.bungee.config.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.CommandSender;
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
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.command.*;
import net.md_5.bungee.config.YamlConfig;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.protocol.PacketDefinitions;
import net.md_5.bungee.scheduler.BungeeThreadPool;
import net.md_5.bungee.util.CaseInsensitiveMap;

/**
 * Main BungeeCord proxy class.
 */
public class BungeeCord extends ProxyServer
{

    /**
     * Current operation state.
     */
    public volatile boolean isRunning;
    /**
     * Configuration.
     */
    public final Configuration config = new Configuration();
    /**
     * Localization bundle.
     */
    public final ResourceBundle bundle = ResourceBundle.getBundle( "messages_en" );
    /**
     * Thread pools.
     */
    public final ScheduledThreadPoolExecutor executors = new BungeeThreadPool( new ThreadFactoryBuilder().setNameFormat( "Bungee Pool Thread #%1$d" ).build() );
    public final MultithreadEventLoopGroup eventLoops = new NioEventLoopGroup( NioEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS, new ThreadFactoryBuilder().setNameFormat( "Netty IO Thread #%1$d" ).build() );
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
    private final TMap<String, UserConnection> connections = new CaseInsensitiveMap<>();
    private final ReadWriteLock connectionLock = new ReentrantReadWriteLock();
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
    public final PluginManager pluginManager = new PluginManager( this );
    @Getter
    @Setter
    private ReconnectHandler reconnectHandler;
    @Getter
    @Setter
    private ConfigurationAdapter configurationAdapter = new YamlConfig();
    private final Collection<String> pluginChannels = new HashSet<>();
    @Getter
    private final File pluginsFolder = new File( "plugins" );
    @Getter
    private final TaskScheduler scheduler = new BungeeScheduler();
    @Getter
    private final AsyncHttpClient httpClient = new AsyncHttpClient(
            new NettyAsyncHttpProvider(
            new AsyncHttpClientConfig.Builder().setAsyncHttpClientProviderConfig(
            new NettyAsyncHttpProviderConfig().addProperty( NettyAsyncHttpProviderConfig.BOSS_EXECUTOR_SERVICE, executors ) ).setExecutorService( executors ).build() ) );

    
    {
        // TODO: Proper fallback when we interface the manager
        getPluginManager().registerCommand( null, new CommandReload() );
        getPluginManager().registerCommand( null, new CommandEnd() );
        getPluginManager().registerCommand( null, new CommandList() );
        getPluginManager().registerCommand( null, new CommandServer() );
        getPluginManager().registerCommand( null, new CommandIP() );
        getPluginManager().registerCommand( null, new CommandAlert() );
        getPluginManager().registerCommand( null, new CommandBungee() );
        getPluginManager().registerCommand( null, new CommandPerms() );
        getPluginManager().registerCommand( null, new CommandSend() );
        getPluginManager().registerCommand( null, new CommandWhois() );

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
     * @throws Exception when the server cannot be started
     */
    public static void main(String[] args) throws Exception
    {
        Calendar deadline = Calendar.getInstance();
        deadline.set( 2013, 6, 14 ); // year, month, date
        if ( Calendar.getInstance().after( deadline ) )
        {
            System.err.println( "*** Warning, this build is outdated ***" );
            System.err.println( "*** Please download a new build from http://ci.md-5.net/job/BungeeCord ***" );
            System.err.println( "*** You will get NO support regarding this build ***" );
            System.err.println( "*** Server will start in 15 seconds ***" );
            Thread.sleep( TimeUnit.SECONDS.toMillis( 15 ) );
        }

        BungeeCord bungee = new BungeeCord();
        ProxyServer.setInstance( bungee );
        bungee.getLogger().info( "Enabled BungeeCord version " + bungee.getVersion() );
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
     * @throws Exception
     */
    @Override
    public void start() throws Exception
    {
        pluginsFolder.mkdir();
        pluginManager.loadPlugins( pluginsFolder );
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
        for ( final ListenerInfo info : config.getListeners() )
        {
            ChannelFutureListener listener = new ChannelFutureListener()
            {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception
                {
                    if ( future.isSuccess() )
                    {
                        listeners.add( future.channel() );
                        getLogger().info( "Listening on " + info.getHost() );
                    } else
                    {
                        getLogger().log( Level.WARNING, "Could not bind to host " + info.getHost(), future.cause() );
                    }
                }
            };
            new ServerBootstrap()
                    .channel( NioServerSocketChannel.class )
                    .childAttr( PipelineUtils.LISTENER, info )
                    .childHandler( PipelineUtils.SERVER_CHILD )
                    .group( eventLoops )
                    .localAddress( info.getHost() )
                    .bind().addListener( listener );
        }
    }

    public void stopListeners()
    {
        for ( Channel listener : listeners )
        {
            getLogger().log( Level.INFO, "Closing listener {0}", listener );
            try
            {
                listener.close().syncUninterruptibly();
            } catch ( ChannelException ex )
            {
                getLogger().severe( "Could not close listen thread" );
            }
        }
        listeners.clear();
    }

    @Override
    public void stop()
    {
        this.isRunning = false;

        httpClient.close();
        executors.shutdown();

        stopListeners();
        getLogger().info( "Closing pending connections" );

        connectionLock.readLock().lock();
        try
        {
            getLogger().info( "Disconnecting " + connections.size() + " connections" );
            for ( UserConnection user : connections.values() )
            {
                user.disconnect( getTranslation( "restart" ) );
            }
        } finally
        {
            connectionLock.readLock().unlock();
        }

        getLogger().info( "Closing IO threads" );
        eventLoops.shutdownGracefully();

        getLogger().info( "Saving reconnect locations" );
        reconnectHandler.save();
        saveThread.cancel();

        // TODO: Fix this shit
        getLogger().info( "Disabling plugins" );
        for ( Plugin plugin : pluginManager.getPlugins() )
        {
            plugin.onDisable();
            getScheduler().cancel( plugin );
        }

        getLogger().info( "Thankyou and goodbye" );
        System.exit( 0 );
    }

    /**
     * Broadcasts a packet to all clients that is connected to this instance.
     *
     * @param packet the packet to send
     */
    public void broadcast(DefinedPacket packet)
    {
        connectionLock.readLock().lock();
        try
        {
            for ( UserConnection con : connections.values() )
            {
                con.sendPacket( packet );
            }
        } finally
        {
            connectionLock.readLock().unlock();
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
    public String getTranslation(String name)
    {
        String translation = "<translation '" + name + "' missing>";
        try
        {
            translation = bundle.getString( name );
        } catch ( MissingResourceException ex )
        {
        }
        return translation;
    }

    @Override
    public Logger getLogger()
    {
        return BungeeLogger.instance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<ProxiedPlayer> getPlayers()
    {
        connectionLock.readLock().lock();
        try
        {
            return (Collection) new HashSet<>( connections.values() );
        } finally
        {
            connectionLock.readLock().unlock();
        }
    }

    @Override
    public int getOnlineCount()
    {
        return connections.size();
    }

    @Override
    public ProxiedPlayer getPlayer(String name)
    {
        connectionLock.readLock().lock();
        try
        {
            return connections.get( name );
        } finally
        {
            connectionLock.readLock().unlock();
        }
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
        return new PacketFAPluginMessage( "REGISTER", Util.format( pluginChannels, "\00" ).getBytes() );
    }

    @Override
    public byte getProtocolVersion()
    {
        return PacketDefinitions.PROTOCOL_VERSION;
    }

    @Override
    public String getGameVersion()
    {
        return PacketDefinitions.GAME_VERSION;
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address, boolean restricted)
    {
        return new BungeeServerInfo( name, address, restricted );
    }

    @Override
    public CommandSender getConsole()
    {
        return ConsoleCommandSender.getInstance();
    }

    @Override
    public void broadcast(String message)
    {
        getConsole().sendMessage( message );
        broadcast( new Packet3Chat( message ) );
    }

    public void addConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.put( con.getName(), con );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }

    public void removeConnection(UserConnection con)
    {
        connectionLock.writeLock().lock();
        try
        {
            connections.remove( con.getName() );
        } finally
        {
            connectionLock.writeLock().unlock();
        }
    }
}
