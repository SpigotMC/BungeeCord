package net.md_5.bungee;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.log.BungeeLogger;
import net.md_5.bungee.reconnect.YamlReconnectHandler;
import net.md_5.bungee.scheduler.BungeeScheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import net.md_5.bungee.conf.Configuration;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.internal.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.tab.CustomTabList;
import net.md_5.bungee.command.*;
import net.md_5.bungee.conf.YamlConfig;
import net.md_5.bungee.log.LoggingOutputStream;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.query.RemoteQuery;
import net.md_5.bungee.tab.Custom;
import net.md_5.bungee.util.CaseInsensitiveMap;
import org.fusesource.jansi.AnsiConsole;

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
    public final ResourceBundle bundle = ResourceBundle.getBundle( "messages" );
    public final MultithreadEventLoopGroup eventLoops = new NioEventLoopGroup( 0, new ThreadFactoryBuilder().setNameFormat( "Netty IO Thread #%1$d" ).build() );
    /**
     * locations.yml save thread.
     */
    private final Timer saveThread = new Timer( "Reconnect Saver" );
    private final Timer metricsThread = new Timer( "Metrics Thread" );
    /**
     * Server socket listener.
     */
    private Collection<Channel> listeners = new HashSet<>();
    /**
     * Fully qualified connections.
     */
    private final Map<String, UserConnection> connections = new CaseInsensitiveMap<>();
    private final ReadWriteLock connectionLock = new ReentrantReadWriteLock();
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
    private final BungeeScheduler scheduler = new BungeeScheduler();
    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private final Logger logger;
    public final Gson gson = new Gson();
    @Getter
    private ConnectionThrottle connectionThrottle;

    
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
        getPluginManager().registerCommand( null, new CommandFind() );

        registerChannel( "BungeeCord" );
    }

    public static BungeeCord getInstance()
    {
        return (BungeeCord) ProxyServer.getInstance();
    }

    public BungeeCord() throws IOException
    {
        Log.setOutput( new PrintStream( ByteStreams.nullOutputStream() ) ); // TODO: Bug JLine
        AnsiConsole.systemInstall();
        consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents( false );

        logger = new BungeeLogger( this );
        System.setErr( new PrintStream( new LoggingOutputStream( logger, Level.SEVERE ), true ) );
        System.setOut( new PrintStream( new LoggingOutputStream( logger, Level.INFO ), true ) );

        if ( consoleReader.getTerminal() instanceof UnsupportedTerminal )
        {
            logger.info( "Unable to initialize fancy terminal. To fix this on Windows, install the correct Microsoft Visual C++ 2008 Runtime" );
            logger.info( "NOTE: This error is non crucial, and BungeeCord will still function correctly! Do not bug the author about it unless you are still unable to get it working" );
        }

        if ( !NativeCipher.load() )
        {
            logger.warning( "NOTE: Failed to load native code. Falling back to Java cipher." );
        } else
        {
            logger.info( "Native code loaded." );
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
        ResourceLeakDetector.setEnabled( false ); // Eats performance

        pluginsFolder.mkdir();
        pluginManager.detectPlugins( pluginsFolder );
        config.load();
        for ( ListenerInfo info : config.getListeners() )
        {
            if ( !info.isForceDefault() && reconnectHandler == null )
            {
                reconnectHandler = new YamlReconnectHandler();
                break;
            }
        }
        isRunning = true;

        pluginManager.loadAndEnablePlugins();

        connectionThrottle = new ConnectionThrottle( config.getThrottle() );
        startListeners();

        saveThread.scheduleAtFixedRate( new TimerTask()
        {
            @Override
            public void run()
            {
                if ( getReconnectHandler() != null )
                {
                    getReconnectHandler().save();
                }
            }
        }, 0, TimeUnit.MINUTES.toMillis( 5 ) );
        metricsThread.scheduleAtFixedRate( new Metrics(), 0, TimeUnit.MINUTES.toMillis( Metrics.PING_INTERVAL ) );
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

            if ( info.isQueryEnabled() )
            {
                ChannelFutureListener bindListener = new ChannelFutureListener()
                {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception
                    {
                        if ( future.isSuccess() )
                        {
                            listeners.add( future.channel() );
                            getLogger().info( "Started query on " + future.channel().localAddress() );
                        } else
                        {
                            getLogger().log( Level.WARNING, "Could not bind to host " + future.channel().remoteAddress(), future.cause() );
                        }
                    }
                };
                new RemoteQuery( this, info ).start( new InetSocketAddress( info.getHost().getAddress(), info.getQueryPort() ), eventLoops, bindListener );
            }
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
        new Thread( "Shutdown Thread" )
        {
            @Override
            public void run()
            {
                BungeeCord.this.isRunning = false;

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
                try
                {
                    eventLoops.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
                } catch ( InterruptedException ex )
                {
                }

                if ( reconnectHandler != null )
                {
                    getLogger().info( "Saving reconnect locations" );
                    reconnectHandler.save();
                    reconnectHandler.close();
                }
                saveThread.cancel();
                metricsThread.cancel();

                // TODO: Fix this shit
                getLogger().info( "Disabling plugins" );
                for ( Plugin plugin : pluginManager.getPlugins() )
                {
                    try
                    {
                        plugin.onDisable();
                    } catch ( Throwable t )
                    {
                        getLogger().severe( "Exception disabling plugin " + plugin.getDescription().getName() );
                        t.printStackTrace();
                    }
                    getScheduler().cancel( plugin );
                }

                scheduler.shutdown();
                getLogger().info( "Thankyou and goodbye" );
                System.exit( 0 );
            }
        }.start();
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
                con.unsafe().sendPacket( packet );
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
    public String getTranslation(String name, Object... args)
    {
        String translation = "<translation '" + name + "' missing>";
        try
        {
            translation = MessageFormat.format( bundle.getString( name ), args );
        } catch ( MissingResourceException ex )
        {
        }
        return translation;
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

    public PluginMessage registerChannels()
    {
        return new PluginMessage( "REGISTER", Util.format( pluginChannels, "\00" ).getBytes() );
    }

    @Override
    public int getProtocolVersion()
    {
        return Protocol.PROTOCOL_VERSION;
    }

    @Override
    public String getGameVersion()
    {
        return Protocol.MINECRAFT_VERSION;
    }

    @Override
    public ServerInfo constructServerInfo(String name, InetSocketAddress address, String motd, boolean restricted)
    {
        return new BungeeServerInfo( name, address, motd, restricted );
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
        // TODO: Here too
        broadcast( new Chat( Util.stupify( message ) ) );
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

    @Override
    public CustomTabList customTabList(ProxiedPlayer player)
    {
        return new Custom( player );
    }

    public Collection<String> getDisabledCommands()
    {
        return config.getDisabledCommands();
    }
}
